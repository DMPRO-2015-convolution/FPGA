package Core

import Chisel._

// Stole all of this code from some dude on github
import TidbitsOCM._


class ReverseDoubleBuffer(row_length: Int, data_width: Int, kernel_dim: Int) extends Module {

    val total_enq = row_length*7
    val total_deq = row_length*7

    val io = new Bundle {

        val reset = Bool(INPUT)
        val data_in = UInt(INPUT, data_width)

        val slave_enq_input = Bool(INPUT)        // master requests the sdb to write data
        val slave_deq_output = Bool(INPUT)      // master requests to read data from sdb

        val slave_can_enq_input = Bool(OUTPUT)   // slave can be fed data
        val slave_can_deq_output = Bool(OUTPUT) // slave has valid output data

        val data_out = UInt(OUTPUT, data_width)
    }

    val slice1 = Module(new SliceReverseBuffer(row_length, data_width, kernel_dim))
    val slice2 = Module(new SliceReverseBuffer(row_length, data_width, kernel_dim))
    slice1.reset := io.reset
    slice2.reset := io.reset
    slice1.io.data_in := io.data_in
    slice2.io.data_in := io.data_in

    val enq_finished = Reg(init=Bool(false))
    val deqs_finished = Reg(init=Bool(true))

    val init_mode :: normal_mode :: Nil = Enum(UInt(), 2)
    val mode = Reg(init=UInt(init_mode))

    val enq_performed = Reg(init=UInt(0, 32))
    val deq_performed = Reg(init=UInt(0, 32))

    val current = Reg(init=Bool(false))

    when(mode === UInt(init_mode)){
        deqs_finished := Bool(true) 
    }


    // defaults
    slice1.io.enq := Bool(false)
    slice2.io.deq := Bool(false)
    slice1.io.deq := Bool(false)
    slice2.io.enq := Bool(false)
    slice1.io.data_in := io.data_in
    slice2.io.data_in := io.data_in
    io.data_out := UInt(0)
    io.slave_can_enq_input := Bool(false)
    io.slave_can_deq_output := Bool(false)


    // Handle enqueue requests
    when(io.slave_enq_input){
        when(current === Bool(false)){
            slice1.io.enq := Bool(true)
            enq_performed := enq_performed + UInt(1)
        }.otherwise{
            slice2.io.enq := Bool(true)
            enq_performed := enq_performed + UInt(1)
        }
        when( (enq_performed === UInt(total_enq - 1) ) ){
            enq_finished := Bool(true)
        }
    }

    // Handle deq requests
    when(io.slave_deq_output){
        when(current === Bool(true)){
            slice1.io.deq := Bool(true)
            io.data_out := slice1.io.data_out
            deq_performed := deq_performed + UInt(1)
        }.otherwise{
            slice2.io.deq := Bool(true)
            io.data_out := slice2.io.data_out
            deq_performed := deq_performed + UInt(1)
        }
        when( (deq_performed === UInt(total_deq) ) ){
            deqs_finished := Bool(true)
        }
    }

    // When deqs and enqs are done
    when( deqs_finished && enq_finished ){
        when(current === Bool(false)){
            current := Bool(true)
        }.otherwise{
            current := Bool(false)
        }
        // Reset counts
        enq_performed := UInt(0)
        deq_performed := UInt(0)

        enq_finished := Bool(false)
        deqs_finished := Bool(false)

        mode := normal_mode

    }

    // Decide if data should be requested
    when( !enq_finished ){
        io.slave_can_enq_input := Bool(true)
    }
    when( !deqs_finished ){
        io.slave_can_deq_output := Bool(true)
    }

    when(io.reset){
        enq_performed := UInt(0)
        deq_performed := UInt(0)

        enq_finished := Bool(false)
        deqs_finished := Bool(true)
    }

}

class RDBtest(c: ReverseDoubleBuffer) extends Tester(c) {

    poke(c.io.reset, false)
    poke(c.io.slave_enq_input, true)
    poke(c.io.slave_deq_output, false)

    for(i <- 0 until 70){
        poke(c.io.data_in, (i%7) + 1)
        peek(c.current)
        println()
        peek(c.io)
        println()
        peek(c.slice1.io.dbg_enq_row)
        peek(c.slice2.io.dbg_enq_row)
        println()
        peek(c.slice1.io.data_in)
        peek(c.slice2.io.data_in)
        step(1)
    }

    for(i <- 0 until 70){
        poke(c.io.data_in, (i%7) + 1)
        peek(c.current)
        println()
        peek(c.io)
        println()
        peek(c.slice1.io.dbg_enq_row)
        peek(c.slice2.io.dbg_enq_row)
        println()
        peek(c.slice1.io.data_in)
        peek(c.slice2.io.data_in)
        step(1)
    }

    peek(c.io)
    step(1)
    peek(c.io)

    poke(c.io.slave_deq_output, true)
    poke(c.io.slave_enq_input, false)
    step(1)
    peek(c.io.slave_deq_output)
    // poke(c.io.slave_enq_input, false)
    for(i <- 0 until 70){
        poke(c.io.data_in, (i%7) + 1)
        peek(c.current)
        println()
        peek(c.io)
        println()
        peek(c.slice1.io.deq)
        peek(c.slice1.io.dbg_enq_row)
        peek(c.slice1.io.dbg_deq_row)
        peek(c.slice1.io.dbg_row_deq_count)
        println()
        peek(c.slice2.io.deq)
        peek(c.slice2.io.dbg_deq_row)
        peek(c.slice2.io.dbg_enq_row)
        peek(c.slice2.io.dbg_row_deq_count)
        println()
        peek(c.slice1.io.data_in)
        peek(c.slice2.io.data_in)
        println()
        println()
        peek(c.io.data_out)
        peek(c.slice1.io.data_out)
        peek(c.slice2.io.data_out)
        println()
        println()
        step(1)
    }
}
