package Core

import Chisel._
import java.io._
import scala.io.Source


class PixelGrid(data_width: Int, cols: Int, rows: Int) extends Module {
    val io = new Bundle {
        val pixel_in = UInt(INPUT, data_width)
        val read_row = Vec.fill(rows){ Bool(INPUT) }
        val mux_row = Vec.fill(rows){ Bool(INPUT) }
        val shift_mux = Bool(INPUT)
        val reset = Bool(INPUT)

        val stall = Bool(INPUT)

        val data_out = Vec.fill(3){ UInt(OUTPUT, data_width) }
    }

    val pixel_rows = Vec.fill(rows){ Module(new PixelArray(data_width, cols)).io }
    val input_tree = Vec.fill(3){ Reg(init=UInt(0, width = data_width)) }
    val shift_muxes = for(i <- 0 until 3) yield Module(new ShiftMux(data_width, 3, default=(i%3)))

    // Wire input into input tree
    // wire input into first row input tree
    for(i <- 0 until 3){ input_tree(i) := io.pixel_in  }
    for(i <- 0 until 3){ 
        pixel_rows(0).pixel_in(i) := input_tree(i)
        pixel_rows(i).stall := io.stall
    }

    // Wire io between rows
    for(i <- 1 until cols/3){
        for(j <- 0 until rows){
            pixel_rows(i).pixel_in(j) := pixel_rows(i-1).data_out(j)
        }
    }

    // wire primary mux enablers
    
    for(i <- 0 until rows){
        pixel_rows(i).ping_read := io.read_row(i)
        pixel_rows(i).ping_mux := io.mux_row(i)
    }
    
    // Wire shift signals to secondary muxes
    for(i <- 0 until 3){
        shift_muxes(i).io.shift := io.shift_mux
        shift_muxes(i).io.stall := io.stall
        shift_muxes(i).io.reset := io.reset
    }

    // Wire data from primary muxes to secondary muxes
    for(i <- 0 until 3){
        for(j <- 0 until 3){
            shift_muxes(i).io.pixel_in(j) := pixel_rows(i).data_out(j)
            io.data_out(i) := shift_muxes(i).io.data_out 
        }
    }
}
