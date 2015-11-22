package Core

import Chisel._
import TidbitsOCM._



// class ProcessorRunTest(c: Processor) extends Tester(c) {
// 
//     def inspect_kernels(): Unit = {
//         peek(c.kernel_buffer.io.dbg_kernel0)
//         peek(c.kernel_buffer.io.dbg_kernel1)
//         for(i <- 0 until 7){
//             peek(c.ALUs.mappers(i).dbg_kernel)
//         }
//         peek(c.processor_control.kernel_skew)
//         peek(c.processor_control.io.alu_stall)
//         peek(c.processor_control.io.programming_mode)
//     }
// 
// 
//     def sleep(cycles: Int): Unit = {
//         poke(c.io.processor_sleep, true)
//         for(i <- 0 until cycles){
//             // inspect_mappers()
//             inspect_kernels()
//             step(1)
//         }
//     }
// 
// 
//     def inspect_mappers(): Unit = {
//         for(i <- 0 until 7){
//             peek(c.ALUs.mappers(i).pixel_in)
//         }
//         println("---------------------")
// 
//         for(i <- 0 until 7){
//             peek(c.ALUs.selectors(i).dbg_state)
//         }
//         println("---------------------")
// 
//         for(i <- 0 until 7){
//             peek(c.ALUs.mappers(i).dbg_kernel)
//         }
//         println("---------------------")
// 
//         // for(i <- 0 until 7){
//         //     peek(c.ALUs.selectors(i).shift)
//         // }
// 
//         // println("---------------------")
// 
//         // for(i <- 0 until 7){
//         //     peek(c.ALUs.io.selector_shift)
//         // }
// 
//         // println("---------------------")
//     }
// 
//     def inspect_alu_in(): Unit = {
//         peek(c.conveyor.io.data_out)
//     }
// 
// 
//     def inspect_reducers(): Unit = {
//         for(i <- 0 until 7){
//             peek(c.ALUs.reducers(i).red_in)
//             println("---------------------")
//             peek(c.ALUs.reducers(i).red_out)
//             println("---------------------")
//             peek(c.ALUs.reducers(i).flush)
//             println("---------------------\n")
//         }
//     }
// 
// 
//     def inspect_grid(): Unit = {
//         
//         println()
//         peek(c.conveyor.pixel_rows(0).data_out(0))
//         peek(c.conveyor.pixel_rows(0).data_out(1))
//         peek(c.conveyor.pixel_rows(0).data_out(2))
//         println()
//         peek(c.conveyor.pixel_rows(1).data_out(0))
//         peek(c.conveyor.pixel_rows(1).data_out(1))
//         peek(c.conveyor.pixel_rows(1).data_out(2))
//         println()
//         peek(c.conveyor.pixel_rows(2).data_out(0))
//         peek(c.conveyor.pixel_rows(2).data_out(1))
//         peek(c.conveyor.pixel_rows(2).data_out(2))
//         println()
//     }
// 
// 
//     def load_program(identity: Int): Unit = {
//         poke(c.io.processor_sleep, true)
//         poke(c.io.control_data_in, 0)
//         poke(c.io.input_valid, false)
//         poke(c.io.pixel_in, 0)
//         step(1)
//         for(i <- 0 until 19){
//             if(i%2 == 1){
//                 poke(c.io.control_data_in, 1)
//                 poke(c.io.input_valid, true)
//             }
//             else{
//                 poke(c.io.control_data_in, 1)
//                 poke(c.io.input_valid, false)
//             }
//             peek(c.processor_control.kernel_skew)
//             // inspect_kernels()
//             step(1)
//         }
//     }
// 
// 
//     def load_kernels(kernels: Array[Int]): Unit = {
// 
//         var counter = 0
// 
//         for(i <- 0 until 19){
//             if(i%2 == 1){
//                 poke(c.io.control_data_in, kernels(counter))
//                 counter = counter + 1
//                 poke(c.io.input_valid, true)
//             }
//             else{
//                 poke(c.io.control_data_in, 0)
//                 poke(c.io.input_valid, false)
//             }
//             peek(c.processor_control.kernel_skew)
//             peek(c.processor_control.io)
//             step(1)
//         }
//     }
// 
//     def process_silent(cycles: Int) : Unit = {
//         for(i <- 0 until cycles){
//             poke(c.io.pixel_in, (i%9)+1)
//             step(1)
//         }
//     }
// 
//     def process_data(cycles: Int) : Unit = {
//         for(i <- 0 until cycles){
//             poke(c.io.pixel_in, 1)
//             println("\n\n#############################################")
//             println("#############################################")
//             println("STEP: %d\n#############################################".format(i))
//             println("#############################################\n")
//             inspect_alu_in()
//             println("\nMAPPERS\n")
//             inspect_mappers()
//             println("\nREDUCERS\n")
//             inspect_reducers()
//             println("\nCONTROL\n")
//             peek(c.data_control.io.ALU_shift)
//             println("\nOUT\n")
//             peek(c.ALUs.io.data_out)
//             peek(c.ALUs.io.valid_out)
//             println("\n#############################################")
//             println("#############################################")
//             step(1)
//         }
//     }
// 
//     val kernels = Array[Int](1, 2, 3, 4, 5, 6, 7, 8, 9)
// 
//     poke(c.io.pixel_in, 0)
//     poke(c.io.control_data_in, 0)
//     poke(c.io.processor_sleep, true)
//     poke(c.io.processor_configure, true)
//     poke(c.io.input_valid, false)
//     peek(c.io)
//     peek(c.processor_control.io)
//     load_program(0)
//     load_kernels(kernels)
//     poke(c.io.processor_configure, false)
//     sleep(13)
//     poke(c.io.processor_sleep, false)
//     process_silent(28)
//     process_data(15)
//     assert(false)
//     poke(c.io.processor_sleep, true)
//     sleep(20)
//     poke(c.io.processor_sleep, false)
//     process_data(50)
// 
// }
