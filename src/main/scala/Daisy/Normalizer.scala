package Core

import Chisel._

class Normalizer(data_width: Int) extends Module {

    val io = new Bundle {

        val reset = Bool(INPUT)
        val programming_mode = Bool(INPUT)
        // val operand = Bool(INPUT)

        val red_in = SInt(INPUT, 8)
        val green_in = SInt(INPUT, 8)
        val blue_in = SInt(INPUT, 8)

        val data_out = UInt(OUTPUT, data_width)

    }

    io.data_out := UInt(0)

    io.data_out(7, 0) := io.red_in
    io.data_out(15, 8) := io.blue_in
    io.data_out(23, 16) := io.green_in

}
