package Core

import Chisel._
import TidbitsOCM._

// This module serves as the entry point for data for daisy.
// When needed data unpacking schemes may be implemented here.
// In its current incarnation input data width must equal data width

// input buffer supports two way handshakes for determining data validity
// currently this is only performed for input
class IOhandler(img_width: Int, input_data_width: Int, data_width: Int, kernel_dim: Int) extends Module{

    val io = new Bundle {

        val input_ready = Bool(INPUT)
        val data_in = UInt(INPUT, input_data_width)

        val data_out = UInt(OUTPUT, data_width)
        val data_ready = Bool(OUTPUT)

    }

    val input_buffer = Module(new SliceDoubleBuffer(img_width, data_width, kernel_dim))


    input_buffer.io.slave_read_input := io.input_ready

    when(io.input_ready){
        input_buffer.io.data_in := io.data_in
    }.otherwise{
        input_buffer.io.data_in := UInt(57005)
    }


    io.data_out := UInt(57005)
    input_buffer.io.slave_drive_output := Bool(false)

    when(input_buffer.io.slave_can_drive_output){
        io.data_out := input_buffer.io.data_out 
        input_buffer.io.slave_drive_output := Bool(true)
    }


}
