package Core

import Chisel._
import TidbitsOCM._


class MemTest(c: Tile) extends Tester(c) {
    
    val img_width = 640
    val img_depth = 24
    val kernel_dim = 3

    val slice_total = img_width*kernel_dim

    for(i <- 0 until 
}
