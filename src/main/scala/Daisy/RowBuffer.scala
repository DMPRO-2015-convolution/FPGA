package Core

import Chisel._

// Stole all of this code from some dude on github 
import TidbitsOCM._


// A stack which should synthesize to BRAM. Should be used to store slices.
// This stack only serves to buffer slices of our image which is done with two buffers
// thus we dont need to take too many precautions.
class RowBuffer(entries: Int, data_width: Int, number: Int) extends Module {
    
    val io = new Bundle {
        val data_in = UInt(INPUT, data_width)
        val push = Bool(INPUT)
        val pop = Bool(INPUT)

        val data_out = UInt(OUTPUT, data_width)
        val dbg_stack_top = UInt(OUTPUT, 32)
    }

    val stack_top = Reg(init=UInt(0, width=log2Up(entries)))
    val bram = Module(new DualPortBRAM(addrBits=log2Up(entries), dataBits=data_width)).io 

    val writePort = bram.ports(0)
    val readPort = bram.ports(1)

    writePort.req.writeData := io.data_in
    writePort.req.writeEn := Bool(false)
    writePort.req.addr := stack_top

    readPort.req.writeData := UInt(0)
    readPort.req.writeEn := Bool(false)
    readPort.req.addr := stack_top - UInt(1)

    when(io.pop){
        when(stack_top >= UInt(0)){
            stack_top := stack_top - UInt(1)
        }
    }
    when(io.push){
        when(stack_top < UInt(entries)){
            stack_top := stack_top + UInt(1)
            writePort.req.writeEn := Bool(true)
        }
    }

    // io.data_out := readPort.rsp.readData
    io.data_out := UInt(number)
    io.dbg_stack_top := stack_top
}
