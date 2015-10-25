package Core

import Chisel._
import java.io._
import scala.io.Source


class PixelGrid(data_width: Int, cols: Int, rows: Int) extends Module {
    val io = new Bundle {
        val data_in = UInt(INPUT, data_width)
        val control_in = Vec.fill(7){ Bool(INPUT) }

        val data_out = Vec.fill(3){ UInt(OUTPUT, data_width) }
    }

    val pixel_rows = Vec.fill(rows){ Module(new PixelArray(data_width, cols)).io }
    val input_tree = Vec.fill(3){ Reg(init=UInt(0, width = data_width)) }
    val shift_muxes = for(i <- 0 until 3) yield Module(new ShiftMux3(data_width, 3, default=((i + 1) % 3))).io

    // Wire input into input tree
    // wire input into first row input tree
    for(i <- 0 until 3){ input_tree(i) := io.data_in  }
    for(i <- 0 until 3){ pixel_rows(0).data_in(i) := input_tree(i) }

    // Wire io between rows
    for(i <- 1 until cols/3){
        for(j <- 0 until rows){
            pixel_rows(i).data_in(j) := pixel_rows(i-1).data_out(j)
        }
    }

    // wire primary mux enablers
    pixel_rows(0).ping_read  :=  io.control_in(1)
    pixel_rows(0).ping_mux   :=  io.control_in(2)
    pixel_rows(1).ping_read  :=  io.control_in(3)
    pixel_rows(1).ping_mux   :=  io.control_in(4)
    pixel_rows(2).ping_read  :=  io.control_in(5)
    pixel_rows(2).ping_mux   :=  io.control_in(6)
    
    // Wire shift signals to secondary muxes
    for(i <- 0 until 3){
        shift_muxes(i).shift := io.control_in(0)
    }

    // Wire data from primary muxes to secondary muxes
    for(i <- 0 until 3){
        for(j <- 0 until 3){
            shift_muxes(i).data_in(j) := pixel_rows(i).data_out(j)
            io.data_out(i) := shift_muxes(i).data_out 
        }
    }

}
