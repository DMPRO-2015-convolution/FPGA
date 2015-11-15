`timescale 1ns / 1ps

module pixelfeeder
#(parameter DATA_WIDTH=8, parameter ADDR_WIDTH=8)
(
    input wire SYS_CLK, 
	 input [(ADDR_WIDTH-1):0] addr,
    output reg [(DATA_WIDTH-1):0] red_data,
	 output reg [(DATA_WIDTH-1):0] green_data,
	 output reg [(DATA_WIDTH-1):0] blue_data,
);

//Block SelectRAM Instantiation
RAMB16_S36 U_RAMB16_S36 (   .DI(),    //insert 32-bit data_in bus ([31:0])
			    .DIP(),   //insert 4-bit parity data_in bus ([35:32])
 			    .ADDR(),  //insert 9-bit address bus ([8:0])
			    .EN(),    //insert enable signal
		 	    .WE(),    //insert write enable signal
			    .SSR(),   //insert set/reset signal
			    .CLK(),   //insert clock signal
			    .DO(),    //insert 32-bit data_out bus ([31:0])
			    .DOP()    //insert 4-bit parity data_out bus ([35:32])
			     );

RAMB16BWER ram (
	// input
	.DIA(),
	.DIPA(),
	.ADDRA(),
	.WEA(),
	.ENA(),
	.REGCEA(),
	.RSTA(),
	.CLKA(),
	
	.DIB(),
	.DIPB(),
	.ADDRB(),
	.WEB(),
	.ENB(),
	.REGCEB(),
	.RSTB(),
	.CLKB(),
	
	// output

	.DOA(),
	.DOPA(),
	
	.DOB(),
	.DOPB()
	);
	
// Demo: instantiate bram, load picture
// Real: set up sram connection

// both: set address to 0
// increment, when addr == width: set to 0

    // Declare the ROM variable
    reg [DATA_WIDTH-1:0] rom[2**ADDR_WIDTH-1:0];

    initial
    begin
        $readmemh("single_port_rom_init.txt", rom);
    end

    always @ (posedge clk)
    begin
        q <= rom[addr];
    end

endmodule