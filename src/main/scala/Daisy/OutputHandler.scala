package Core

import Chisel._
import TidbitsOCM._

// Buffers output which can be fed out at whatever pace
class OutputHandler(row_length: Int, pixel_data_width: Int, output_data_width: Int, img_height: Int, kernel_dim: Int) extends Module {
 
    val entries = (row_length*pixel_data_width)/output_data_width

    val mantle_width = (kernel_dim)/2
    val valid_rows_per_image = img_height - (mantle_width*2)
    val valid_pixels_per_row = row_length - (mantle_width*2)
    val slices_per_image = valid_rows_per_image / (kernel_dim*kernel_dim - 2)

    println("Slices: %d".format(slices_per_image))
    
    val io = new Bundle {

        val data_in = UInt(INPUT, pixel_data_width)
        val input_valid = Bool(INPUT)

        val ready_for_input = Bool(OUTPUT)

        val request_output = Bool(INPUT)
        val output_ready = Bool(OUTPUT)
        val output_valid = Bool(OUTPUT)

        val data_out = UInt(OUTPUT, output_data_width)

        val dbg_output_buffer = new Bundle {

            val can_enqueue = Bool(OUTPUT)
            val can_dequeue = Bool(OUTPUT)

            val data_out = UInt(OUTPUT, output_data_width)

            val buf1 = UInt(OUTPUT, 32)
            val buf2 = UInt(OUTPUT, 32)

            val enq_row = UInt(OUTPUT, 32)
            val deq_row = UInt(OUTPUT, 32)
        }
    }

    val translator = Module(new twentyfour_sixteen())
    translator.io.req_in := Bool(false)
    translator.io.req_out := Bool(false)
    val output_buffer = Module(new SliceReverseBuffer(row_length: Int, pixel_data_width: Int, kernel_dim))
    output_buffer.io.enqueue := Bool(false)
    output_buffer.io.dequeue := Bool(false)
    output_buffer.io.data_in := io.data_in

    // io.ready_for_input := Bool(false)
    io.ready_for_input := output_buffer.io.can_enqueue
    io.output_ready := output_buffer.io.can_dequeue

    val chip_sel = Reg(init=Bool(false)) 

    when(io.input_valid){
        output_buffer.io.enqueue := Bool(true)
    }

    translator.io.d_in := output_buffer.io.data_out
    translator.io.req_out := io.request_output
    
    when(io.request_output){
        translator.io.req_in := Bool(true)
        output_buffer.io.dequeue := translator.io.rdy_in
    }

    io.data_out := translator.io.d_out
    io.output_valid := translator.io.rdy_out


    io.dbg_output_buffer.can_enqueue := output_buffer.io.can_enqueue
    io.dbg_output_buffer.can_dequeue := output_buffer.io.can_dequeue
    io.dbg_output_buffer.data_out := output_buffer.io.data_out
    io.dbg_output_buffer.buf1 := translator.io.dbg_buf1
    io.dbg_output_buffer.buf2 := translator.io.dbg_buf2
    io.dbg_output_buffer.enq_row := output_buffer.io.dbg_enq_row
    io.dbg_output_buffer.deq_row := output_buffer.io.dbg_deq_row

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
        peek(c.translator.io)
        peek(c.translator.inputs_finished)
        peek(c.translator.outputs_finished)
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
            peek(c.output_buffer.dequeue_row)
            peek(c.output_buffer.row_dequeue_count)
            peek(c.translator.dbg_reads)
            peek(c.translator.io.dbg_buf1)
            peek(c.translator.io.dbg_buf2)
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
