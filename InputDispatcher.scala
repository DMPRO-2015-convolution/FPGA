package Core

import Chisel._

class Dispatcher(data_width: Int, kernels: Int) extends Module {
    val io = new Bundle {
        val data_in = UInt(INPUT, data_width)
        val kernel_in = UInt(INPUT, data_width)

        val data_out = UInt(INPUT, data_width)
        val kernel_out = UInt(INPUT, data_width)
    }

    val kernel_buffer = Vec.fill(2){ Reg(init=SInt(0, width=data_width)) }
    val s0 :: s1 :: s2 :: s3 :: s4 :: s5 :: s6 :: s7 :: s8 :: done :: Nil = Enum(UInt(), 10)
    val k_state = Reg(init=UInt(width=data_width))
    when(k_state === done){
        kernel_buffer(0) := io.kernel_in
    }
    .otherwise{
        k_state := k_state + UInt(1)
        kernel_buffer(0) := io.data_in
    }
    kernel_buffer(1) := kernel_buffer(0)
    io.kernel_out := kernel_buffer(1)
}
