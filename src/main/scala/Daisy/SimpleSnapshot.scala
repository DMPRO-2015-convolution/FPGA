package Core
import Chisel._
import java.io._
import scala.collection.mutable.ListBuffer
import scala.io.Source

// class SimpleSnap(c: Tile) extends Tester(c) {
// 
//     def push_kernel(kernel: Array[Int]) : Unit = {
//         poke(c.io.active, true)
//         poke(c.io.data_in, kernel(0))
//         step(1)
//         poke(c.io.data_in, kernel(1))
//         step(1)
//         poke(c.io.data_in, kernel(2))
//         step(1)
//         poke(c.io.data_in, kernel(3))
//         step(1)
//         poke(c.io.data_in, kernel(4))
//         step(1)
//         poke(c.io.data_in, kernel(5))
//         step(1)
//         poke(c.io.data_in, kernel(6))
//         step(1)
//         poke(c.io.data_in, kernel(7))
//         step(1)
//         poke(c.io.data_in, kernel(8))
//         step(1)
//     }
// 
//     def feed_row(runs: Int){
// 
//         var data_tree = new ListBuffer[Array[Int]]() 
// 
//         var row1 = new ListBuffer[Array[Int]]() 
//         var row2 = new ListBuffer[Array[Int]]() 
//         var row3 = new ListBuffer[Array[Int]]() 
// 
//         var row_out_1 = new ListBuffer[Array[Int]]() 
//         var row_out_2 = new ListBuffer[Array[Int]]() 
//         var row_out_3 = new ListBuffer[Array[Int]]() 
// 
//         var shift_mux = new ListBuffer[Array[Int]]() 
//         var shift_mux_in1 = new ListBuffer[Array[Int]]() 
//         var shift_mux_in2 = new ListBuffer[Array[Int]]() 
//         var shift_mux_in3 = new ListBuffer[Array[Int]]() 
// 
//         var selected = new ListBuffer[Array[Int]]() 
// 
//         var pings = new ListBuffer[Array[Int]]() 
// 
//         var kernels = new ListBuffer[Array[Int]]() 
//         var ALU_in = new ListBuffer[Array[Int]]() 
// 
//         var meta = new ListBuffer[Int]() 
// 
//         var accumulators = new ListBuffer[Array[Int]]() 
//         
//         var output = new ListBuffer[Int]() 
// 
//         poke(c.io.data_in, 1)
// 
//         for(x <- 0 until runs){
//             for(y <- 0 until 12){
//                 var selected_slice = ListBuffer[BigInt]()
//                 var mux_slice = ListBuffer[BigInt]()
//                  
//                 // data_tree :: peek(c.io.data_in)
//  
//                 if((y == 9) || (y == 3) || (y == 2)){
//                     poke(c.io.active, false)
//                 }
//                 else{
//                     poke(c.io.active, true)
//                 }
//                 row1 += peek(c.memory.pixel_rows(0).dbg_reg_contents).map(_.toInt)
//                 row2 += peek(c.memory.pixel_rows(1).dbg_reg_contents).map(_.toInt)
//                 row3 += peek(c.memory.pixel_rows(2).dbg_reg_contents).map(_.toInt)
// 
//                 mux_slice += peek(c.memory.shift_muxes(0).dbg_enable)
//                 mux_slice += peek(c.memory.shift_muxes(1).dbg_enable)
//                 mux_slice += peek(c.memory.shift_muxes(2).dbg_enable)
//                 shift_mux += mux_slice.toArray.map(_.toInt)
//  
//                 shift_mux_in1 += peek(c.memory.shift_muxes(0).data_in).map(_.toInt)
//                 shift_mux_in2 += peek(c.memory.shift_muxes(1).data_in).map(_.toInt)
//                 shift_mux_in3 += peek(c.memory.shift_muxes(2).data_in).map(_.toInt)
// 
//                 row_out_1 += peek(c.memory.pixel_rows(0).data_out).map(_.toInt)
//                 row_out_2 += peek(c.memory.pixel_rows(1).data_out).map(_.toInt)
//                 row_out_3 += peek(c.memory.pixel_rows(2).data_out).map(_.toInt)
//  
//                 pings += peek(c.orchestrator.io.pings).map(_.toInt)
//                 
//                 selected_slice += peek(c.memory.shift_muxes(0).data_out)
//                 selected_slice += peek(c.memory.shift_muxes(1).data_out)
//                 selected_slice += peek(c.memory.shift_muxes(2).data_out)
//                 selected += selected_slice.toArray.map(_.toInt)
//  
//                 ALU_in += peek(c.ALUs.io.dbg_multipliers_in).map(_.toInt)
//  
//                 kernels += peek(c.ALUs.io.dbg_kernel_out).map(_.toInt)
//                 accumulators += peek(c.ALUs.io.dbg_accumulators_out).map(_.toInt)
// 
//  
//                 var out = peek(c.io.data_out).toInt
//                 output += out
// 
//                 step(1)
//             }
//         }
//         
//         var state = Array.ofDim[Int](9, 9)
// 
//         for(i <- 0 until runs){
// 
//             print("\n\n")
//             print("STEP: ")
//             print(i)
//             print(", mod STEP: ")
//             print((i) % 9)
//             print("\n\n")
// 
//             state = draw_pings(state, pings(i))
// 
//             print("\n\nINPUT TREE      [")
//             // print(data_tree(i).reverse.mkString("]         ["))
// 
//             print("]\n\nROW 1       [")
//             print (row1(i).reverse.mkString("] ["))
// 
//             print("]\n\nOUT 1           [")
//             print (row_out_1(i).reverse.mkString("]         ["))
// 
//             print("]\n\nROW 2       [")
//             print (row2(i).reverse.mkString("] ["))
// 
//             print("]\n\nOUT 2           [")
//             print (row_out_2(i).reverse.mkString("]         ["))
// 
//             print("]\n\nROW 3       [")
//             print (row3(i).reverse.mkString("] ["))
// 
//             print("]\n\nOUT 3           [")
//             print (row_out_3(i).reverse.mkString("]         ["))
//             
//             print("]\n\n")
//             print("\n\nSHIFT MUX STATUS:      [")
//             print (shift_mux(i).mkString("]   ["))
// 
//             print("]\n\n")
//             print("\n\nSHIFT MUX IN:      [")
//             print (shift_mux_in1(i).reverse.mkString("]["))
//             print("] - [")
//             print (shift_mux_in2(i).reverse.mkString("]["))
//             print("] - [")
//             print (shift_mux_in3(i).reverse.mkString("]["))
//             print("]\n\n")
// 
//             print("\n\nSELECTED    :   [")
//             print (selected(i).mkString("]         ["))
// 
//             print("]\n\nALU_IN      :   [")
//             print (ALU_in(i).reverse.mkString("] ["))
// 
//             print("]\n\nKERNELS     :   [")
//             print (kernels(i).reverse.mkString("] ["))
// 
//             print("]\n\nACCUMULATORS:   [")
//             print (accumulators(i).reverse.mkString("] ["))
// 
//             print("]\n\nOUTPUT:             *~>>>>  ")
//             print (output(i))
//             print("  <<<<~*")
//             print("\n\n")
//             print("\n\n")
//             print("--------------------------------------------------------------------------\n")
//             print("--------------------------------------------------------------------------\n\n")
// 
//         }
//         
//     }
// 
//     def draw_pings(state: Array[Array[Int]], pings: Array[Int]) : Array[Array[Int]] = {
//         print("   ")
//         for(i <- 0 until state.length){
//             print(" "); print(i); print(" ")
//         }
//         println()
//         for(i <- 0 until pings.length){
//             print(i)
//             print(": ")
//             for(j <- 0 until state.length){
//                 if(state(i)(j) == 1){
//                     print(" # ")
//                 }
//                 else{ print(" . ") }
//             }
//             println()
//             for(j <- state.length-2 to 0 by - 1){
//                 val temp = state(i)(j)
//                 state(i)(j) = state(i)(j+1)
//                 state(i)(j+1) = temp
//             }
//             state(i)(0) = pings(8-i)
//         } 
//         return state
//     }
// 
//     val kernel = Array[Int](1, 1, 0, 1, 0, 1, 0, 1, 1)
// 
//     poke(c.io.active, true)
//     push_kernel(kernel)
// 
//     feed_row(47)
// }
