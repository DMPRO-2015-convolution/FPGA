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


    /*
     * All timers are given in modulo 9, which is a constant we must adhere for all designs using 3 by 3 kernels
     * The calculations are done with respect to the leftmost element in the systolic pixel grid, and since 
     * all signals travel at the same rate this is sufficient. An important point to notice is that these 
     * timestamps describe how much later, mod 9, a signal is dispatched, not the time it is dispatched!!!
     *
     * although modularity is desired this signal box only works for 3 by 3 kernels
     */

    
    val T = 9

    // How long after being input to one row should a register be input to the next row?
    val row_time = 6

    // When pinging it takes one cycle for state to update. The same applies to any register to register transfer
    val ping_delay = 1
    val reg_delay = 1
    val mux_delay = 1
    val ALU_delay = reg_delay

    // How long after input until mux opens (5)
    val mux_wait = row_time - mux_delay

    // data enters input tree. Changing this value should propagate throughout the entire system
    val data_in = 1

    val row1_r = data_in + reg_delay
    val row1_m = data_in + mux_wait

    val row1_out = data_in + row_time


    val row2_r = row1_out
    val row2_m = row1_out + mux_wait

    val row2_out = row1_out + row_time


    val row3_r = row2_out
    val row3_m = row2_out + mux_wait

    val row3_out = row2_out + row_time

    
    // shift muxes operate a little different. Regardless, this val is the timestep which it chooses input 0
    val shift_mux = row3_out

    val grid_out = row3_out + reg_delay

    val ALU_sel = grid_out

    val ALU_rdy = grid_out + reg_delay

    val Acc_flush = ALU_rdy + ALU_delay
    


    // Having gathered the data we calculate when to ping
    var read1_p = row1_r - ping_delay
    var mux1_p = row1_m - ping_delay

    var read2_p = row2_r - ping_delay
    var mux2_p = row2_m - ping_delay

    var read3_p = row3_r - ping_delay
    var mux3_p = row3_m - ping_delay

    var shift_mux_p = shift_mux - ping_delay
    var ALU_sel_p = ALU_sel - ping_delay 

    var flush_p = Acc_flush - ping_delay


    // We now normalize the values
    read1_p = read1_p % T
    mux1_p = mux1_p % T

    read2_p = read2_p % T
    mux2_p = mux2_p % T

    read3_p = read3_p % T
    mux3_p = mux3_p % T

    var shift_mux_p1 = shift_mux_p % T
    var shift_mux_p2 = (shift_mux_p + 3) % T
    var shift_mux_p3 = (shift_mux_p + 6) % T

    var ALU_sel_p1 = ALU_sel_p % T
    var ALU_sel_p2 = (ALU_sel_p + 3) % T
    var ALU_sel_p3 = (ALU_sel_p + 6) % T

    flush_p = flush_p % T



    val s0 :: s1 :: s2 :: s3 :: s4 :: s5 :: s6 :: s7 :: s8 :: Nil = Enum(UInt(), 9)
    val state = Reg(init=UInt(0))


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
