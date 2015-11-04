package Core

import Chisel._
import TidbitsOCM._

// This module serves as the entry point for data for daisy. When needed data unpacking schemes may be implemented here
// Processed data is also handled here
class IOhandlr(img_width: Int, input_data_width: Int, data_width: Int, kernel_dim: Int) extends Module{

    val io = new Bundle {

        val instream = new Bundle {
            val input_ready = Bool(INPUT)
            val data_in = UInt(INPUT, input_data_width)

            val data_out = UInt(OUTPUT, data_width)
            val data_ready = Bool(OUTPUT)
            val error = Bool(OUTPUT)
        }

        val outstream = new Bundle {
            val data_in = UInt(INPUT, data_width)
            val data_out = UInt(OUTPUT, data_width) //TBD
            val valid_in = Bool(INPUT)
            val valid_out = Bool(OUTPUT)
        }

        val ready = Bool(INPUT)

    }

    val input_buffer = Module(new SliceDoubleBuffer(img_width, data_width, kernel_dim))

    when(io.ready){
        // Instream in
        input_buffer.io.data_in := io.instream.data_in
        input_buffer.io.data_write := io.instream.input_ready

        // Instream out
        when(input_buffer.io.data_ready){
            input_buffer.io.data_read := Bool(true)    
            io.instream.data_out := input_buffer.io.data_out
            io.instream.data_ready := Bool(true)
        }.otherwise{
            input_buffer.io.data_read := Bool(false)
            io.instream.data_out := UInt(0)
            io.instream.data_ready := Bool(false)
        }

        io.instream.error := input_buffer.io.error
        

        // Outstream
        io.outstream.data_out := io.outstream.data_in
        io.outstream.valid_out := io.outstream.valid_in
    }

}
