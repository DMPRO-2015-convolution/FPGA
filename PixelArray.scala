package Core

import Chisel._

// TODO figure out how to assert cols div 3
class PixelArray(data_width: Int, cols: Int) extends Module {
    val io = new Bundle {
        val data_in = Vec.fill(cols/3){UInt(INPUT, data_width)}
        val ping_read = Bool(INPUT)
        val ping_mux = Bool(INPUT)

        val data_out = Vec.fill(cols/3){UInt(OUTPUT, data_width)}


        val dbg_mux_enable = Vec.fill(3){ Bool(OUTPUT) }
        val dbg_m_data_out = UInt(OUTPUT)
        val dbg_m_data_in = Vec.fill(3){ UInt(OUTPUT) }
        val dbg_data_out = Vec.fill(3){ UInt(OUTPUT) }
        val dbg_data_in = Vec.fill(3){ UInt(OUTPUT) }
        val dbg_ping_read = Bool(OUTPUT)
        val dbg_ping_mux = Bool(OUTPUT)
    }

    val pixels = Vec.fill(cols){ Module(new PixelReg(data_width)).io } 
    val primary_muxes = Vec.fill(cols/3) { Module(new Mux3(data_width, cols/3)).io }
    

    // Wire control chains
    // 

    // wire primary mux enablers
    primary_muxes(0).enable_in := io.ping_mux
    primary_muxes(1).enable_in := primary_muxes(0).enable_out
    primary_muxes(2).enable_in := primary_muxes(1).enable_out
    

    // wire pixel read ping chain
    pixels(0).enable_in := io.ping_read
    for(i <- 1 until cols){
        pixels(i).enable_in := pixels(i-1).enable_out
    }


    // Wire inputs and outputs
    //

    // wire input tree to pixels
    for (i <- 0 until cols){
       pixels(i).data_in := io.data_in(i%3)
    }


    // Wire pixels to primary muxes
    for (i <- 0 until cols){
        primary_muxes(i/3).data_in(i%3) := pixels(i).data_out
    }
    for (i <- 0 until cols/3){
        io.data_out(i) := primary_muxes(i).data_out
    }


    // DBG WIRING

    for(i <- 0 until 3){
        io.dbg_mux_enable(i) := primary_muxes(0).dbg_enable(i)
        io.dbg_m_data_in(i) := primary_muxes(0).dbg_data_in(i)
        io.dbg_data_out(i) := primary_muxes(i).data_out
        io.dbg_data_in(i) := io.data_in(i)
    }
    io.dbg_m_data_out := primary_muxes(0).dbg_data_out
    io.dbg_ping_read := io.ping_read
    io.dbg_ping_mux := io.ping_mux
}

class PixelArrayTest(c: PixelArray, data_width: Int, cols: Int) extends Tester(c) {
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
        poke(c.io.data_in(0), i)
        poke(c.io.data_in(1), i)
        poke(c.io.data_in(2), i)
        peek(c.io.data_out(0))
        peek(c.io.data_out(1))
        peek(c.io.data_out(2))
        //println("DBG\n\n")
        //peek(c.io.dbg_mux_enable)
        //peek(c.io.dbg_m_data_in)
        //peek(c.io.dbg_m_data_out)
        //println("\n\n")
        step(1)
    }
}


