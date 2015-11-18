package Core

import Chisel._
import TidbitsOCM._

class InputTest(c: Tile) extends Tester(c) {
    
    val r = scala.util.Random
    

    def default(): Unit = {
        poke(c.io.control_data_in, 0)
        poke(c.io.control_input_valid, false)
        poke(c.io.hdmi_data_in, 0)
        poke(c.io.hdmi_input_valid, false)
        poke(c.io.reset, false)
        step(1)
    }


    def inspect_control(): Unit = {
        println("\nInspecting control status-----------")
        peek(c.SystemControl.state)
        peek(c.SystemControl.io.control_input_valid)
        println("\nStage")
        peek(c.SystemControl.stage)
        peek(c.SystemControl.io.processor_configure)
        peek(c.SystemControl.io.processor_sleep)
        peek(c.SystemControl.io.processor_control_input_valid)
        peek(c.SystemControl.io.processor_control_input)
        peek(c.Processor.processor_control.io.load_kernel)
        peek(c.Processor.processor_control.io.load_instruction)
        peek(c.Processor.processor_control.io.programming_mode)
        // println("\nTranslator")
        // println("----------------------------------")
        // peek(c.SystemControl.translator.io)
        println("\n---------------------------------------\n")
    }


    def inspect_program(): Unit = {
        println("\nInstructions")
        for(i <- 0 until 7){
            peek(c.Processor.ALUs.mappers(i).dbg_instr)
        }
        println()
    }


    def inspect_kernels(): Unit = {
        println("\nKernels")
        
        peek(c.Processor.kernel_buffer.io.dbg_kernel0)
        peek(c.Processor.kernel_buffer.io.dbg_kernel1)
        for(i <- 0 until 7){
            peek(c.Processor.ALUs.mappers(i).dbg_kernel)
        }
        println()
    }

    def inspect_run(): Unit = {
        println("--------------------")
        peek(c.io.output_valid)
        peek(c.Processor.io.pixel_in)
        peek(c.io.data_out)
        peek(c.io.output_valid)
        println("--------------------")
    }

    def input_program(): Unit = {
        println("STARTING PROGRAM INPUT")
        var ops = 0
        while(ops <= 15){
            if(r.nextInt(5) == 1){
                ops = ops + 1
                poke(c.io.control_data_in, 4369)
                poke(c.io.control_input_valid, true)
                inspect_control()
                inspect_program()
            }
            else{
                poke(c.io.control_input_valid, false)
            }
            step(1)
        }
    }

    
    def load_kernels(): Unit = {
        println("STARTING PROGRAM INPUT")
        var ops = 0
        while(ops <= 21){
            if(r.nextInt(5) == 1){
                ops = ops + 1
                poke(c.io.control_data_in, 8738)
                poke(c.io.control_input_valid, true)
                inspect_control()
                inspect_kernels()
            }
            else{
                poke(c.io.control_input_valid, false)
            }
            step(1)
        }
    }


    def load_row_small(value: Int): Unit = {
        println("------------------------ LOAD ROW SMALL ------------------")
        var loads = 0
        while(loads < 30){
            if(r.nextInt(5) == 1){
                loads = loads + 1
                poke(c.io.hdmi_data_in, value)
                poke(c.io.hdmi_input_valid, true)
                inspect_input_buffer()
            }
            else{
                poke(c.io.hdmi_input_valid, false)
            }
            step(1)
        }
        println("------------------------------------------")
    }

    def load_row(value: Int): Unit = {
        var loads = 0
        while(loads < 640){
            if(r.nextInt(5) == 1){
                loads = loads + 1
                poke(c.io.hdmi_data_in, value)
                poke(c.io.hdmi_input_valid, true)
                peek(c.InputHandler.input_buffer.slice1.push_row)
                peek(c.InputHandler.input_buffer.slice1.push_top)
            }
            else{
                poke(c.io.hdmi_input_valid, false)
                peek(c.InputHandler.input_buffer.slice1.push_row)
                peek(c.InputHandler.input_buffer.slice1.push_top)
            }
            step(1)
        }
    }


    def inspect_input_buffer(): Unit = {
        println("Taking a peak at input büffer")
        peek(c.InputHandler.input_buffer.reads_finished)
        peek(c.InputHandler.input_buffer.writes_finished)
        peek(c.InputHandler.input_buffer.reads_performed)
        peek(c.InputHandler.input_buffer.writes_performed)
        println("BUF 1")
        peek(c.InputHandler.input_buffer.slice1.push_row)
        peek(c.InputHandler.input_buffer.slice1.pop_row)
        peek(c.InputHandler.input_buffer.slice1.push_top)
        println("BUF 2")
        peek(c.InputHandler.input_buffer.slice2.push_row)
        peek(c.InputHandler.input_buffer.slice2.pop_row)
        peek(c.InputHandler.input_buffer.slice2.push_top)
        println()
    }

    def run_output(): Unit = {
        for(i <- 0 until 60){
            println()
            println("OUTPUT STEP %d".format(i))
            inspect_run() 
            inspect_input_buffer() 
            println()
            step(1)
            println()
        }
    }
    
    default()
    inspect_control()
    input_program()
    load_kernels()
    println("REACTORS: ONLINE\n\nWEAPONS: ONLINE\n\nALL SYSTEMS NOMINAL\n\n")
    inspect_kernels()
    inspect_control()
    load_row_small(1)
    load_row(1)
    load_row_small(2)
    // load_row(2)
    // load_row(3)
    // load_row(4)
    // load_row(5)
    // load_row(6)
    // load_row(7)
    // load_row(8)
    // load_row(9)
    inspect_control()
    inspect_input_buffer()
    // run_output()       

}
