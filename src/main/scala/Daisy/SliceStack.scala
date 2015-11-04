package Core

import Chisel._
import TidbitsOCM._

// A stack which should synthesize to BRAM. Should be used to store slices.
// This stack only serves to buffer slices of our image which is done with two buffers
// thus we dont need to take too many precautions.

class RowBuffer(entries: Int, data_width: Int) extends Module {
    
    val io = new Bundle {
        val data_in = UInt(INPUT, data_width)
        val push = Bool(INPUT)
        val pop = Bool(INPUT)

        val reset = Bool(INPUT)

        val data_out = UInt(OUTPUT, data_width)
    }

    val stack_top = Reg(init=UInt(0, width=log2Up(entries)))
    val bram = Module(new DualPortBRAM(addrBits=log2Up(entries), dataBits=data_width)).io 

    val writePort = bram.ports(0)
    val readPort = bram.ports(1)
    writePort.req.writeData := io.data_in
    writePort.req.writeEn := Bool(false)
    writePort.req.addr := stack_top

    readPort.req.writeData := io.data_out
    readPort.req.writeEn := Bool(false)
    readPort.req.addr := stack_top

    when(io.pop){
        when(stack_top >= UInt(0)){
            stack_top := stack_top + UInt(1)
            writePort.req.writeEn := Bool(true)
        }
    }
    when(io.push){
        when(stack_top < UInt(entries)){
            stack_top := stack_top - UInt(1)
        }
    }
}

class SliceBuffer(row_length: Int, data_width: Int, kernel_dim: Int) extends Module {
    
    val io = new Bundle {
        val data_in = UInt(INPUT, data_width)
        val push = Bool(INPUT)
        val pop = Bool(INPUT)

        val reset = Bool(INPUT)

        val data_out = UInt(OUTPUT, data_width)
    }
      
    val row_buffers = Vec.fill(kernel_dim){ Module(new RowBuffer(row_length, data_width)).io }
    val current_row = Reg(init=UInt(0, kernel_dim))

    when(io.push){
        when(current_row <= UInt(kernel_dim)){
            current_row := current_row + UInt(1)    
        }
        .otherwise{
            current_row := UInt(0)
        }
    }

    when(io.pop){
        when(current_row > UInt(0)){
            current_row := current_row - UInt(1)    
        }
        .otherwise{
            current_row := UInt(kernel_dim)
        }
    }
    row_buffers(current_row).data_in := io.data_in
    row_buffers(current_row).push    := io.push
    row_buffers(current_row).pop     := io.data_in
    io.data_out := row_buffers(current_row).data_out
}

class SliceDoubleBuffer(img_width: Int, data_width: Int, kernel_dim: Int) extends Module {

    val total_reads = img_width*kernel_dim
    val total_writes = img_width*kernel_dim

    val io = new Bundle {
        val data_in = UInt(INPUT, data_width)
        val data_write = Bool(INPUT)
        val data_read = Bool(INPUT)

        val reads_completed = Bool(OUTPUT)
        val writes_completed = Bool(OUTPUT)

        val data_out = UInt(INPUT, data_width)
        val error = Bool(OUTPUT)
    }

    val slice1 = Module(new SliceBuffer(img_width, data_width, kernel_dim))
    val slice2 = Module(new SliceBuffer(img_width, data_width, kernel_dim))

    val reads_done = Reg(init=Bool(false))
    val writes_done = Reg(init=Bool(false))

    val reads = Reg(init=UInt(0))
    val writes = Reg(init=UInt(0))

    val current = Reg(init=UInt(0))

    // defaults
    slice1.io.pop := Bool(false)
    slice2.io.pop := Bool(false)
    slice1.io.push := Bool(false)
    slice2.io.push := Bool(false)
    io.error := Bool(false)

    when(io.data_read){
        when(current === UInt(0)){
            slice1.io.pop := Bool(true)
            io.data_out := slice1.io.data_out
            reads := reads + UInt(1)
        }.otherwise{
            slice2.io.pop := Bool(true)
            io.data_out := slice1.io.data_out
            reads := reads + UInt(1)
        }
    }

    when(io.data_write){
        when(current === UInt(0)){
            slice1.io.push := Bool(true)
            slice1.io.data_in := io.data_in
            writes := writes + UInt(1)
        }.otherwise{
            slice2.io.push := Bool(true)
            slice2.io.data_in := io.data_in
            writes := writes + UInt(1)
        }
    }

    // Check if reads/writes are finished
    when( (reads_done === UInt(total_reads)) ){
        io.reads_completed := Bool(true)
    }
    when( (writes_done === UInt(total_writes)) ){
        io.writes_completed := Bool(true)
    }

    // Do the switcheroo
    when( (reads_done === UInt(total_reads)) && (writes_done === UInt(total_writes)) ){
        when(current === UInt(0)){
            current := UInt(1)
        }.otherwise{
            current := UInt(0)
        }
        reads_done := UInt(0)
        writes_done := UInt(0)
    }

    // Should never happen, but who am I kidding?
    when( (reads_done > UInt(total_reads)) || (writes_done > UInt(total_writes)) ){
        io.error := Bool(true)
    }
}
