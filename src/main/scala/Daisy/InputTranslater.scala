package Core

import Chisel._
import TidbitsOCM._

// Currently only a system to translate data widths.
// Can be enhanced if needed.
// Endianness will fug shit up!
// Only works if output_width > Input_width
// Dont judge me, I know it looks shit
// Also I think the output is still wrong and works backwards. ex dee
class WidthTranslator(input_width: Int, output_width: Int) extends Module{


    // From rosetta code
    def gcd(a: Int, b: Int):Int=if (b==0) a.abs else gcd(b, a%b)
    def lcm(a: Int, b: Int)=(a*b).abs/gcd(a,b)

    var buf_size = lcm(input_width, output_width)

    var total_inputs = buf_size/input_width
    var total_outputs = buf_size/output_width

    println("%d, %d, %d".format(total_inputs, total_outputs, buf_size)) 

    val io = new Bundle {

        val input_valid = Bool(INPUT)
        val input_data = UInt(INPUT, input_width)

        val output_valid = Bool(OUTPUT)
        val output_data = UInt(OUTPUT, output_width)

        val dbg_current = Bool(OUTPUT)

        val dbg_inputs_performed = UInt(OUTPUT)
        val dbg_outputs_performed = UInt(OUTPUT)

        val dbg_inputs_finished = Bool(OUTPUT)
        val dbg_outputs_finished = Bool(OUTPUT)

        val dbg_buf1 = UInt(OUTPUT)
        val dbg_buf2 = UInt(OUTPUT)
    }


    val buffer1 = Reg(init=UInt(0, buf_size))
    val buffer2 = Reg(init=UInt(0, buf_size))

    val inputs_finished = Reg(init=Bool(false))
    val outputs_finished = Reg(init=Bool(true))

    val inputs_performed = Reg(init=UInt(0, 8))
    val outputs_performed = Reg(init=UInt(0, 8))

    val current = Reg(init=Bool(false))

    io.dbg_current := current
    io.dbg_inputs_performed := inputs_performed
    io.dbg_outputs_performed := outputs_performed
    io.dbg_inputs_finished := inputs_finished
    io.dbg_outputs_finished := outputs_finished
    io.dbg_buf1 := buffer1
    io.dbg_buf2 := buffer2

    // fug...
    for(i <- 0 until total_inputs){
        when(io.input_valid){
            when(inputs_performed === UInt(i)){
                when(current){
                    buffer1(  (total_inputs - i)*input_width - 1, (total_inputs - i - 1)*input_width) := io.input_data
                }
                .otherwise{
                    buffer2(  (total_inputs - i)*input_width - 1, (total_inputs - i - 1)*input_width) := io.input_data
                }
            }
        }
    }

    when(io.input_valid){
        when(inputs_performed === UInt(total_inputs - 1)){
            inputs_performed := UInt(0)
            inputs_finished := Bool(true)
        }
        .otherwise{
            inputs_performed := inputs_performed + UInt(1)
        }
    }



    for(i <- 1 until total_outputs){
        when(!outputs_finished){
            io.output_valid := Bool(true)

            when(outputs_performed === UInt(i)){
                when(current){
                    io.output_data := buffer2( (i+1)*output_width - 1, (i)*output_width)
                }
                .otherwise{
                    io.output_data := buffer1( (i+1)*output_width - 1, (i)*output_width)
                }
            }
        }
    }

    when(!outputs_finished){
        when(outputs_performed === UInt(total_outputs)){
            outputs_finished := Bool(true)
            io.output_valid := Bool(false)
        }
        .otherwise{
            outputs_performed := outputs_performed + UInt(1)
        }
    }
    .otherwise{
        io.output_valid := Bool(false)
        io.output_data := UInt(57005)
    }

    when(outputs_finished && inputs_finished){
        current := ~current
        outputs_finished := Bool(false)
        inputs_finished := Bool(false)
        
        when(io.input_valid){
            inputs_performed := UInt(1)

            when(~current){
                buffer1( (total_inputs*input_width) - 1, (total_inputs - 1)*input_width) := io.input_data
            }
            .otherwise{
                buffer2( (total_inputs*input_width) - 1, (total_inputs - 1)*input_width) := io.input_data
            }
        }

        outputs_performed := UInt(1)
        io.output_valid := Bool(true)

        when(current){
            io.output_data := buffer1( output_width - 1, 0)
        }
        .otherwise{
            io.output_data := buffer2( output_width - 1, 0)
        }
    }
}

class TranslatorTest(c: WidthTranslator) extends Tester(c) {
    
    poke(c.io.input_valid, false)
    step(1)
    
    println("Slow translate test")
    for(i <- 1 until 25){
        if(i%2 == 0){
            if(i%6 == 0){
                println("3456")
                poke(c.io.input_data, 13398)
                poke(c.io.input_valid, true)
            }else if(i%4 == 0){
                println("5612")
                poke(c.io.input_data, 22034)
                poke(c.io.input_valid, true)
            }else{
                println("1234")
                poke(c.io.input_data, 4660)
                poke(c.io.input_valid, true)
            }
        }
        else{
            poke(c.io.input_data, 0)
            poke(c.io.input_valid, false)
        }
        println()
        println()
        println("STATE")
        peek(c.io.dbg_current)
        peek(c.io.dbg_buf1)
        peek(c.io.dbg_buf2)
        println("IN")
        peek(c.io.dbg_inputs_performed)
        peek(c.io.dbg_inputs_finished)
        peek(c.io.input_valid)
        peek(c.io.input_data)
        println("OUT")
        peek(c.io.dbg_outputs_performed)
        peek(c.io.dbg_outputs_finished)
        peek(c.io.output_valid)
        peek(c.io.output_data)
        println()
        step(1)
        println()
    }
}
