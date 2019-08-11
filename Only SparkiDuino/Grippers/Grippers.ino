/*******************************************
This code picks up object using gripper.
********************************************/
#include <Sparki.h> // include the sparki library

void setup() // code inside these brackets runs first, and only once
{
  sparki.clearLCD();
  sparki.println("Starting");
  sparki.updateLCD();
  sparki.gripperOpen();
  delay(3000);
  sparki.gripperStop();
  sparki.moveForward(20.0);
  sparki.gripperClose();
  delay(3000);
  sparki.gripperStop();
  sparki.moveBackward(20.0);
  sparki.moveLeft(180.0);
}

void loop() // code inside these brackets runs over and over forever
{
}
