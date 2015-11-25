package Core

import Chisel._

// Stole all of this code from some dude on github 
import TidbitsOCM._


class SliceBuffer(row_length: Int, data_width: Int, kernel_dim: Int) extends Module {

    val cols = kernel_dim*kernel_dim - 2

    val io = new Bundle {

        val reset = Bool(INPUT)
        val data_in = UInt(INPUT, data_width)

        val push = Bool(INPUT)
        val pop = Bool(INPUT)

        val data_out = UInt(OUTPUT, data_width)
        val push_row = UInt(OUTPUT, 8)

        val pop_row = UInt(OUTPUT, 8)
        val push_top = UInt(OUTPUT, 16)

    }

    val row_buffers = for(i <- 0 until cols) yield Module(new RowBuffer(row_length, data_width)).io

    val push_row = Reg(init=UInt(0, 32))
    val pop_row  = Reg(init=UInt(0, 32))
    val push_top = Reg(init=UInt(0, 32))

    when(io.reset){
        pop_row := UInt(0)
        push_row := UInt(0)
        push_top := UInt(0)
    }

    io.push_row := push_row
    io.pop_row := pop_row
    io.data_out := UInt(57005)
    io.push_top := push_top

    // Maintain push row
    when(push_top === UInt(row_length - 1)){
        push_top := UInt(0)
        when(push_row < UInt(cols - 1)){
            push_row := push_row + UInt(1)
        }.otherwise{
            push_row := UInt(0)
        }
    }

    when(io.push){
        when(push_top === UInt(row_length - 1)){
            push_top := UInt(0)
            when(push_row < UInt(cols - 1)){
                push_row := push_row + UInt(1)
            }.otherwise{
                push_row := UInt(0)
            }
        }.otherwise{
            push_top := push_top + UInt(1)
        }
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
        row_buffers(i).reset := io.reset
        when(pop_row  === UInt(i)){
            row_buffers(i).pop := io.pop
            io.data_out := row_buffers(i).data_out
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
