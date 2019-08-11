/*******************************************
 Basic Sparki Code

Prints all possible colors on RGB led 
********************************************/
#include <Sparki.h> // include the sparki library

void setup() // code inside these brackets runs first, and only once
{
  for(int i = 0; i <= 100; i++) {
    for(int j = 0; j <= 100; j++) {
      for(int k = 0; k <= 100; k++) {
        sparki.RGB(i,j,k);
        delay(1);
      }
    }
  }
  
  sparki.moveStop();
}

void loop() // code inside these brackets runs over and over forever
{
  
}
