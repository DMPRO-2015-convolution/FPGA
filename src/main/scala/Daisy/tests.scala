// package Core
// import Chisel._
// import java.io._
// import scala.collection.mutable.ListBuffer
// import scala.io.Source
// 
// class GridTest(c: Tile) extends Tester(c) {
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
//     val kernel = Array[Int](1, 0, 0, 0, 0, 0, 0, 0, 1)
//     push_kernel(kernel)
// 
//     for(i <- 0 until 60){
//         step(1)
//     }
// 
//     
// }
