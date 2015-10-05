#define _GNU_SOURCE
#include <stdio.h>
#include "lodepng.h"
#include <stdbool.h>
#include <stdlib.h>


int main( int argc, char ** argv){
    char* in_filename = "big_disaster.txt";
    int width = 640;
    int height = 480;
    
    unsigned char* image = calloc(3*width*height, sizeof(unsigned char));

    FILE* fp;
    char* line = NULL;
    size_t len = 0;
    ssize_t read;

    fp = fopen(in_filename, "r");
    if (fp == NULL)
        exit(EXIT_FAILURE);

    int line_n = 0;
    while ((read = getline(&line, &len, fp)) != -1){
        int val = atoi(line);

        unsigned char bytes[3];

        bytes[0] = (val >> 16) & 0xFF;
        bytes[1] = (val >> 8) & 0xFF;
        bytes[2] = val & 0xFF;

        printf("%x, %x, %x\n", bytes[0], bytes[1], bytes[2]);  
        
        image[line_n*3] = bytes[0];
        image[(line_n*3) + 1] = bytes[1];
        image[(line_n*3) + 2] = bytes[2];

        line_n++;
    }

    lodepng_encode24_file("disaster_conv.png", image , width, height);

    fclose(fp);
    if (line)
        free(line);
    exit(EXIT_SUCCESS);
}
