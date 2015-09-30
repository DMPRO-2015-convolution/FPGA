package Core

import Chisel._

// Controls transmitting of data from core to SRAM buffer
class SRAMhandler(data_width: Int, address_width: Int)  extends Module {
    val io = new Bundle {
        val reset = Bool(INPUT)

        val selected = Bool(OUTPUT)
        val address = UInt(OUTPUT, width=address_width)
    }

    val sram_select = Reg(init=Bool(false))
    val address = UInt(0, width=data_width)

    val MAX_ADDRESS = UInt(1234)

    val ram0 :: ram1 :: Nil = Enum(UInt(), 2)
    val selected = Reg(init=ram0) 

    when(address === MAX_ADDRESS){
        address := UInt(0)
        when(selected === ram0){
            selected := ram1
        }
        .otherwise{
            selected := ram0
        }
    }
    .otherwise{
        address := address + UInt(1)
    }

    io.selected := selected
    io.address := address
}
