package Core

import Chisel._
import java.io._
import scala.io.Source


// TODO move ALU out of PG
// TODO kernel in should also be moved out at some point
class PixelGrid(data_width: Int, cols: Int, rows: Int) extends Module {
    val io = new Bundle {
        val data_in = UInt(INPUT, data_width)

        val data_out = UInt(OUTPUT, data_width)
    }

    val pixel_rows = Vec.fill(rows){ Module(new PixelArray(data_width, cols)).io }
    val input_tree = Vec.fill(3){ Reg(init=UInt(0, width = data_width)) }
    val shift_muxes = for(i <- 0 until 3) yield Module(new ShiftMux3(data_width, 3, default=((i + 1) % 3))).io
    val pinger = Module(new Orchestrator(cols, rows)).io


    //////////////////////////////////////
    ///////////   GRID
    ///////////
    ///////////
    
    // wire input into first row input tree
    for(i <- 0 until cols){
        pixel_rows(0).data_in(i%3) := input_tree(i/3)
    }


    // Wire io between rows
    for(i <- 1 until cols/3){
        for(j <- 0 until rows){
            pixel_rows(i).data_in(j) := pixel_rows(i-1).data_out(j)
        }
    }


    // wire primary mux enablers
    pixel_rows(0).ping_read := pinger.pings(1)
    pixel_rows(0).ping_mux := pinger.pings(2)
    pixel_rows(1).ping_read := pinger.pings(3)
    pixel_rows(1).ping_mux := pinger.pings(4)
    pixel_rows(2).ping_read := pinger.pings(5)
    pixel_rows(2).ping_mux := pinger.pings(6)
    

    // Wire shift signals to secondary muxes
    for(i <- 0 until 3){
        shift_muxes(i).shift := pinger.pings(0)
    }


    // Wire data from primary muxes to secondary muxes
    for(i <- 0 until 3){
        for(j <- 0 until 3){
            shift_muxes(i).data_in(j) := pixel_rows(i).data_out(j)
        }
    }
    


    //////////////////////////////////////
    ///////////   ALUs
    ///////////
    ///////////
    val ALUs = Module(new ALUrow(data_width, cols, rows)).io


    // Wire memory outputs to ALUs
    for(i <- 0 until rows){
        ALUs.data_in(i) := shift_muxes(i).data_out 
    }

    io.data_out := ALUs.data_out

    // Wire ctrl pings to ALUs
    ALUs.accumulator_flush := pinger.pings(8)
    ALUs.selector_shift_enable := pinger.pings(7)


    // Ghetto kernel shit
    val kernel_buffer = Vec.fill(2){ Reg(init=UInt(0, width=data_width)) }
    val s0 :: s1 :: s2 :: s3 :: s4 :: s5 :: s6 :: s7 :: s8 :: done :: Nil = Enum(UInt(), 10)
    val k_state = Reg(init=UInt(width=data_width))

    when(k_state === done){
        for(i <- 0 until 3){ 
            input_tree(i) := io.data_in
            kernel_buffer(0) := ALUs.kernel_out
        }
    }
    .otherwise{
        k_state := k_state + UInt(1)
        kernel_buffer(0) := io.data_in
    }

    kernel_buffer(1) := kernel_buffer(0)
    ALUs.kernel_in := kernel_buffer(1)
}

class PixelGridTest(c: PixelGrid, data_width: Int, cols: Int, rows: Int) extends Tester(c) {

    poke(c.io.data_in, 1)
    for(i <- 0 to 71){
        peek(c.pinger.pings(8))
        peek(c.ALUs)
        peek(c.pixel_rows(0).data_out)
        peek(c.pixel_rows(1).data_out)
        peek(c.pixel_rows(2).data_out)
        println("\n")
        peek(c.io.data_out)
        step(1)
        println("\n")
    }
}


class image(c: PixelGrid, data_width: Int, cols: Int, rows: Int) extends Tester(c) {
    import scala.collection.mutable.ListBuffer

    val width = 80
    val height = 80
    val sweep_input_depth = 9
    val sweep_output_depth = sweep_input_depth - 2
    val sweeps = height/sweep_output_depth
    val inputs_per_sweep = sweep_input_depth*width
    val outputs_per_sweep = sweep_output_depth*width

    // probably not sane :>
    
    var total_pixels_collected = 0
    var total_pixels_fed = 0
    var pixels_fed = 0
    var rows_swept = 0



    def feed_row(y_offset: Int, img: Array[Array[Int]], conv_img: Array[Array[Int]]) : Unit = {
        var count = 0
        var collected = new ListBuffer[Int]()

        for(x <- 0 until width){
            for(y <- 0 until sweep_input_depth){
                print("[")
                print(x)
                print("][")
                print(y + y_offset)
                print("]\n")

                poke(c.io.data_in, img(y + y_offset)(x))

                var out = peek(c.io.data_out).toInt

                if(!(y == 2 || y == 3)){
                    collected += out
                }

                step(1)
            }
        }

        println(collected.length)
        for(x <- 1 until width - 1){
            for(y <- 0 until 7){
                if(y == 1 || y == 0 || y == 2){
                    conv_img(y + 4 + y_offset + 1)(x) = collected(x*7 + y)
                }
                else{
                    conv_img(y - 3 + y_offset + 1)(x+1) = collected(x*7 + y)
                }
            }
        }
    }

    val flat_array = Source.fromFile("Conv/tiny_pattern.txt").getLines.toArray.map(_.toInt)
    val img_array = Array.ofDim[Int](height, width)
    var convoluted = Array.ofDim[Int](height, width)
    for(y <- 0 until height){
        for(x <- 0 until width){
            img_array(y)(x) = flat_array(x + width*y) 
            convoluted(y)(x) = 1
        }
    }

    def feed_img(img: Array[Array[Int]], conv_img: Array[Array[Int]]) : Unit = {
        for(i <- 0 until sweeps){
            feed_row(i*7, img, conv_img)
        }
    }

    // feed_row(0, img_array, convoluted)
    feed_img(img_array, convoluted)

    import java.io._
    val w = new PrintWriter("Conv/tiny_disaster.txt")
    for(y <- 0 until height){
        val s = convoluted(y).mkString("\n")
        w.write(s)
        w.write("\n")
    }
}
