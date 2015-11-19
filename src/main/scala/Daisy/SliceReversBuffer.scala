package Core

import Chisel._

// Stole all of this code from some dude on github 
import TidbitsOCM._


class SliceReverseBuffer(row_length: Int, data_width: Int, kernel_dim: Int) extends Module {

    val cols = kernel_dim*kernel_dim - 2
    val total_enqueues = cols*(row_length - 2)
    val total_dequeues = total_enqueues
    val deqs_per_row = total_dequeues/7
    
    val io = new Bundle {
        val data_in = UInt(INPUT, data_width)

        val enqueue = Bool(INPUT)
        val dequeue = Bool(INPUT)

        val can_enqueue = Bool(OUTPUT)
        val can_dequeue = Bool(OUTPUT)

        val data_out = UInt(OUTPUT, data_width)

        val dbg_enq_row = UInt(OUTPUT, 32)
        val dbg_deq_row = UInt(OUTPUT, 32)
    }

    val row_buffers = for(i <- 0 until cols) yield Module(new RowBuffer(row_length, data_width, i)).io
      
    val enqueue_row = Reg(init=UInt(0, 32))
    val dequeue_row  = Reg(init=UInt(0, 32))
    io.dbg_enq_row := enqueue_row
    io.dbg_deq_row := dequeue_row

    val row_dequeue_count = Reg(init=UInt(0, 32))

    val enqueues_performed = Reg(init=UInt(0, 32))
    val dequeues_performed = Reg(init=UInt(0, 32))

    val deq_mode :: enq_mode :: Nil = Enum(UInt(), 2)
    val mode = Reg(init=enq_mode)

    io.data_out := UInt(57005)

    io.can_dequeue := Bool(false)
    io.can_enqueue := Bool(false)

    when(row_dequeue_count === UInt(deqs_per_row - 1)){
        row_dequeue_count := UInt(0)
        when(dequeue_row < UInt(cols)){
            dequeue_row := dequeue_row + UInt(1)
        }.otherwise{
            dequeue_row := UInt(0)
        }
    }

    when(io.dequeue){
        when(row_dequeue_count === UInt(deqs_per_row - 1)){
            row_dequeue_count := UInt(0)
        }.otherwise{
            row_dequeue_count := row_dequeue_count + UInt(1)
        }
    }

    // Maintain enqueue row
    when(io.enqueue){
        when(enqueue_row < UInt(cols - 1)){
            enqueue_row := enqueue_row  + UInt(1)    
        }.otherwise{
            enqueue_row  := UInt(0)
        }
    }

    // enqueue data 
    for(i <- 0 until cols){
        when(enqueue_row  === UInt(i)){
            row_buffers(i).push := io.enqueue
            row_buffers(i).data_in := io.data_in
        }.otherwise{
            row_buffers(i).push := Bool(false)
            row_buffers(i).data_in := UInt(57005)
        }
    }

    io.data_out := UInt(57005)

    // deq data
    for(i <- 0 until cols){
        when(dequeue_row === UInt(i)){
            row_buffers(i).pop := io.dequeue
            io.data_out := row_buffers(i).data_out
        }.otherwise{
            row_buffers(i).pop := Bool(false)
        }
    }

    when(io.dequeue){
        dequeues_performed := enqueues_performed + UInt(1)

        when(dequeues_performed === UInt(total_dequeues - 1)){
            mode := enq_mode
            dequeues_performed := UInt(0)
        }
    }

    when(io.enqueue){
        enqueues_performed := enqueues_performed + UInt(1)

        when(enqueues_performed === UInt(total_enqueues - 1)){
            mode := deq_mode
            enqueues_performed := UInt(0)
        }
    }

    when(mode === deq_mode){
        io.can_dequeue := Bool(true)
    }

    when(mode === enq_mode){
        io.can_enqueue := Bool(true)
    }
}

class SliceReverseBufferTest(c: SliceReverseBuffer) extends Tester(c) {

    
    poke(c.io.enqueue, true)
    poke(c.io.dequeue, false)

    for(i <- 0 until 30*7){
        poke(c.io.data_in, (i%7)+1)
        peek(c.enqueue_row)
        peek(c.dequeue_row)
        peek(c.io.can_enqueue)
        peek(c.io.can_dequeue)
        peek(c.mode)
        step(1)
    }

    poke(c.io.enqueue, false)

    println("FEED COMPLETE\n")
    for(i <- 0 until 3){
        peek(c.enqueue_row)
        peek(c.dequeue_row)
        peek(c.io.can_enqueue)
        peek(c.io.can_dequeue)
        peek(c.mode)
        step(1)
    }

    poke(c.io.dequeue, true)

    println("\n\nSTARTING OUTPUT\n")
    for(i <- 0 until 30*7){
        peek(c.enqueue_row)
        peek(c.dequeue_row)
        peek(c.io.can_enqueue)
        peek(c.io.can_dequeue)
        peek(c.mode)
        peek(c.io.data_out)
        step(1)
    }
}
