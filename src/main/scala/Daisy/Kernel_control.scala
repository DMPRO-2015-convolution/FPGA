package Core

import Chisel._

class KernelController(data_width: Int, kernel_dim: Int) extends Module {

    val inactive_kernels = (kernel_dim/2)*2   // In case of even numbered kernel
    val total_kernels = kernel_dim*kernel_dim

    val io = new Bundle {
        val kernel_valid = Bool(INPUT)
        val kernel_in = UInt(INPUT, data_width)

        val active = Bool(INPUT)

        val kernel_out = UInt(OUTPUT, data_width)
        val freeze_kernels = Bool(OUTPUT)
    }

    val kernel_buffer = Vec.fill(inactive_kernels){ Reg(init=SInt(0, width=data_width)) }
    val kernel_count = Reg(init=UInt(0, 32))
    val instruction_mode :: sleep :: Nil = Enum(UInt(), 2)
    val state = Reg(init=UInt(instruction_mode))


    // Wish I was better at scala
    def propagate_kernels(): Unit = { for(i <- 1 until inactive_kernels){ 
            kernel_buffer(i) := kernel_buffer(i-1)
            io.kernel_out := kernel_buffer(inactive_kernels - 1)}
    }


    io.freeze_kernels := Bool(false)
    io.kernel_out := UInt(57005)


    // When in instruction mode we want to feed the kernel chain
    when(state === instruction_mode){
        when(io.kernel_valid){
            kernel_count := kernel_count + UInt(1)
            kernel_buffer(0) := io.kernel_in 
            propagate_kernels()
        }
        .otherwise{
            io.freeze_kernels := Bool(true)
        }

        when(kernel_count === UInt(total_kernels - 1)){
            state := sleep
        }
    }


    io.kernel_out := UInt(57005)

    when(state === sleep){
        propagate_kernels()
        io.kernel_out := kernel_buffer(inactive_kernels - 1)
    }
 
}
