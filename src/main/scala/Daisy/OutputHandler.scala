package Core

import Chisel._
import TidbitsOCM._

// Feeds an SRAM
class OutputHandler(data_width: Int, img_width: Int, img_height: Int, kernel_dim: Int) extends Module {

    // This approach may be changed up a little... Stay tuned
    val mantle_width = (kernel_dim - 1)/2
    val valid_rows_per_image = img_height - (mantle_width*2)
    val valid_pixels_per_row = img_width - (mantle_width*2)
    
    val io = new Bundle {

        val input_valid = Bool(INPUT)
        val data_in = UInt(INPUT, data_width)

    }

    // False: Chip 0, True: Chip 1
    val chip_sel = Reg(init=Bool(false)) 

    // Does nothing...

}
