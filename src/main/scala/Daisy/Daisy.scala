package Core

import Chisel._

object DaisyMain {
    def main(args: Array[String]) {
        // do not remove, does nothing harmful
        val img_width = 60
        val data_width = 24
        val kernel_dim = 3
        val rows = kernel_dim
        val cols = kernel_dim*kernel_dim



        ///////////////////////////////////
        /////////////////
        /////////////////  WIDTH
        /////////////////
        ///////////////////////////////////

        // chiselMainTest(args, () => Module(new sixteen_twentyfour())) { c => new Translator1624Test(c) }

        // chiselMainTest(args, () => Module(new twentyfour_sixteen())) { c => new Translator2416Test(c) }





        ///////////////////////////////////
        /////////////////
        /////////////////  TILE
        /////////////////
        ///////////////////////////////////
       
        // chiselMainTest(args, () => Module(new Tile(img_width, control_data_width, pixel_data_width, HDMI_data_width, output_width, cols, rows))) { c => new TileTest(c) }

        chiselMainTest(args, () => Module(new Tile(img_width, data_width, cols, rows))) { c => new InputTest(c) }

        

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
