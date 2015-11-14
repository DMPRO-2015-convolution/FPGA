package Core

import Chisel._
import TidbitsOCM._

// Currently only a system to translate data widths.
// Can be enhanced if needed.
// Endianness will fug shit up!
class InputTranslater(input_width: Int, output_width: Int) extends Module{

    val io = new Bundle {

        val input_valid = Bool(INPUT)
        val input_data = UInt(INPUT, input_width)

        val output_valid = UInt(OUTPUT, output_width)
        val output_data = Bool(OUTPUT)
    }

    var buf_size = 0

    if(input_width > output_width){
        buf_size = (input_width + (input_width - output_width))
    }
    else{
        buf_size = 2*output_width
    }


    // Way overshoots but thats OK
    val buffer = Reg(init=UInt(0, 128))
    val top = Reg(init=UInt(0, 8)) 

    // We have three cases to consider:
    //
    // #1: input is valid and there is not enough data for a valid output
    // #2: input is valid and we have enough data to output
    // #3: neither in or out is valid
    
    // when(io.input_valid){
    //     // 1
    //     when(top > UInt(output_width)){
    //         top := top + UInt(input_width)
    //         buffer := buffer + (input_data << top)
    //     }
    //     // 2
    //     when(top <= UInt(output_width)){
    //         output_data := UInt(123)
    //         top := top + UInt(input_width - output_width)
    //         buffer(0, UInt(output
    //         
    // }
    
    io.output_data := UInt(0)
    io.output_valid := Bool(false)
}

