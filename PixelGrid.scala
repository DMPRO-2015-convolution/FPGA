package Core

import Chisel._

class PixelGrid(data_width: Int, cols: Int, rows: Int) extends Module {
    val io = new Bundle {
        val data_in = UInt(INPUT, data_width)

        val data_out = Vec.fill(cols/3){ UInt(OUTPUT, data_width) }


        // Debug wiring
        val dbg_row_0_in = Vec.fill(3){ UInt(OUTPUT) }
        val dbg_row_0_out = Vec.fill(3){ UInt(OUTPUT) }
        val dbg_row_0_read = Bool(OUTPUT)
        val dbg_row_0_mux = Bool(OUTPUT)

        val dbg_row_1_in = Vec.fill(3){ UInt(OUTPUT) }
        val dbg_row_1_out = Vec.fill(3){ UInt(OUTPUT) }
        val dbg_row_1_read = Bool(OUTPUT)
        val dbg_row_1_mux = Bool(OUTPUT)

        val dbg_row_2_in = Vec.fill(3){ UInt(OUTPUT) }
        val dbg_row_2_out = Vec.fill(3){ UInt(OUTPUT) }
        val dbg_row_2_read = Bool(OUTPUT)
        val dbg_row_2_mux = Bool(OUTPUT)

        val dbg_reg_vals = Vec.fill(3){Vec.fill(9){UInt(OUTPUT)}}
        val dbg_reg_enables = Vec.fill(3){Vec.fill(9){Bool(OUTPUT)}}
    }

    val pixel_rows = Vec.fill(rows){ Module(new PixelArray(data_width, cols)).io }
    val secondary_muxes = Vec.fill(rows){ Module(new Mux3(data_width, cols/3)).io }
    val pinger = Module(new Orchestrator(cols, rows)).io


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

    
    // DBG WIRING

    for(i <- 0 until 3){
        io.dbg_row_0_in(i) := pixel_rows(0).dbg_data_in(i)
        io.dbg_row_1_in(i) := pixel_rows(1).dbg_data_in(i)
        io.dbg_row_2_in(i) := pixel_rows(2).dbg_data_in(i)
    }

    for(i <- 0 until 3){
        io.dbg_row_0_out(i) := pixel_rows(0).dbg_data_out(i)
        io.dbg_row_1_out(i) := pixel_rows(1).dbg_data_out(i)
        io.dbg_row_2_out(i) := pixel_rows(2).dbg_data_out(i)
    }
    io.dbg_row_0_read := pixel_rows(0).dbg_ping_read
    io.dbg_row_1_read := pixel_rows(1).dbg_ping_read
    io.dbg_row_2_read := pixel_rows(2).dbg_ping_read

    io.dbg_row_0_mux := pixel_rows(0).dbg_ping_mux
    io.dbg_row_1_mux := pixel_rows(1).dbg_ping_mux
    io.dbg_row_2_mux := pixel_rows(2).dbg_ping_mux

    for(i <- 0 until 3){
        for(j <- 0 until 9){
            io.dbg_reg_vals(i)(j) := pixel_rows(i).dbg_reg_vals(j)
            io.dbg_reg_enables(i)(j) := pixel_rows(i).dbg_reg_enables(j)
        }
    }
}

class PixelGridTest(c: PixelGrid, data_width: Int, cols: Int, rows: Int) extends Tester(c) {
    println("PixelGridTest")

    var grid = Array.ofDim[Array[Array[BigInt]]](61)
    var enable = Array.ofDim[Array[Array[BigInt]]](61)

    for(i <- 0 to 60){

        var gridslice = Array.ofDim[BigInt](3, 9)
        for(j <- 0 until 3){
            gridslice(j) = peek(c.io.dbg_reg_vals(j))
        }
        grid(i) = gridslice

        var enableslice = Array.ofDim[BigInt](3, 9)
        for(j <- 0 until 3){
            enableslice(j) = peek(c.io.dbg_reg_enables(j))
        }
        enable(i) = enableslice

        poke(c.io.data_in, (i)%9 + 1)

        step(1)
    }
    for(i <- 0 to 60){
        println("### enable ###")
        enable(i) foreach { row => row foreach print; println }
        println("### value ###")
        grid(i) foreach { row => row foreach print; println }
        println("###")
        println()
    }
}
