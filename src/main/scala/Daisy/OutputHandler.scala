package Core

import Chisel._
import TidbitsOCM._

// Feeds an SRAM
class OutputHandler(data_width: Int, img_width: Int) extends Module {
    
    val io = new Bundle {

        val input_valid = Bool(INPUT)
        val data_in = UInt(INPUT, data_width)

    }

    // Does nothing...

}
