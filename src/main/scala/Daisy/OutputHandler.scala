package Core

import Chisel._
import TidbitsOCM._

// Buffers output which can be fed out at whatever pace
class OutputHandler(row_length: Int, data_width: Int, img_height: Int, kernel_dim: Int) extends Module {
 
    val entries = row_length

    val mantle_width = (kernel_dim)/2
    val valid_rows_per_image = img_height - (mantle_width*2)
    val slices_per_image = valid_rows_per_image / (kernel_dim*kernel_dim - 2)

    println("Slices: %d".format(slices_per_image))

    val io = new Bundle {

        val reset = Bool(INPUT)

        val data_in = UInt(INPUT, data_width)
        val input_valid = Bool(INPUT)

        val ready_for_input = Bool(OUTPUT)

        val request_output = Bool(INPUT)
        val output_ready = Bool(OUTPUT)

        val frame_finished = Bool(OUTPUT)

        val data_out = UInt(OUTPUT, data_width)

    }

    val output_buffer = Module(new ReverseDoubleBuffer(row_length: Int, data_width: Int, kernel_dim))
    output_buffer.reset := io.reset


    output_buffer.io.slave_enq_input := Bool(false)
    output_buffer.io.slave_deq_output := Bool(false)
    output_buffer.io.slave_enq_input := io.input_valid

    io.ready_for_input := output_buffer.io.slave_can_enq_input
    io.output_ready := output_buffer.io.slave_can_deq_output
    io.data_out := output_buffer.io.data_out


    output_buffer.io.data_in := UInt(57005)
    when(io.input_valid){
        output_buffer.io.data_in := io.data_in
    }

    when(io.request_output){
        output_buffer.io.slave_deq_output := Bool(true)
    }

}

