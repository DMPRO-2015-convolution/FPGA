package Core

import Chisel._

import scala.collection.mutable.ListBuffer

class Orchestrator(cols: Int, rows: Int)  extends Module {

    val io = new Bundle {
        val reset = Bool(INPUT)
        val active = Bool(INPUT)

        val read_row  = Vec.fill(rows){ Bool(OUTPUT) }
        val mux_row = Vec.fill(rows){ Bool(OUTPUT) }
        val shift_mux = Bool(OUTPUT)

        val accumulator_flush = Bool(OUTPUT)
        val ALU_shift = Bool(OUTPUT)

        val dbg_enable = UInt(OUTPUT)
    }

    val period = rows

    // How long after being input to one row should a register be input to the next row?
    val row_time = (cols - rows)

    // We need to know how long a pixel is ready befor it is actually read
    val read_delay = cols - rows

    // We need to know how many cycles a full calculation takes
    val cycle_time = cols
    val time_to_fill = (rows - 1)*row_time

    val ALU_delay = 1

    // We now have everything we need to calculate the time it takes for the first valid output
    // with data in input tree as T0
    val data_in_tree = 1
    val conveyor_rdy = data_in_tree + time_to_fill
    val conveyor_start = conveyor_rdy + read_delay
    val conveyor_done = conveyor_start + cycle_time
    val data_out = conveyor_done + ALU_delay

    println("Time of first data in tree:\t\t\t\t%d".format(data_in_tree))
    println("Time of conveyor being ready:\t\t\t\t%d".format(conveyor_rdy))
    println("Time of first read cycle start:\t\t\t\t%d".format(conveyor_start))
    println("Time of last read of first read cycle finished:\t\t%d".format(conveyor_done))
    println("Time of first valid data output:\t\t\t%d".format(data_out))
    println()

    // Use the timings of the major events to decide when and where to issue reads and writes
    var rowreads = new ListBuffer[Int]()
    var rowmuxes = new ListBuffer[Int]()

    for(i <- 0 until rows){
        rowreads += data_in_tree + i*row_time    
        rowmuxes += data_in_tree + (i + 1)*row_time - 1    
    }

    for(i <- 0 until rows){
        println("Time of read for row\t %d:\t\t\t\t%d".format(i, rowreads(i)))
        println("Time of mux for row\t %d:\t\t\t\t%d".format(i, rowmuxes(i)))
        println()
    }


    // In order to hit the timings we need to take into account the fact that
    // all elements perform their action when they have the key, not when
    // they are offered the key. If we want an element to do an action at T we must offer
    // the key at T-1
    val ping_delay = 1

    // We have no more use for fixed timings, so we collect the timings congruent to the periond
    rowreads.map(_ - ping_delay).map(_%period)
    rowmuxes.map(_ - ping_delay).map(_%period)


    // We now have everything we need to create the grid control state machine
    val time = Reg(init=(UInt(0)))

    when(io.active){
        when(time === UInt(period - 1)){
            time := UInt(0)
        }.otherwise{
            time := time + UInt(1)
        }
    }
    
    for(i <- 0 until rows){
        when(time === UInt(rowreads(i))){
            io.read_row(i) := Bool(true)
        }.otherwise{
            io.read_row(i) := Bool(false)
        }

        when(time === UInt(rowmuxes(i))){
            io.mux_row(i) := Bool(true)
        }.otherwise{
            io.mux_row(i) := Bool(false)
        }
    }
}




