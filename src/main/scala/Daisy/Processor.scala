package Core

import Chisel._
import TidbitsOCM._

class Processor(data_width: Int, val cols: Int, rows: Int, kernel_dim: Int) extends Module{

    val io = new Bundle {

        val pixel_in = UInt(INPUT, data_width)

        val processor_configure = Bool(INPUT)
        val control_data_in = UInt(INPUT, data_width)
        val processor_sleep = Bool(INPUT)
        val input_valid = Bool(INPUT)

        val ALU_data_out = UInt(OUTPUT, data_width)
        val ALU_data_is_valid = Bool(OUTPUT)
    }

    val conveyor = Module(new PixelGrid(data_width, cols, rows))
    val data_control = Module(new Orchestrator(cols, rows))
    val kernel_buffer = Module(new KernelBuffer(data_width, rows))
    val ALUs = Module(new ALUrow(data_width, cols, rows, kernel_dim))
    val processor_control = Module(new ProcessorController(data_width, cols, rows, kernel_dim))

    conveyor.io.read_row    :=   data_control.io.read_row
    conveyor.io.mux_row     :=   data_control.io.mux_row
    conveyor.io.pixel_in    :=   io.pixel_in
    conveyor.io.shift_mux   :=   data_control.io.shift_mux
    conveyor.io.stall       :=   processor_control.io.alu_stall

    for(i <- 0 until rows ) { 
        ALUs.io.pixel_in((rows - 1) - i ) := conveyor.io.data_out(i)
    }

    ALUs.io.selector_shift := data_control.io.ALU_shift
    ALUs.io.accumulator_flush := data_control.io.accumulator_flush
    ALUs.io.kernel_in := kernel_buffer.io.kernel_out
    ALUs.io.stall := processor_control.io.alu_stall
    ALUs.io.load_instruction := processor_control.io.load_instruction 

    kernel_buffer.io.kernel_in := ALUs.io.kernel_out
    kernel_buffer.io.stall := processor_control.io.alu_stall
    kernel_buffer.io.data_in := io.control_data_in
    kernel_buffer.io.load_kernel := processor_control.io.load_kernel

    processor_control.io.input_valid := io.input_valid
    processor_control.io.programming_mode := io.processor_configure
    processor_control.io.processor_sleep := io.processor_sleep

    data_control.io.reset := io.processor_sleep

    io.ALU_data_out := ALUs.io.data_out
    io.ALU_data_is_valid := ALUs.io.valid_out


    val first_valid_output = data_control.data_out
}

