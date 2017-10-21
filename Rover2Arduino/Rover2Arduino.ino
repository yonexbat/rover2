#include <Servo.h>
Servo esc;
Servo servo;

void setup() {
  SerialUSB.begin(9600);
  esc.attach(9);
  servo.attach(8);
}

void loop() {
  while (SerialUSB.available() > 0) {
    
    int command = SerialUSB.parseInt();
    int value = SerialUSB.parseInt();
    
    String log = "command received : ";
    log += command;
    log += ", value: ";
    log += value;
    SerialUSB.println(log);

    switch(command)
    {
      case 1:
        esc.writeMicroseconds(value);
        break;
      case 2:
        servo.writeMicroseconds(value);
        break;
    }
    
  }
}
