package Core

import Chisel._

class PixelArray(bits_per_pixel: Int, reg_width: Int) extends Module {
    val io = new Bundle {
        val data_in = UInt(INPUT, bits_per_pixel)
        val data_out = UInt(OUTPUT, bits_per_pixel)
        val ping_key = Bool(INPUT)
    }

    val regs = Vec.fill(reg_width){ Module(new PixelReg(bits_per_pixel)).io } 
    val reg_wire = Vec.fill(reg_width){ UInt(width=bits_per_pixel) }
    val keys = Vec.fill(reg_width){ Bool() }

    // Wires up a daisychain of registers
    for (i <- 1 until reg_width){
        reg_wire(i)            :=      regs(i).data_out
        regs(i).data_in        :=      io.data_in
        keys(i)                :=      regs(i-1).enable_out
        regs(i).enable_in      :=      keys(i)
    }

    io.data_out := reg_wire.reduce(_ + _)

    keys(0) := regs(reg_width-1).enable_out
    regs(0).enable_in := keys(0)
    regs(0).data_in := io.data_in
    
    when(io.ping_key){
        keys(0) := Bool(true)
    }
}

class PixelArrayTest(c: PixelArray, width: Int, pixelReg: Int) extends Tester(c) {
}


