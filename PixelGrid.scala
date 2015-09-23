package Core

import Chisel._

class PixelGrid(data_width: Int, cols: Int, rows: Int) extends Module {
    val io = new Bundle {
        val data_in = UInt(INPUT, data_width)
        val pings = Vec.fill(rows + cols/3 + 1){ Bool(INPUT) }

        val data_out = Vec.fill(cols/3){ UInt(OUTPUT, data_width) }
    }

    val pixel_rows = Vec.fill(rows){ Module(new PixelArray(data_width, cols)).io }
    val secondary_muxes = Vec.fill(rows){ Module(new Mux3(data_width, cols/3)).io }
    val queue_splitter = Vec.fill(cols/3){ Reg(init=UInt(data_width)) }


    // manually wire secondary mux enablers
    secondary_muxes(0).enable_in := io.pings(0)
    secondary_muxes(1).enable_in := secondary_muxes(0).enable_out
    secondary_muxes(2).enable_in := secondary_muxes(1).enable_out


    // wire primary mux enablers
    for(i <- 0 until 3){
        pixel_rows(i).ping_read := io.pings(2*i + 1)
        pixel_rows(i).ping_mux := io.pings(2*i + 2)
    }
    

    // Wire queue data splitter
    for(i <- 0 until cols/3){
        queue_splitter(i) := io.data_in
    }


    // Wire pixel array io
    for(i <- 0 until cols/3){
        pixel_rows(0).data_in(i) := queue_splitter(i) 
    }
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
    println("PixelGridTest")
}
