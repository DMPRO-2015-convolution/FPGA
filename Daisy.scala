package Core

import Chisel._

object DaisyMain {
    def main(args: Array[String]) {
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
        
        // ???
      // chiselMainTest(args, () => Module(new PixelGrid(24, 9, 3))) { c => new image(c, 24, 9, 3) }
        
        // Bretty gud :--DDD
        // chiselMainTest(args, () => Module(new PixelGrid(24, 9, 3))) { c => new Snapshot(c) }
    }
}
