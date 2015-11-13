package Core

import Chisel._

// Stole all of this code from some dude on github 
import TidbitsOCM._


class SliceDoubleBuffer(val row_length: Int, data_width: Int, kernel_dim: Int) extends Module {

    val cols = kernel_dim*kernel_dim
    val total_reads = row_length*cols
    val total_writes = row_length*cols

    val io = new Bundle {
        val data_in = UInt(INPUT, data_width)

        val request_write = Bool(INPUT)   // parent requests the sdb to write data
        val request_read = Bool(INPUT)    // parent requests to read data from sdb

        val request_input = Bool(OUTPUT)  // input can be fed to sdb via request read
        val request_output = Bool(OUTPUT) // output can be extracted from sbd with request write

        val data_out = UInt(OUTPUT, data_width)
        val error = Bool(OUTPUT)
    }

    val slice1 = Module(new SliceBuffer(row_length, data_width, kernel_dim))
    val slice2 = Module(new SliceBuffer(row_length, data_width, kernel_dim))

    val reads_finished = Reg(init=Bool(false))
    val writes_finished = Reg(init=Bool(true))

    val reads_performed = Reg(init=UInt(0, 32))
    val writes_performed = Reg(init=UInt(0, 32))

    val current = Reg(init=Bool(false))

    // defaults
    slice1.io.pop := Bool(false)
    slice2.io.pop := Bool(false)
    slice1.io.push := Bool(false)
    slice2.io.push := Bool(false)
    slice1.io.data_in := UInt(0)
    slice2.io.data_in := UInt(0)
    io.data_out := UInt(0)
    io.error := Bool(false)
    io.request_input := Bool(false)
    io.request_output := Bool(false)

    // Handle read requests
    // This means we want to read some data from, which means we want the buffer to write data out
    when(io.request_read){
        when(current === Bool(false)){
            slice1.io.push := Bool(true)
            slice1.io.data_in := io.data_in
            reads_performed := reads_performed + UInt(1)
        }.otherwise{
            slice2.io.push := Bool(true)
            slice2.io.data_in := io.data_in
            reads_performed := reads_performed + UInt(1)
        }
        when( (reads_performed === UInt(total_reads - 1) ) ){
            reads_finished := Bool(true)
        }
    }

    // Handle write requests
    // This means we want to write some input into buffer
    when(io.request_write){
        when(current === Bool(true)){
            slice1.io.pop := Bool(true)
            io.data_out := slice1.io.data_out
            writes_performed := writes_performed + UInt(1)
        }.otherwise{
            slice2.io.pop := Bool(false)
            io.data_out := slice2.io.data_out
            writes_performed := writes_performed + UInt(1)
        }
    }

    // Check if reads/writes are finished

    when( (writes_performed === UInt(total_writes) ) ){
        writes_finished := Bool(true)
    }

    // Do the switcheroo
    when( reads_finished && writes_finished ){
        when(current === Bool(false)){
            current := Bool(true)
        }.otherwise{
            current := Bool(false)
        }
        // Reset counts
        reads_performed := UInt(0)
        writes_performed := UInt(0)

        reads_finished := Bool(false)
        writes_finished := Bool(false)
    }

    // Decide if data should be requested
    when( !reads_finished ){
        io.request_input := Bool(true)
    }
    when( !writes_finished ){
        io.request_output := Bool(true)
    }

    // Should never happen, but who am I kidding?
    when( (reads_performed > UInt(total_reads)) || (writes_performed > UInt(total_writes)) ){
        io.error := Bool(true)
    }
}


class DoubleBufferTest(c: SliceDoubleBuffer) extends Tester(c) {
    
    // Inspect initial state
    poke(c.io.data_in, 0)
    poke(c.io.request_write, 0)
    poke(c.io.request_read, 0)
    peek(c.reads_finished)
    peek(c.writes_finished)
    peek(c.reads_performed)
    peek(c.writes_performed)
    peek(c.io)

    // Fill first buffer
    println("Filling slice 1")
    for(i <- 0 until c.cols*c.row_length){
        poke(c.io.request_read, true)
        println()
        poke(c.io.data_in, (i+1))
        peek(c.current)
        peek(c.reads_finished)
        peek(c.writes_finished)
        peek(c.reads_performed)
        peek(c.writes_performed)
        peek(c.io.request_write)
        peek(c.io.request_read)
        peek(c.io.request_input)
        peek(c.io.request_output)
        println("Slice 1")
        peek(c.slice1.io)
        println("Slice 2")
        peek(c.slice2.io)
        println()
        peek(c.io.data_out)
        println()
        step(1)
    }
    println("\nSlice 1 filled!\n")
    poke(c.io.request_read, false)
    peek(c.current)
    peek(c.reads_finished)
    peek(c.writes_finished)
    peek(c.reads_performed)
    peek(c.writes_performed)
    println()
    peek(c.io.data_out)
    println()

    step(1)
    // Check state
    println("\nChecking state after filling slice 1\n")
    peek(c.io)
    peek(c.reads_finished)
    peek(c.writes_finished)
    peek(c.reads_performed)
    peek(c.writes_performed)
    peek(c.slice1.io)
    peek(c.slice2.io)
    peek(c.current)
    println()
    peek(c.io.data_out)
    println()

    // Extract one buffer, fill the other
    println("\nExtracting written data from buffer 1, filling buffer 2 \n")
    for(i <- 0 until c.cols*c.row_length + 1){
        poke(c.io.request_write, true)
        poke(c.io.request_read, true)
        poke(c.io.data_in, (i+1) + 256)
        println()
        peek(c.current)
        peek(c.reads_finished)
        peek(c.writes_finished)
        peek(c.reads_performed)
        peek(c.writes_performed)
        peek(c.io.request_write)
        peek(c.io.request_read)
        peek(c.io.request_input)
        peek(c.io.request_output)
        println("Slice 1")
        peek(c.slice1.io)
        println("Slice 2")
        peek(c.slice2.io)
        println()
        peek(c.io.data_out)
        println()
        step(1)
    }
    poke(c.io.request_read, false)
}
