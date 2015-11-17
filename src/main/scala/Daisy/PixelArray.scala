package Core

import Chisel._

class PixelArray(data_width: Int, cols: Int) extends Module {

    val n_column_groups = cols/3

    val io = new Bundle {
        val pixel_in = Vec.fill(n_column_groups){UInt(INPUT, data_width)}
        val ping_read = Bool(INPUT)
        val ping_mux = Bool(INPUT)
        val stall = Bool(INPUT)

        val data_out = Vec.fill(n_column_groups){UInt(OUTPUT, data_width)}

        val dbg_reg_contents = Vec.fill(cols){ UInt(OUTPUT, width=data_width) }
    }

    val pixels = Vec.fill(cols){ Module(new PixelReg(data_width)).io } 
    val primary_muxes = Vec.fill(n_column_groups) { Module(new Mux3(data_width, n_column_groups)).io }
    

    // Wire control chains
    // 

    // wire primary mux enabler chain
    primary_muxes(0).enable_in := io.ping_mux
    primary_muxes(1).enable_in := primary_muxes(0).enable_out
    primary_muxes(2).enable_in := primary_muxes(1).enable_out

    primary_muxes(0).stall := io.stall
    primary_muxes(1).stall := io.stall
    primary_muxes(2).stall := io.stall

    // wire pixel read read enable chain
    pixels(0).enable_in := io.ping_read
    for(i <- 1 until cols){
        pixels(i).enable_in := pixels(i-1).enable_out
    }


    // Wire inputs and outputs
    //

    // wire input tree to pixels
    for (i <- 0 until cols){
       pixels(i).pixel_in := io.pixel_in(i/3)
       pixels(i).stall := io.stall
    }


    // Wire pixel data out to primary muxes
    for (i <- 0 until cols){
        primary_muxes(i/3).pixel_in(i%3) := pixels(i).data_out
    }
    // Wire mux out data to pixelArray out
    for (i <- 0 until n_column_groups){
        io.data_out(i) := primary_muxes(i).data_out
    }

    for (i <- 0 until cols){
        io.dbg_reg_contents(i) := pixels(i).data_out
    }
}


class PixelArrayTest(c: PixelArray) extends Tester(c) {
    println("Pixel Array test")
    poke(c.io.ping_read, false)
    poke(c.io.ping_mux, false)
    
    for (i <- 0 until 27){
        if(i%9 == 0){
            poke(c.io.ping_read, true)
        }
        else if(i%9 == 5){
            poke(c.io.ping_mux, true)
        }
        else{
            poke(c.io.ping_read, false)
            poke(c.io.ping_mux, false)
        }
        poke(c.io.pixel_in(0), i)
        poke(c.io.pixel_in(1), i)
        poke(c.io.pixel_in(2), i)
        peek(c.io.data_out(0))
        peek(c.io.data_out(1))
        peek(c.io.data_out(2))
        step(1)
    }
}


