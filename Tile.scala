package Core

import Chisel._

class Tile(data_width: Int, cols: Int, rows: Int) extends Module{


    val io = new Bundle {
        val data_in = UInt(INPUT, data_width)
        val reset = Bool(INPUT)

        // val data_out = Vec.fill(rows){UInt(OUTPUT, data_width)}
    }

    val memory = Module(new PixelGrid(data_width, cols, rows)).io


    // Wire data inputs and outputs
    // io.data_out := memory.data_out
    // memory.data_in := io.data_in

}

class CoreTest(c: Tile) extends Tester(c) {
    
    step(1)
    step(1)
    for(i <- 0 until 60){
        step(1)
    }
}

object CoreMain {
    def main(args: Array[String]): Unit = {
        // chiselMainTest(args, () => Module(new Tile(24, 9, 3))) { c => new CoreTest(c) }

        // Behaves
        // chiselMainTest(args, () => Module(new PixelArray(24, 9))) { c => new PixelArrayTest(c, 24, 9) }

        // Behaves
        // chiselMainTest(args, () => Module(new Mux3(24, 3))) { c => new Mux3Test(c, 24, 3) }
        
        // Behaves
        // chiselMainTest(args, () => Module(new ShiftMux3(24, 3, 0))) { c => new ShiftMux3Test(c, 24, 3) }

        // Behaves
        // chiselMainTest(args, () => Module(new Orchestrator(9, 3))) { c => new OrchestratorTest(c, 24, 9) }

        // Behaves
        // chiselMainTest(args, () => Module(new PixelReg(24))) { c => new PixelRegTest(c, 24) }

        // somewhat behaves
        // chiselMainTest(args, () => Module(new PixelGrid(24, 9, 3))) { c => new PixelGridTest(c, 24, 9, 3) }
        
        // Behaves
        // chiselMainTest(args, () => Module(new ALUrow(24, 9))) { c => new ALUtest(c, 24, 9) }
        
        // Behaves(?)
        // chiselMainTest(args, () => Module(new PixelGrid(24, 9, 3))) { c => new Img_test(c, 24, 9, 3) }
        
    }
}

object Util {
    def somefun(someval: Int) : Unit = {}
}
