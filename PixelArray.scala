package Core

import Chisel._

// TODO figure out how to assert cols div 3
class PixelArray(data_width: Int, cols: Int) extends Module {
    val io = new Bundle {
        val data_in = Vec.fill(cols/3){UInt(INPUT, data_width)}
        val ping_read_key = Bool(INPUT)
        val ping_mux_key = Bool(INPUT)

        val data_out = Vec.fill(cols/3){UInt(OUTPUT, data_width)}
    }

    val pixels = Vec.fill(cols){ Module(new PixelReg(data_width)).io } 
    val primary_muxes = Vec.fill(cols/3) { Module(new Mux3(data_width, cols/3)).io }
    


    // (manually) wire mux enablers
    primary_muxes(0).enable_in := (io.ping_mux_key || primary_muxes(2).enable_out)
    primary_muxes(1).enable_in := primary_muxes(0).enable_out
    primary_muxes(2).enable_in := primary_muxes(1).enable_out
    

    // Wire pixels to input group and output primary_muxes
    for (i <- 0 until cols){
        primary_muxes(i/3).data_in(i%3) := pixels(i).data_out
    }
    for (i <- 0 until cols/3){
        io.data_out(i) := primary_muxes(i).data_out
    }


    // Wire the pixel read keychain
    pixels(0).enable_in := (io.ping_read_key || pixels(cols-1).enable_out)
    for (i <- 1 until cols){
        pixels(i).enable_in := pixels(i-1).enable_out
    }
}

class PixelArrayTest(c: PixelArray, width: Int, pixelReg: Int) extends Tester(c) {
}

