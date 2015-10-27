package Core
import Chisel._
import java.io._
import scala.collection.mutable.ListBuffer
import scala.io.Source

class Snapshot(c: PixelGrid) extends Tester(c) {

    val width = 80
    val height = 80
    val sweep_input_depth = 9
    val sweep_output_depth = sweep_input_depth - 2
    val sweeps = height/sweep_output_depth -1
    val inputs_per_sweep = sweep_input_depth*width
    val outputs_per_sweep = sweep_output_depth*width

    // probably not sane :>
    val first_output = 29
    
    var total_pixels_collected = 0
    var total_pixels_fed = 0
    var pixels_fed = 0
    var rows_swept = 0

    def push_kernel(kernel: Array[Int]) : Unit = {
        poke(c.io.data_in, kernel(0))
        step(1)
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
    }

    def feed_row(y_offset: Int, img: Array[Array[Int]], conv_img: Array[Array[Int]]) : Unit = {

        
        var data_tree = new ListBuffer[Array[Int]]() 

        var row1 = new ListBuffer[Array[Int]]() 
        var row2 = new ListBuffer[Array[Int]]() 
        var row3 = new ListBuffer[Array[Int]]() 

        var row_out_1 = new ListBuffer[Array[Int]]() 
        var row_out_2 = new ListBuffer[Array[Int]]() 
        var row_out_3 = new ListBuffer[Array[Int]]() 

        var shift_mux = new ListBuffer[Array[Int]]() 
        var shift_mux_in1 = new ListBuffer[Array[Int]]() 
        var shift_mux_in2 = new ListBuffer[Array[Int]]() 
        var shift_mux_in3 = new ListBuffer[Array[Int]]() 

        var selected = new ListBuffer[Array[Int]]() 

        var pings = new ListBuffer[Array[Int]]() 

        var kernels = new ListBuffer[Array[Int]]() 
        var ALU_in = new ListBuffer[Array[Int]]() 

        var meta = new ListBuffer[Int]() 

        var accumulators = new ListBuffer[Array[Int]]() 
        
        var output = new ListBuffer[Int]() 

        for(x <- 0 until height){
            for(y <- 0 until sweep_input_depth){

                poke(c.io.data_in, 1)
                var selected_slice = ListBuffer[BigInt]()
                var mux_slice = ListBuffer[BigInt]()
                 
                data_tree += peek(c.pixel_rows(0).data_in).map(_.toInt)
 
                row1 += peek(c.pixel_rows(0).dbg_reg_contents).map(_.toInt)
                row2 += peek(c.pixel_rows(1).dbg_reg_contents).map(_.toInt)
                row3 += peek(c.pixel_rows(2).dbg_reg_contents).map(_.toInt)

                mux_slice += peek(c.shift_muxes(0).dbg_enable)
                mux_slice += peek(c.shift_muxes(1).dbg_enable)
                mux_slice += peek(c.shift_muxes(2).dbg_enable)
                shift_mux += mux_slice.toArray.map(_.toInt)
 
                shift_mux_in1 += peek(c.shift_muxes(0).data_in).map(_.toInt)
                shift_mux_in2 += peek(c.shift_muxes(1).data_in).map(_.toInt)
                shift_mux_in3 += peek(c.shift_muxes(2).data_in).map(_.toInt)

                row_out_1 += peek(c.pixel_rows(0).data_out).map(_.toInt)
                row_out_2 += peek(c.pixel_rows(1).data_out).map(_.toInt)
                row_out_3 += peek(c.pixel_rows(2).data_out).map(_.toInt)
 
                pings += peek(c.pinger.pings).map(_.toInt)
                
                selected_slice += peek(c.shift_muxes(0).data_out)
                selected_slice += peek(c.shift_muxes(1).data_out)
                selected_slice += peek(c.shift_muxes(2).data_out)
                selected += selected_slice.toArray.map(_.toInt)
 
                ALU_in += peek(c.ALUs.dbg_multipliers_in).map(_.toInt)
 
                kernels += peek(c.ALUs.dbg_kernel_out).map(_.toInt)
                accumulators += peek(c.ALUs.dbg_accumulators_out).map(_.toInt)

 
                var out = peek(c.io.data_out).toInt
                output += out

                step(1)
            }
        }

        
        var state = Array.ofDim[Int](9, 9)

        for(i <- 0 until 300){

            print("\n\n")
            print("STEP: ")
            print(i)
            print(", mod STEP: ")
            print((i) % 9)
            print("\n\n")

            state = draw_pings(state, pings(i))

            print("\n\nINPUT TREE      [")
            print(data_tree(i).reverse.mkString("]         ["))

            print("]\n\nROW 1       [")
            print (row1(i).reverse.mkString("] ["))

            print("]\n\nOUT 1           [")
            print (row_out_1(i).reverse.mkString("]         ["))

            print("]\n\nROW 2       [")
            print (row2(i).reverse.mkString("] ["))

            print("]\n\nOUT 2           [")
            print (row_out_2(i).reverse.mkString("]         ["))

            print("]\n\nROW 3       [")
            print (row3(i).reverse.mkString("] ["))

            print("]\n\nOUT 3           [")
            print (row_out_3(i).reverse.mkString("]         ["))
            
            print("]\n\n")
            print("\n\nSHIFT MUX STATUS:      [")
            print (shift_mux(i).mkString("]   ["))

            print("]\n\n")
            print("\n\nSHIFT MUX IN:      [")
            print (shift_mux_in1(i).reverse.mkString("]["))
            print("] - [")
            print (shift_mux_in2(i).reverse.mkString("]["))
            print("] - [")
            print (shift_mux_in3(i).reverse.mkString("]["))
            print("]\n\n")

            print("\n\nSELECTED    :   [")
            print (selected(i).mkString("]         ["))

            print("]\n\nALU_IN      :   [")
            print (ALU_in(i).reverse.mkString("] ["))

            print("]\n\nKERNELS     :   [")
            print (kernels(i).reverse.mkString("] ["))

            print("]\n\nACCUMULATORS:   [")
            print (accumulators(i).reverse.mkString("] ["))

            print("]\n\nOUTPUT:             *~>>>>  ")
            print (output(i))
            print("  <<<<~*")
            print("\n\n")
            print("\n\n")
            print("\n\n")

        }
        
    }

    def draw_pings(state: Array[Array[Int]], pings: Array[Int]) : Array[Array[Int]] = {
        print("   ")
        for(i <- 0 until state.length){
            print(" "); print(i); print(" ")
        }
        println()
        for(i <- 0 until pings.length){
            print(i)
            print(": ")
            for(j <- 0 until state.length){
                if(state(i)(j) == 1){
                    print(" # ")
                }
                else{ print(" . ") }
            }
            println()
            for(j <- state.length-2 to 0 by - 1){
                val temp = state(i)(j)
                state(i)(j) = state(i)(j+1)
                state(i)(j+1) = temp
            }
            state(i)(0) = pings(8-i)
        } 
        return state
    }

    val kernel = Array[Int](1, 0, 1, 0, -4, 0, 1, 0, 1)
    push_kernel(kernel)

    val flat_array = Source.fromFile("Conv/tiny_pattern.txt").getLines.toArray.map(_.toInt)
    val img_array = Array.ofDim[Int](height, width)
    var convoluted = Array.ofDim[Int](height, width)

    for(y <- 0 until height){
        for(x <- 0 until width){
            img_array(y)(x) = flat_array(x + width*y) 
            convoluted(y)(x) = 1
        }
    }

    feed_row(0, img_array, convoluted)
}
