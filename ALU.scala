package Core

import Chisel._

class Multiplier(data_width: Int) extends Module {

    val io = new Bundle { 
        val pixel_in = UInt(INPUT, data_width)
        val kernel_in = UInt(INPUT, data_width)

        val data_out = UInt(OUTPUT, data_width) 
        val kernel_out = UInt(OUTPUT, data_width)
    } 

    val kernel = Reg(UInt(width=data_width))
    kernel := io.kernel_in
    io.kernel_out := kernel

    io.data_out := io.pixel_in*kernel
}

class Accumulator(data_width: Int) extends Module {

    val io = new Bundle { 
        val pixel_in = UInt(INPUT, data_width)
        val flush_in = Bool(INPUT)

        val data_out = UInt(OUTPUT, data_width) 
        val flush_out = Bool(OUTPUT)

    } 

    val accumulator = Reg(UInt(width=data_width))
    val flush = Reg(Bool())

    flush := io.flush_in
    io.flush_out := flush

    when(flush){ accumulator := io.pixel_in }
    .otherwise{ accumulator := accumulator + io.pixel_in }

    io.data_out := accumulator
}


class ALUrow(data_width: Int, cols: Int) extends Module{
    val io = new Bundle { 
        val data_in = Vec.fill(3){ UInt(INPUT, width=data_width) }
        val kernel_in = UInt(INPUT, width=data_width)
        val accumulator_flush = Bool(INPUT)
        val selector_shift_enable = Bool(INPUT)

        val data_out = Vec.fill(cols-2){ UInt(OUTPUT, width=data_width) }
        val kernel_out = UInt(OUTPUT, width=data_width)
    } 

    val multipliers = Vec.fill(cols-2){ Module(new Multiplier(data_width)).io }
    val accumulators = Vec.fill(cols-2){ Module(new Accumulator(data_width)).io }

    val selectors = Vec.fill(cols-2){ Module(new ShiftMux3(data_width, 3, 0)).io }
    val shift_enablers = Vec.fill(cols-2){ Bool() }

    
    // Wire ALU selectors
    for(i <- 0 until cols-2){
        for(j <- 0 until 3){
            selectors(i).data_in(j) := io.data_in(j)
        }
        multipliers(i).pixel_in := selectors(i).data_out 
        selectors(i).shift := shift_enablers(i)
    }
    // Wire shift enablers
    for(i <- 1 until (cols-2)){
        shift_enablers(i-1) := shift_enablers(i)
    }


    // Wire kernel chain
    multipliers(0).kernel_in := io.kernel_in
    multipliers(0).pixel_in := selectors(0).data_out
    accumulators(0).pixel_in := selectors(0).data_out
    
    for(i <- 1 until cols-2){
        multipliers(i).kernel_in := multipliers(i-1).kernel_out    
        multipliers(i).pixel_in := selectors(i).data_out
        accumulators(i).pixel_in := multipliers(i).data_out
    }
    // Since the kernel chain is cyclic it is needed handled outside this scope
    io.kernel_out := multipliers(cols-3).kernel_out


    // Wire accumulator chain
    io.data_out := accumulators(0).data_out 
    accumulators(0).flush_in := io.accumulator_flush

    for(i <- 1 until cols-2){
        io.data_out(i) := accumulators(i).data_out
        accumulators(i).flush_in := accumulators(i-1).flush_out
    }
}
