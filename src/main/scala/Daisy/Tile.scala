package Core

import Chisel._
import TidbitsOCM._

// TODO add instruction specific module
class Tile(img_width: Int, input_data_width: Int, data_width: Int, cols: Int, rows: Int) extends Module{

    val kernel_dim = rows

    val io = new Bundle {
        val data_in = UInt(INPUT, data_width)
        val input_valid = Bool(INPUT)

        val reset = Bool(INPUT)
        val active = Bool(INPUT) //Not used, but wired

        val data_out = UInt(OUTPUT, data_width)
        val output_valid = Bool(OUTPUT)
    }

    val InputHandler = Module(new InputHandler(img_width, input_data_width, data_width, kernel_dim))
    val Processor = Module(new Processor(data_width, cols, rows))
    val Controller = Module(new TileController(data_width, img_width, kernel_dim, 30))
    val OutputHandler = Module(new OutputHandler(data_width, img_width))
    // insert output handler
    
    InputHandler.io.input_ready := io.input_valid
    InputHandler.io.data_in := io.data_in

    Processor.io.data_in := InputHandler.io.data_out

    Controller.io.ALU_Input := Processor.io.ALU_data_out
    Controller.io.ALU_Input_valid := Processor.io.ALU_data_is_valid
    Controller.io.conveyor_is_fed := InputHandler.io.data_ready

    OutputHandler.io.data_in := Controller.io.ALU_output
    OutputHandler.io.input_valid := Controller.io.ALU_output_is_valid
}

class TileTest(c: Tile) extends Tester(c) {
}

