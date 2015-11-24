package Core

import Chisel._

// Stole all of this code from some dude on github 
import TidbitsOCM._


class SliceDoubleBuffer(val row_length: Int, data_width: Int, kernel_dim: Int) extends Module {

    val cols = kernel_dim*kernel_dim
    val total_push = row_length*cols
    val total_pops = row_length*cols


    val io = new Bundle {

        val reset = Bool(INPUT)

        val data_in = UInt(INPUT, data_width)

        val slave_push_input = Bool(INPUT)        // master requests the sdb to write data
        val slave_pop_output = Bool(INPUT)        // master requests to read data from sdb

        val slave_can_push_input = Bool(OUTPUT)   // slave can be fed data
        val slave_can_pop_output = Bool(OUTPUT) // slave has valid output data

        val data_out = UInt(OUTPUT, data_width)
        val error = Bool(OUTPUT)
    }

    val slice1 = Module(new SliceBuffer(row_length, data_width, kernel_dim))
    val slice2 = Module(new SliceBuffer(row_length, data_width, kernel_dim))
    slice1.reset := io.reset
    slice2.reset := io.reset

    val bonus_row1 = Module(new RowBuffer(row_length, data_width, 0))
    val bonus_row2 = Module(new RowBuffer(row_length, data_width, 0))

    val push_finished = Reg(init=Bool(false))
    val pop_finished = Reg(init=Bool(true))

    val push_performed = Reg(init=UInt(0, 32))
    val pop_performed = Reg(init=UInt(0, 32))

    val current = Reg(init=Bool(false))

    val bonus_mode :: normal_mode = Enum(UInt(), 2)
    val fill_mode = Reg(init=bonus_mode)

    // defaults
    slice1.io.pop := Bool(false)
    slice1.io.push := Bool(false)
    slice1.io.data_in := UInt(0)

    slice2.io.pop := Bool(false)
    slice2.io.push := Bool(false)
    slice2.io.data_in := UInt(0)

    bonus_row1.io.reset := io.reset
    bonus_row1.io.data_in := io.data_in
    bonus_row1.io.push := Bool(false)
    bonus_row1.io.pop := Bool(false)

    bonus_row2.io.reset := io.reset
    bonus_row2.io.data_in := io.data_in
    bonus_row2.io.push := Bool(false)
    bonus_row2.io.pop := Bool(false)

    io.data_out := UInt(0)
    io.error := Bool(false)
    io.slave_can_push_input := Bool(false)
    io.slave_can_pop_output := Bool(false)

    when(io.reset){
        push_performed := UInt(0)
        pop_performed := UInt(0)
        push_finished := Bool(false)
        pop_finished := Bool(true)
    }

    // Handle input data
    when(io.slave_push_input){
        when(fill_mode === bonus_mode){
            when(current === Bool(false)){
                bonus_row1.io.push := Bool(true)
            }
            .otherwise{
                bonus_row2.io.push := Bool(true)
            }
        }
        .otherwise{
            when(current === Bool(false)){
                slice1.io.push := Bool(true)
                slice1.io.data_in := io.data_in
            }.otherwise{
                slice2.io.push := Bool(true)
                slice2.io.data_in := io.data_in
            }
        }

        push_performed := push_performed + UInt(1)

        when( (push_performed === UInt(total_push - 1) ) ){
            push_finished := Bool(true)
        }
    }

    // Handle output requests
    when(io.slave_pop_output){
        when(current === Bool(true)){
            slice1.io.pop := Bool(true)
            io.data_out := slice1.io.data_out
            pop_performed := pop_performed + UInt(1)
        }.otherwise{
            slice2.io.pop := Bool(false)
            io.data_out := slice2.io.data_out
            pop_performed := pop_performed + UInt(1)
        }
    }


    // Check if pop is done
    when( (pop_performed === UInt(total_pops) ) ){
        pop_finished := Bool(true)
    }

    // Do the switcheroo
    when( pop_finished && push_finished ){
        when(current === Bool(false)){
            current := Bool(true)
        }.otherwise{
            current := Bool(false)
        }
        // Reset counts
        pop_performed := UInt(0)
        push_performed := UInt(0)

        pop_finished := Bool(false)
        push_finished := Bool(false)
    }

    // Decide if data should be requested
    when( !push_finished ){
        io.slave_can_push_input := Bool(true)
    }
    when( !pop_finished ){
        io.slave_can_pop_output := Bool(true)
    }
}


class DoubleBufferTest(c: SliceDoubleBuffer) extends Tester(c) {
} 
