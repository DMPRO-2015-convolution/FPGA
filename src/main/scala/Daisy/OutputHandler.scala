package Core

import Chisel._
import TidbitsOCM._

// Buffers output which can be fed out at whatever pace
class OutputHandler(row_length: Int, data_width: Int, img_height: Int, kernel_dim: Int) extends Module {
 
    val entries = row_length

    val mantle_width = (kernel_dim)/2
    val valid_rows_per_image = img_height - (mantle_width*2)
    val valid_pixels_per_row = row_length - (mantle_width*2)
    val slices_per_image = valid_rows_per_image / (kernel_dim*kernel_dim - 2)

    println("Slices: %d".format(slices_per_image))
    
    val io = new Bundle {

        val data_in = UInt(INPUT, data_width)
        val input_valid = Bool(INPUT)

        val ready_for_input = Bool(OUTPUT)

        val request_output = Bool(INPUT)
        val output_ready = Bool(OUTPUT)
        val output_valid = Bool(OUTPUT)

        val data_out = UInt(OUTPUT, data_width)

        val dbg_enq_row = UInt(OUTPUT)
        val dbg_deq_row = UInt(OUTPUT)

        val dbg_row_deq_count = UInt(OUTPUT)
    }

    val output_buffer = Module(new SliceReverseBuffer(row_length: Int, data_width: Int, kernel_dim))
    output_buffer.io.enq := Bool(false)
    output_buffer.io.deq := Bool(false)
    output_buffer.io.data_in := io.data_in

    io.ready_for_input := output_buffer.io.can_enq
    io.output_ready := output_buffer.io.can_deq

    val chip_sel = Reg(init=Bool(false)) 

    when(io.input_valid){
        output_buffer.io.enq := Bool(true)
    }
    
    when(io.request_output){
        output_buffer.io.deq := Bool(true)
    }

    io.output_valid := output_buffer.io.can_deq
    io.data_out := output_buffer.io.data_out


    io.dbg_enq_row := output_buffer.io.dbg_enq_row
    io.dbg_deq_row := output_buffer.io.dbg_deq_row
    io.dbg_row_deq_count := output_buffer.io.dbg_row_deq_count

}

class OutputHandlerTest(c: OutputHandler) extends Tester(c) {
    
    poke(c.io.data_in, 0)
    poke(c.io.input_valid, false)
    poke(c.io.request_output, false)

    peek(c.io)

    step(1)

    poke(c.io.input_valid, true)

    for(i <- 0 until 30*7){
        poke(c.io.data_in, ((i%7)+1)*1118481)
        println()
        peek(c.io)
        step(1)
    }

    println("\n\nDONERINO\n\n")
    poke(c.io.request_output, true)
    poke(c.io.input_valid, false)

    poke(c.io.data_in, 0)

    var outputs = 0
    var count = 0
    while(outputs < 30*9*24/16){
    // while(outputs < 7){
        count = count + 1
        if(count % 4 == 0){
            outputs = outputs + 1
            poke(c.io.request_output, true)
            peek(c.io.data_out)
            peek(c.output_buffer.deq_row)
            peek(c.output_buffer.row_deq_count)
            println()
            println()
            println("outputs: %d".format(outputs))
            println()
            println()
        }
        else{
            poke(c.io.request_output, false)
        }
        step(1)
    }
}

    // for(i <- 0 until 6){
    //     println()
    //     peek(c.io.data_out)
    //     peek(c.output_buffer.io)
    //     println()
    //     println()
    //     peek(c.translator.io)
    //     peek(c.translator.inputs_finished)
    //     peek(c.translator.outputs_finished)
    //     println()
    //     println()

    //     step(1)
    // }
