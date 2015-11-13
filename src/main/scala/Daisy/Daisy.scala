package Core

import Chisel._

object DaisyMain {
    def main(args: Array[String]) {

        // chiselMainTest(args, () => Module(new PixelGrid(24, 9, 3))) { c => new PixelGridTest(c) }

        // chiselMainTest(args, () => Module(new Tile(24, 9, 3))) { c => new CoreTest(c) }

        // chiselMainTest(args, () => Module(new Tile(24, 9, 3))) { c => new Snapshot(c) }
        
        // chiselMainTest(args, () => Module(new Tile(24, 9, 3))) { c => new SimpleSnap(c) }
        
        // chiselMainTest(args, () => Module(new MinimalBRAM())) { c => new InputTest(c) }
        
        // Behaves
        // chiselMainTest(args, () => Module(new RowBuffer(10, 24, 1))) { c => new RowBufferTest(c) }

        // Behaves
        // chiselMainTest(args, () => Module(new SliceBuffer(3, 24, 2))) { c => new SliceBufferTest(c) }
        
        // Behaves
        // chiselMainTest(args, () => Module(new SliceDoubleBuffer(3, 24, 2))) { c => new DoubleBufferTest(c) }
    }
}
