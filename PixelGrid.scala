package Core

import Chisel._

class PixelGrid(data_width: Int, cols: Int, rows: Int) extends Module {
    val io = new Bundle {
        val data_in = UInt(INPUT, data_width)

        val data_out = Vec.fill(cols/3){ UInt(OUTPUT, data_width) }
    }

    val pixel_rows = Vec.fill(rows){ Module(new PixelArray(data_width, cols)).io }
    val secondary_muxes = Vec.fill(rows){ Module(new Mux3(data_width, cols/3)).io }
    val pinger = Module(new Orchestrator(cols, rows)).io


    // wire input into first row input tree
    for(i <- 0 until cols){
        pixel_rows(0).data_in(i%3) := io.data_in
    }


    // wire secondary mux enablers
    secondary_muxes(0).enable_in := pinger.pings(0)
    secondary_muxes(1).enable_in := secondary_muxes(0).enable_out
    secondary_muxes(2).enable_in := secondary_muxes(1).enable_out


    // wire primary mux enablers
    pixel_rows(0).ping_read := pinger.pings(1)
    pixel_rows(0).ping_mux := pinger.pings(2)
    pixel_rows(1).ping_read := pinger.pings(3)
    pixel_rows(1).ping_mux := pinger.pings(4)
    pixel_rows(2).ping_read := pinger.pings(5)
    pixel_rows(2).ping_mux := pinger.pings(6)
    

    // Do uhhh
    for(i <- 1 until cols/3){
        for(j <- 0 until rows){
            pixel_rows(i).data_in(j) := pixel_rows(i-1).data_out(j)
        }
    }


    // Wire grid data out from secondary muxes
    for(i <- 0 until cols/3){
        io.data_out(i) := secondary_muxes(i).data_out
    }
}

class PixelGridTest(c: PixelGrid, data_width: Int, cols: Int, rows: Int) extends Tester(c) {
    for(i <- 0 to 27){
    }
    println("PixelGridTest")
}
