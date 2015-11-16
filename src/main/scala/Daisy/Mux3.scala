package Core

import Chisel._

class Mux3(data_width: Int, regs_in: Int) extends Module {
    val io = new Bundle { 
        val data_in = Vec.fill(regs_in){ UInt(INPUT, data_width) }
        val enable_in = Bool(INPUT)
        val stall = Bool(INPUT)

        val data_out = UInt(OUTPUT, data_width) 
        val enable_out = Bool(OUTPUT)

        val dbg_enable = UInt(OUTPUT)
    } 

    val balancer = Reg(UInt(width=data_width))

    io.data_out := balancer

    val sleep :: s0 :: s1 :: s2 :: Nil = Enum(UInt(), 4)
    val state = Reg(init=UInt(width=data_width))

    when(io.enable_in){
        state := s0
    }

    io.enable_out := Bool(false)

    when(!io.stall){
        when(state === sleep){
        }otherwise{
            when(state === s2){
                state := sleep
                io.enable_out := Bool(true)
            }.otherwise{
                state := state + UInt(1)
            }
        }
    }
    

    switch (state) {
        is (s0){ balancer := io.data_in(0) }
        is (s1){ balancer := io.data_in(1) }
        is (s2){ balancer := io.data_in(2) }
        is (sleep) { balancer := UInt(57005) }
    }

    io.dbg_enable := state
}

class Mux3Test(c: Mux3, data_width: Int, regs_in: Int) extends Tester(c) {
    println("Mux3 Test")
    for(i <- 0 until 18){
        step(1)
        poke(c.io.data_in(i%3), i)
        peek(c.io.data_out)
        peek(c.io.dbg_enable)
        if(i%9 == 0){
            poke(c.io.enable_in, true)
        }
        else{
            (poke(c.io.enable_in, false))
        }
    }
}
