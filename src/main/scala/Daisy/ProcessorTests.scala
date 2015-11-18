package Core

import Chisel._
import TidbitsOCM._



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
        peek(c.processor_control.io.dbg_kernel_skew)
        peek(c.kernel_buffer.io)
        println("Instruction stage")
        peek(c.kernel_buffer.io.dbg_kernel0)
        peek(c.kernel_buffer.io.dbg_kernel1)
        for(i <- 0 until 7){
            peek(c.ALUs.mappers(i).dbg_kernel)
            peek(c.ALUs.mappers(i).dbg_instr)
        }
        println()
        println()
        peek(c.processor_control.io.dbg_kernel_skew)
        println()
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
        peek(c.processor_control.io.dbg_kernel_skew)
        peek(c.kernel_buffer.io)
        println("Instruction stage")
        peek(c.kernel_buffer.io.dbg_kernel0)
        peek(c.kernel_buffer.io.dbg_kernel1)
        for(i <- 0 until 7){
            peek(c.ALUs.mappers(i).dbg_kernel)
            peek(c.ALUs.mappers(i).dbg_instr)
        }
        println()
        println()
        peek(c.processor_control.io.dbg_kernel_skew)
        println()
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
        peek(c.processor_control.io.dbg_kernel_skew)
        peek(c.kernel_buffer.io)
        println("Instruction stage")
        peek(c.kernel_buffer.io.dbg_kernel0)
        peek(c.kernel_buffer.io.dbg_kernel1)
        for(i <- 0 until 7){
            peek(c.ALUs.mappers(i).dbg_kernel)
            peek(c.ALUs.mappers(i).dbg_instr)
        }
        println()
        println()
        peek(c.processor_control.io.dbg_kernel_skew)
        println()
        println()
        step(1)
        println()
    }
    println()
    println()
    println("Preparing to run")
    println()
    println()
    for(i <- 0 until 20){
        poke(c.io.processor_sleep, true)
        peek(c.kernel_buffer.io.dbg_kernel0)
        peek(c.kernel_buffer.io.dbg_kernel1)
        for(i <- 0 until 7){
            peek(c.ALUs.mappers(i).dbg_kernel)
            peek(c.ALUs.mappers(i).dbg_instr)
        }
        println()
        println()
        peek(c.processor_control.io.dbg_kernel_skew)
        println()
        println()
        step(1)
        println()
    }
}

class ProcessorRunTest(c: Processor) extends Tester(c) {

    poke(c.io.processor_sleep, true)
    poke(c.io.control_data_in, 0)
    poke(c.io.input_valid, false)
    poke(c.io.pixel_in, 0)
    poke(c.io.processor_configure, false)
    step(1)
    peek(c.processor_control.io)

    // Fill kernel data etc
    println("Commencing instruction loading")
    poke(c.io.processor_configure, true)
    for(i <- 0 until 60){
        if(i%2 == 1){
            poke(c.io.control_data_in, 1)
            poke(c.io.input_valid, true)
        }
        else{
            poke(c.io.control_data_in, 0)
            poke(c.io.input_valid, false)
        }
        step(1)
    }

    println("Resetting")
    poke(c.io.processor_configure, false)
    poke(c.io.processor_sleep, true)
    for(i <- 0 until 20){
        step(1)
    }

    println("Processor state is:")
    peek(c.kernel_buffer.io.dbg_kernel0)
    peek(c.kernel_buffer.io.dbg_kernel1)

    for(i <- 0 until 7){
        peek(c.ALUs.mappers(i).dbg_kernel)
        peek(c.ALUs.mappers(i).dbg_instr)
    }
    peek(c.data_control.io.dbg_counter)

    println("Commencing data feed")
    println()
    poke(c.io.processor_sleep, false)
    for(i <- 0 until 27){
        poke(c.io.pixel_in, 0)
        peek(c.io.ALU_data_out)
        step(1)
    }

    poke(c.io.processor_sleep, true)
    for(i <- 0 until 20){
        peek(c.processor_control.io.dbg_kernel_skew)
        peek(c.processor_control.io.processor_sleep)
        peek(c.io.processor_configure)
        step(1)
    }

    println()
    println()
    println()
    peek(c.processor_control.io.dbg_kernel_skew)
    println()
    peek(c.kernel_buffer.io.dbg_kernel0)
    peek(c.kernel_buffer.io.dbg_kernel1)
    for(i <- 0 until 7){
        peek(c.ALUs.mappers(i).dbg_kernel)
        peek(c.ALUs.mappers(i).dbg_instr)
    }
    peek(c.data_control.io.dbg_counter)
    println()
    println()
    println()
    poke(c.io.processor_sleep, false)
    for(i <- 0 until 60){
        println()
        println("\n\nSTEP %d".format(i))
        println()
        poke(c.io.pixel_in, (i%9)+1)
        peek(c.processor_control.io.dbg_kernel_skew)
        peek(c.io.ALU_data_out)
        println("Accumulator values")
        // println()
        // println()
        // peek(c.conveyor.pixel_rows(0).dbg_reg_contents)
        // println()
        // peek(c.conveyor.pixel_rows(0).data_out(0))
        // peek(c.conveyor.pixel_rows(0).data_out(1))
        // peek(c.conveyor.pixel_rows(0).data_out(2))
        // println()
        // peek(c.conveyor.shift_muxes(0).io.dbg_state)
        // peek(c.conveyor.shift_muxes(0).io.data_out)
        // println()
        // println()
        // peek(c.conveyor.pixel_rows(1).dbg_reg_contents)
        // println()
        // peek(c.conveyor.pixel_rows(1).data_out(0))
        // peek(c.conveyor.pixel_rows(1).data_out(1))
        // peek(c.conveyor.pixel_rows(1).data_out(2))
        // println()
        // peek(c.conveyor.shift_muxes(1).io.dbg_state)
        // peek(c.conveyor.shift_muxes(1).io.data_out)
        // println()
        // println()
        // peek(c.conveyor.pixel_rows(2).dbg_reg_contents)
        println()
        peek(c.conveyor.pixel_rows(2).data_out(0))
        peek(c.conveyor.pixel_rows(2).data_out(1))
        peek(c.conveyor.pixel_rows(2).data_out(2))
        println()
        peek(c.conveyor.shift_muxes(2).io.dbg_state)
        peek(c.conveyor.shift_muxes(2).io.data_out)
        println()
        println()
        peek(c.ALUs.io.pixel_in(0))
        peek(c.ALUs.io.pixel_in(1))
        peek(c.ALUs.io.pixel_in(2))
        println()
        println()
        peek(c.ALUs.selectors(0).dbg_state)
        peek(c.ALUs.mappers(0).mapped_pixel)
        peek(c.ALUs.reducers(0).data_out)
        println()
        peek(c.ALUs.reducers(0).valid_out)
        println()
        println()
        println()
        peek(c.io.ALU_data_out)
        println()
        println()
        step(1)
    }
}
