package Core

import Chisel._

// Stole all of this code from some dude on github 
import TidbitsOCM._


class SliceReverseBuffer(row_length: Int, data_width: Int, kernel_dim: Int) extends Module {

    val cols = kernel_dim*kernel_dim - 2
    val total_enq = cols*row_length
    val total_deq = total_enq
    val deqs_per_row = row_length

    val io = new Bundle {

        val reset = Bool(INPUT)
        val data_in = UInt(INPUT, data_width)

        val enq = Bool(INPUT)
        val deq = Bool(INPUT)

        val data_out = UInt(OUTPUT, data_width)


        val dbg_enq_row = UInt(OUTPUT, 32)
        val dbg_deq_row = UInt(OUTPUT, 32)

        val dbg_row_deq_count = UInt(OUTPUT, 32)
    }

    val row_buffers = for(i <- 0 until cols) yield Module(new RowBuffer(row_length, data_width, i)).io


    // Keep track of which row to enqueue or dequeue
    val enq_row = Reg(init=UInt(0, 32))
    val deq_row  = Reg(init=UInt(0, 32))
    io.dbg_enq_row := enq_row
    io.dbg_deq_row := deq_row


    // Keep track of deqs for current row
    val row_deq_count = Reg(init=UInt(0, 32))
    io.dbg_row_deq_count := row_deq_count


    when(io.reset){
        enq_row := UInt(0)
        deq_row := UInt(0)
        row_deq_count := UInt(0)
    }


    io.data_out := UInt(57005)


    // Maintain deq row
    when(io.deq){
        when(row_deq_count === UInt(row_length)){
            when(deq_row < UInt(cols)){
                deq_row := deq_row + UInt(1)
            }.otherwise{
                deq_row := UInt(0)
            }
            row_deq_count := UInt(0)
        }
        .otherwise{
            row_deq_count := row_deq_count + UInt(1)
        }
    }


    // Maintain enq row
    when(io.enq){
        when(enq_row < UInt(cols-1)){
            enq_row := enq_row  + UInt(1)    
        }.otherwise{
            enq_row  := UInt(0)
        }
    }


    // Handle deq
    for(i <- 0 until cols){
        when(deq_row  === UInt(i)){
            row_buffers(i).pop := io.deq
            io.data_out := row_buffers(i).data_out
        }.otherwise{
            row_buffers(i).pop := Bool(false)
        }
    }


    // Handle enq
    for(i <- 0 until cols){
        when(enq_row  === UInt(i)){
            row_buffers(i).push := io.enq
            row_buffers(i).data_in := io.data_in
        }.otherwise{
            row_buffers(i).push := Bool(false)
            row_buffers(i).data_in := UInt(57005)
        }
        // Piggyback the reset signal
        row_buffers(i).reset := io.reset
    }
}

