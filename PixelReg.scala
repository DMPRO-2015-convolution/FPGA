package Core
 
import Chisel._ 
 
class PixelReg(data_width: Int)  extends Module { 
    val io = new Bundle { 
        val data_in = UInt(INPUT, data_width) 
        val enable_in = Bool(INPUT)

        val data_out = UInt(OUTPUT, data_width) 
        val enable_out = Bool(OUTPUT)
    } 

    val data = Reg(init=UInt(0, width = data_width))
    val enable = Reg(init=Bool(false))
    val read_switch = Reg(init=Bool(false))

    // Wire enable
    io.enable_out := enable
    enable := io.enable_in
    io.data_out := data

    when (io.enable_in){
        data := io.data_in
    }
} 

class PixelRegTest(c: PixelReg, data_width: Int) extends Tester(c) {
    println("PixelRegTest")
    for(i <- 0 until 20){
        step(1)
        poke(c.io.data_in, i)
        peek(c.io.data_out)
        peek(c.io.enable_out)
        if(i%9 == 0){
            poke(c.io.enable_in, true)
        }else{poke(c.io.enable_in,false)}
    }
}
