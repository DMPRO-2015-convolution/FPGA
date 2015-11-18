package Core

import Chisel._

class KernelBuffer(data_width: Int, kernel_dim: Int) extends Module {

    val inactive_kernels = kernel_dim - 1
    val total_kernels = kernel_dim*kernel_dim

    val io = new Bundle {
        val kernel_in = UInt(INPUT, data_width)

        val data_in = UInt(INPUT, data_width)
        val stall = Bool(INPUT)

        val load_kernel = Bool(INPUT)
        val kernel_out = UInt(OUTPUT, data_width)

        val dbg_kernel0 = UInt(OUTPUT, data_width)
        val dbg_kernel1 = UInt(OUTPUT, data_width)
    }

    val kernel_buffer = Vec.fill(inactive_kernels){ Reg(init=SInt(0, width=data_width)) }
    val kernel_count = Reg(init=UInt(0, 32))

    // Wish I was better at scala
    def propagate_kernels(): Unit = { for(i <- 1 until inactive_kernels){ 
            kernel_buffer(i) := kernel_buffer(i-1)
            io.kernel_out := kernel_buffer(inactive_kernels - 1)}
    }

    io.kernel_out := UInt(57005)
    
    // When in instruction mode we want to feed the kernel chain
    when(io.load_kernel){
        when(!io.stall){
            kernel_count := kernel_count + UInt(1)
            kernel_buffer(0) := io.data_in
            propagate_kernels()
        }
    }
    .elsewhen(!io.stall){
        propagate_kernels()
        io.kernel_out := kernel_buffer(inactive_kernels - 1)
        kernel_buffer(0) := io.kernel_in
    }
 
    io.dbg_kernel0 := kernel_buffer(0)
    io.dbg_kernel1 := kernel_buffer(1)
}
