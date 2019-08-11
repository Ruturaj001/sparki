/*******************************************
 Basic Sparki Code
 
This code detects distance to obstacle (using ultrasonic) and prints.
********************************************/
#include <Sparki.h> // include the sparki library

void setup() {
    sparki.servo(SERVO_CENTER);
}

void loop() // code inside these brackets runs over and over forever
{
    sparki.clearLCD(); // wipe the LCD clear
    sparki.print(sparki.ping()); // reads the ultrasonic ranger and prints the result in the LCD
    sparki.println(" cm");
    sparki.updateLCD(); // put the drawings on the screen
    delay(100); 
}
