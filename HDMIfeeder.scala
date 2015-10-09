package Core

import Chisel._

class HDMIfeeder(data_width: Int, address_width: Int) extends Module{
    val io = new Bundle {
        val sram_0_bus_control = Bool(INPUT)
        val sram_1_bus_control = Bool(INPUT)
        val read_address = UInt(INPUT, address_width)

        val sram_0_bus_data = UInt(OUTPUT, data_width)
        val sram_1_bus_data = UInt(OUTPUT, data_width)
        val ram_request_accepted = Bool(OUTPUT)
    }

    // clock sources and reset?
    // video_unit in has clock_sys, at least 25mhz+some, clock_25, clock_125, clock_125n and reset

    when(io.sram_1_bus_control){
       // set ram_request_accepted outside? 
        io.ram_request_accepted := True
        io.sram_1_bus_data := sram1(io.read_address)
    }
    // sufficient with otherwise?
    when(io.sram_0_bus_control){
       // set ram_request_accepted outside? 
        io.ram_request_accepted := True
        io.sram_0_bus_data := sram0(io.read_address)
    }
}
