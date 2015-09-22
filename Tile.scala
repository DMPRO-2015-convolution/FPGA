package Core

import Chisel._

class Tile(data_width: Int, cols: Int, rows: Int) extends Module{

    val io = new Bundle {
        val data_in = UInt(INPUT, data_width)
        val ping_key = Vec.fill(rows){Bool(INPUT)}
        val data_out = Vec.fill(rows){UInt(OUTPUT, data_width)}
    }
    val memory = Module(new PixelGrid(data_width, cols, rows))

    io.data_out := memory.io.data_out
    memory.io.data_in := io.data_in
    // memory.io.ping_key := io.ping_key
    
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
    
