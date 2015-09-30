package Core

import Chisel._

class HDMIfeeder() extends Module{
    val io = new Bundle {
        val sram_read_select = Bool(INPUT)
        val read_address = UInt(INPUT, address_width)

        val data_out = UInt(OUTPUT, data_width)

    }

    when (io.sram_read_select){
        io.data_out := sram1(io.read_address)
    }
    when (!io.sram_read_select){
        io.data_out := sram0(io.read_address)
    }
}

