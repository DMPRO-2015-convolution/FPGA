package Core

import Chisel._

// Currently only sums, but should be extended to be programmable to perform any form of reduce
// operation we need. Should be parametrized some day.

class Reducer(data_width: Int) extends Module {

    val io = new Bundle { 

        val load_instruction = Bool(INPUT)

        val red_in = SInt(INPUT, 8)
        val green_in = SInt(INPUT, 8)
        val blue_in = SInt(INPUT, 8)

        val red_out = SInt(OUTPUT, 8)
        val green_out = SInt(OUTPUT, 8)
        val blue_out = SInt(OUTPUT, 8)

        val flush = Bool(INPUT)
        val stall = Bool(INPUT)

        val valid_out = Bool(OUTPUT)

        val dbg_flush = Bool(OUTPUT)
    } 

    val instruction = Reg(UInt(0, 4))
    val accumulator = Reg(init=SInt(0, data_width))

    val red = Reg(init=SInt(0, 8))
    val green = Reg(init=SInt(0, 8))
    val blue = Reg(init=SInt(0, 8))

    when(!io.stall){
        
        when(io.load_instruction){
            instruction := io.red_in
        }
        .elsewhen(io.flush){ 
            red := red
            green := green
            blue := blue

        }.otherwise{
            
            when(instruction === UInt(0)){
                red := io.red_in            * red
                green := io.green_in        * green
                blue := io.blue_in          * blue
            }

            when(instruction === UInt(1)){
                red := io.red_in            + red
                green := io.green_in        + green
                blue := io.blue_in          + blue
            }

            when(instruction === UInt(2)){
                when(red < io.red_in)       { red := io.red_in     } 
                when(green < io.green_in)   { green := io.green_in } 
                when(blue < io.blue_in)     { blue := io.blue_in   } 
            }
        }
    }

    io.red_out := red
    io.green_out := green
    io.blue_out := blue

    io.valid_out := io.flush
}
