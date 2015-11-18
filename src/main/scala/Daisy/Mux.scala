package Core

import Chisel._

class Mux(data_width: Int, regs_in: Int) extends Module {
    val io = new Bundle { 
        val pixel_in = Vec.fill(regs_in){ UInt(INPUT, data_width) }
        val enable_in = Bool(INPUT)
        val stall = Bool(INPUT)

        val data_out = UInt(OUTPUT, data_width) 
        val enable_out = Bool(OUTPUT)

        val dbg_enable = UInt(OUTPUT)
    } 

    val selected = Reg(UInt(width=data_width))

    io.data_out := selected

    val state = Reg(init=UInt(0, 8))

    when(io.enable_in){
        state := UInt(0)
    }

    io.enable_out := Bool(false)

    when(!io.stall){
        when(state === UInt(regs_in)){
        }.otherwise{
            state := state + UInt(1)
            when(state === UInt(regs_in - 1)){
                io.enable_out := Bool(true)
            }.otherwise{
                state := state + UInt(1)
            }
        }
    }
    
    for(i <- 0 until regs_in){
        when (state === UInt(i)){
            selected := io.pixel_in(i)
        }
    }

    io.dbg_enable := state
}
