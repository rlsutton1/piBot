//rotaryencoder.h
//17 pins / 2 pins per encoder = 8 maximum encoders
#define max_encoders 8

struct encoder
{
    int pin_a;
    int pin_b;
    volatile long value;
    volatile int lastEncoded;
};

//Pre-allocate encoder objects on the stack so we don't have to
//worry about freeing them
struct encoder encoders[max_encoders];

/*
  Should be run for every rotary encoder you want to control
  Returns a pointer to the new rotary encoder structer
  The pointer will be NULL is the function failed for any reason
*/
struct encoder *setupencoder(int pin_a, int pin_b);

//and:

//rotaryencoder.c
#include <wiringPi.h>

#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>

//#include "rotaryencoder.h"

int main( int argc, const char* argv[] )
{
        int enc1a;
        int enc1b;

        struct encoder *enc1;

        wiringPiSetup();

        // ensure the correct number of parameters are used.
        if ( argc == 3 )
        {
                enc1a = atoi( argv[1] );
                enc1b = atoi( argv[2] );
                enc1 = setupencoder(enc1a,enc1b);

                int    c;
                size_t s = 0;

                while ((c = getchar()) != EOF)
                {
                        printf("%ld\n", enc1->value);
                        fflush(stdout);
                }


        }

}

int numberofencoders = 0;

void updateEncoders()
{
    struct encoder *encoder = encoders;
    for (; encoder < encoders + numberofencoders; encoder++)
    {
        int MSB = digitalRead(encoder->pin_a);
        int LSB = digitalRead(encoder->pin_b);

        int encoded = (MSB << 1) | LSB;
        int sum = (encoder->lastEncoded << 2) | encoded;

        if(sum == 0b1101 || sum == 0b0100 || sum == 0b0010 || sum == 0b1011) encoder->value++;
        if(sum == 0b1110 || sum == 0b0111 || sum == 0b0001 || sum == 0b1000) encoder->value--;

        encoder->lastEncoded = encoded;
    }
}

struct encoder *setupencoder(int pin_a, int pin_b)
{
    if (numberofencoders > max_encoders)
    {
        printf("Maximum number of encodered exceded: %i\n", max_encoders);
        return NULL;
    }

    struct encoder *newencoder = encoders + numberofencoders++;
    newencoder->pin_a = pin_a;
    newencoder->pin_b = pin_b;
    newencoder->value = 0;
    newencoder->lastEncoded = 0;

    pinMode(pin_a, INPUT);
    pinMode(pin_b, INPUT);
    pullUpDnControl(pin_a, PUD_UP);
    pullUpDnControl(pin_b, PUD_UP);
    wiringPiISR(pin_a,INT_EDGE_BOTH, updateEncoders);
    wiringPiISR(pin_b,INT_EDGE_BOTH, updateEncoders);

    return newencoder;
}
