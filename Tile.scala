package Core

import Chisel._

class Tile(data_width: Int, cols: Int, rows: Int) extends Module{


    val io = new Bundle {
        val data_in = UInt(INPUT, data_width)
        val reset = Bool(INPUT)
        val active = Bool(INPUT)

        val data_out = UInt(OUTPUT, data_width)
    }

    val kernel_control = Module(new KernelController(data_width, 9))
    val memory = Module(new PixelGrid(data_width, cols, rows))
    val orchestrator = Module(new Orchestrator(cols, rows))
    val ALUs = Module(new ALUrow(data_width, cols, rows))

    memory.io.data_in := io.data_in

    for(i <- 0 to 6) {
        memory.io.control_in(i) := orchestrator.io.pings(i)
    }

    for(i <- 0 until 3 ) { 
        ALUs.io.data_in(2-i) := memory.io.data_out(i)
    }
    ALUs.io.selector_shift := orchestrator.io.pings(7)
    ALUs.io.accumulator_flush := orchestrator.io.pings(8)
    ALUs.io.kernel_in := kernel_control.io.kernel_out
    kernel_control.io.kernel_in  := ALUs.io.kernel_out
    kernel_control.io.data_in := io.data_in 

    kernel_control.io.active := io.active
    memory.io.active := io.active
    ALUs.io.active := io.active

    io.data_out := ALUs.io.data_out
}

