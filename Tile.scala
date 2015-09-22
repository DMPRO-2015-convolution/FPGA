package Core

import Chisel._

class Tile(data_width: Int, cols: Int, rows: Int) extends Module{

    val io = new Bundle {
        val data_in = UInt(INPUT, data_width)
        val reset = Bool(INPUT)

        val data_out = Vec.fill(rows){UInt(OUTPUT, data_width)}
    }

    val memory = Module(new PixelGrid(data_width, cols, rows)).io
    val control = Module(new Orchestrator(cols, rows)).io


    // Wire data inputs and outputs
    io.data_out := memory.data_out
    memory.data_in := io.data_in


    // Wire control to memory
    memory.pings := control.pings
    control.reset := io.reset
}

class CoreTest(c: Tile) extends Tester(c) {
    
    step(1)
    step(1)
    poke(c.io.data_in, 1) 
    for(i <- 0 until 30){
        step(1)
    }
    peek(c.io.data_out)
}

object CoreMain {
    def main(args: Array[String]): Unit = {
        chiselMainTest(args, () => Module(new Tile(24, 9, 3))) {
            c => new CoreTest(c)
        }
    }
}
    
