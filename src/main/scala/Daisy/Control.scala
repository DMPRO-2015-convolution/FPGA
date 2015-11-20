package Core

import Chisel._
import TidbitsOCM._

// This module has the final say wrt output validity. A top level module is required for this
// because we cannot always rely on the submodules to discern the validity of their output 
// because there is a significant time delay between valid input and output.
//
// It is also responsible for handling programming of kernel values and operators
class TileController(data_width: Int, img_width: Int, kernel_dim: Int, first_valid_output: Int) extends Module {

    val mantle_width = (kernel_dim - 1)/2
    val valid_rows_per_slice = (kernel_dim*kernel_dim) - 2*mantle_width
    val valid_cols_per_slice = img_width - 2*mantle_width

    val outputs_per_slice = img_width*valid_cols_per_slice

    val total_kernels = kernel_dim*kernel_dim

    val io = new Bundle {

        val reset = Bool(INPUT)

        val control_data_in = UInt(INPUT, data_width)
        val control_input_valid = Bool(INPUT)

        val processor_control_input = UInt(OUTPUT, data_width)
        val processor_control_input_valid = Bool(OUTPUT)

        val processor_input_is_valid = Bool(INPUT)

        val ALU_output_is_valid = Bool(INPUT)

        val processor_output_is_valid = Bool(OUTPUT)

        val processor_sleep = Bool(OUTPUT)
        val processor_configure = Bool(OUTPUT)
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
  
    io.processor_output_is_valid := Bool(false)

    when(state === data_mode){
        when(io.processor_input_is_valid){
            
            io.processor_sleep := Bool(false)

            // 1:
            when(valid_processor_input_count < UInt(first_valid_output)){
                valid_processor_input_count := valid_processor_input_count + UInt(1)
            }

            // 2
            .otherwise{
                // We test whether the ALUs produce valid output
                when(io.ALU_output_is_valid){
                    io.processor_output_is_valid := Bool(true)
                    valid_processor_output_count := valid_processor_output_count + UInt(1)
                }
            }
        }
        // We make sure to not trigger when waiting for next feed.
        .elsewhen(valid_processor_output_count > UInt(0)){
            // 3
            when(valid_processor_output_count < UInt(outputs_per_slice)){
                when(io.ALU_output_is_valid){
                    io.processor_output_is_valid := Bool(true)
                    valid_processor_output_count := valid_processor_output_count + UInt(1)
                }
            }
        }

        // Reset counters after a slice has been fed
        when(valid_processor_output_count === UInt(outputs_per_slice)){
            valid_processor_input_count := UInt(0)
            valid_processor_output_count := UInt(0)
            io.processor_sleep := Bool(true)
        }
    }
    .otherwise{
        io.processor_sleep := Bool(true)
    }


    val stage = Reg(init=UInt(0, 32))
    val total_stages = total_kernels*2 + 2 + 1

    io.processor_control_input := io.control_data_in
    io.processor_control_input_valid := io.control_input_valid
    io.processor_configure := Bool(false)

    when(state === control_mode){
        
        io.processor_configure := Bool(true)

        when(io.control_input_valid){
            stage := stage + UInt(1)
            io.processor_sleep := Bool(false)

            when(stage === UInt(total_stages - 1)){
                state := data_mode
            }
        }
    }

    when(io.reset){
        state := control_mode    
        stage := UInt(0)
    }
}

class ControllerTest(c: TileController) extends Tester(c){
    // Aint nuttin here...
}
