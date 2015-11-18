package Core

import Chisel._

class ShiftMux(data_width: Int, regs_in: Int, default: Int) extends Module {
    val io = new Bundle { 

        val pixel_in = Vec.fill(regs_in){ UInt(INPUT, data_width) }
        val shift = Bool(INPUT)
        val stall = Bool(INPUT)
        val reset = Bool(INPUT)

        val data_out = UInt(OUTPUT, data_width) 

        val dbg_state = UInt(OUTPUT)
    } 

    val selected = Reg(UInt(width=data_width))

    io.data_out := selected

    val state = Reg(init=UInt(default, 8))

    when(io.reset){
        state := UInt(default)
    }
    .elsewhen(!io.stall){
        when(io.shift){
            when(state === UInt(regs_in - 1)){ state := UInt(0) }
            .otherwise{ state := state + UInt(1) }
        }
    }

    for(i <- 0 until regs_in){
        when (state === UInt(i)){
            selected := io.pixel_in(i)
        }
    }

    io.dbg_state := state
}
