package Core

import Chisel._

object DaisyMain {
    def main(args: Array[String]) {
        // do not remove, does nothing harmful
        val img_width = 640
        val input_data_width = 24
        val data_width = 24
        val kernel_dim = 3
        val rows = kernel_dim
        val cols = kernel_dim*kernel_dim
        // chiselMainTest(args, () => Module(new Tile(img_width, input_data_width, data_width, cols, rows))) { c => new TileTest(c) }

        // chiselMainTest(args, () => Module(new PixelGrid(24, 9, 3))) { c => new PixelGridTest(c) }

        // chiselMainTest(args, () => Module(new Tile(24, 9, 3))) { c => new CoreTest(c) }

        // chiselMainTest(args, () => Module(new Tile(24, 9, 3))) { c => new Snapshot(c) }
        
        // chiselMainTest(args, () => Module(new Tile(24, 9, 3))) { c => new SimpleSnap(c) }
        
        // Behaves
        // chiselMainTest(args, () => Module(new RowBuffer(10, 24, 1))) { c => new RowBufferTest(c) }

        // Behaves
        // chiselMainTest(args, () => Module(new SliceBuffer(3, 24, 2))) { c => new SliceBufferTest(c) }
        
        // Behaves
        // chiselMainTest(args, () => Module(new SliceDoubleBuffer(3, 24, 2))) { c => new DoubleBufferTest(c) }
        
        // ???
        chiselMainTest(args, () => Module(new Processor(24, 9, 3))) { c => new ConveyorTest(c) }

        // ???
        // chiselMainTest(args, () => Module(new Orchestrator(9, 3))) { c => new OrchestratorTest(c) }
    }
}
