package Core

import Chisel._
import TidbitsOCM._

// Processor controller decides when the processor should reset, and sends control signals
// to the computation units
class ProcessorController(data_width: Int, cols: Int, rows: Int, kernel_dims: Int) extends Module{

    val total_kernels = kernel_dims*kernel_dims

    val io = new Bundle {

        val input_valid = Bool(INPUT)
        val programming_mode = Bool(INPUT)
        val processor_sleep = Bool(INPUT)

        val alu_stall = Bool(OUTPUT)
        val kernel_load = Bool(OUTPUT)
        val instruction_configure = Bool(OUTPUT)
    }

    val stage = Reg(init=UInt(0, 32))

    io.alu_stall := Bool(false)
    io.kernel_load := Bool(false)
    io.instruction_configure := Bool(false)

    when(io.programming_mode){
        when(io.input_valid){

            io.alu_stall := Bool(false) 

            when(stage === UInt(total_kernels)){
                io.instruction_configure := Bool(true)
            }
            .otherwise{
                io.kernel_load := Bool(true)
            }

            stage := stage + UInt(1)
        }
        io.alu_stall := Bool(true) 
    }

}
