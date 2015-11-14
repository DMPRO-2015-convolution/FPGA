package Core

import Chisel._
import TidbitsOCM._

// This module has the final say wrt output validity. A top level module is required for this
// because we cannot always rely on the submodules to discern the validity of their output 
// because there is a significant time delay between valid input and output.
//
// It is also responsible for handling programming of kernel values and operators
class TileController(data_width: Int, img_width: Int, kernel_dim: Int, remnant_outputs: Int) extends Module {

    val mantle_width = (kernel_dim - 1)/2
    val valid_rows_per_slice = (kernel_dim*kernel_dim) - 2*mantle_width
    val valid_cols_per_slice = img_width - 2*mantle_width

    val valid_outputs_per_slice = valid_rows_per_slice*valid_cols_per_slice
    val drain_time = remnant_outputs
    val first_valid_output = 30

    val total_kernels = kernel_dim*kernel_dim
    
    println("calculated outputs per slice to be %d".format(valid_outputs_per_slice))

    val io = new Bundle {
        // val active = Bool(INPUT)
        val processor_input_is_valid = Bool(INPUT)

        val ALU_output_is_valid = Bool(INPUT)
        val ALU_output = UInt(INPUT, data_width)

        val processor_output_is_valid = Bool(OUTPUT)
        val processor_output = UInt(OUTPUT, data_width)
    }

    val valid_processor_input_count = Reg(init=UInt(0, 32))
    val valid_processor_output_count = Reg(init=UInt(0, 32))
    
    val control_mode :: data_mode :: Nil = Enum(UInt(), 2)
    val state = Reg(init=control_mode)

    // When a slice is fed to the Processor we have the following cases:
    //
    // #1 Valid input is being fed, but the output is not yet valid, leading to invalid output
    // #2 Valid input is being fed, validity is supplied by ALU
    // #3 Valid data is no longer being fed, but there is still valid output in the conveyor.
    //    validity is decided by ALU
    // #4 Valid data is not being fed, the controller should wait 
  
    io.processor_output := UInt(57005)
    io.processor_output_is_valid := Bool(false)

    when(state === data_mode){
        when(io.processor_output_is_valid){

            // 1:
            when(valid_processor_input_count < UInt(first_valid_output)){
                valid_processor_input_count := valid_processor_input_count + UInt(1)
            }

            // 2
            .otherwise{
                // We test whether the ALUs produce valid output
                when(io.ALU_output_is_valid){
                    io.processor_output := io.ALU_output
                    io.processor_output_is_valid := Bool(true)
                    valid_processor_output_count := valid_processor_output_count + UInt(1)
                }
            }
        }
        // We make sure to not trigger when waiting for next feed.
        .elsewhen(valid_processor_output_count > UInt(0)){
            // 3
            when(valid_processor_output_count < UInt(valid_outputs_per_slice)){
                when(io.ALU_output_is_valid){
                    io.processor_output := io.ALU_output
                    io.processor_output_is_valid := Bool(true)
                    valid_processor_output_count := valid_processor_output_count + UInt(1)
                }
            }
        }

        // Reset counters after a slice has been fed
        when(valid_processor_output_count === UInt(valid_outputs_per_slice)){
            valid_processor_input_count := UInt(0)
            valid_processor_output_count := UInt(0)
        }
    }

    val kernel :: reduce :: map :: Nil = Enum(UInt(), 3)
    val programming_stage = Reg(init=UInt(kernel))
    val kernel_count = Reg(UInt(32))

    when(state === control_mode){
        
        when(programming_stage === kernel){
            when(kernel_count < UInt(total_kernels)){
                kernel_count := kernel_count + UInt(1)
            }
            when(kernel_count === UInt(total_kernels - 1)){
            }
        }
    }
}

class ControllerTest(c: TileController) extends Tester(c){
    // Aint nuttin here...
}
