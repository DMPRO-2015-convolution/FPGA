package Core

import Chisel._


// TODO move ALU out of PG
class PixelGrid(data_width: Int, cols: Int, rows: Int) extends Module {
    val io = new Bundle {
        val data_in = UInt(INPUT, data_width)

        val data_out = Vec.fill(cols/3){ UInt(OUTPUT, data_width) }
    }

    val pixel_rows = Vec.fill(rows){ Module(new PixelArray(data_width, cols)).io }
    val secondary_muxes = for(i <- 0 until 3) yield Module(new ShiftMux3(data_width, 3, ((i) % 3) )).io
    val pinger = Module(new Orchestrator(cols, rows)).io


    //////////////////////////////////////
    ///////////   GRID
    ///////////
    ///////////
   
    
    // wire input into first row input tree
    for(i <- 0 until cols){
        pixel_rows(0).data_in(i%3) := io.data_in
    }


    // Wire io between rows
    for(i <- 1 until cols/3){
        for(j <- 0 until rows){
            pixel_rows(i).data_in(j) := pixel_rows(i-1).data_out(j)
        }
    }


    // wire primary mux enablers
    pixel_rows(0).ping_read := pinger.pings(1)
    pixel_rows(0).ping_mux := pinger.pings(2)
    pixel_rows(1).ping_read := pinger.pings(3)
    pixel_rows(1).ping_mux := pinger.pings(4)
    pixel_rows(2).ping_read := pinger.pings(5)
    pixel_rows(2).ping_mux := pinger.pings(6)
    

    // Wire shift signals to secondary muxes
    for(i <- 0 until 3){
        secondary_muxes(i).shift := pinger.pings(0)
    }


    // Wire data from primary muxes to secondary muxes
    for(i <- 0 until 3){
        for(j <- 0 until 3){
            secondary_muxes(i).data_in(j) := pixel_rows(i).data_out(j)
        }
    }
    

    // Wire grid data out from secondary muxes
    for(i <- 0 until cols/3){
        io.data_out(i) := secondary_muxes(i).data_out
    }


    //////////////////////////////////////
    ///////////   ALUs
    ///////////
    ///////////
    val ALUs = Module(new ALUrow(data_width, cols)).io


    // Wire memory outputs to ALUs
    for(i <- 0 until cols-2){
    
    }

}

class PixelGridTest(c: PixelGrid, data_width: Int, cols: Int, rows: Int) extends Tester(c) {
    println("PixelGridTest")
    for(i <- 0 to 60){
        poke(c.io.data_in, ((i-1)%9)+1)
        peek(c.io.data_out)
        step(1)
        println("\n\n\n")
    }
}
