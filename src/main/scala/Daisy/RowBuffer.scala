package Core

import Chisel._

// Stole all of this code from some dude on github 
import TidbitsOCM._


// A stack which should synthesize to BRAM. Should be used to store slices.
// This stack only serves to buffer slices of our image which is done with two buffers
// thus we dont need to take too many precautions.
class RowBuffer(entries: Int, data_width: Int) extends Module {

    val io = new Bundle {

        val reset = Bool(INPUT)

        val data_in = UInt(INPUT, data_width)
        val push = Bool(INPUT)
        val pop = Bool(INPUT)

        val data_out = UInt(OUTPUT, data_width)
    }

    val stack_top = Reg(init=UInt(0, width=log2Up(entries)))
    val bram = Module(new DualPortBRAM(addrBits=log2Up(entries), dataBits=data_width)).io 

    val writePort = bram.ports(0)
    val readPort = bram.ports(1)

    when(io.reset){
        stack_top := UInt(0)
    }

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

    io.data_out := readPort.rsp.readData
}

class RowBufferTest(c: RowBuffer) extends Tester(c) {

    // Fill data
    poke(c.io.push, true)
    poke(c.io.pop, false)
    for(i <- 0 until 10){
        poke(c.io.data_in, (i%10))
        step(1)
    }
    poke(c.io.push, false)
    poke(c.io.pop, false)
    step(1)

    // pop data
    poke(c.io.push, false)
    poke(c.io.pop, true)
    for(i <- 0 until 11){
        peek(c.io.data_out)
        step(1)
    }
}

