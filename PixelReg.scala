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

    io.enable_out := enable
    enable := io.enable_in

    when (io.enable_in){
        data := io.data_in
        io.data_out := data
        data := io.data_in
    }.otherwise{
        io.data_out := UInt(0)
    }
    

} 
