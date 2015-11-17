package Core

import Chisel._
import TidbitsOCM._

class Tile(img_width: Int, control_data_width: Int, pixel_data_width: Int, HDMI_data_width: Int, cols: Int, rows: Int) extends Module{

    val kernel_dim = rows
    val img_height = 480

    val io = new Bundle {
        val control_data_in = UInt(INPUT, control_data_width)
        val control_input_valid = Bool(INPUT)

        val hdmi_data_in = UInt(INPUT, HDMI_data_width)
        val hdmi_input_valid = Bool(INPUT)

        val reset = Bool(INPUT)
        val active = Bool(INPUT) //Not used, but wired

        val data_out = UInt(OUTPUT, pixel_data_width)
        val output_valid = Bool(OUTPUT)
    }

    val InputHandler = Module(new InputHandler(img_width, HDMI_data_width, pixel_data_width, kernel_dim))
    val Processor = Module(new Processor(pixel_data_width, cols, rows, kernel_dim))
    val SystemControl = Module(new TileController(control_data_width, pixel_data_width, img_width, kernel_dim, 30))
    val OutputHandler = Module(new OutputHandler(pixel_data_width, img_width, img_height, kernel_dim))
    

    // Input handler takes an input stream from any source and width and translates to data_width
    InputHandler.io.input_ready := io.hdmi_input_valid
    InputHandler.io.data_in := io.hdmi_data_in

    // Processor processes data. Incredible
    Processor.io.pixel_in := InputHandler.io.data_out
    Processor.io.processor_sleep := SystemControl.io.processor_sleep
    Processor.io.processor_configure := SystemControl.io.processor_configure
    Processor.io.input_valid := SystemControl.io.processor_control_input_valid
    Processor.io.control_data_in := SystemControl.io.processor_control_input

    // Controller checks input and output for the processor, determining validity.
    // Handles instructing the processor
    SystemControl.io.processor_input_is_valid := InputHandler.io.data_ready
    SystemControl.io.ALU_output := Processor.io.ALU_data_out
    SystemControl.io.ALU_output_is_valid := Processor.io.ALU_data_is_valid

    // Output handler recieves data from the controller, aswell as a valid bit
    OutputHandler.io.input_valid := SystemControl.io.processor_output_is_valid
    OutputHandler.io.data_in := SystemControl.io.processor_output
}


// A test to measure input tolerance to slack
class InputTest(c: Tile) extends Tester(c) {
    
    // poke(c.io.input_valid, false) 
    // poke(c.io.reset, false) 
    // poke(c.io.active, true) 
    // step(1)

    // // Feed data for a slice, see what happens
    // for(i <- 0 until ((90*7)/6) - 1){
    //     if(i%7 == 0){
    //         poke(c.io.data_in, 57005)
    //         poke(c.io.input_valid, false)
    //     }
    //     else{
    //         poke(c.io.data_in, i)
    //         poke(c.io.input_valid, true)
    //     }
    //     if(i%20 == 0){
    //         println("After %d cycles we have:".format(i))
    //         peek(c.InputHandler.input_buffer.current)
    //         println("For reads we have:")
    //         peek(c.InputHandler.input_buffer.reads_performed)
    //         peek(c.InputHandler.input_buffer.reads_finished)
    //         println("For writes we have:")
    //         peek(c.InputHandler.input_buffer.writes_performed)
    //         peek(c.InputHandler.input_buffer.writes_finished)
    //     }
    //     step(1)
    // }

    // println()
    // println("Initial feeding done, transition")
    // poke(c.io.input_valid, false)
    // 
    // for(i <- 0 until 20){
    //     peek(c.InputHandler.input_buffer.current)
    //     println("For reads we have:")
    //     peek(c.InputHandler.input_buffer.reads_performed)
    //     peek(c.InputHandler.input_buffer.reads_finished)
    //     println("For writes we have:")
    //     peek(c.InputHandler.input_buffer.writes_performed)
    //     peek(c.InputHandler.input_buffer.writes_finished)
    //     println()
    //     peek(c.InputHandler.input_buffer.io.data_out)
    //     println()
    //     step(1)
    //     println()
    // }

    // println("Performing next slice feed")
    // for(i <- 0 until ((70*7)/6) - 1){
    //     if(i%7 == 0){
    //         poke(c.io.data_in, 57005)
    //         poke(c.io.input_valid, false)
    //     }
    //     else{
    //         poke(c.io.data_in, i)
    //         poke(c.io.input_valid, true)
    //     }
    //     if(i%20 == 0){
    //         println("After %d cycles we have:".format(i))
    //         peek(c.InputHandler.input_buffer.current)
    //         println("For reads we have:")
    //         peek(c.InputHandler.input_buffer.reads_performed)
    //         peek(c.InputHandler.input_buffer.reads_finished)
    //         println("For writes we have:")
    //         peek(c.InputHandler.input_buffer.writes_performed)
    //         peek(c.InputHandler.input_buffer.writes_finished)
    //     }
    //     step(1)
    // }

    // println()
    // println("Monitoring transition")
    // poke(c.io.input_valid, true)
    // 
    // for(i <- 0 until 60){
    //     if(i%2 == 0){
    //         poke(c.io.data_in, 57005)
    //         poke(c.io.input_valid, false)
    //     }
    //     else{
    //         poke(c.io.data_in, i)
    //         poke(c.io.input_valid, true)
    //     }
    //     peek(c.InputHandler.input_buffer.current)
    //     println("For reads we have:")
    //     peek(c.InputHandler.input_buffer.reads_performed)
    //     peek(c.InputHandler.input_buffer.reads_finished)
    //     println("For writes we have:")
    //     peek(c.InputHandler.input_buffer.writes_performed)
    //     peek(c.InputHandler.input_buffer.writes_finished)
    //     println()
    //     peek(c.InputHandler.input_buffer.io.data_out)
    //     println()
    //     step(1)
    //     println()
    // }

}

class TileTest(c: Tile) extends Tester(c) {
}
