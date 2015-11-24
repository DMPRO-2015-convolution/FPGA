package Core

import Chisel._
import TidbitsOCM._

class InputTest(c: Tile) extends Tester(c) {

    poke(c.io.control_data_in, 0)
    poke(c.io.control_input_valid, false)
    poke(c.io.reset, false)
    poke(c.io.hdmi_input_valid, true)
    poke(c.io.request_processed_data, true)

    for(i <- 0 until 90){
        poke(c.io.hdmi_data_in, (i % 9) + 1)
        poke(c.io.hdmi_input_valid, true)
        println()
        peek(c.InputHandler.io)
        println()
        peek(c.SystemControl.io)
        println()
        step(1)
    }
    println()
    println()
    poke(c.io.hdmi_input_valid, false)
    peek(c.InputHandler.io)
    peek(c.SystemControl.io)
    step(1)
    println()
    println()
    peek(c.InputHandler.io)
    peek(c.SystemControl.io)
    println()
    println()
    for(i <- 0 until 90){
        poke(c.io.hdmi_data_in, (i % 9) + 10)
        poke(c.io.hdmi_input_valid, true)
        println()
        peek(c.InputHandler.io.data_out)
        peek(c.InputHandler.input_buffer.bonus_pop)
        println()
        peek(c.SystemControl.io)
        println()
        step(1)
    }
    peek(c.InputHandler.io)
    peek(c.SystemControl.io)
    step(1)
    for(i <- 0 until 90){
        poke(c.io.hdmi_data_in, (i % 9) + 19)
        poke(c.io.hdmi_input_valid, true)
        println()
        peek(c.InputHandler.io.data_out)
        peek(c.InputHandler.input_buffer.bonus_pop)
        println()
        peek(c.SystemControl.io)
        println()
        println()
        println()
        peek(c.io.data_out)
        println()
        println()
        println()
        step(1)
    }
    assert(false)
    step(1)
    poke(c.io.hdmi_input_valid, false)
    peek(c.InputHandler.io)
    peek(c.SystemControl.io)
    println("-----------------")
    for(i <- 0 until 90){
        //  peek(c.OutputBuffer.io)
        peek(c.Processor.io)
        step(1)
    }

    poke(c.io.request_processed_data, true)
    for(i <- 0 until 90){
        peek(c.io.data_out)
        peek(c.OutputBuffer.io)
        peek(c.io.output_valid)
        step(1)
    }

}
