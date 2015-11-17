package Core

import Chisel._
import TidbitsOCM._


class ConveyorTest(c: Processor) extends Tester(c) {

    poke(c.io.input_valid, true)

    for(cycle <- 0 until 6){
        for(i <- 0 until c.cols){
            poke(c.io.pixel_in, (i%c.cols)+1)
            peek(c.conveyor.io.data_out)
            println("\nPixel row 1")
            peek(c.conveyor.shift_muxes(0).state)
            peek(c.conveyor.pixel_rows(0).data_out)
            println("\nPixel row 2")
            peek(c.conveyor.shift_muxes(1).state)
            peek(c.conveyor.pixel_rows(1).data_out)
            println("\nPixel row 3")
            peek(c.conveyor.shift_muxes(2).state)
            peek(c.conveyor.pixel_rows(2).data_out)
            println()
            step(1)
            println()
        }
    }

}


class ProcessorTest(c: Processor) extends Tester(c) {

    poke(c.io.input_valid, true)

    for(cycle <- 0 until 6){
        for(i <- 0 until c.cols){
            poke(c.io.pixel_in, (i%c.cols)+1)
            peek(c.conveyor.io.data_out)
            peek(c.ALUs.io)
            peek(c.ALUs.selectors(0).dbg_state)
            println()
            step(1)
            println()
        }
    }
    
}


class ProcessorInitTest(c: Processor) extends Tester(c) {

    println("Here goes...")
    poke(c.io.processor_sleep, true)
    poke(c.io.control_data_in, 0)
    poke(c.io.input_valid, false)
    poke(c.io.pixel_in, 0)
    poke(c.io.processor_configure, false)
    step(1)
    peek(c.processor_control.io)

    println()
    println("Commencing instruction loading")
    poke(c.io.processor_configure, true)
    for(i <- 0 until 19){
        if(i%2 == 1){
            poke(c.io.control_data_in, 1)
            poke(c.io.input_valid, true)
        }
        else{
            poke(c.io.control_data_in, 0)
            poke(c.io.input_valid, false)
        }
        println("controller stage")
        peek(c.processor_control.stage)
        peek(c.processor_control.io.alu_stall)
        peek(c.processor_control.io.load_kernel)
        peek(c.processor_control.io.load_instruction)
        peek(c.kernel_buffer.io)
        println("Instruction stage")
        for(i <- 0 until 7){
            peek(c.ALUs.mappers(i).dbg_kernel)
            peek(c.ALUs.mappers(i).dbg_instr)
        }
        println()
        step(1)
        println()
    }

    println()
    println()
    println("Commencing kernel loading")
    poke(c.io.processor_configure, true)
    for(i <- 0 until 20){
        if(i%2 == 1){
            poke(c.io.control_data_in, i + 1)
            poke(c.io.input_valid, true)
        }
        else{
            poke(c.io.control_data_in, 0)
            poke(c.io.input_valid, false)
        }
        println("controller stage")
        peek(c.processor_control.stage)
        peek(c.processor_control.io.alu_stall)
        peek(c.processor_control.io.load_kernel)
        peek(c.processor_control.io.load_instruction)
        peek(c.kernel_buffer.io)
        println("Instruction stage")
        for(i <- 0 until 7){
            peek(c.ALUs.mappers(i).dbg_kernel)
            peek(c.ALUs.mappers(i).dbg_instr)
        }
        println()
        step(1)
        println()
    }
    println()
    println()
    println("Taking a look")
    poke(c.io.processor_sleep, false)
    poke(c.io.processor_configure, false)
    for(i <- 0 until 19){
        if(i%2 == 1){
            poke(c.io.control_data_in, i + 1)
            poke(c.io.input_valid, true)
        }
        else{
            poke(c.io.control_data_in, 0)
            poke(c.io.input_valid, false)
        }
        println("controller stage")
        peek(c.processor_control.stage)
        peek(c.processor_control.io.alu_stall)
        peek(c.processor_control.io.load_kernel)
        peek(c.processor_control.io.load_instruction)
        peek(c.kernel_buffer.io)
        println("Instruction stage")
        for(i <- 0 until 7){
            peek(c.ALUs.mappers(i).dbg_kernel)
            peek(c.ALUs.mappers(i).dbg_instr)
        }
        println()
        step(1)
        println()
    }

}
