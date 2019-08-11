/*******************************************
This was also used to test calibration of sparki 
by drawing square.
********************************************/
#include <Sparki.h> // include the sparki library

void setup() // code inside these brackets runs first, and only once
{
  for(int i = 0; i < 4; i++) {
    sparki.moveForward(10.0);
    sparki.moveLeft(90.0);
  }
  
  sparki.moveStop();
}

void loop() // code inside these brackets runs over and over forever
{
  
}
