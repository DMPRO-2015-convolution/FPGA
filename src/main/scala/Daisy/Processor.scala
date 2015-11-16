package Core

import Chisel._
import TidbitsOCM._

class Processor(data_width: Int, val cols: Int, rows: Int, kernel_dim: Int) extends Module{

    val io = new Bundle {

        val active = Bool(INPUT)

        val input_ready = Bool(INPUT)
        val data_in = UInt(INPUT, data_width)

        val ALU_data_out = UInt(OUTPUT, data_width)
        val ALU_data_is_valid = Bool(OUTPUT)
    }

    val conveyor = Module(new PixelGrid(data_width, cols, rows))
    val processor_control = Module(new Orchestrator(cols, rows))
    val kernel_control = Module(new KernelController(data_width, rows))
    val ALUs = Module(new ALUrow(data_width, cols, rows, kernel_dim))

    conveyor.io.read_row := processor_control.io.read_row
    conveyor.io.mux_row := processor_control.io.mux_row
    conveyor.io.data_in := io.data_in
    conveyor.io.shift_mux := processor_control.io.shift_mux
    conveyor.io.active := io.active

    for(i <- 0 until rows ) { 
        ALUs.io.data_in( (rows - 1) - i ) := conveyor.io.data_out(i)
    }

    ALUs.io.selector_shift := processor_control.io.ALU_shift
    ALUs.io.accumulator_flush := processor_control.io.accumulator_flush
    ALUs.io.kernel_in := kernel_control.io.kernel_out
    ALUs.io.freeze_kernels := kernel_control.io.freeze_kernels
    ALUs.io.active := io.active

    kernel_control.io.kernel_in := io.data_in
    kernel_control.io.active := io.active
    kernel_control.io.kernel_valid := io.input_ready

    processor_control.io.active := io.active

    io.ALU_data_out := ALUs.io.data_out
    io.ALU_data_is_valid := ALUs.io.valid_out
}

class ConveyorTest(c: Processor) extends Tester(c) {

    poke(c.io.active, true)
    poke(c.io.input_ready, true)

    for(cycle <- 0 until 6){
        for(i <- 0 until c.cols){
            poke(c.io.data_in, (i%c.cols)+1)
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

    poke(c.io.active, true)
    poke(c.io.input_ready, true)

    for(cycle <- 0 until 6){
        for(i <- 0 until c.cols){
            poke(c.io.data_in, (i%c.cols)+1)
            peek(c.conveyor.io.data_out)
            peek(c.ALUs.io)
            peek(c.ALUs.selectors(0).dbg_state)
            println()
            step(1)
            println()
        }
    }
    
}
