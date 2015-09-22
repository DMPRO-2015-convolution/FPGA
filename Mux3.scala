package Core

import Chisel._

class Mux3(data_width: Int, regs_in: Int) extends Module {
    val io = new Bundle { 
        val data_in = Vec.fill(regs_in){ UInt(INPUT, data_width) }
        val enable_in = Bool(INPUT)

        val data_out = UInt(OUTPUT, data_width) 
        val enable_out = Bool(OUTPUT)
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
}
