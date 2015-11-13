package Core

import Chisel._
import TidbitsOCM._

class Processor(data_width: Int, cols: Int, rows: Int) extends Module{

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
    
    for(i <- 0 until 3 ) { 
        ALUs.io.data_in(2-i) := conveyor.io.data_out(i)
    }

    ALUs.io.selector_shift := control.io.ALU_shift
    ALUs.io.accumulator_flush := control.io.accumulator_flush
    ALUs.io.kernel_in := kernel_control.io.kernel_out
    kernel_control.io.kernel_in  := ALUs.io.kernel_out
    kernel_control.io.data_in := io.data_in 

    kernel_control.io.active := io.active
    conveyor.io.active := io.active
    ALUs.io.active := io.active

    io.data_out := ALUs.io.data_out
}
