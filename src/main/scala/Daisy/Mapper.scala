package Core

import Chisel._

// This component is currently a multiplier, but should be extended 
// to handle more interesting features. Low priority, but should be parametrized
// somewhere down the road.

class Mapper(data_width: Int) extends Module {

    val io = new Bundle { 

        val pixel_in = UInt(INPUT, data_width)
        val kernel_in = SInt(INPUT, data_width)
        val stall = Bool(INPUT)

        val mapped_pixel = SInt(OUTPUT, data_width) 
        val kernel_out = SInt(OUTPUT, data_width)
    } 

    val kernel = Reg(UInt(width=data_width))

    val color1 = io.pixel_in(7,0)
    val color2 = io.pixel_in(15,8)
    val color3 = io.pixel_in(23,16)

    io.kernel_out := UInt(57005)
    when(!io.stall){
        kernel := io.kernel_in
        io.kernel_out := kernel
    }

    io.mapped_pixel := UInt(0)

    io.mapped_pixel(7, 0) := color1*kernel
    io.mapped_pixel(15, 8) := color2*kernel
    io.mapped_pixel(23, 16) := color3*kernel
}
