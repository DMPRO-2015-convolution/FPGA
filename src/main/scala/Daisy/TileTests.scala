package Core

import Chisel._
import TidbitsOCM._

class InputTest(c: Tile) extends Tester(c) {

    def inspect_rows1_in() : Unit = {
        println("Current")
        peek(c.InputHandler.input_buffer.current_feeding_buffer)
        println("\n#############")
        println("##### 1 #####")
        peek(c.InputHandler.input_buffer.push_performed)
        peek(c.InputHandler.input_buffer.push_finished)
        peek(c.InputHandler.input_buffer.slice1.io.push_row)
        println()
        peek(c.InputHandler.input_buffer.slice1_bonus_row1.io.push)
        println()
        peek(c.InputHandler.input_buffer.slice1_bonus_row2.io.push)
        println("#############\n")
    }


    def inspect_rows1_out() : Unit = {
        println("Current")
        peek(c.InputHandler.input_buffer.current_feeding_buffer)
        println("\n#############")
        println("##### 1 #####")
        peek(c.InputHandler.input_buffer.pop_performed)
        peek(c.InputHandler.input_buffer.pop_finished)
        peek(c.InputHandler.input_buffer.slice1.io.pop_row)
        println()
        peek(c.InputHandler.input_buffer.slice1_bonus_row1.io.pop)
        peek(c.InputHandler.input_buffer.slice1_bonus_row1.io.data_out)
        println()
        peek(c.InputHandler.input_buffer.slice1_bonus_row2.io.pop)
        peek(c.InputHandler.input_buffer.slice1_bonus_row2.io.data_out)
        println("#############\n")
    }


    def inspect_rows2_in() : Unit = {
        println()
        println("Current")
        peek(c.InputHandler.input_buffer.current_feeding_buffer)
        println("\n#############")
        println("##### 2 #####")
        peek(c.InputHandler.input_buffer.push_performed)
        peek(c.InputHandler.input_buffer.push_finished)
        peek(c.InputHandler.input_buffer.slice2.io.push_row)
        println()
        peek(c.InputHandler.input_buffer.slice2_bonus_row1.io.push)
        println()
        peek(c.InputHandler.input_buffer.slice2_bonus_row2.io.push)
        println("#############\n")
    }


    def inspect_rows2_out() : Unit = {
        println()
        println("Current")
        peek(c.InputHandler.input_buffer.current_feeding_buffer)
        println("\n#############")
        println("##### 2 #####")
        peek(c.InputHandler.input_buffer.pop_performed)
        peek(c.InputHandler.input_buffer.pop_finished)
        peek(c.InputHandler.input_buffer.slice2.io.pop_row)
        println()
        peek(c.InputHandler.input_buffer.slice2_bonus_row1.io.pop)
        peek(c.InputHandler.input_buffer.slice2_bonus_row1.io.data_out)
        println()
        peek(c.InputHandler.input_buffer.slice2_bonus_row2.io.pop)
        peek(c.InputHandler.input_buffer.slice2_bonus_row2.io.data_out)
        println("#############\n")
    }


    poke(c.io.control_data_in, 0)
    poke(c.io.control_input_valid, false)
    poke(c.io.reset, false)
    poke(c.io.hdmi_input_valid, true)
    poke(c.io.request_processed_data, false)

    for(i <- 0 until 20){
        poke(c.io.hdmi_data_in, (i / 10) + 1)
        poke(c.io.hdmi_input_valid, true)
        println()
        step(1)
    }

    for(i <- 0 until 70){
        poke(c.io.hdmi_data_in, (i / 10) + 3)
        poke(c.io.hdmi_input_valid, true)
        inspect_rows1_in()
        println()
        step(1)
    }
    println("//////////////////////////////")
    println("//////////////////////////////")
    poke(c.io.hdmi_input_valid, false)
    inspect_rows1_in()
    step(1)
    inspect_rows1_in()
    poke(c.io.hdmi_input_valid, true)
    for(i <- 0 until 70){
        inspect_rows2_in()
        inspect_rows1_out()
        poke(c.io.hdmi_data_in, (i / 10) + 10)
        println()
        peek(c.InputHandler.io.data_out)
        println()
        step(1)
    }
    println("//////////////////////////////")
    println("//////////////////////////////")

    poke(c.io.hdmi_input_valid, false)
    for(i <- 0 until 20){
        inspect_rows1_out()
        poke(c.io.hdmi_data_in, (i / 10) + 10)
        println()
        peek(c.InputHandler.io.data_out)
        println()
        step(1)
    }
    println("//////////////////////////////")
    println("//////////////////////////////")
    inspect_rows2_in()
    poke(c.io.hdmi_input_valid, false)
    step(1)
    inspect_rows2_in()
    poke(c.io.hdmi_input_valid, true)
    for(i <- 0 until 70){
        inspect_rows2_out()
        inspect_rows1_out()
        poke(c.io.hdmi_data_in, (i % 10) + 19)
        println()
        peek(c.InputHandler.io.data_out)
        println()
        step(1)
    }
    println("//////////////////////////////")
    println("//////////////////////////////")
    assert(false)
}
