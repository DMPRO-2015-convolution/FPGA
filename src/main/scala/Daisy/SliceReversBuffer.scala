package Core

import Chisel._

// Stole all of this code from some dude on github 
import TidbitsOCM._


class SliceReverseBuffer(row_length: Int, data_width: Int, kernel_dim: Int) extends Module {

    val cols = kernel_dim*kernel_dim - 2
    val total_enq = cols*row_length
    val total_deq = total_enq
    val deqs_per_row = row_length

    println("Slice reverse buffer deqs per row is %d".format(deqs_per_row))
    
    val io = new Bundle {
        val data_in = UInt(INPUT, data_width)

        val enq = Bool(INPUT)
        val deq = Bool(INPUT)

        val can_enq = Bool(OUTPUT)
        val can_deq = Bool(OUTPUT)

        val data_out = UInt(OUTPUT, data_width)

        val dbg_enq_row = UInt(OUTPUT, 32)
        val dbg_deq_row = UInt(OUTPUT, 32)

        val dbg_row_deq_count = UInt(OUTPUT, 32)
    }

    val row_buffers = for(i <- 0 until cols) yield Module(new RowBuffer(row_length, data_width, i)).io
      
    val enq_row = Reg(init=UInt(0, 32))
    val deq_row  = Reg(init=UInt(0, 32))
    io.dbg_enq_row := enq_row
    io.dbg_deq_row := deq_row

    val row_deq_count = Reg(init=UInt(0, 32))
    io.dbg_row_deq_count := row_deq_count

    val enq_performed = Reg(init=UInt(0, 32))
    val deq_performed = Reg(init=UInt(0, 32))

    val deq_mode :: enq_mode :: Nil = Enum(UInt(), 2)
    val mode = Reg(init=enq_mode)

    io.data_out := UInt(57005)

    io.can_deq := Bool(false)
    io.can_enq := Bool(false)

    when(row_deq_count === UInt(deqs_per_row - 1)){
        row_deq_count := UInt(0)
        when(deq_row < UInt(cols - 1)){
            deq_row := deq_row + UInt(1)
        }.otherwise{
            deq_row := UInt(0)
        }
    }

    when(io.deq){
        when(row_deq_count === UInt(deqs_per_row - 1)){
            row_deq_count := UInt(0)
        }.otherwise{
            row_deq_count := row_deq_count + UInt(1)
        }
    }

    // Maintain enqueue row
    when(io.enq){
        when(enq_row < UInt(cols - 1)){
            enq_row := enq_row  + UInt(1)    
        }.otherwise{
            enq_row  := UInt(0)
        }
    }

    // enqueue data 
    for(i <- 0 until cols){
        when(enq_row  === UInt(i)){
            row_buffers(i).push := io.enq
            row_buffers(i).data_in := io.data_in
        }.otherwise{
            row_buffers(i).push := Bool(false)
            row_buffers(i).data_in := UInt(57005)
        }
    }

    io.data_out := UInt(57005)

    // deq data
    for(i <- 0 until cols){
        when(deq_row === UInt(i)){
            row_buffers(i).pop := io.deq
            io.data_out := row_buffers(i).data_out
        }.otherwise{
            row_buffers(i).pop := Bool(false)
        }
    }

    when(io.deq){
        deq_performed := deq_performed + UInt(1)

        when(deq_performed === UInt(total_deq - 1)){
            mode := enq_mode
            deq_performed := UInt(0)
        }
    }

    when(io.enq){
        enq_performed := enq_performed + UInt(1)

        when(enq_performed === UInt(total_enq - 1)){
            mode := deq_mode
            enq_performed := UInt(0)
        }
    }

    when(mode === deq_mode){
        io.can_deq := Bool(true)
    }

    when(mode === enq_mode){
        io.can_enq := Bool(true)
    }
}

class SliceReverseBufferTest(c: SliceReverseBuffer) extends Tester(c) {

    
    poke(c.io.enq, true)
    poke(c.io.deq, false)

    for(i <- 0 until 30*7){
        poke(c.io.data_in, (i%7)+1)
        peek(c.enq_row)
        peek(c.deq_row)
        peek(c.io.can_enq)
        peek(c.io.can_deq)
        peek(c.mode)
        step(1)
    }

    poke(c.io.enq, false)

    println("FEED COMPLETE\n")
    for(i <- 0 until 3){
        peek(c.enq_row)
        peek(c.deq_row)
        peek(c.io.can_enq)
        peek(c.io.can_deq)
        peek(c.mode)
        step(1)
    }

    poke(c.io.deq, true)

    println("\n\nSTARTING OUTPUT\n")
    for(i <- 0 until 30*7){
        peek(c.enq_row)
        peek(c.deq_row)
        peek(c.io.can_enq)
        peek(c.io.can_deq)
        peek(c.mode)
        peek(c.io.data_out)
        step(1)
    }
}
