package Core

import Chisel._

class Orchestrator(cols: Int, rows: Int)  extends Module {
    val io = new Bundle {
        val reset = Bool(INPUT)

        val pings = Vec.fill(cols/3 + rows + 1){ Bool(OUTPUT) }


        // val out = UInt(OUTPUT)
    }

    /*
    *   0 - Secondary mux
    *   1 - READ 0
    *   2 - PRIMARY MUX 0
    *   3 - READ 1
    *   4 - PRIMARY MUX 1
    *   5 - READ 2
    *   6 - PRIMARY MUX 2
    */

    val s0 :: s1 :: s2 :: s3 :: s4 :: s5 :: s6 :: s7 :: s8 :: Nil = Enum(UInt(), 9)
    val state = Reg(init=s0)


    // State transitions
    when(io.reset === Bool(true)){
        state := UInt(0)
    }.otherwise{
        when(state === s8){ state := s0
        }.otherwise(state := state + UInt(1))
    }

    
    // Default pings
    for(i <- 0 until io.pings.size){
        io.pings(i) := Bool(false) 
    }


    // See commet for map
    switch (state) {
        is (s0){ io.pings(6) := Bool(true) }
        is (s1){ io.pings(0) := Bool(true); io.pings(1) := Bool(true) }
        is (s3){ io.pings(4) := Bool(true) }
        is (s4){ io.pings(5) := Bool(true) }
        is (s6){ io.pings(2) := Bool(true) }
        is (s7){ io.pings(3) := Bool(true) }
        is (s8){ state := s0 }
    }

    // io.out := state
}

class OrchestratorTest(c: Orchestrator, cols: Int, rows: Int) extends Tester(c) {
    println("OrchestratorTest")
    step(5)
    poke(c.io.reset, true)
    peek(c.io)
    step(2)
    peek(c.io)
    poke(c.io.reset, false)
    for(i <- 0 until 8){
        step(1)
        peek(c.io)
    }
}
