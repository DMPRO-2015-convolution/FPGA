package Core

import Chisel._
import TidbitsOCM._

class BRAMtest() extends Module {

    val data_width = 24

    val io = new Bundle {

        val data_in = UInt(INPUT, data_width)

        val enq = Bool(INPUT)
        val can_enq = Bool(OUTPUT)

        val deq = Bool(INPUT)
        val can_deq = Bool(OUTPUT)

        val data_out = UInt(OUTPUT, data_width)

    }

    val bram = Module(new DualPortBRAM(addrBits=log2Up(24), dataBits=data_width)).io 
    val writePort = bram.ports(0)
    val readPort = bram.ports(1)

    val enq :: deq :: Nil = Enum(UInt(), 2)
    val mode = Reg(init=UInt(enq))

    val q_end = Reg(init=UInt(0, width=log2Up(24)))

    io.can_enq := Bool(false)
    io.can_deq := Bool(false)


    writePort.req.writeData := io.data_in
    writePort.req.writeEn := Bool(false)
    writePort.req.addr := q_end


    readPort.req.writeData := UInt(0)
    readPort.req.writeEn := Bool(false)
    readPort.req.addr := q_end - UInt(1)

    io.data_out := UInt(0)

    when(mode === UInt(enq)){
        io.can_enq := Bool(true)
        when(io.enq){
            writePort.req.writeEn := Bool(true)
            q_end := q_end + UInt(1)

        }
        when(q_end === UInt(24)){
            mode := UInt(deq)
        }
    }

    when(mode === UInt(deq)){
        io.can_deq := Bool(true)
        when(io.deq){
            q_end := q_end - UInt(1)
            io.data_out := readPort.rsp.readData
        }
        when(q_end === UInt(0)){
          mode := enq
        }
    }



}

class TestTest(c: BRAMtest) extends Tester(c) {
    println("What the fug")
    poke(c.io.enq, true)
    poke(c.io.deq, false)
    poke(c.io.data_in, 0)
    peek(c.io)
    for(i <- 0 until 25){
        poke(c.io.data_in, i)
        step(1)
    }
    poke(c.io.enq, false)
    poke(c.io.deq, true)
    peek(c.q_end)
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
