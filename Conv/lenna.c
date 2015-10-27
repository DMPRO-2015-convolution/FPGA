#include <stdio.h>
#include "lodepng.h"
#include <stdbool.h>
#include <stdlib.h>

bool valid(int width, int height, int x, int y);
void convolute(int** kernel, int height, int width, int pixel_n, unsigned char* image, unsigned char* conv_image);
void dump_bytes(unsigned char* image, int size, char* filename);
void dump24(unsigned char* image, int size, char* filename);
void create_pattern_dump(int size, char* filename);


int main( int argc, char ** argv){
	
/*lodepng_load_file() reads a png file into memory, 
  lodepng_decode24() decodes a png image in memory into a RGB 8 bit per channel  vector*/
  
    size_t pngsize;
    unsigned char* png = NULL;
    char* filename = "Daisy.png";
    lodepng_load_file(&png, &pngsize, filename);
    printf("lodepng: file loaded file\n");

    unsigned int width, height;
    unsigned char* image = NULL;
    unsigned int error = lodepng_decode24(&image, &width, &height, png, pngsize);
    printf("lodepng: image decoded\n");

    int** kernel = malloc(sizeof(int*)*3);
    for(int i = 0; i < 3; i++){
        kernel[i] = malloc(3*sizeof(int));
    }
    kernel[0][0] = 1;
    kernel[0][1] = 0;
    kernel[0][2] = 1;

    kernel[1][0] = 0;
    kernel[1][1] = -4;
    kernel[1][2] = 0;
    
    kernel[2][0] = 1;
    kernel[2][1] = 0;
    kernel[2][2] = 1;
    

    if(error) {
        printf("error %u: %s\n", error, lodepng_error_text(error));
    }

    unsigned char* convoluted_image = malloc(sizeof(unsigned char)*3*height*width);
    for (int i = 0; i < width*height; i++) {
        convolute(kernel, height, width, i, image, convoluted_image);
    }

    dump24(image, width*height, "disaster24dump.txt");
    create_pattern_dump(80*80, "tiny_pattern.txt");

    return 0;
}




bool valid(int width, int height, int x, int y){
    return(( x >= 1 && x < width) && ( y >= 1 && y < height));
} 

void to_coord(int height, int width, int* x, int*y, int current){
    *x = current%width;
    *y = current/width;
}

int to_point(int height, int width, int x, int y){
    return y*width + x;
}

void convolute(int** kernel, int height, int width, int pixel_n, unsigned char* image, unsigned char* conv_image){    

    unsigned char accumulator1 = 0;
    unsigned char accumulator2 = 0;
    unsigned char accumulator3 = 0;

    int x, y;
    to_coord(height, width, &x, &y, pixel_n);

    if(valid(width, height, x, y)){
        for(int i = 0; i < 3; i++){
            for(int j = 0; j < 3; j++){
                int img_index = to_point(height, width, x + (i-1), y + (j-1));
                accumulator1 += kernel[i][j]*image[(img_index*3)];
                accumulator2 += kernel[i][j]*image[(img_index*3)+1];
                accumulator3 += kernel[i][j]*image[(img_index*3)+2];
                
            }
        }
    }

    conv_image[pixel_n*3] = accumulator1;
    conv_image[(pixel_n*3)+1] = accumulator2;
    conv_image[(pixel_n*3)+2] = accumulator3;
}

void dump_bytes(unsigned char* image, int size, char* filename){
    FILE* fp;
    fp = fopen(filename, "w");
    for(int i = 0; i < size; i++){
        int byte1 = image[i*3];
        int byte2 = image[(i*3) + 1];
        int byte3 = image[(i*3) + 2];
        fprintf(fp, "%d ", byte1);
        fprintf(fp, "%d ", byte2);
        fprintf(fp, "%d ", byte3);
    }
    printf("done");
}

void dump24(unsigned char* image, int size, char* filename){
    FILE* fp;
    fp = fopen(filename, "w");
    for(int i = 0; i < size; i++){
        int byte1 = image[i*3];
        int byte2 = (image[(i*3) + 1] << 8) + byte1;
        int byte3 = (image[(i*3) + 2] << 16) + byte2;

        fprintf(fp, "%d\n", byte3);
    }
}

void create_pattern_dump(int size, char* filename){
    FILE* fp;
    fp = fopen(filename, "w");
    for(int i = 0; i < size; i++){

        int pattern = i / 80;
        fprintf(fp, "%d\n", pattern + 1);
    }
}
