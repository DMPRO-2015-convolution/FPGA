package Core

import Chisel._

// Stole all of this code from some dude on github 
import TidbitsOCM._


class SliceDoubleBuffer(img_width: Int, data_width: Int, kernel_dim: Int) extends Module {

    val cols = kernel_dim*kernel_dim
    val total_reads = img_width*cols
    val total_writes = img_width*cols

    println("sdb total_reads: %d".format(total_reads))

    val io = new Bundle {
        val data_in = UInt(INPUT, data_width)

        val request_write = Bool(INPUT)   // parent requests the sdb to write data
        val request_read = Bool(INPUT)    // parent requests to read data from sdb

        val request_input = Bool(OUTPUT)
        val request_output = Bool(OUTPUT)

        val data_out = UInt(OUTPUT, data_width)
        val error = Bool(OUTPUT)
    }

    val slice1 = Module(new SliceBuffer(img_width, data_width, kernel_dim))
    val slice2 = Module(new SliceBuffer(img_width, data_width, kernel_dim))

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
            slice1.io.pop := Bool(true)
            io.data_out := slice1.io.data_out
            writes_performed := writes_performed + UInt(1)
        }.otherwise{
            slice2.io.pop := Bool(true)
            io.data_out := slice2.io.data_out
            writes_performed := writes_performed + UInt(1)
        }
    }

    // Handle write requests
    // This means we want to write some input into buffer
    when(io.request_write){
        when(current === Bool(false)){
            slice1.io.push := Bool(true)
            slice1.io.data_in := io.data_in
            reads_performed := reads_performed + UInt(1)
        }.otherwise{
            slice2.io.push := Bool(true)
            slice2.io.data_in := io.data_in
            reads_performed := reads_performed + UInt(1)
        }
    }

    // Check if reads/writes are finished
    when( (reads_performed === UInt(total_reads) ) ){
        reads_finished := Bool(true)
    }

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
