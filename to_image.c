#include <stdio.h>
#include "lodepng.h"
#include <stdbool.h>
#include <stdlib.h>


int main( int argc, char ** argv){
    char* out_filename = "lenna512x512_orig.png";
    char* in_filename = "chisel_conv.txt";
    
    unsigned char* image = calloc(3*512*512, sizeof(unsigned char))
}
