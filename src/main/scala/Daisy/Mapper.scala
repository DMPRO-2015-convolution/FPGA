package Core

import Chisel._

// This component is currently a multiplier, but should be extended 
// to handle more interesting features. Low priority, but should be parametrized
// somewhere down the road.

class Mapper(data_width: Int) extends Module {

    val io = new Bundle { 

        val load_instruction = Bool(INPUT)

        val pixel_in = UInt(INPUT, data_width)
        val kernel_in = SInt(INPUT, 8)
        val stall = Bool(INPUT)

        val red = SInt(OUTPUT, 8) 
        val green = SInt(OUTPUT, 8) 
        val blue = SInt(OUTPUT, 8) 

        val kernel_out = SInt(OUTPUT, 8)

        val dbg_kernel = SInt(OUTPUT, 8)
        val dbg_instr = SInt(OUTPUT, 4)

    } 

    val instruction = Reg(UInt(0, 4))

    val kernel = Reg(UInt(width=data_width))

    val red = Reg(init=UInt(0, 8))
    val green = Reg(init=UInt(0, 8))
    val blue = Reg(init=UInt(0, 8))

    io.dbg_kernel := kernel
    io.dbg_instr := instruction

    io.kernel_out := UInt(57005)
    when(!io.stall){

        when(io.load_instruction){
            instruction := io.kernel_in(3, 0)
            io.red := io.kernel_in(7, 0)
        }

        kernel := io.kernel_in
        io.kernel_out := kernel

        when(instruction === UInt(0)){
            red := io.pixel_in(7, 0)         * kernel
            green := io.pixel_in(15, 8)      * kernel
            blue := io.pixel_in(23, 16)      * kernel
        }

        when(instruction === UInt(1)){
            red := io.pixel_in(7, 0)         + kernel
            green := io.pixel_in(15, 8)      + kernel
            blue := io.pixel_in(23, 16)      + kernel
        }

        when(instruction === UInt(2)){
            red := io.pixel_in(7, 0)         / kernel
            green := io.pixel_in(15, 8)      / kernel
            blue := io.pixel_in(23, 16)      / kernel
        }
    }

    io.red := UInt(0)
    io.green := UInt(0)
    io.blue := UInt(0)

    io.red(7, 0)    := red
    io.green(15, 8) := green
    io.blue(23, 16) := blue

}
