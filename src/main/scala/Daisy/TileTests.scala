package Core

import Chisel._
import TidbitsOCM._

// class InputTest(c: Tile) extends Tester(c) {
//     
//     val r = scala.util.Random
//     
// 
//     def default(): Unit = {
//         poke(c.io.control_data_in, 0)
//         poke(c.io.control_input_valid, false)
//         poke(c.io.hdmi_data_in, 0)
//         poke(c.io.hdmi_input_valid, false)
//         poke(c.io.reset, false)
//         step(1)
//     }
// 
// 
//     def inspect_control(): Unit = {
//         println("\nInspecting control status-----------")
//         peek(c.SystemControl.state)
//         peek(c.SystemControl.io.control_input_valid)
//         println("\nStage")
//         peek(c.SystemControl.stage)
//         peek(c.SystemControl.io.processor_configure)
//         peek(c.SystemControl.io.processor_sleep)
//         peek(c.SystemControl.io.processor_control_input_valid)
//         peek(c.SystemControl.io.processor_control_input)
//         peek(c.Processor.processor_control.io.load_kernel)
//         peek(c.Processor.processor_control.io.load_instruction)
//         peek(c.Processor.processor_control.io.programming_mode)
//         // println("\nTranslator")
//         // println("----------------------------------")
//         println("\n---------------------------------------\n")
//     }
// 
// 
//     def inspect_program(): Unit = {
//         println("\nInstructions")
//         for(i <- 0 until 7){
//             peek(c.Processor.ALUs.mappers(i).dbg_instr)
//         }
// 
//         for(i <- 0 until 7){
//             peek(c.Processor.ALUs.reducers(i).dbg_instr)
//         }
//         println()
//     }
// 
// 
//     def inspect_kernels(): Unit = {
//         println("\nKernels")
//         
//         peek(c.Processor.processor_control.kernel_skew)
//         peek(c.Processor.kernel_buffer.io.dbg_kernel0)
//         peek(c.Processor.kernel_buffer.io.dbg_kernel1)
//         for(i <- 0 until 7){
//             peek(c.Processor.ALUs.mappers(i).dbg_kernel)
//         }
//         println()
//     }
// 
//     def inspect_run(): Unit = {
//         println("--------------------")
//         peek(c.io.output_valid)
//         peek(c.Processor.io.pixel_in)
//         peek(c.io.data_out)
//         peek(c.io.output_valid)
//         println("--------------------")
//     }
// 
//     def input_program(): Unit = {
//         println("STARTING PROGRAM INPUT")
//         var ops = 0
//         while(ops <= 12){
//             if(r.nextInt(5) == 1){
//                 ops = ops + 1
//                 poke(c.io.control_data_in, 0)
//                 poke(c.io.control_input_valid, true)
//                 inspect_kernels()
//                 inspect_program()
//             }
//             else{
//                 poke(c.io.control_input_valid, false)
//             }
//             step(1)
//         }
//     }
// 
//     
//     def load_kernels(): Unit = {
//         println("STARTING PROGRAM INPUT")
//         var ops = 0
//         var counter = 0
//         while(ops <= 10){
//             if(counter%4 == 1){
//                 ops = ops + 1
//                 poke(c.io.control_input_valid, true)
//                 poke(c.io.control_data_in, 1)
//                 peek(c.SystemControl.io)
//                 peek(c.Processor.processor_control.kernel_skew)
//                 inspect_kernels()
//             }
//             else{
//                 poke(c.io.control_input_valid, false)
//             }
//             counter = counter + 1
//             step(1)
//         }
//     }
// 
// 
//     def load_row_small(value: Int): Unit = {
//         println("------------------------ LOAD ROW SMALL ------------------")
//         var loads = 0
//         while(loads < 30){
//             if(r.nextInt(5) == 1){
//                 loads = loads + 1
//                 poke(c.io.hdmi_data_in, value)
//                 poke(c.io.hdmi_input_valid, true)
//                 inspect_input_buffer()
//             }
//             else{
//                 poke(c.io.hdmi_input_valid, false)
//             }
//             step(1)
//         }
//         println("------------------------------------------")
//     }
// 
//     def load_row(value: Int, entries: Int): Unit = {
//         var loads = 0
//         while(loads < entries){
//             if(r.nextInt(5) == 1){
//                 loads = loads + 1
//                 poke(c.io.hdmi_data_in, value)
//                 poke(c.io.hdmi_input_valid, true)
//             }
//             else{
//                 poke(c.io.hdmi_input_valid, false)
//             }
//             println("LOADS: %d".format(loads))
//             peek(c.InputHandler.input_buffer.slice1.push_row)
//             peek(c.InputHandler.input_buffer.slice1.push_top)
//             inspect_input_buffer()
//             step(1)
//         }
//     }
// 
//     def load_row_sparse(value: Int, entries: Int): Unit = {
//         var loads = 0
//         while(loads < entries){
//             if(r.nextInt(5) == 1){
//                 loads = loads + 1
//                 poke(c.io.hdmi_data_in, value)
//                 poke(c.io.hdmi_input_valid, true)
//             }
//             else{
//                 poke(c.io.hdmi_input_valid, false)
//             }
//             step(1)
//         }
//     }
// 
// 
//     def inspect_input_buffer(): Unit = {
//         println("Taking a peek at input buffer")
//         peek(c.InputHandler.input_buffer.reads_finished)
//         peek(c.InputHandler.input_buffer.writes_finished)
//         peek(c.InputHandler.input_buffer.reads_performed)
//         peek(c.InputHandler.input_buffer.writes_performed)
//         println("BUF 1")
//         peek(c.InputHandler.input_buffer.slice1.push_row)
//         peek(c.InputHandler.input_buffer.slice1.pop_row)
//         peek(c.InputHandler.input_buffer.slice1.push_top)
//         println("BUF 2")
//         peek(c.InputHandler.input_buffer.slice2.push_row)
//         peek(c.InputHandler.input_buffer.slice2.pop_row)
//         peek(c.InputHandler.input_buffer.slice2.push_top)
//         println("INPUT HANDLER")
//         peek(c.InputHandler.io)
//         println()
//     }
// 
// 
//     def run_processor_ALU_focus(runs: Int): Unit = {
//         for(i <- 0 until runs){
//             println()
//             println("OUTPUT STEP %d".format(i))
//             println()
//             println("INPUT IS:\n")
//             inspect_input_buffer()
//             println("\n\nPROCESSOR STATE:\n\n")
//             inspect_processor()
//             // println("\n\nOUTPUT_STATE:\n\n")
//             // inspect_output_buffer()
//             println("\n\nFIN\n\n")
//             step(1)
//             println()
//         }
//     }
// 
// 
//     def run_processor_output_focus(runs: Int): Unit = {
//         for(i <- 0 until runs){
//             println()
//             println("OUTPUT STEP %d".format(i))
//             println()
//             peek(c.SystemControl.io.dbg_processor_valid_output_count)
//             peek(c.SystemControl.io.processor_output_is_valid)
//             peek(c.SystemControl.io.processor_input_is_valid)
//             // println("INPUT IS:\n")
//             // inspect_input_buffer()
//             println("\n\nPROCESSOR STATE:\n\n")
//             // inspect_processor()
//             peek(c.Processor.io.ALU_data_out)
//             println("\n\nOUTPUT_STATE:\n\n")
//             inspect_output_buffer()
//             println("\n\nFIN\n\n")
//             step(1)
//             println()
//         }
//     }
// 
//     def inspect_output_buffer(): Unit = {
//         println("Taking a look at output")
//         peek(c.OutputHandler.io)
//     }
// 
//     def extract_output(): Unit = {
//         for(i <- 0 until 58*7){
//             poke(c.io.request_processed_data, true)
//             peek(c.io.data_out)
//             peek(c.OutputHandler.io)
//             step(1)
//         }
//     }
// 
//     def inspect_processor(): Unit = {
//         peek(c.Processor.io)
//         peek(c.SystemControl.io.processor_output_is_valid)
//         peek(c.Processor.processor_control.io)
// 
//         for(i <- 0 until 7){
//             peek(c.Processor.ALUs.mappers(i).pixel_in)
//         }
//         println("---------------------")
// 
//         peek(c.Processor.ALUs.mappers(0).dbg_instr)
// 
//         for(i <- 0 until 7){
//             peek(c.Processor.ALUs.selectors(i).dbg_state)
//         }
// 
//         println("---------------------")
// 
//         for(i <- 0 until 7){
//             peek(c.Processor.ALUs.reducers(i).red_in)
//             println("---------------------")
//             peek(c.Processor.ALUs.reducers(i).red_out)
//             println("---------------------")
//             peek(c.Processor.ALUs.reducers(i).flush)
//             println("---------------------\n")
//         }
//         peek(c.Processor.ALUs.reducers(0).dbg_instr)
// 
//         peek(c.Processor.ALUs.io)
//     }
//     
//     poke(c.io.reset, false)
//     step(1)
//     peek(c.io.dbg_rdy_for_output)
//     peek(c.io.dbg_rdy_for_input)
//     step(1)
//     poke(c.io.request_processed_data, false)
//     default()
//     inspect_control()
//     input_program()
//     load_kernels()
//     println("REACTORS: ONLINE\n\nWEAPONS: ONLINE\n\nALL SYSTEMS NOMINAL\n\n")
//     inspect_kernels()
//     inspect_control()
//     load_row(1, 60)
//     load_row_sparse(2, 60)
//     load_row_sparse(3, 60)
//     load_row_sparse(4, 60)
//     load_row_sparse(5, 60)
//     load_row_sparse(6, 60)
//     load_row_sparse(7, 60)
//     load_row_sparse(9, 60)
//     load_row(8, 60)
//     poke(c.io.control_data_in, 15)
//     inspect_kernels()
//     peek(c.Processor.processor_control.kernel_skew)
//     inspect_control()
//     inspect_input_buffer()
//     peek(c.io.dbg_rdy_for_output)
//     peek(c.io.dbg_rdy_for_input)
// 
//     poke(c.io.hdmi_input_valid, false)
// 
//     run_processor_output_focus(9*60 + 30)       
//     assert(false)
// 
//     inspect_program()
//     inspect_kernels()
//     println("------")
// 
//     inspect_output_buffer()
// 
// 
//     inspect_output_buffer()
// 
//     poke(c.io.request_processed_data, true)
//     peek(c.io)
//     step(1)
//     extract_output()
// 
// 
// 
// 
// }
