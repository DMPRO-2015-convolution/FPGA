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
    val secondary_muxes = for(i <- 0 until 3) yield Module(new ShiftMux3(data_width, 3, ((i) % 3) )).io
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
    

    // Wire grid data out from secondary muxes
    // for(i <- 0 until cols/3){
    //    io.data_out(i) := secondary_muxes(i).data_out
    //}


    //////////////////////////////////////
    ///////////   ALUs
    ///////////
    ///////////
    val ALUs = Module(new ALUrow(data_width, cols)).io


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

    def coords_to_val(x: Int, y: Int, width: Int) : Int = {
        return y*width + x
    }

    def feed_row(y: Int, img: Array[Int], width: Int, height: Int) : Array[Int] = {
        var conv = new ListBuffer[Int]()
        for(i <- 0 until width){
            for(j <- 0 until 9){
                val d = coords_to_val(i, j, width)
                if(i*j < width*9){
                    poke(c.io.data_in, d)
                }
                var out = peek(c.io.data_out)
                if((out.toInt != 0) && (i*j > 31) && (i*j < 9*width + 9)){
                    conv += out.toInt
                }
                step(1)
            }
        }
        return conv.toArray
    }

    def serialize(rowslices: Array[Int], col_len: Int) : Array[Int] = {
        var serialized = new ListBuffer[Int]()
        for(i <- 0 until col_len){
            for(j <- 0 until rowslices.length/col_len){
                serialized += rowslices(j*col_len + i)    
            }
        }
        return serialized.toArray
    }

    def feed_image(img: Array[Int], width: Int, height: Int) : Array[Int] = {
        var conv = new ListBuffer[Int]()
        for(i <- 0 until height/9 - 1){
            var conv_slice = feed_row(i*9, img, width, height)
            conv ++= serialize(conv_slice, 7)
        }
        return conv.toArray
    }
    
    val img = Source.fromFile("Conv/orig_24bit_dump.txt").getLines()
    val img_array = img.next().split(" +").map(_.toInt)

    val conv_img_array = feed_image(img_array, 512, 512)
    val conv_img_string = conv_img_array.mkString(" ")

    new PrintWriter("Conv/chisel_conv.txt"){ write(conv_img_string); close }

    /*
    poke(c.io.data_in, 1)
    for(i <- 0 to 60){
        peek(c.ALUs.data_out)
        step(1)
        println("\n")

        pw.write(i)
    }
    poke(c.io.data_in, 0)
    for(i <- 0 to 40){
        peek(c.ALUs.data_out)
        step(1)
        println("\n")
    }
    */

}

