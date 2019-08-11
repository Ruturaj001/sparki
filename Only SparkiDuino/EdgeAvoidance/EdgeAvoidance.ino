#include <Sparki.h> // include the sparki library

/*
 * This code enables sparki to not fall off
 * the table.
 */

void setup()
{       
}

void loop() {
  int edgeLeft   = sparki.edgeLeft();   // measure the left edge IR sensor
  int edgeRight  = sparki.edgeRight();  // measure the right edge IR sensor

  int threshold = 200; // if below this value, no surface underneath

  if (edgeLeft < threshold) // if no surface underneath left sensor
  {
    sparki.moveBackward(5);
    sparki.moveRight(20); // turn right
  }

  if (edgeRight < threshold) // if no surface underneath right sensor
  {
    sparki.moveBackward(5);
    sparki.moveLeft(20); // turn left
  }

  sparki.moveForward(); // move forward
  delay(100); // wait 0.1 seconds
}
