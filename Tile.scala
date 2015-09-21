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
    memory.io.ping_key := io.ping_key
    
}

class CoreTest(c: Tile) extends Tester(c) {
    poke(c.io.ping_key(2), true)
    for(i <- 0 to 27){
        if (i == 1){ poke(c.io.ping_key(2), false) }
        if (i == 3){ poke(c.io.ping_key(1), true) }
        if (i == 4){ poke(c.io.ping_key(1), false) }
        if (i == 6){ poke(c.io.ping_key(0), true) }
        if (i == 7){ poke(c.io.ping_key(0), false) }
        poke(c.io.data_in, i)
        step(1)
    }
    step(1)
    peek(c.io.data_out(0))
    peek(c.io.data_out(1))
    peek(c.io.data_out(2))

    step(1)
    peek(c.io.data_out(0))
    peek(c.io.data_out(1))
    peek(c.io.data_out(2))

    step(1)
    peek(c.io.data_out(0))
    peek(c.io.data_out(1))
    peek(c.io.data_out(2))
}

object CoreMain {
    def main(args: Array[String]): Unit = {
        chiselMainTest(args, () => Module(new Tile(24, 9, 3))) {
            c => new CoreTest(c)
        }
    }
}
    
