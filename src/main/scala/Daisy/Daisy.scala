package Core

import Chisel._

object DaisyMain {
    def main(args: Array[String]) {
        // do not remove, does nothing harmful
        val img_width = 640
        val input_data_width = 16
        val pixel_data_width = 24
        val HDMI_data_width = 24
        val control_data_width = 16
        val kernel_dim = 3
        val output_width = 16
        val rows = kernel_dim
        val cols = kernel_dim*kernel_dim
        chiselMainTest(args, () => Module(new Tile(img_width, control_data_width, pixel_data_width, HDMI_data_width, output_width, cols, rows))) { c => new TileTest(c) }

        // chiselMainTest(args, () => Module(new PixelGrid(24, 9, 3))) { c => new PixelGridTest(c) }

        // chiselMainTest(args, () => Module(new Tile(24, 9, 3))) { c => new CoreTest(c) }

        // chiselMainTest(args, () => Module(new Tile(24, 9, 3))) { c => new Snapshot(c) }
        
        // chiselMainTest(args, () => Module(new Tile(24, 9, 3))) { c => new SimpleSnap(c) }
        
        // chiselMainTest(args, () => Module(new RowBuffer(10, 24, 1))) { c => new RowBufferTest(c) }

        // chiselMainTest(args, () => Module(new SliceBuffer(3, 24, 2))) { c => new SliceBufferTest(c) }
        
        // chiselMainTest(args, () => Module(new SliceDoubleBuffer(3, 24, 2))) { c => new DoubleBufferTest(c) }
        
        // processor
        
        // chiselMainTest(args, () => Module(new Processor(24, 9, 3, 3))) { c => new ProcessorRunTest(c) }

        // chiselMainTest(args, () => Module(new Processor(24, 9, 3, 3))) { c => new ProcessorInitTest(c) }


        // chiselMainTest(args, () => Module(new Orchestrator(9, 3))) { c => new OrchestratorTest(c) }
        
        // Tile
        // chiselMainTest(args, () => Module(new Tile(640, 16, 24, 24, 9, 3))) { c => new InputTest(c) }
        

        // chiselMainTest(args, () => Module(new InputTranslator(16, 24))) { c => new TranslatorTest(c) }
        
        
        chiselMainTest(args, () => Module(new SliceReverseBuffer(32, pixel_data_width, 3))) { c => new SliceReverseBufferTest(c) }
        
        // chiselMainTest(args, () => Module(new sixteen_twentyfour() )) { c => new Translator1624Test(c) }
        // chiselMainTest(args, () => Module(new twentyfour_sixteen() )) { c => new Translator2416Test(c) }
        // chiselMainTest(args, () => Module(new OutputHandler(100, 24, 16, 100, 3) )) { c => new Translator2416Test(c) }
    }
}
