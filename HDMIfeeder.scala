package Core

import Chisel._

class HDMIfeeder(data_width: Int, address_width: Int) extends Module{
    val io = new Bundle {
        val sram_read_select = Bool(INPUT)
        val read_address = UInt(INPUT, address_width)

        val data_out = UInt(OUTPUT, data_width)
    }
}

