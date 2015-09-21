package Core

import Chisel._

class PixelArray(data_width: Int, cols: Int) extends Module {
    val io = new Bundle {
        val data_in = UInt(INPUT, data_width)
        val data_out = UInt(OUTPUT, data_width)
        val ping_key = Bool(INPUT)
    }

    val pixels = Vec.fill(cols){ Module(new PixelReg(data_width)).io } 
    val reg_wire = Vec.fill(cols){ UInt(width=data_width) }
    val keys = Vec.fill(cols){ Bool() }

    // Wires up a daisychain of registers
    for (i <- 1 until cols){
        reg_wire(i)            :=      pixels(i).data_out
        // pixels(i).data_in        :=      io.data_in
        keys(i)                :=      pixels(i-1).enable_out
        pixels(i).enable_in      :=      keys(i)
    }

    io.data_out := reg_wire.reduce(_ + _)

    keys(0) := pixels(cols-1).enable_out
    pixels(0).enable_in := keys(0)
    pixels(0).data_in := io.data_in
    
    when(io.ping_key){
        keys(0) := Bool(true)
    }
}

class PixelArrayTest(c: PixelArray, width: Int, pixelReg: Int) extends Tester(c) {
}


