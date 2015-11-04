package Core

import Chisel._
import TidbitsOCM._

// This module serves as the entry point for data for daisy. When needed data unpacking schemes may be implemented here
// Processed data is also handled here
class IOhandler(img_width: Int, input_data_width: Int, data_width: Int, kernel_dim: Int) extends Module{

    val io = new Bundle {
        val data_in = UInt(INPUT, input_data_width)
        val input_ready = Bool(INPUT)
        val output_request = Bool(INPUT)

        val ready = Bool(INPUT)

        val data_out = UInt(OUTPUT, data_width)
        val error = Bool(OUTPUT)
    }

    val input_buffer = Module(new SliceDoubleBuffer(img_width, data_width, kernel_dim))

    input_buffer.io.data_in := io.data_in
    io.data_out := input_buffer.io.data_out

    input_buffer.io.data_write := io.input_ready
    input_buffer.io.data_read := io.output_request

    io.error := input_buffer.io.error
}
