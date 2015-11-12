package Core

import Chisel._
import TidbitsOCM._

class Tile(data_width: Int, cols: Int, rows: Int) extends Module{

    val img_width = 640
    val img_depth = 24
    val kernel_dim = 3

    val io = new Bundle {
        val data_in = UInt(INPUT, data_width)
        val input_valid = Bool(INPUT)

        val reset = Bool(INPUT)
        val active = Bool(INPUT) //Not used, but wired

        val data_out = UInt(OUTPUT, data_width)
        val output_valid = Bool(OUTPUT)
    }

    // val orchestrator = Module(new Orchestrator(cols, rows))
    // val IO_handler = Module(new IOhandler(img_width, img_depth, data_width, kernel_dim)).io
    // val kernel_control = Module(new KernelController(data_width, rows))
    // val memory = Module(new PixelGrid(data_width, cols, rows))
    // val ALUs = Module(new ALUrow(data_width, cols, rows))

    // IO_handler.instream.data_in := io.data_in 
    // IO_handler.instream.input_ready := io.input_valid 
    // memory.io.data_in := IO_handler.instream.data_out
    // IO_handler.ready := kernel_control.io.ready

    // memory.io.read_row := orchestrator.io.read_row
    // memory.io.mux_row := orchestrator.io.mux_row
    // 
    // for(i <- 0 until 3 ) { 
    //     ALUs.io.data_in(2-i) := memory.io.data_out(i)
    // }

    // ALUs.io.selector_shift := orchestrator.io.ALU_shift
    // ALUs.io.accumulator_flush := orchestrator.io.accumulator_flush
    // ALUs.io.kernel_in := kernel_control.io.kernel_out
    // kernel_control.io.kernel_in  := ALUs.io.kernel_out
    // kernel_control.io.data_in := io.data_in 

    // kernel_control.io.active := io.active
    // memory.io.active := io.active
    // ALUs.io.active := io.active

    // IO_handler.outstream.data_in := ALUs.io.data_out
    // IO_handler.outstream.valid_in := ALUs.io.valid_out
    // io.data_out := IO_handler.outstream.data_out
    // io.output_valid := IO_handler.outstream.valid_out
}


class CoreTest(c: Tile) extends Tester(c) {
}
