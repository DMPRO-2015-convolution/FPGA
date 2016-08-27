To compile:

$ sbt
> run --backend c --genHarness --compile --test --debug

To test:

See methods in Tile.scala. The test you want to run is the one with image in its name

In order to feed an image you need to edit lenna.c and to_img.c which are currently hardcoded for resolution and filename
compile and run to create text files with the encoder using ./encoder
adjust the width and height in the chisel test method

run
$ sbt
> run --backend c --genHarness --compile --test --debug

run the decoder ./decoder

Marvel at the pshychedelic daisy


!!! READ hello.txt !!!
!!! READ hello.txt !!!
!!! READ hello.txt !!!
!!! READ hello.txt !!!
!!! READ hello.txt !!!
!!! READ hello.txt !!!
!!! READ hello.txt !!!
!!! READ hello.txt !!!
!!! READ hello.txt !!!
