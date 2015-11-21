package Core

import Chisel._
import TidbitsOCM._

// Processor controller decides when the processor should reset, and sends control signals
// to the computation units. Keeps track of kernel skew and instructions
class ProcessorController(data_width: Int, cols: Int, rows: Int, kernel_dims: Int) extends Module{

    val total_kernels = kernel_dims*kernel_dims

    val io = new Bundle {

        val input_valid = Bool(INPUT)
        val programming_mode = Bool(INPUT)
        val processor_sleep = Bool(INPUT)

        val alu_stall = Bool(OUTPUT)
        val load_kernel = Bool(OUTPUT)
        val load_instruction = Bool(OUTPUT)

        val reset = Bool(OUTPUT)

        val dbg_kernel_skew = UInt(OUTPUT, 32)
    }

    val stage = Reg(init=UInt(0, 32))
    val kernel_skew = Reg(init=UInt(0, 8))

    io.alu_stall := Bool(false)
    io.load_kernel := Bool(false)
    io.load_instruction := Bool(false)
    io.reset := Bool(false)

    // Let instruction propagate first, then load kernels
    when(io.programming_mode){
        when(io.input_valid){
            
            io.load_kernel := Bool(true)
            io.load_instruction := Bool(true)

            when(stage > UInt(cols)){
                io.load_instruction := Bool(false)
                when(kernel_skew === UInt(cols - 1)){
                    kernel_skew := UInt(0)
                }
                .otherwise{
                    kernel_skew := kernel_skew + UInt(1)
                }
            }
            stage := stage + UInt(1)
        }
        .otherwise{
            io.alu_stall := Bool(true)
        }
    }
    .elsewhen(io.processor_sleep && (kernel_skew === UInt(8))){
        io.alu_stall := Bool(true)
    }
    .otherwise{
        when(kernel_skew === UInt(cols - 1)){
            kernel_skew := UInt(0)
        }
        .otherwise{
            kernel_skew := kernel_skew + UInt(1)
        }
        stage := UInt(0)
    }

    io.dbg_kernel_skew := kernel_skew
    
}
