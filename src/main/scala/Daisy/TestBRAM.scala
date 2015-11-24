package Core

import Chisel._
import TidbitsOCM._

class BRAMtest() extends Module {

    val data_width = 24

    val io = new Bundle {

        val data_in = UInt(INPUT, data_width)

        val data_out = UInt(OUTPUT, data_width)

    }

    val bram = Module(new DualPortBRAM(addrBits=log2Up(24), dataBits=data_width)).io 
    val writePort = bram.ports(0)
    val readPort = bram.ports(1)

    val q_end = Reg(init=UInt(0, width=log2Up(24)))
    val q_start = Reg(init=UInt(0, width=log2Up(24)))



    writePort.req.writeData := io.data_in
    writePort.req.writeEn := Bool(true)
    writePort.req.addr := q_end


    readPort.req.writeData := UInt(0)
    readPort.req.writeEn := Bool(false)
    readPort.req.addr := q_start


    when(q_end === UInt(24)){
        q_end := UInt(0)
    }
    .otherwise{
        q_end := q_end + UInt(1)
    }

    when(q_start === UInt(0)){
        q_start := UInt(24)
    }
    .otherwise{
        q_start := q_start - UInt(1)
    }

    io.data_out := readPort.rsp.readData

}

class TestTest(c: BRAMtest) extends Tester(c) {
    println("What the fug")
    poke(c.io.data_in, 0)
    peek(c.io)
    for(i <- 0 until 25){
        poke(c.io.data_in, i)
        step(1)
    }
    peek(c.io)
    for(i <- 0 until 27){
        poke(c.io.data_in, i)
        peek(c.q_end)
        peek(c.io)
        println()
        println()
        peek(c.io.data_out)
        println()
        println()
        step(1)
    }
}
