// package Core
// 
// import Chisel._
// 
// // Stole all of this code from some dude on github 
// import TidbitsOCM._
// 
// 
// // A stack which should synthesize to BRAM. Should be used to store slices.
// // This stack only serves to buffer slices of our image which is done with two buffers
// // thus we dont need to take too many precautions.
// class tRowBuffer(entries: Int, data_width: Int, number: Int) extends Module {
//     
//     val io = new Bundle {
//         val data_in = UInt(INPUT, data_width)
//         val push = Bool(INPUT)
//         val pop = Bool(INPUT)
// 
//         val data_out = UInt(OUTPUT, data_width)
//         val dbg_stack_top = UInt(OUTPUT, 32)
//     }
// 
//     val stack_top = Reg(init=UInt(0, width=log2Up(entries)))
//     val bram = Module(new DualPortBRAM(addrBits=log2Up(entries), dataBits=data_width)).io 
// 
//     val writePort = bram.ports(0)
//     val readPort = bram.ports(1)
// 
//     writePort.req.writeData := io.data_in
//     writePort.req.writeEn := Bool(false)
//     writePort.req.addr := stack_top
// 
//     readPort.req.writeData := UInt(0)
//     readPort.req.writeEn := Bool(false)
//     readPort.req.addr := stack_top - UInt(1)
// 
//     when(io.pop){
//         when(stack_top >= UInt(0)){
//             stack_top := stack_top - UInt(1)
//         }
//     }
//     when(io.push){
//         when(stack_top < UInt(entries)){
//             stack_top := stack_top + UInt(1)
//             writePort.req.writeEn := Bool(true)
//         }
//     }
// 
//     // io.data_out := readPort.rsp.readData
//     io.data_out := UInt(number)
//     io.dbg_stack_top := stack_top
// }
// 
// class tSliceBuffer(row_length: Int, data_width: Int, kernel_dim: Int) extends Module {
// 
//     val cols = kernel_dim*kernel_dim
//     
//     val io = new Bundle {
//         val data_in = UInt(INPUT, data_width)
//         val push = Bool(INPUT)
//         val pop = Bool(INPUT)
// 
//         val data_out = UInt(OUTPUT, data_width)
// 
//         val dbg = new Bundle {
//             val sb_push_row  = UInt(OUTPUT, 32)
//             val sb_push_top = UInt(OUTPUT, 32)
//             val sb_pop_row = UInt(OUTPUT, 32)
// 
//             val rb1_data_out = UInt(OUTPUT)
//             val rb1_data_in = UInt(OUTPUT)
//             val rb1_stack_top = UInt(OUTPUT)
// 
//             val rb2_data_out = UInt(OUTPUT)
//             val rb2_data_in = UInt(OUTPUT)
//             val rb2_stack_top = UInt(OUTPUT)
//         }
//     }
// 
// 
//     val row_buffers = for(i <- 0 until cols) yield Module(new RowBuffer(row_length, data_width, i)).io
//       
//     val push_row = Reg(init=UInt(0, 32))
//     val pop_row  = Reg(init=UInt(0, 32))
//     val push_top = Reg(init=UInt(0, 32))
// 
//     io.data_out := UInt(0)
// 
//     io.dbg.sb_push_row := push_row
//     io.dbg.sb_push_top := push_top
//     io.dbg.sb_pop_row := pop_row
// 
//     io.dbg.rb1_data_out := row_buffers(0).data_out
//     io.dbg.rb2_data_out := row_buffers(1).data_out
// 
//     io.dbg.rb1_data_in := io.data_in
//     io.dbg.rb2_data_in := io.data_in
// 
//     io.dbg.rb1_stack_top := row_buffers(0).dbg_stack_top
//     io.dbg.rb2_stack_top := row_buffers(1).dbg_stack_top
// 
// 
//     // Maintain push row
//     when(push_top === UInt(row_length)){
//         when(push_row < UInt(cols - 1)){
//             push_row := push_row + UInt(1)
//         }.otherwise{
//             push_row := UInt(0)
//         }
//         push_top := UInt(0)
//     }
// 
//     // Maintain pop row
//     when(io.pop){
//         when(pop_row < UInt(cols - 1)){
//             pop_row := pop_row  + UInt(1)    
//         }.otherwise{
//             pop_row  := UInt(0)
//         }
//     }
// 
//     // pop data 
//     for(i <- 0 until cols){
//         when(pop_row  === UInt(i)){
//             row_buffers(i).pop := io.pop
//             io.data_out := row_buffers( ((i-1)+cols) % cols ).data_out
//         }.otherwise{
//             row_buffers(i).pop := Bool(false)
//         }
//     }
// 
//     // push data
//     for(i <- 0 until cols){
//         when(push_row === UInt(i)){
//             row_buffers(i).push := io.push
//             row_buffers(i).data_in := io.data_in
//         }.otherwise{
//             row_buffers(i).data_in := UInt(0)
//             row_buffers(i).push := Bool(false)
//         }
//     }
// }
// 
// // The interface of the buffer. Should tell when it is has buffered a slice
// class tSliceDoubleBuffer(img_width: Int, data_width: Int, kernel_dim: Int) extends Module {
// 
//     val cols = kernel_dim*kernel_dim
//     val total_reads = img_width*cols
//     val total_writes = img_width*cols
// 
//     println("sdb total_reads: %d".format(total_reads))
// 
//     val io = new Bundle {
//         val data_in = UInt(INPUT, data_width)
// 
//         val request_write = Bool(INPUT)   // parent requests the sdb to write data
//         val request_read = Bool(INPUT)    // parent requests to read data from sdb
// 
//         val request_input = Bool(OUTPUT)
//         val request_output = Bool(OUTPUT)
// 
//         val data_out = UInt(OUTPUT, data_width)
//         val error = Bool(OUTPUT)
// 
//         val dbg_reads_performed = UInt(OUTPUT, 32)
//         val dbg_writes_performed = UInt(OUTPUT, 32)
//         val dbg_current = Bool(OUTPUT)
// 
//         val dbg_reads_finished = Bool(OUTPUT)
//         val dbg_writes_finished = Bool(OUTPUT)
// 
//         val dbg_slice1_in = UInt(OUTPUT, 32)
//         val dbg_slice1_out = UInt(OUTPUT, 32)
//         val dbg_slice2_in = UInt(OUTPUT, 32)
//         val dbg_slice2_out = UInt(OUTPUT, 32)
// 
//         val dbg_db_slice1_pop_row = UInt(OUTPUT, 32)
//         val dbg_db_slice1_push_row = UInt(OUTPUT, 32)
//         val dbg_db_slice2_pop_row = UInt(OUTPUT, 32)
//         val dbg_db_slice2_push_row = UInt(OUTPUT, 32)
// 
//         val dbg_db_slice1_rb1_stack_top = UInt(OUTPUT, 32)
//         val dbg_db_slice1_rb2_stack_top= UInt(OUTPUT, 32)
// 
//         val dbg_db_slice2_rb1_stack_top = UInt(OUTPUT, 32)
//         val dbg_db_slice2_rb2_stack_top= UInt(OUTPUT, 32)
//     }
// 
//     val slice1 = Module(new SliceBuffer(img_width, data_width, kernel_dim))
//     val slice2 = Module(new SliceBuffer(img_width, data_width, kernel_dim))
// 
//     val reads_finished = Reg(init=Bool(false))
//     val writes_finished = Reg(init=Bool(true))
// 
//     val reads_performed = Reg(init=UInt(0, 32))
//     val writes_performed = Reg(init=UInt(0, 32))
// 
//     val current = Reg(init=Bool(false))
// 
//     io.dbg_reads_performed := reads_performed
//     io.dbg_writes_performed := writes_performed
//     io.dbg_current := current
//     io.dbg_reads_finished := reads_finished
//     io.dbg_writes_finished := writes_finished
//     io.dbg_slice1_in := slice1.io.data_in
//     io.dbg_slice2_in := slice2.io.data_in
//     io.dbg_slice1_out := slice1.io.data_out
//     io.dbg_slice2_out := slice2.io.data_out
// 
//     io.dbg_db_slice1_push_row := slice1.io.dbg.sb_push_row
//     io.dbg_db_slice1_pop_row := slice1.io.dbg.sb_pop_row
// 
//     io.dbg_db_slice2_push_row := slice2.io.dbg.sb_push_row
//     io.dbg_db_slice2_pop_row := slice2.io.dbg.sb_pop_row
// 
//     io.dbg_db_slice1_rb1_stack_top := slice1.io.dbg.rb1_stack_top
//     io.dbg_db_slice1_rb2_stack_top := slice1.io.dbg.rb2_stack_top
// 
//     io.dbg_db_slice2_rb1_stack_top := slice2.io.dbg.rb1_stack_top
//     io.dbg_db_slice2_rb2_stack_top := slice2.io.dbg.rb2_stack_top
// 
//     // defaults
//     slice1.io.pop := Bool(false)
//     slice2.io.pop := Bool(false)
//     slice1.io.push := Bool(false)
//     slice2.io.push := Bool(false)
//     slice1.io.data_in := UInt(0)
//     slice2.io.data_in := UInt(0)
//     io.data_out := UInt(0)
//     io.error := Bool(false)
//     io.request_input := Bool(false)
//     io.request_output := Bool(false)
// 
//     // Handle read requests
//     // This means we want to read some data from, which means we want the buffer to write data out
//     when(io.request_read){
//         when(current === Bool(false)){
//             slice1.io.pop := Bool(true)
//             io.data_out := slice1.io.data_out
//             writes_performed := writes_performed + UInt(1)
//         }.otherwise{
//             slice2.io.pop := Bool(true)
//             io.data_out := slice2.io.data_out
//             writes_performed := writes_performed + UInt(1)
//         }
//     }
// 
//     // Handle write requests
//     // This means we want to write some input into buffer
//     when(io.request_write){
//         when(current === Bool(false)){
//             slice1.io.push := Bool(true)
//             slice1.io.data_in := io.data_in
//             reads_performed := reads_performed + UInt(1)
//         }.otherwise{
//             slice2.io.push := Bool(true)
//             slice2.io.data_in := io.data_in
//             reads_performed := reads_performed + UInt(1)
//         }
//     }
// 
//     // Check if reads/writes are finished
//     when( (reads_performed === UInt(total_reads) ) ){
//         reads_finished := Bool(true)
//     }
// 
//     when( (writes_performed === UInt(total_writes) ) ){
//         writes_finished := Bool(true)
//     }
// 
//     // Do the switcheroo
//     when( reads_finished && writes_finished ){
//         when(current === Bool(false)){
//             current := Bool(true)
//         }.otherwise{
//             current := Bool(false)
//         }
//         // Reset counts
//         reads_performed := UInt(0)
//         writes_performed := UInt(0)
// 
//         reads_finished := Bool(false)
//         writes_finished := Bool(false)
//     }
// 
//     // Decide if data should be requested
//     when( !reads_finished ){
//         io.request_input := Bool(true)
//     }
//     when( !writes_finished ){
//         io.request_output := Bool(true)
//     }
// 
//     // Should never happen, but who am I kidding?
//     when( (reads_performed > UInt(total_reads)) || (writes_performed > UInt(total_writes)) ){
//         io.error := Bool(true)
//     }
// }
