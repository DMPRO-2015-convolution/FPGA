package Core

import Chisel._
import TidbitsOCM._

// Processor controller decides when the processor should reset, and sends control signals
// to the computation units
class ProcessorController(data_width: Int, cols: Int, rows: Int, kernel_dims: Int) extends Module{

    val io = new Bundle {

        val input_valid = Bool(INPUT)
        val alu_stall = Bool(OUTPUT)
    }

    val programming_mode :: data_mode :: sleep :: Nil = Enum(UInt(), 3)
}
