package Core

import Chisel._

class Tile(data_width: Int, cols: Int, rows: Int) extends Module{


    val io = new Bundle {
        val data_in = UInt(INPUT, data_width)
        val reset = Bool(INPUT)

        val data_out = Vec.fill(rows){UInt(OUTPUT, data_width)}
    }

    val memory = Module(new PixelGrid(data_width, cols, rows)).io
    val Orchestrator = Module(new Orchestrator(cols, rows)).io
    val ALUs = Module(new ALUrow(data_width, cols, rows)).io

    
}

