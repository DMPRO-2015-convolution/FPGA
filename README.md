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
