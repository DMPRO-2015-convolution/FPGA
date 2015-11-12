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

    }


    val row_buffers = for(i <- 0 until cols) yield Module(new RowBuffer(row_length, data_width, i)).io
      
    val push_row = Reg(init=UInt(0, 32))
    val pop_row  = Reg(init=UInt(0, 32))
    val push_top = Reg(init=UInt(0, 32))

    io.data_out := UInt(0)

    // Maintain push row
    when(push_top === UInt(row_length)){
        when(push_row < UInt(cols - 1)){
            push_row := push_row + UInt(1)
        }.otherwise{
            push_row := UInt(0)
        }
    }

    when(io.push){
        when(push_top === UInt(row_length)){
            push_top := UInt(0)
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

class SliceBufferTest(c: SliceBuffer) extends Tester(c) {
    
    // Fill all buffers  
    poke(c.io.push, true)
    poke(c.io.pop, false)
    for(i <- 0 until 4*2){
        poke(c.io.data_in, i)
        peek(c.push_row)
        peek(c.pop_row)
        peek(c.push_top)
        step(1)
    }

    poke(c.io.push, false)
    poke(c.io.pop, false)
    step(1)

    // Retrieve data
    poke(c.io.push, false)
    poke(c.io.pop, true)
    for(i <- 0 until 4*2){
        peek(c.push_row)
        peek(c.pop_row)
        peek(c.push_top)
        peek(c.io.data_out)
        step(1)
    }

}
