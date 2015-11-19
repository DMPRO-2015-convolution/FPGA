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



        // chiselMainTest(args, () => Module(new OutputHandler(32, 24, 16, 32, 3))) { c => new OutputHandlerTest(c) }

    }
}
