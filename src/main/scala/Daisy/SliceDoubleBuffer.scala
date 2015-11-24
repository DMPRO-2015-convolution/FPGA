package Core

import Chisel._

// Stole all of this code from some dude on github 
import TidbitsOCM._


class SliceDoubleBuffer(val row_length: Int, data_width: Int, kernel_dim: Int) extends Module {

    val cols = kernel_dim*kernel_dim
    val total_push = row_length*(cols - 2)
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

    val slice1_bonus_row1 = Module(new RowBuffer(row_length, data_width, 0))
    val slice1_bonus_row2 = Module(new RowBuffer(row_length, data_width, 0))

    val slice2_bonus_row1 = Module(new RowBuffer(row_length, data_width, 0))
    val slice2_bonus_row2 = Module(new RowBuffer(row_length, data_width, 0))

    val push_finished = Reg(init=Bool(false))
    val pop_finished = Reg(init=Bool(true))

    val push_performed = Reg(init=UInt(0, 32))
    val pop_performed = Reg(init=UInt(0, 32))

    val current = Reg(init=Bool(false))

    val normal_mode :: init_mode_1 :: init_mode_2 :: Nil = Enum(UInt(), 3)
    val fill_mode = Reg(init=init_mode_1)

    val bonus_pop = Reg(init=UInt(0))

    // defaults
    slice1.io.pop := Bool(false)
    slice1.io.push := Bool(false)
    slice1.io.data_in := UInt(0)
    slice1.io.reset := io.reset

    slice2.io.pop := Bool(false)
    slice2.io.push := Bool(false)
    slice2.io.data_in := UInt(0)
    slice2.io.reset := io.reset

    slice1_bonus_row1.io.reset := io.reset
    slice1_bonus_row1.io.data_in := io.data_in
    slice1_bonus_row1.io.push := Bool(false)
    slice1_bonus_row1.io.pop := Bool(false)

    slice1_bonus_row2.io.reset := io.reset
    slice1_bonus_row2.io.data_in := io.data_in
    slice1_bonus_row2.io.push := Bool(false)
    slice1_bonus_row2.io.pop := Bool(false)

    slice2_bonus_row1.io.reset := io.reset
    slice2_bonus_row1.io.data_in := io.data_in
    slice2_bonus_row1.io.push := Bool(false)
    slice2_bonus_row1.io.pop := Bool(false)

    slice2_bonus_row2.io.reset := io.reset
    slice2_bonus_row2.io.data_in := io.data_in
    slice2_bonus_row2.io.push := Bool(false)
    slice2_bonus_row2.io.pop := Bool(false)

    io.data_out := UInt(0)
    io.error := Bool(false)
    io.slave_can_push_input := Bool(false)
    io.slave_can_pop_output := Bool(false)

    when(io.reset){
        push_performed := UInt(0)
        pop_performed := UInt(0)
        push_finished := Bool(false)
        pop_finished := Bool(true)
        fill_mode := init_mode_1
    }

    // Handle input data
    when(io.slave_push_input){

        // We first fill the two bonus buffers
        when(!(fill_mode === normal_mode)){

            when(fill_mode === init_mode_1){
                slice2_bonus_row1.io.push := Bool(true)
                push_performed := push_performed + UInt(1)
                when( (push_performed === UInt(row_length - 1) ) ){
                    push_performed := UInt(0)
                    fill_mode := init_mode_2
                }
            }
            .otherwise{
                slice2_bonus_row2.io.push := Bool(true)
                push_performed := push_performed + UInt(1)
                when( (push_performed === UInt(row_length - 1) ) ){
                    push_performed := UInt(0)
                    fill_mode := normal_mode
                }
            }
        }
        .otherwise{

            // In normal mode we fill bonus buffers when needed
            when(current === Bool(false)){
                when(slice1.io.push_row === UInt(6)){
                    slice1_bonus_row1.io.push := Bool(true)
                }.elsewhen(slice1.io.push_row === UInt(7)){
                    slice1_bonus_row2.io.push := Bool(true)
                }
                slice1.io.push := Bool(true)
                slice1.io.data_in := io.data_in
            }.otherwise{
                when(slice2.io.push_row === UInt(6)){
                    slice2_bonus_row1.io.push := Bool(true)
                }.elsewhen(slice2.io.push_row === UInt(7)){
                    slice2_bonus_row2.io.push := Bool(true)
                }
                slice2.io.push := Bool(true)
                slice2.io.data_in := io.data_in
            }

            push_performed := push_performed + UInt(1)

            when( (push_performed === UInt(total_push - 1) ) ){
                push_finished := Bool(true)
            }
        }
    }


    // Handle output requests
    when(io.slave_pop_output){
        when(current === Bool(true)){
            when(bonus_pop === UInt(0)){

                bonus_pop := UInt(1)
                slice2_bonus_row1.io.pop := Bool(true)
                io.data_out := slice2_bonus_row1.io.data_out

            }.elsewhen(bonus_pop === UInt(1)){

                bonus_pop := UInt(2)
                slice2_bonus_row2.io.pop := Bool(true)
                io.data_out := slice2_bonus_row2.io.data_out

            }
            .otherwise{
                slice1.io.pop := Bool(true)
                io.data_out := slice1.io.data_out
                when(slice1.io.pop_row === UInt(6)){
                    bonus_pop := UInt(0)
                }
            }
            pop_performed := pop_performed + UInt(1)

        }.otherwise{
            when(bonus_pop === UInt(0)){

                bonus_pop := UInt(1)
                slice1_bonus_row1.io.pop := Bool(true)
                io.data_out := slice2_bonus_row1.io.data_out

            }.elsewhen(bonus_pop === UInt(1)){

                io.data_out := slice2_bonus_row1.io.data_out
                bonus_pop := UInt(2)
                slice1_bonus_row2.io.pop := Bool(true)

            }
            .otherwise{
                slice2.io.pop := Bool(true)
                io.data_out := slice2.io.data_out
                when(slice2.io.pop_row === UInt(6)){
                    bonus_pop := UInt(0)
                }
            }
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

    poke(c.io.slave_pop_output, false)
    poke(c.io.reset, false)
    for(i <- 0 until 90){
        poke(c.io.data_in, (i/10) + 1)
        poke(c.io.slave_push_input, true)
        println()
        peek(c.io)
        println()
        peek(c.slice1.io)
        println()
        peek(c.fill_mode)
        step(1)
    }

    peek(c.io)
    peek(c.slice1.io)
    poke(c.io.slave_push_input, false)
    step(1)
    peek(c.io)
    peek(c.slice1.io)

    step(1)
    poke(c.io.slave_pop_output, true)
    poke(c.io.slave_push_input, true)

    for(i <- 0 until 70){
        println("-----------------------")
        poke(c.io.data_in, (i/10) + 1)
        poke(c.io.slave_push_input, true)
        println()
        peek(c.io)
        println()
        peek(c.slice1.io)
        println()
        peek(c.slice2.io)
        println()
        peek(c.fill_mode)
        println()
        peek(c.io.data_out)
        println()
        peek(c.bonus_pop)
        println("-----------------------")
        step(1)
    }

}
