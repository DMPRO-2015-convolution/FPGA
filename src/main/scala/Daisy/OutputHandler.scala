package Core

import Chisel._
import TidbitsOCM._

// Buffers output which can be fed out at whatever pace
class OutputHandler(row_length: Int, pixel_data_width: Int, output_data_width: Int, img_width: Int, img_height: Int, kernel_dim: Int) extends Module {
 
    val entries = (row_length*pixel_data_width)/output_data_width

    val mantle_width = (kernel_dim)/2
    val valid_rows_per_image = img_height - (mantle_width*2)
    val valid_pixels_per_row = img_width - (mantle_width*2)
    val slices_per_image = valid_rows_per_image / (kernel_dim*kernel_dim - 2)

    println("Slices: %d".format(slices_per_image))
    
    val io = new Bundle {

        val input_valid = Bool(INPUT)
        val data_in = UInt(INPUT, pixel_data_width)

    }

    // False: Chip 0, True: Chip 1
    val chip_sel = Reg(init=Bool(false)) 
    val bram = Module(new DualPortBRAM(addrBits=log2Up(entries), dataBits=pixel_data_width)).io 

    val translator = Module(new WidthTranslator(pixel_data_width, output_data_width))

    // Does nothing...

}
