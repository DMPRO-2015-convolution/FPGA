package Core

import Chisel._

class ALUrow(data_width: Int, cols: Int, rows: Int, kernel_dim: Int) extends Module{

    val mantle_width = kernel_dim/2
    val n_ALUs = cols - mantle_width*2  

    val io = new Bundle { 
        val pixel_in = Vec.fill(rows){ UInt(INPUT, width=data_width) }
        val kernel_in = SInt(INPUT, 8)
        val accumulator_flush = Bool(INPUT)
        val selector_shift = Bool(INPUT)
        val stall = Bool(INPUT)

        val load_instruction = Bool(INPUT)

        val data_out = UInt(OUTPUT, width=data_width)
        val kernel_out = SInt(OUTPUT, 8)
        val valid_out = Bool(OUTPUT)
    } 

    val mappers = Vec.fill(n_ALUs){ Module(new Mapper(data_width)).io }
    val reducers = Vec.fill(n_ALUs){ Module(new Reducer(data_width)).io }
    val selectors = Vec.fill(n_ALUs){ Module(new ShiftMux(data_width, rows, 2)).io }

    val normalizer = Module(new Normalizer(data_width))
    normalizer.io.red_in := UInt(0)
    normalizer.io.green_in := UInt(0)
    normalizer.io.blue_in := UInt(0)

    val shift_enablers = Vec.fill(n_ALUs){ Reg(Bool()) }
    val flush_signals = Vec.fill(n_ALUs){ Reg(Bool()) }


    // Wire ALU selectors
    for(i <- 0 until n_ALUs){
        for(j <- 0 until 3){
            selectors(i).pixel_in(j) := io.pixel_in(j)
            selectors(i).stall := io.stall
            selectors(i).reset := io.stall
        }
        mappers(i).pixel_in := selectors(i).data_out 
        selectors(i).shift := shift_enablers(i)
    }

    daisy_chain(io.selector_shift, shift_enablers)
    daisy_chain(io.accumulator_flush, flush_signals)
    

    // Wire flush enablers
    for(i <- 0 until (n_ALUs)){
        reducers(i).flush := flush_signals(i)
    }


    // Wire kernel chain
    mappers(0).kernel_in := io.kernel_in
    mappers(0).pixel_in := selectors(0).data_out
    mappers(0).stall := io.stall
    mappers(0).load_instruction := io.load_instruction

    reducers(0).red_in := mappers(0).red
    reducers(0).green_in := mappers(0).green
    reducers(0).blue_in := mappers(0).blue

    reducers(0).stall := io.stall
    reducers(0).load_instruction := io.load_instruction
    
    for(i <- 1 until n_ALUs){
        mappers(i).kernel_in := mappers(i-1).kernel_out    
        mappers(i).pixel_in := selectors(i).data_out

        reducers(i).red_in := mappers(i).red
        reducers(i).green_in := mappers(i).green
        reducers(i).blue_in := mappers(i).blue

        mappers(i).stall := io.stall
        reducers(i).stall := io.stall
        reducers(i).load_instruction := io.load_instruction
        mappers(i).load_instruction := io.load_instruction
    }


    // Since the kernel chain is cyclic it is needed outside this scope
    io.kernel_out := mappers(n_ALUs - 1).kernel_out


    // Get output, or DEAD when none available
    io.data_out := UInt(57005)
    io.valid_out := Bool(false)
    for(i <- 0 until n_ALUs){
        when(flush_signals(i)){
            io.data_out(7, 0) := reducers(i).red_out
            io.data_out(15, 8) := reducers(i).green_out
            io.data_out(23, 16) := reducers(i).blue_out
            normalizer.io.red_in := reducers(i).red_out
            normalizer.io.green_in := reducers(i).green_out
            normalizer.io.blue_in := reducers(i).blue_out
            io.valid_out := Bool(true)
        }
    }

    // io.data_out := normalizer.io.data_out


    def daisy_chain[T <: Data](input: T, elements: Vec[T]){
        elements(0) := input
        for(i <- 1 until elements.length){
            elements(i) := elements(i-1)
        }
    }
}
