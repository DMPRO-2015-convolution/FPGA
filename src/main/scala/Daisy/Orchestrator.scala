package Core

import Chisel._



/*
 *   GRID
 *
 *   0 - Secondary mux
 *   1 - READ 0
 *   2 - PRIMARY MUX 0
 *   3 - READ 1
 *   4 - PRIMARY MUX 1
 *   5 - READ 2
 *   6 - PRIMARY MUX 2
 *
 *   No amounts of commentig is going to make this clear.
 *   View the documentation in order to understand the timing
 *
 *
 *   ALUS
 *
 *   7 - ALU mux shift
 *   8 - Accumulator flush signal
 *
 */

class Orchestrator(cols: Int, rows: Int)  extends Module {

    var n_pings =
        3 +         // 3 read enables
        3 +         // 3 mux enables
        1 +         // 1 secondry mux enable
        1 +         // 1 ALU mux shift enable
        1           // 1 Accumulator flush signal

    val io = new Bundle {
        val pings = Vec.fill(n_pings){ Bool(OUTPUT) }
        val dbg_enable = UInt(OUTPUT)
    }

    val T = rows

    /*
     * All timers are given in modulo T, given by elements per row
     * The calculations are done with respect to the leftmost element in the systolic pixel grid.
     * Each value is calculated relatively to the first valid output (thus they are all negative)
     * After calculating the timestep for each value we normalize relative to the lowest value,
     * then calculate the congruence mod T for each value
     */

    // How long after being input to one row should a register be input to the next row?
    val row_time = (cols - rows)

    // When pinging it takes one cycle for state to update. The same applies to any register to register transfer
    // A component will activate once it has a ping in its register, not as soon as it is offered
    val ping_delay = 1

    // The cycle it takes for a value to travel between registers
    val reg_delay = 1

    // The cycle, as a result of reg delays a value has to spend in the multiplexers balance register
    val mux_delay = 1

    val ALU_delay = reg_delay

    // In order to open the muxes in time for hitting the row time timing we account for the mux delay
    val mux_wait = row_time - mux_delay

    // We need to know how many cycles a full calculation takes
    val cycle_time = cols*rows


    // To make the calculations simpler we let the first valid output from the system to be time 0
    val first_data_out = 0

    // TODO implement
    val calculated_ALU_delay = 0

    // Having calculated the time it took from last read until outputting our first valid data we can calculate row read and write timings. 
    // We will use the first conveyor read (which in this case is the bottom left pixel) as measuring point to time control signals
    val last_conveyor_read = calculated_ALU_delay
    val first_valid_conveyor_read = last_conveyor_read - (cycle_time - 1)

    var row_read_start = ArrayBuffer[Int]()
    var row_mux_start = ArrayBuffer[Int]()
    var row_out_start = ArrayBuffer[Int]()
    for(i <- 0 until rows){
        row_read_start += first_valid_conveyor_read + (i*(cols))
        row_mux_start  += first_valid_conveyor_read + (i*(cols) - mux_delay)
        row_out_start  += first_valid_conveyor_read + (i*(cols) - (mux_delay + reg_delay))
    }
    
    // We use the timing of one of the first read to calculate secondary mux shift timing 
    // The shift mux will be pinged at a much faster interval, so we dont care which row we use 
    // to calculate the shift timing.
    val shift_mux = row_out_start(0)


    // State transitions
    when(state === s8){
        state := s0
    }.otherwise{
        state := state + UInt(1)
    }

    io.dbg_enable := state


    // Default pings
    for(i <- 0 until io.pings.size){
        io.pings(i) := Bool(false) 
    }


    // See comments for descriptions. In scala all matching code will be run
    switch (state) {
        is( UInt(read1_p)                    ){ io.pings(1) := Bool(true) }
        is( UInt(read2_p)                    ){ io.pings(3) := Bool(true) }
        is( UInt(read3_p)                    ){ io.pings(5) := Bool(true) }
        is( UInt(mux1_p)                     ){ io.pings(2) := Bool(true) }
        is( UInt(mux2_p)                     ){ io.pings(4) := Bool(true) }
        is( UInt(mux3_p)                     ){ io.pings(6) := Bool(true) }
        is( UInt(shift_mux_p1)               ){ io.pings(0) := Bool(true) }
        is( UInt(shift_mux_p2)               ){ io.pings(0) := Bool(true) }
        is( UInt(shift_mux_p3)               ){ io.pings(0) := Bool(true) }
        is( UInt(ALU_sel_p1)                 ){ io.pings(7) := Bool(true) }
        is( UInt(ALU_sel_p2)                 ){ io.pings(7) := Bool(true) }
        is( UInt(ALU_sel_p3)                 ){ io.pings(7) := Bool(true) }
        is( UInt(flush_p)                    ){ io.pings(8) := Bool(true) }
    }

    val print_times = true
    if(print_times){
        print("READ 1: %d\n".format(read1_p))
        print("MUX 1: %d\n".format(mux1_p))
        println()

        print("READ 2: %d\n".format(read2_p))
        print("MUX 2: %d\n".format(mux2_p))
        println()
        
        print("READ 3: %d\n".format(read3_p))
        print("MUX 3: %d\n".format(mux3_p))
        println()

        print("SECONDARY MUX 1 (shiftmux): %d\n".format(shift_mux_p1))
        print("SECONDARY MUX 2 (shiftmux): %d\n".format(shift_mux_p2))
        print("SECONDARY MUX 3 (shiftmux): %d\n".format(shift_mux_p3))

        print("ALU MUX SHIFT 1: %d\n".format(ALU_sel_p1))
        print("ALU MUX SHIFT 2: %d\n".format(ALU_sel_p2))
        print("ALU MUX SHIFT 3: %d\n".format(ALU_sel_p3))
        println()

        print("ACCUMULATOR FLUSH: %d\n".format(flush_p))
        println()

        val first_valid_output = 
            1            +        // wait for input to be available to first row
            3*row_time   +        // cross three rows
            1            +        // secondary mux
            1            +        // ALU mux
            1*ALU_delay  +        // wait for ALU ops
            1                     // wait for accumulator to update

        print("FIRST VALID ACCUMULATOR INPUT: %d\n".format(first_valid_output))
        print("FIRST VALID ACCUMULATOR OUTPUT: %d\n".format(first_valid_output + T))

    }

}

class OrchestratorTest(c: Orchestrator, cols: Int, rows: Int) extends Tester(c) {
    println("OrchestratorTest")
    step(5)
    peek(c.io)
    step(2)
    peek(c.io)
    for(i <- 0 until 8){
        step(1)
        peek(c.io)
    }
}
