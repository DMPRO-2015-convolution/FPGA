package Core

import Chisel._
import TidbitsOCM._

// Buffers output which can be fed out at whatever pace
class OutputHandler(row_length: Int, pixel_data_width: Int, output_data_width: Int, img_height: Int, kernel_dim: Int) extends Module {
 
    val entries = (row_length*pixel_data_width)/output_data_width

    val mantle_width = (kernel_dim)/2
    val valid_rows_per_image = img_height - (mantle_width*2)
    val valid_pixels_per_row = row_length - (mantle_width*2)
    val slices_per_image = valid_rows_per_image / (kernel_dim*kernel_dim - 2)

    println("Slices: %d".format(slices_per_image))
    
    val io = new Bundle {

        val input_valid = Bool(INPUT)
        val data_in = UInt(INPUT, pixel_data_width)

        val request_output = Bool(INPUT)
        val output_ready = Bool(OUTPUT)
        val data_out = UInt(OUTPUT, output_data_width)

    }

    val translator = Module(new twentyfour_sixteen())
    translator.io.req_in := Bool(false)
    translator.io.req_out := Bool(false)

    val chip_sel = Reg(init=Bool(false)) 

    val queue_start = Reg(init=UInt(row_length, 32))
    val queue_end = Reg(init=UInt(row_length, 32))

    val bram = Module(new DualPortBRAM(addrBits=log2Up(entries), dataBits=pixel_data_width)).io 

    val writePort = bram.ports(0)
    val readPort = bram.ports(1)

    writePort.req.writeData := io.data_in
    writePort.req.writeEn := Bool(false)
    writePort.req.addr := queue_start

    readPort.req.writeData := UInt(0)
    readPort.req.writeEn := Bool(false)
    readPort.req.addr := queue_end


    when(io.input_valid){
        queue_start := queue_start - UInt(1)
        writePort.req.writeEn := Bool(true)
    }

    when(translator.io.rdy_in){
        translator.io.req_in := Bool(true)
        queue_end := queue_end - UInt(1)
    }

    translator.io.d_in := readPort.rsp.readData
    translator.io.req_out := io.request_output

    io.data_out := translator.io.d_out
}

class OutputHandlerTest(c: OutputHandler) extends Tester(c) {
    
}
