package Core

import Chisel._

class PixelArray(data_width: Int, cols: Int) extends Module {
    val io = new Bundle {
        val data_in = Vec.fill(cols){UInt(INPUT, data_width)}
        val data_out = Vec.fill(cols){UInt(OUTPUT, data_width)}
        val data_select = UInt(OUTPUT, data_width)
        val ping_key = Bool(INPUT)
    }

    val pixels = Vec.fill(cols){ Module(new PixelReg(data_width)).io } 


    // Wire the keychain
    for (i <- 1 until cols){
        pixels(i).enable_in := pixels(i-1).enable_out
    }
    pixels(0).enable_in := (io.ping_key || pixels(cols-1).enable_out)


    // Wire data out and in
    for (i <- 0 until cols){
        pixels(i).data_in := io.data_in(i)
        io.data_out(i) := pixels(i).data_out
    }


    // Wire selected out TODO must be a better way
    io.data_select := UInt(0)
    for(i <- 0 until cols){
        when(pixels(i).selected_out === UInt(0)){
        }.otherwise{io.data_select := pixels(i).selected_out}
    }
}

class PixelArrayTest(c: PixelArray, width: Int, pixelReg: Int) extends Tester(c) {
}


