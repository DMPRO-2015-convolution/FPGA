package Core

import Chisel._

class Tile(data_width: Int, cols: Int, rows: Int) extends Module{

    val io = new Bundle {
        val data_in = UInt(INPUT, data_width)

        val pings = Vec.fill(cols/3 + rows + 1){Bool(OUTPUT)}
        val data_out = Vec.fill(rows){UInt(OUTPUT, data_width)}
    }

    val memory = Module(new PixelGrid(data_width, cols, rows)).io
    val control = Module(new Orchestrator(cols, rows)).io


    io.data_out := memory.data_out
    memory.data_in := io.data_in

    
}

class CoreTest(c: Tile) extends Tester(c) {
}

object CoreMain {
    def main(args: Array[String]): Unit = {
        chiselMainTest(args, () => Module(new Tile(24, 9, 3))) {
            c => new CoreTest(c)
        }
    }
}
    
