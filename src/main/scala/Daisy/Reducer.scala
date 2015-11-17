package Core

import Chisel._

// Currently only sums, but should be extended to be programmable to perform any form of reduce
// operation we need. Should be parametrized some day.

class Reducer(data_width: Int) extends Module {

    val io = new Bundle { 

        val get_reduce_instruction = Bool(INPUT)

        val mapped_pixel = SInt(INPUT, data_width)
        val flush = Bool(INPUT)
        val stall = Bool(INPUT)

        val data_out = UInt(OUTPUT, data_width) 
        val valid_out = Bool(OUTPUT)
    } 

    val instruction = Reg(UInt(0, 24))
    val accumulator = Reg(init=SInt(0, data_width))

    val color1 = io.mapped_pixel(7,0)
    val color2 = io.mapped_pixel(15,8)
    val color3 = io.mapped_pixel(23,16)

    when(!io.stall){
        
        when(io.get_reduce_instruction){
            instruction := io.mapped_pixel
        }
        .elsewhen(io.flush){ 
            accumulator(7, 0) := color1
            accumulator(15, 8) := color2
            accumulator(23, 16) := color3

        }.otherwise{
            accumulator(7, 0) := accumulator(7, 0) + color1
            accumulator(15, 8) := accumulator(15, 8) + color2
            accumulator(23, 16) := accumulator(23, 16) + color3
        }
    }

    io.data_out := accumulator

}
