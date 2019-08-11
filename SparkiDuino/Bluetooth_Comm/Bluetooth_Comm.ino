#include <Sparki.h>  // include the sparki library
/*
 * This code is for testing bluetooth communication
 * It prints char after receiving
 */
void setup()
{
  Serial1.begin(9600);
  sparki.clearLCD();
  sparki.println("Starting..");
  sparki.updateLCD();
}

void loop()
{
  if (Serial1.available()) 
  {
    int inByte = Serial1.read();
    sparki.print("Bluetooth: ");
    sparki.println((char)inByte); 
    sparki.updateLCD();
  }
  if (Serial.available()) 
  {
    int inByte = Serial.read();
    sparki.print("Serial: ");
    sparki.println((char)inByte); 
    sparki.updateLCD();
  }
}
