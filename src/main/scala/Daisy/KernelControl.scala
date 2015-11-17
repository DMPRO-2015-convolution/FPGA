package Core

import Chisel._

class KernelController(data_width: Int, kernel_dim: Int) extends Module {

    val inactive_kernels = kernel_dim - 1
    val total_kernels = kernel_dim*kernel_dim

    val io = new Bundle {
        val kernel_in = UInt(INPUT, data_width)

        val input_valid = Bool(INPUT)
        val data_in = UInt(INPUT, data_width)

        val kernel_stage = Bool(INPUT)
        val stall = Bool(INPUT)

        val kernel_out = UInt(OUTPUT, data_width)
        val stall_alu = Bool(OUTPUT)
    }

    val kernel_buffer = Vec.fill(inactive_kernels){ Reg(init=SInt(0, width=data_width)) }
    val kernel_count = Reg(init=UInt(0, 32))

    // Wish I was better at scala
    def propagate_kernels(): Unit = { for(i <- 1 until inactive_kernels){ 
            kernel_buffer(i) := kernel_buffer(i-1)
            io.kernel_out := kernel_buffer(inactive_kernels - 1)}
    }


    io.stall_alu := Bool(false)
    io.kernel_out := UInt(57005)
    
    // When in instruction mode we want to feed the kernel chain
    when(io.kernel_stage){
        when(io.input_valid){
            kernel_count := kernel_count + UInt(1)
            kernel_buffer(0) := io.data_in
            propagate_kernels()
        }
        .otherwise{
            io.stall_alu := Bool(true)
        }
    }
    .elsewhen(!io.stall){
        propagate_kernels()
        io.kernel_out := kernel_buffer(inactive_kernels - 1)
        kernel_buffer(0) := io.kernel_in
    }
 
}
