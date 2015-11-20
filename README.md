Create verilog code: 

$ sbt
> run --backend c --genHarness --compile --test --debug -v

Relies on the fpga-tidbits repo

https://github.com/maltanar/fpga-tidbits/tree/platform-layer

remember to use the correct branch (platform layer)
project is built on commit: 

commit a06dcea9612a33f9634ddfdf45143f86b84e6646

In order to synthesize the following verilog file must be added:

https://github.com/maltanar/fpga-tidbits/blob/platform-layer/on-chip-memory/DualPortBRAM.v
(Which lies in the on chip memory folder in the fpga tidbits repo already)

Instruction format: The instruction must be sent ten times for a 3x3 kernel, or 26 for a 5x5
The 8 lowest bits will be used. The 4 lowest will be used in the mapping units, the 4 highest will be used in the reducer.

The instructions are as follows:

0x0 - multiply
0x1 - add
0x2 - mask (pixel is 0 if less than kernel)
0x3 - div  (not available for reducer)

After sending instructions the kernels should be fed. Each kernel should be 8 bits, and will occupy the 8 lowest bits. When all kernels have been sent the processor enters data mode
until the reset signal is sent.
