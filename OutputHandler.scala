package Core

import Chisel._

class OutputHandler(data_width: Int, address_width: Int) extends Module{
    val io = new Bundle {
        val processed_data_in = UInt(INPUT, width=data_width)
        val sram0_in = UInt(INPUT, width=data_width)
        val sram1_in = UInt(INPUT, width=data_width)
        val sram_select = Bool(INPUT)

        val HDMI_feed = UInt(OUTPUT, width=data_width)
        val address_out = UInt(OUTPUT, width=address_width)
    }
    
    val HDMI = Module(new HDMIfeeder(data_width, address_width)).io
    val RAM = Module(new SRAMhandler(data_width, address_width)).io

    val sram0 = Mem(Bits(width=data_width), 4096, seqRead=true)
    val sram1 = Mem(Bits(width=data_width), 4096, seqRead=true)

}
