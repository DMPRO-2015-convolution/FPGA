package Core

import Chisel._

class ShiftMux3(data_width: Int, regs_in: Int, default: Int) extends Module {
    val io = new Bundle { 
        val data_in = Vec.fill(regs_in){ UInt(INPUT, data_width) }
        val shift = Bool(INPUT)
        val active = Bool(INPUT)

        val data_out = UInt(OUTPUT, data_width) 

        val dbg_state = UInt(OUTPUT)
    } 

    val balancer = Reg(UInt(width=data_width))

    io.data_out := balancer

    val s0 :: s1 :: s2 :: Nil = Enum(UInt(), 3)
    val state = Reg(init=UInt(default, width=data_width))

    when(io.active){
        when(io.shift){
            when(state === s2){ state := s0 }
            .otherwise{state := state + UInt(1)}
        }
    }

    switch (state) {
        is (s0){ balancer := io.data_in(0) }
        is (s1){ balancer := io.data_in(1) }
        is (s2){ balancer := io.data_in(2) }
    }

    io.dbg_state := state
}
