package Core

import Chisel._
import TidbitsOCM._

class Processor(data_width: Int, val cols: Int, rows: Int) extends Module{

    val io = new Bundle {

        val active = Bool(INPUT)

        val input_ready = Bool(INPUT)
        val data_in = UInt(INPUT, data_width)

        val data_out = UInt(OUTPUT, data_width)
        val data_ready = Bool(OUTPUT)
    }

    val conveyor = Module(new PixelGrid(data_width, cols, rows))
    val control = Module(new Orchestrator(cols, rows))
    val kernel_control = Module(new KernelController(data_width, rows))
    val ALUs = Module(new ALUrow(data_width, cols, rows))

    conveyor.io.read_row := control.io.read_row
    conveyor.io.mux_row := control.io.mux_row
    conveyor.io.data_in := io.data_in
    conveyor.io.shift_mux := control.io.shift_mux

    for(i <- 0 until rows ) { 
        ALUs.io.data_in( (rows - 1) - i ) := conveyor.io.data_out(i)
    }

    ALUs.io.selector_shift := control.io.ALU_shift
    ALUs.io.accumulator_flush := control.io.accumulator_flush
    ALUs.io.kernel_in := kernel_control.io.kernel_out
    kernel_control.io.kernel_in  := ALUs.io.kernel_out
    kernel_control.io.data_in := io.data_in 

    kernel_control.io.active := io.active
    conveyor.io.active := io.active
    ALUs.io.active := io.active
    control.io.active := io.active

    io.data_out := ALUs.io.data_out
    io.data_ready := Bool(true)
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
