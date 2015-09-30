package Core

import Chisel._

class SRAMhandler()  extends Module {
    val io = new Bundle {
        val data_in = UINT(INPUT, data_width)
    }

    val sram_select = 0
    val address = 0

    val max_address = MAX_ADDRESS

    val sram0 = Mem(Bits(width=data_width), 4096, seqRead=True)
    val sram1 = Mem(Bits(width=data_width), 4096, seqRead=True)

    when (data_in){
        if (!sram_select){
            sram0(address) := io.data_in
            if (address = max_address){
                addr := 0
                sram_select := 1
            }  
        }
        else{
            sram1(addr) := io.data_in
            if (address = max_address){
                addr := 0
                sram_select := 0
            }  
        }
    }
}
