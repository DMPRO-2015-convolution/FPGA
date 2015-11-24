package Core

import Chisel._
import TidbitsOCM._

// Buffers output which can be fed out at whatever pace
// class OutputHandler(row_length: Int, data_width: Int, kernel_dim: Int) extends Module {
//  
//     val entries = row_length
//     val mantle_width = (kernel_dim)/2
// 
//     val io = new Bundle {
// 
//         val reset = Bool(INPUT)
// 
//         val data_in = UInt(INPUT, data_width)
//         val input_valid = Bool(INPUT)
// 
//         val request_output = Bool(INPUT)
//         val output_ready = Bool(OUTPUT)
// 
//         val data_out = UInt(OUTPUT, data_width)
// 
//     }
// 
//     val output_buffer = Module(new ReverseDoubleBuffer(row_length: Int, data_width: Int, kernel_dim))
//     output_buffer.io.reset := Bool(false)
// 
//     output_buffer.io.slave_enq_input := Bool(false)
//     output_buffer.io.slave_deq_output := Bool(false)
// 
//     output_buffer.io.slave_enq_input := io.input_valid
// 
//     io.output_ready := output_buffer.io.slave_can_deq_output
//     io.data_out := output_buffer.io.data_out
// 
//     output_buffer.io.data_in := io.data_in
// 
//     output_buffer.io.slave_deq_output := io.request_output
// 
// }
// 
// class OutputHandlerTest(c: OutputHandler) extends Tester(c) {
//     peek(c.output_buffer.mode)
//     peek(c.output_buffer.deqs_finished)
//     poke(c.io.reset, false)
//     poke(c.io.input_valid, true)
//     poke(c.io.request_output, false)
//     peek(c.output_buffer.io.reset)
//     peek(c.output_buffer.io.some_numbers)
//     step(1)
//     peek(c.output_buffer.deqs_finished)
//     for(i <- 0 until 70){
//         poke(c.io.data_in, (i%7) + 1)
//         peek(c.io)
//         peek(c.output_buffer.mode)
//         peek(c.output_buffer.deqs_finished)
//         peek(c.output_buffer.enq_finished)
//         step(1)
//     }
//     peek(c.output_buffer.mode)
//     peek(c.output_buffer.deqs_finished)
//     peek(c.output_buffer.enq_finished)
//     step(1)
// 
//     poke(c.io.input_valid, false)
//     poke(c.io.request_output, true)
//     for(i <- 0 until 70){
//         peek(c.io.data_out)
//         peek(c.output_buffer.io)
//         step(1)
//     }
// }
// 
