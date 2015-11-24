package Core

import Chisel._
import TidbitsOCM._

// This module serves as the entry point for data for daisy.

// input buffer supports two way handshakes for determining data validity
// currently this is only performed for input
class InputHandler(img_width: Int, data_width: Int, kernel_dim: Int) extends Module{

    val io = new Bundle {

        val reset = Bool(INPUT)

        val data_mode = Bool(INPUT)

        val input_ready = Bool(INPUT)
        val data_in = UInt(INPUT, data_width)

        val data_out = UInt(OUTPUT, data_width)
        val data_ready = Bool(OUTPUT)

    }

    val input_buffer = Module(new SliceDoubleBuffer(img_width, data_width, kernel_dim))
    input_buffer.reset := io.reset


    io.data_out := UInt(57005)
    input_buffer.io.data_in := UInt(57005)

    io.data_ready := Bool(false)
    input_buffer.io.slave_push_input := Bool(false)
    input_buffer.io.slave_pop_output := Bool(false)


    when(io.data_mode){

        input_buffer.io.slave_push_input := io.input_ready

        when(io.input_ready){
            input_buffer.io.data_in := io.data_in
        }

        input_buffer.io.slave_pop_output := Bool(false)

        when(input_buffer.io.slave_can_pop_output){
            io.data_out := input_buffer.io.data_out 
            input_buffer.io.slave_pop_output := Bool(true)
        }

        io.data_ready := input_buffer.io.slave_can_pop_output
    }
    .otherwise{
        when(io.input_ready){
            io.data_out := io.data_in
            io.data_ready := Bool(true)
        }
    }
}
