package Core

import Chisel._

object DaisyMain {
    def main(args: Array[String]) {
        // do not remove, does nothing harmful
        val img_width = 640
        val data_width = 24
        val kernel_dim = 3
        val rows = kernel_dim
        val cols = kernel_dim*kernel_dim

        // val daisy = Module(new Tile(img_width, data_width, cols, rows))


        // object chiselMain {
        //     def apply[T <: Module]
        //     (args: Array[String], daisy: () => T): T
        // }

        chiselMain(args, () => Module(new Tile(img_width, data_width, cols, rows)))


        ///////////////////////////////////
        /////////////////
        /////////////////  TILE
        /////////////////
        ///////////////////////////////////
       
        // chiselMainTest(args, () => Module(new Tile(img_width, data_width, cols, rows))) { c => new TileTest(c) }

        // chiselMainTest(args, () => Module(new Tile(img_width, data_width, cols, rows))) { c => new InputTest(c) }

        

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
        // chiselMainTest(args, () => Module(new OutputHandler(32, 24, 16, 32, 3))) { c => new OutputHandlerTest(c) }

    }
}
