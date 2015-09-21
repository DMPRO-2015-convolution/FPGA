package Core

import Chisel._

class PixelGrid(data_width: Int, cols: Int, rows: Int) extends Module {
    val io = new Bundle {
        val data_in = UInt(INPUT, data_width)
        val data_out = Vec.fill(rows){ UInt(OUTPUT, data_width) }
        val ping_key = Vec.fill(rows){Bool(INPUT)}
    }

    val pixel_rows = Vec.fill(rows){ Module(new PixelArray(data_width, cols)).io }

    /* 
    *   This describes a grid of pixel arrays, the code is written as explicitly
    *   as possible, but even then some things just arent self documenting...
    */

    // Top row
    for (i <- 0 until cols){
        pixel_rows(0).data_in(i) := io.data_in
    }
    pixel_rows(0).ping_key := io.ping_key(0)

    // Wire cols together
    for(i <- 0 until cols){
        for(j <- 1 until rows){
            pixel_rows(i).data_in := pixel_rows(i-1).data_out
        }
    }

    // Wire key pings
    for(i <- 0 until rows){
        pixel_rows(i).ping_key := io.ping_key(i)
    }

    // Wire data out
    for (i <- 0 until rows){
        pixel_rows(i).data_out := io.data_out(i)
    }
}

