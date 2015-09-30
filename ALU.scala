package Core

import Chisel._

class Multiplier(data_width: Int) extends Module {

    val io = new Bundle { 
        val pixel_in = UInt(INPUT, data_width)
        val kernel_in = SInt(INPUT, data_width)

        val data_out = UInt(OUTPUT, data_width) 
        val kernel_out = UInt(OUTPUT, data_width)
    } 

    val kernel = Reg(UInt(width=data_width))

    val color1 = io.pixel_in(7,0)
    val color2 = io.pixel_in(15,8)
    val color3 = io.pixel_in(23,16)

    kernel := io.kernel_in
    io.kernel_out := kernel

    io.data_out := UInt(0)

    io.data_out(7, 0) := color1*kernel
    io.data_out(15, 8) := color2*kernel
    io.data_out(23, 16) := color3*kernel
}

class Accumulator(data_width: Int) extends Module {

    val io = new Bundle { 
        val pixel_in = UInt(INPUT, data_width)
        val flush = Bool(INPUT)

        val data_out = UInt(OUTPUT, data_width) 

    } 

    val accumulator = Reg(UInt(width=data_width))

    val color1 = io.pixel_in(7,0)
    val color2 = io.pixel_in(15,8)
    val color3 = io.pixel_in(23,16)

    when(io.flush){ 
        accumulator(7, 0) := color1
        accumulator(15, 8) := color2
        accumulator(23, 16) := color3
    }.otherwise{
        accumulator(7, 0) := accumulator(7, 0) + color1
        accumulator(15, 8) := accumulator(15, 8) + color2
        accumulator(23, 16) := accumulator(23, 16) + color3
    }

    io.data_out := accumulator
}


class ALUrow(data_width: Int, cols: Int, rows: Int) extends Module{

    val n_ALUs = cols - 2  

    val io = new Bundle { 
        val data_in = Vec.fill(rows){ UInt(INPUT, width=data_width) }
        val kernel_in = UInt(INPUT, width=data_width)
        val accumulator_flush = Bool(INPUT)
        val selector_shift_enable = Bool(INPUT)

        val data_out = UInt(OUTPUT, width=data_width)
        val kernel_out = UInt(OUTPUT, width=data_width)

        val dbg_accumulators = Vec.fill(n_ALUs){ UInt(OUTPUT, width=data_width) }
    } 

    val multipliers = Vec.fill(n_ALUs){ Module(new Multiplier(data_width)).io }
    val accumulators = Vec.fill(n_ALUs){ Module(new Accumulator(data_width)).io }

    val selectors = Vec.fill(n_ALUs){ Module(new ShiftMux3(data_width, 3, 2)).io }
    val shift_enablers = Vec.fill(n_ALUs){ Reg(Bool()) }
    val flush_signals = Vec.fill(n_ALUs){ Reg(Bool()) }


    // Wire ALU selectors
    for(i <- 0 until n_ALUs){
        for(j <- 0 until 3){
            // Pay attention to the reversal!
            selectors(i).data_in(2-j) := io.data_in(j)
        }
        multipliers(i).pixel_in := selectors(i).data_out 
        selectors(i).shift := shift_enablers(i)
    }
    // Wire shift enablers
    for(i <- 1 until (n_ALUs)){
        shift_enablers(i) := shift_enablers(i-1)
    }
    shift_enablers(0) := io.selector_shift_enable


    // Wire flush enablers
    for(i <- 1 until (n_ALUs)){
        flush_signals(i) := flush_signals(i-1)
        accumulators(i).flush := flush_signals(i)
    }
    accumulators(0).flush := flush_signals(0)
    flush_signals(0) := io.accumulator_flush


    // Wire kernel chain
    multipliers(0).kernel_in := io.kernel_in
    multipliers(0).pixel_in := selectors(0).data_out
    accumulators(0).pixel_in := selectors(0).data_out
    
    for(i <- 1 until n_ALUs){
        multipliers(i).kernel_in := multipliers(i-1).kernel_out    
        multipliers(i).pixel_in := selectors(i).data_out
        accumulators(i).pixel_in := multipliers(i).data_out
    }
    // Since the kernel chain is cyclic it is needed outside this scope
    io.kernel_out := multipliers(n_ALUs - 1).kernel_out


    // TODO brain this into using a tree or something
    io.data_out := UInt(0)
    for(i <- 0 until n_ALUs){
        when(flush_signals(i)){ io.data_out := accumulators(i).data_out }

        io.dbg_accumulators := accumulators(i).data_out
    }
}


class ALUtest(c: ALUrow, data_width: Int, cols: Int) extends Tester(c) {
    println("ALU testan")

    poke(c.io.kernel_in, 1)


    for(i <- 0 to 60){
        poke(c.io.data_in(0), (i + 7) %9 + 1)
        poke(c.io.data_in(1), (i + 1) %9 + 1)
        poke(c.io.data_in(2), (i + 4) %9 + 1)

        if(i%9 == 0){ poke(c.io.accumulator_flush, true) } else {poke(c.io.accumulator_flush, false)} 
        if(i%3 == 0){ poke(c.io.selector_shift_enable, true) } else {poke(c.io.selector_shift_enable, false)} 
        println("\n")
        println("sel 0\n")
        peek(c.selectors(0))
        println("\n")
        // println("sel 1\n")
        // peek(c.selectors(1))
        // println("\n")
        peek(c.io.data_out)
        println("\n\n\n")
        step(1)
        println("\n")
    }
}

