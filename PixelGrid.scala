package Core

import Chisel._
import java.io._
import scala.io.Source


// TODO move ALU out of PG
class PixelGrid(data_width: Int, cols: Int, rows: Int) extends Module {
    val io = new Bundle {
        val data_in = UInt(INPUT, data_width)

        val data_out = UInt(OUTPUT, data_width)
    }

    val pixel_rows = Vec.fill(rows){ Module(new PixelArray(data_width, cols)).io }
    val secondary_muxes = for(i <- 0 until 3) yield Module(new ShiftMux3(data_width, 3, default=(i % 3) )).io
    val pinger = Module(new Orchestrator(cols, rows)).io


    //////////////////////////////////////
    ///////////   GRID
    ///////////
    ///////////
   
    
    // wire input into first row input tree
    for(i <- 0 until cols){
        pixel_rows(0).data_in(i%3) := io.data_in
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
        secondary_muxes(i).shift := pinger.pings(0)
    }


    // Wire data from primary muxes to secondary muxes
    for(i <- 0 until 3){
        for(j <- 0 until 3){
            secondary_muxes(i).data_in(j) := pixel_rows(i).data_out(j)
        }
    }
    


    //////////////////////////////////////
    ///////////   ALUs
    ///////////
    ///////////
    val ALUs = Module(new ALUrow(data_width, cols, rows)).io


    // Wire memory outputs to ALUs
    for(i <- 0 until rows){
        ALUs.data_in(i) := secondary_muxes(i).data_out 
    }

    io.data_out := ALUs.data_out

    // Wire ctrl pings to ALUs
    ALUs.accumulator_flush := pinger.pings(8)
    ALUs.selector_shift_enable := pinger.pings(7)
    ALUs.kernel_in := UInt(1)

}

class PixelGridTest(c: PixelGrid, data_width: Int, cols: Int, rows: Int) extends Tester(c) {

    import scala.collection.mutable.ListBuffer

    val width = 640
    val height = 480
    val sweep_input_depth = 9
    val sweep_output_depth = sweep_input_depth - 2
    val sweeps = height/sweep_output_depth -1
    val inputs_per_sweep = sweep_input_depth*width
    val outputs_per_sweep = sweep_output_depth*width

    // probably not sane :>
    val input_delay = 31
    val output_delay = 9
    
    var total_pixels_collected = 0
    var total_pixels_fed = 0
    var pixels_fed = 0
    var rows_swept = 0
    
    

    def coords_to_val(x: Int, y: Int) : Int = { return y*width + x }

    def feed_row(y: Int, img: Array[Int]) : Array[Int] = {

        var conv = new ListBuffer[Int]()
        var pixels_collected = 0
        for(i <- 0 until width+200){
            for(j <- sweep_input_depth-1 to 0 by -1){
                
                if(j == 0){ poke(c.ALUs.kernel_in, 0) }
                if(j == 1){ poke(c.ALUs.kernel_in, 1) }
                if(j == 2){ poke(c.ALUs.kernel_in, 0) }
                if(j == 3){ poke(c.ALUs.kernel_in, 1) }
                if(j == 4){ poke(c.ALUs.kernel_in, -4) }
                if(j == 5){ poke(c.ALUs.kernel_in, 0) }
                if(j == 6){ poke(c.ALUs.kernel_in, 1) }
                if(j == 7){ poke(c.ALUs.kernel_in, 0) }
                if(j == 8){ poke(c.ALUs.kernel_in, 1) }

                // input data
                if( i*j < inputs_per_sweep ){
                    poke(c.io.data_in, img(coords_to_val(i, j+y)))
                    pixels_fed += 1
                    total_pixels_fed += 1
                }

                // extract if valid
                var out = peek(c.io.data_out)
                if((out.toInt != 0) && (i*j > input_delay) && (pixels_collected < outputs_per_sweep)){
                    pixels_collected += 1
                    total_pixels_collected += 1
                    conv += out.toInt
                }
                step(1)
            }
        }
        rows_swept += 1
        return conv.toArray
    }

    def serialize(rowslices: Array[Int]) : Array[Int] = {
        println("start serialize")
        println(rowslices.length)
        println(width*sweep_output_depth)
        println(total_pixels_collected)
        println(total_pixels_fed)
        var serialized = new ListBuffer[Int]()

        for(i <- 0 until sweep_output_depth){
            for(j <- 0 until width){
                serialized += rowslices(j*sweep_output_depth + i)    
            }
        }
        println("Done serialize")
        return serialized.toArray
    }

    def feed_image(img: Array[Int]) : Array[Int] = {
        var conv = new ListBuffer[Int]()

        for(i <- 1 until sweeps-1){
            var conv_slice = feed_row(i*sweep_output_depth, img)
            conv ++= serialize(conv_slice)
        }
        return conv.toArray
    }
    
    val img = Source.fromFile("Conv/orig_24bit_dump.txt").getLines()
    val img_array = img.next().split(" +").map(_.toInt)

    val conv_img_array = feed_image(img_array)
    val conv_img_string = conv_img_array.mkString("\n")

    new PrintWriter("Conv/chisel_conv.txt"){ write(conv_img_string); close }

    println(total_pixels_collected)
    println(rows_swept)
    
    // poke(c.io.data_in, 1)
    // for(i <- 0 to 60){
    //     peek(c.ALUs.data_out)
    //     step(1)
    //     println("\n")

    //     pw.write(i)
    // }
    // poke(c.io.data_in, 0)
    // for(i <- 0 to 40){
    //     peek(c.ALUs.data_out)
    //     step(1)
    //     println("\n")
    // }
    

}

