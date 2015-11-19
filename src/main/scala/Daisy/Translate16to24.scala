package Core

import Chisel._
import TidbitsOCM._

// Kind of ghetto, but hey, beggars and choosers and all that.
class sixteen_twentyfour() extends Module {

    val io = new Bundle {
        
        val d_in = UInt(INPUT, 16)
        val rdy_in = Bool(OUTPUT)
        val req_in = Bool(INPUT)

        val d_out = UInt(OUTPUT, 24)
        val rdy_out = Bool(OUTPUT)
        val req_out = Bool(INPUT)

        val dbg_buf1 = UInt(OUTPUT)
        val dbg_buf2 = UInt(OUTPUT)
    }

    io.rdy_in  := Bool(false)
    io.rdy_out := Bool(false)

    val buffer1 = Reg(init=UInt(0, 48))
    val buffer2 = Reg(init=UInt(0, 48))

    io.dbg_buf1 := buffer1
    io.dbg_buf2 := buffer2

    val inputs_finished = Reg(init=Bool(false))
    val outputs_finished = Reg(init=Bool(true))

    val inputs_performed = Reg(init=UInt(0, 8))
    val outputs_performed = Reg(init=UInt(0, 8))

    val current = Reg(init=Bool(false))

    // Should signal when ready for input/output
    
    io.d_out := UInt(57005)
    
    when(!outputs_finished){
        io.rdy_out := Bool(true)
    }

    when(!inputs_finished){
        io.rdy_in := Bool(true)
    }

    when(io.req_in){
        when(current){
            when(inputs_performed === UInt(2)){ buffer1(15, 0)  := io.d_in } 
            when(inputs_performed === UInt(1)){ buffer1(31, 16) := io.d_in } 
            when(inputs_performed === UInt(0)){ buffer1(47, 32) := io.d_in } 
        }
        .otherwise{
            when(inputs_performed === UInt(2)){ buffer2(15, 0)  := io.d_in } 
            when(inputs_performed === UInt(1)){ buffer2(31, 16) := io.d_in } 
            when(inputs_performed === UInt(0)){ buffer2(47, 32) := io.d_in } 
        }
        when(inputs_performed === UInt(2)){ 
            inputs_finished  := Bool(true) 
            inputs_performed := UInt(0)
        }
        .otherwise{ inputs_performed := inputs_performed + UInt(1) }
    }

    when(io.req_out){
        when(current){
            when(outputs_performed === UInt(1)){ io.d_out := buffer2(23, 0)  } 
            when(outputs_performed === UInt(0)){ io.d_out := buffer2(47, 24) } 
        }
        .otherwise{
            when(outputs_performed === UInt(1)){ io.d_out := buffer1(23, 0)  } 
            when(outputs_performed === UInt(0)){ io.d_out := buffer1(47, 24) } 
        }
        when(outputs_performed === UInt(1)){ 
            outputs_finished  := Bool(true) 
            outputs_performed := UInt(0)
        }
        .otherwise{ outputs_performed := outputs_performed + UInt(1) }
    }

    when(outputs_finished && inputs_finished){
        current := ~current
        outputs_finished := Bool(false)
        inputs_finished := Bool(false)
    }
}


class Translator1624Test(c: sixteen_twentyfour) extends Tester(c) {
    
    poke(c.io.req_in, false)
    poke(c.io.req_out, false)
    poke(c.io.d_in, 0)

    step(1)
    peek(c.io.rdy_in)
    poke(c.io.d_in, 4660)
    poke(c.io.req_in, true)
    peek(c.buffer1)
    peek(c.buffer2)
    println()

    step(1)
    poke(c.io.req_in, false)
    println()
    peek(c.io)
    println()
    peek(c.buffer1)
    peek(c.buffer2)
    println()

    step(1)
    poke(c.io.d_in, 17185)
    poke(c.io.req_in, true)
    println()
    peek(c.io)
    peek(c.buffer1)
    peek(c.buffer2)
    println()
    
    step(1)
    poke(c.io.d_in, 26214)
    poke(c.io.req_in, true)
    println()
    peek(c.io)
    peek(c.inputs_finished)
    peek(c.outputs_finished)
    println()

    step(1)
    println()
    poke(c.io.req_in, false)
    println()
    peek(c.io)
    peek(c.inputs_finished)
    peek(c.outputs_finished)

    step(1)
    poke(c.io.req_out, true)
    peek(c.io)
    peek(c.inputs_finished)
    peek(c.outputs_finished)

    step(1)
    poke(c.io.req_out, true)
    peek(c.io)
    peek(c.inputs_finished)
    peek(c.outputs_finished)
}
