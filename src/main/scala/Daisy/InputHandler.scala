package Core

import Chisel._
import TidbitsOCM._

// This module serves as the entry point for data for daisy. When needed data unpacking schemes may be implemented here
// Processed data is also handled here
// class IOhandler(img_width: Int, input_data_width: Int, data_width: Int, kernel_dim: Int) extends Module{

//    val io = new Bundle {
//
//        val instream = new Bundle {
//            val input_ready = Bool(INPUT)
//            val data_in = UInt(INPUT, input_data_width)
//
//            val data_out = UInt(OUTPUT, data_width)
//            val data_ready = Bool(OUTPUT)
//            val error = Bool(OUTPUT)
//        }
//
//        val outstream = new Bundle {
//            val data_in = UInt(INPUT, data_width)
//            val valid_in = Bool(INPUT)
//
//            val data_out = UInt(OUTPUT, data_width) //TBD
//            val valid_out = Bool(OUTPUT)
//        }
//        val ready = Bool(INPUT)
//    }
//
//    val input_buffer = Module(new SliceDoubleBuffer(img_width, data_width, kernel_dim))
//    val valid_countdown = Reg(init=UInt(0))
//    val processed_pixels = Reg(init=UInt(0))
//
//    val pixel_per_slice = img_width*kernel_dim
//    // What the fuck?
//    val magic = 1000
//
//    io.instream.data_out := UInt(0)
//    io.outstream.data_out := UInt(0)
//    io.outstream.valid_out := Bool(false)
//    io.instream.data_ready := Bool(false)
//
//    input_buffer.io.data_in := UInt(0)
//    input_buffer.io.data_write := Bool(false)
//    input_buffer.io.data_read := Bool(false)
//
//    when(io.ready){
//        // Instream in
//        input_buffer.io.data_in := io.instream.data_in
//        input_buffer.io.data_write := io.instream.input_ready
//
//        // when input has data ready instream feeds the conveyor
//        when(input_buffer.io.data_ready){
//            input_buffer.io.data_read := Bool(true)    
//            io.instream.data_out := input_buffer.io.data_out
//        }.otherwise{
//            input_buffer.io.data_read := Bool(false)
//            io.instream.data_out := UInt(0)
//        }
//
//
//        // When data is being fed to the conveyor we count down to the first valid output value
//        // as well as count the valid data in the pipe as data stops being fed.
//        // We take the burden of invalidating missing output in the case where it is a border pixels
//        // turn to output data.
//
//        // The case when we are reading, but no data has come through yet
//        when(input_buffer.io.data_read && valid_countdown === UInt(0)){
//            when(io.outstream.valid_in){
//                io.outstream.valid_out := Bool(true)
//                processed_pixels := processed_pixels + UInt(1)
//            }
//        }
//
//        // The case where data is being fed and retrieved
//        when(input_buffer.io.data_read && !(valid_countdown === UInt(0))){
//              io.outstream.valid_out := Bool(false)
//              valid_countdown := valid_countdown - UInt(1)
//        }
//
//        // The case where data is no longer fed, but some valid data remains. 
//        // In order for the processed_pixels counter to be reset without triggering this
//        // conditional we check if processed pixels isnt in its reset state
//        when(!(input_buffer.io.data_read) && !(processed_pixels === UInt(0)) && (processed_pixels < UInt(pixel_per_slice))){
//            when(io.outstream.valid_in){
//                io.outstream.valid_out := Bool(true)
//                processed_pixels := processed_pixels + UInt(1) 
//            }
//        }
//
//        // When we are done with a feed cycle we reset all the counters while waiting
//        // for the next buffer to be filled.
//        when(processed_pixels === UInt(pixel_per_slice)){
//            processed_pixels := UInt(0)
//        }
//
//        when(!input_buffer.io.data_read){
//            valid_countdown := UInt(magic)
//        }
//        
//        io.outstream.data_out := io.outstream.data_in
//        io.outstream.valid_out := io.outstream.valid_in
//    }
// }
