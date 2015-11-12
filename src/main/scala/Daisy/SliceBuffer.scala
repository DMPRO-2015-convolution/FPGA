package Core

import Chisel._

// Stole all of this code from some dude on github 
import TidbitsOCM._


class SliceBuffer(row_length: Int, data_width: Int, kernel_dim: Int) extends Module {

    val cols = kernel_dim*kernel_dim
    
    val io = new Bundle {
        val data_in = UInt(INPUT, data_width)
        val push = Bool(INPUT)
        val pop = Bool(INPUT)

        val data_out = UInt(OUTPUT, data_width)

        val dbg = new Bundle {
            val sb_push_row  = UInt(OUTPUT, 32)
            val sb_push_top = UInt(OUTPUT, 32)
            val sb_pop_row = UInt(OUTPUT, 32)

            val rb1_data_out = UInt(OUTPUT)
            val rb1_data_in = UInt(OUTPUT)
            val rb1_stack_top = UInt(OUTPUT)

            val rb2_data_out = UInt(OUTPUT)
            val rb2_data_in = UInt(OUTPUT)
            val rb2_stack_top = UInt(OUTPUT)
        }
    }


    val row_buffers = for(i <- 0 until cols) yield Module(new RowBuffer(row_length, data_width, i)).io
      
    val push_row = Reg(init=UInt(0, 32))
    val pop_row  = Reg(init=UInt(0, 32))
    val push_top = Reg(init=UInt(0, 32))

    io.data_out := UInt(0)

    io.dbg.sb_push_row := push_row
    io.dbg.sb_push_top := push_top
    io.dbg.sb_pop_row := pop_row

    io.dbg.rb1_data_out := row_buffers(0).data_out
    io.dbg.rb2_data_out := row_buffers(1).data_out

    io.dbg.rb1_data_in := io.data_in
    io.dbg.rb2_data_in := io.data_in

    io.dbg.rb1_stack_top := row_buffers(0).dbg_stack_top
    io.dbg.rb2_stack_top := row_buffers(1).dbg_stack_top


    // Maintain push row
    when(push_top === UInt(row_length)){
        when(push_row < UInt(cols - 1)){
            push_row := push_row + UInt(1)
        }.otherwise{
            push_row := UInt(0)
        }
        push_top := UInt(0)
    }

    // Maintain pop row
    when(io.pop){
        when(pop_row < UInt(cols - 1)){
            pop_row := pop_row  + UInt(1)    
        }.otherwise{
            pop_row  := UInt(0)
        }
    }

    // pop data 
    for(i <- 0 until cols){
        when(pop_row  === UInt(i)){
            row_buffers(i).pop := io.pop
            io.data_out := row_buffers( ((i-1)+cols) % cols ).data_out
        }.otherwise{
            row_buffers(i).pop := Bool(false)
        }
    }

    // push data
    for(i <- 0 until cols){
        when(push_row === UInt(i)){
            row_buffers(i).push := io.push
            row_buffers(i).data_in := io.data_in
        }.otherwise{
            row_buffers(i).data_in := UInt(0)
            row_buffers(i).push := Bool(false)
        }
    }
}
