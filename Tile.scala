package Core

import Chisel._

class Tile(data_width: Int, cols: Int, rows: Int) extends Module{


    val io = new Bundle {
        val data_in = UInt(INPUT, data_width)
        val reset = Bool(INPUT)

        val data_out = UInt(OUTPUT, data_width)
    }

    val kernel_control = Module(new KernelController(data_width, 9)).io
    val memory = Module(new PixelGrid(data_width, cols, rows)).io
    val orchestrator = Module(new Orchestrator(cols, rows)).io
    val ALUs = Module(new ALUrow(data_width, cols, rows)).io

    memory.data_in := io.data_in

    for(i <- 0 to 6) {
        memory.control_in(i) := orchestrator.pings(i)
    }

    for(i <- 0 until 3 ) { 
        ALUs.data_in(2-i) := memory.data_out(i)
    }
    ALUs.selector_shift := orchestrator.pings(7)
    ALUs.accumulator_flush := orchestrator.pings(8)
    ALUs.kernel_in := kernel_control.kernel_out
    kernel_control.kernel_in  := ALUs.kernel_out
    kernel_control.data_in := io.data_in 


    io.data_out := ALUs.data_out
}

class CoreTest(c: Tile) extends Tester(c) {
    poke(c.io.data_in, 1)
    step(1)
    for(i <- 0 until 80){
        // poke(c.io.data_in, 1+(i%9))
        peek(c.io.data_out)
        step(1)
    }
}
