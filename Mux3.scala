package Core

import Chisel._

class Mux3(data_width: Int, regs_in: Int) extends Module {
    val io = new Bundle { 
        val data_in = Vec.fill(regs_in){ UInt(INPUT, data_width) }
        val enable_in = Bool(INPUT)

        val data_out = UInt(OUTPUT, data_width) 
        val enable_out = Bool(OUTPUT)

        val dbg_enable = Vec.fill(3)( {Bool(OUTPUT)} )
        val dbg_data_out = UInt(OUTPUT, data_width) 
        val dbg_data_in = Vec.fill(regs_in){ UInt(OUTPUT, data_width) }
    } 

    val balancer = Reg(UInt(width=data_width))
    val read_keys = Vec.fill(regs_in){ Reg(init=Bool(false)) }


    // (manually) wire up the internal read chain
    read_keys(0) := io.enable_in
    read_keys(1) := read_keys(0)
    read_keys(2) := read_keys(1)
    io.enable_out := read_keys(2)


    // three line mux
    when(read_keys(0) === Bool(true)){ balancer := io.data_in(0) 
    }.elsewhen(read_keys(1) === Bool(true)) { balancer := io.data_in(1)
    }.otherwise{ balancer := io.data_in(2) }

    io.data_out := balancer

    for(i <- 0 until 3){
        io.dbg_enable(i) := read_keys(i)
    }
}

class Mux3Test(c: Mux3, data_width: Int, regs_in: Int) extends Tester(c) {
    println("Mux3 Test")
    for(i <- 0 until 18){
        step(1)
        poke(c.io.data_in(i%3), i)
        peek(c.io.data_out)
        peek(c.io.enable_out)
        if(i%9 == 0){
            poke(c.io.enable_in, true)
        }
        else{
            (poke(c.io.enable_in, false))
        }
    }
}
