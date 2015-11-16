package Core

import Chisel._
import TidbitsOCM._

// Processor controller decides when the processor should reset, and sends control signals
// to the computation units
class ProcessorController(data_width: Int, cols: Int, rows: Int, kernel_dims: Int) extends Module{

    val io = new Bundle {
        
        val control_signals = new Bundle {
            val reset = Bool(INPUT)
        }

    }

    val programming_mode :: data_mode :: sleep :: Nil = Enum(UInt(), 3)


}
