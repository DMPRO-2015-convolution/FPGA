package Core

import Chisel._

object DaisyMain {
    def main(args: Array[String]) {
        // do not remove, does nothing harmful
        val img_width = 10
        val data_width = 24
        val kernel_dim = 3
        val rows = kernel_dim
        val cols = kernel_dim*kernel_dim

        // val daisy = Module(new Tile(img_width, data_width, cols, rows))

        // chiselMain(args, () => Module(new Tile(img_width, data_width, cols, rows)))


        ///////////////////////////////////
        /////////////////
        /////////////////  TILE
        /////////////////
        ///////////////////////////////////

        chiselMainTest(args, () => Module(new Tile(img_width, data_width, cols, rows))) { c => new TileTest(c) }

        chiselMainTest(args, () => Module(new Tile(img_width, data_width, cols, rows))) { c => new InputTest(c) }




        // chiselMainTest(args, () => Module(new BRAMtest())) { c => new TestTest(c) }

        ///////////////////////////////////
        /////////////////
        /////////////////  PROCESSOR
        /////////////////
        ///////////////////////////////////

        // chiselMainTest(args, () => Module(new Processor(data_width, cols, rows, kernel_dim))) { c => new ProcessorRunTest(c) }



        ///////////////////////////////////
        /////////////////
        /////////////////  OUTPUT
        /////////////////
        ///////////////////////////////////

        // chiselMainTest(args, () => Module(new ReverseDoubleBuffer(10, 24, 3))) { c => new RDBtest(c) }



        ///////////////////////////////////
        /////////////////
        /////////////////  INPUT
        /////////////////
        ///////////////////////////////////

        // chiselMainTest(args, () => Module(new SliceDoubleBuffer(10, 24, 3))) { c => new DoubleBufferTest(c) }
    }
}
