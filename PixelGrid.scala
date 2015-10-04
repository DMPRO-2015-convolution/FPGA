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


class image(c: PixelGrid, data_width: Int, cols: Int, rows: Int) extends Tester(c) {
    import scala.collection.mutable.ListBuffer

    val width = 640
    val height = 480
    val sweep_input_depth = 9
    val sweep_output_depth = sweep_input_depth - 2
    val sweeps = height/(sweep_output_depth)
    val inputs_per_sweep = sweep_input_depth*width
    val outputs_per_sweep = sweep_output_depth*width

    var first_valid = 30
    
    var total_pixels_collected = 0
    var total_pixels_fed = 0
    var pixels_fed = 0
    var rows_swept = 0


    def push_kernel(kernel: Array[Int]) : Unit = {
        poke(c.io.data_in, kernel(1))
        step(1)
        poke(c.io.data_in, kernel(2))
        step(1)
        poke(c.io.data_in, kernel(3))
        step(1)
        poke(c.io.data_in, kernel(4))
        step(1)
        poke(c.io.data_in, kernel(5))
        step(1)
        poke(c.io.data_in, kernel(6))
        step(1)
        poke(c.io.data_in, kernel(7))
        step(1)
        poke(c.io.data_in, kernel(8))
        step(1)
        poke(c.io.data_in, kernel(0))
        step(1)
    }

    def feed_row(y_offset: Int, img: Array[Array[Int]], conv_img: Array[Array[Int]]) : Unit = {
        var count = 0

        for(x <- 0 until width){
            var collected = new ListBuffer[Int]()
            for(y <- 0 until sweep_input_depth){
                poke(c.io.data_in, img(y + y_offset)(x))
                var out = peek(c.io.data_out).toInt

                if(x+(y*9) >= 30){
                    if(out != 0){
                        collected += out
                    }
                }
                step(1)
            }
            if(collected.length == 7 && x < 639){
                for(y <- 0 until 7){
                    var ty = (y + 6) % 7 
                    var tx = x
                    if(ty == 6){ tx -= 1 }
                    conv_img(ty+y_offset)(tx) = collected(y)
                }
            }
        }
    }

    val flat_array = Source.fromFile("Conv/Daisy24dump.txt").getLines.toArray.map(_.toInt)
    val img_array = Array.ofDim[Int](height, width)
    var convoluted = Array.ofDim[Int](height, width)
    for(y <- 0 until height){
        for(x <- 0 until width){
            img_array(y)(x) = flat_array(x + width*y) 
            convoluted(y)(x) = 0
        }
    }

    def feed_img(img: Array[Array[Int]], conv_img: Array[Array[Int]]) : Unit = {
        for(i <- 0 until sweeps-1){
            feed_row(i*7, img, conv_img)
        }
    }


    val kernel = Array[Int](1, 0, 0, 0, 0, 0, 0, 0, 1)
    push_kernel(kernel)

    // feed_row(0, img_array, convoluted)
    feed_img(img_array, convoluted)

    import java.io._
    val w = new PrintWriter("Conv/big_disaster.txt")
    for(y <- 0 until height){
        val s = convoluted(y).mkString("\n")
        w.write(s)
        w.write("\n")
    }
}
