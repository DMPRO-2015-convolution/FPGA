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
        println("\nTranslator")
        println("----------------------------------")
        peek(c.SystemControl.translator.io)
        println("\n---------------------------------------\n")
    }

    def inspect_program(): Unit = {
        println("\nKernels")
        for(i <- 0 until 7){
            peek(c.Processor.ALUs.mappers(i).dbg_kernel)
        }

        println("\nInstructions")
        for(i <- 0 until 7){
            peek(c.Processor.ALUs.mappers(i).dbg_instr)
        }
        println()
    }

    def input_program(): Unit = {
        println("STARTING PROGRAM INPUT")
        var ops = 0
        while(ops < 15){
            if(r.nextInt(5) == 1){
                ops = ops + 1
                poke(c.io.control_data_in, 4369)
                poke(c.io.control_input_valid, true)
            }
            else{
                poke(c.io.control_input_valid, false)
            }
            inspect_control()
            step(1)
        }
    }
    
    default()
    inspect_control()
    input_program()
    inspect_program()
}
