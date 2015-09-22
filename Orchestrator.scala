package Core

import Chisel._

class Orchestrator(cols: Int, rows: Int)  extends Module {
    val io = new Bundle {
        val pings = Vec.fill(cols/3 + rows + 1){ Bool(INPUT) }
    }


    /*
    *   0 - Secondary mux
    *   1 - READ 0
    *   2 - PRIMARY MUX 0
    *   3 - READ 1
    *   4 - PRIMARY MUX 1
    *   5 - READ 2
    *   6 - PRIMARY MUX 2
    */


    val counter = Reg(init=UInt(0, width=4))

    for(i <- 0 until io.pings.size){
        io.pings(i) := Bool(false) 
    }
    counter := counter + UInt(1)

    when(counter === UInt(0)){
        io.pings(6) := Bool(true)
    }.elsewhen(counter === UInt(1)){
        io.pings(0) := Bool(true)
        io.pings(1) := Bool(true)
    }.elsewhen(counter === UInt(3)){
        io.pings(4) := Bool(true)
    }.elsewhen(counter === UInt(4)){
        io.pings(5) := Bool(true)
    }.elsewhen(counter === UInt(6)){
        io.pings(2) := Bool(true)
    }.elsewhen(counter === UInt(7)){
        io.pings(3) := Bool(true)
    }.elsewhen(counter === UInt(8)){
        counter := UInt(0)
    }
}
