/*
 * This was used on sparki side for menu driven and mapping code
 * for bluetooth communication.
 *
 */
#include <Sparki.h>  // include the sparki library

#define STATUS_OK 0
#define MOVE_FORWARD 1
#define MOVE_BACKWARD 2
#define MOVE_LEFT 3
#define MOVE_RIGHT 4
#define SERVO 5
#define ULTRASONIC_PING 6
#define DELAY 7
#define LED 8
#define GRIPPER 9

void setup()
{
  Serial1.begin(9600);
  sparki.servo(SERVO_CENTER);
  sparki.clearLCD();
  sparki.println("Starting..");
  sparki.updateLCD();
}

void loop()
{
  if (Serial1.available()) 
  {
    byte opcode = Serial1.read();
//    sparki.print("OPCODE: ");
    sparki.print(opcode);
    sparki.print("-");
    sparki.updateLCD();
    
    switch(opcode) {
      case MOVE_FORWARD:
        {
          sparki.print("MOVE_FORWARD: ");
          while(!Serial1.available());
          byte distance = Serial1.read();
          sparki.println(distance);
          sparki.updateLCD();
          sparki.moveForward(distance);
          Serial1.print((char)STATUS_OK);
        }
        break;
      case MOVE_BACKWARD:
        {
          sparki.print("MOVE_BACKWARD: ");
          while(!Serial1.available());
          byte distance = Serial1.read();
          sparki.println(distance);
          sparki.updateLCD();
          sparki.moveBackward(distance);
          Serial1.print((char)STATUS_OK);
        }
        break;
      case MOVE_LEFT:
        {
          sparki.print("MOVE_LEFT: ");
          while(!Serial1.available());
          byte angle = Serial1.read();
          sparki.println(angle);
          sparki.updateLCD();
          sparki.moveLeft(angle);
          Serial1.print((char)STATUS_OK);
        }
        break;
      case MOVE_RIGHT:
        {
          sparki.print("MOVE_RIGHT: ");
          while(!Serial1.available());
          byte angle = Serial1.read();
          sparki.println(angle);
          sparki.updateLCD();
          sparki.moveRight(angle);
          Serial1.print((char)STATUS_OK);
        }
        break;
      case SERVO:
        {
          sparki.print("SERVO: ");
          while(!Serial1.available());
          int angle = Serial1.read();
          angle -= 90;
          sparki.println(angle);
          sparki.updateLCD();
          sparki.servo(angle);
          Serial1.print((char)STATUS_OK);
        }
        break;
      case ULTRASONIC_PING:
        {
          sparki.print("PING: ");
          short ping = sparki.ping();
          sparki.println(ping);
          char pinglsb = ping & 0xFF;
          char pingmsb = ping >> 8;
          /*sparki.println((int)pingmsb);
          sparki.println((int)pinglsb);*/
          sparki.updateLCD();
          
          Serial1.print(pingmsb);
          Serial1.print(pinglsb);
          
        }
        break;
      case DELAY:
        {
          sparki.print("DELAY: ");
          while(!Serial1.available());
          byte time = Serial1.read();
          int time_100 = time * 100;
          sparki.println(time_100);
          sparki.updateLCD();
          delay(time_100);
          Serial1.print((char)STATUS_OK);
        }
        break;
      default:
        sparki.println("Wrong OPCODE");
        sparki.updateLCD();
        Serial1.print((char)STATUS_OK);
    }
  }
}
