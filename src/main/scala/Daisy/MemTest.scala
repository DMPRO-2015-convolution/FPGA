package Core

import Chisel._
import TidbitsOCM._


class MinimalBRAM() extends Module{

    val row_size = 10
    val data_width = 8
    val rows = 3
    
    val io = new Bundle {
        val ready = Bool(INPUT)
        val data_in = UInt(INPUT, data_width)
        val data_out = UInt(OUTPUT, data_width)
    }


    val input_buffer = Module(new SliceDoubleBuffer(row_size, data_width, rows))

    input_buffer.io.data_in := UInt(0)
    input_buffer.io.request_write := Bool(false)
    input_buffer.io.request_read := Bool(false)

    when(io.ready){
        when(input_buffer.io.request_input){
            input_buffer.io.data_in := io.data_in
            input_buffer.io.request_write := Bool(true)
        }

        when(input_buffer.io.request_output){
            input_buffer.io.request_read := Bool(true)
        }
    }

    io.data_out := input_buffer.io.data_out
}

class InputTest(c: MinimalBRAM) extends Tester(c) {

    poke(c.io.ready, true)

    for(i <- 0 until 110){
        poke(c.io.data_in, i)
        peek(c.input_buffer.io)

        // peek(c.input_buffer.io.dbg_reads_performed)
        // peek(c.input_buffer.io.dbg_writes_performed)
        // peek(c.input_buffer.io.dbg_current)
        // peek(c.input_buffer.io.dbg_reads_finished)
        // peek(c.input_buffer.io.dbg_writes_finished)
        // peek(c.input_buffer.io.dbg_slice1_in)
        // peek(c.input_buffer.io.dbg_slice1_out)
        // peek(c.input_buffer.io.dbg_slice2_in)
        // peek(c.input_buffer.io.dbg_slice2_out)


        step(1)
    }

}
