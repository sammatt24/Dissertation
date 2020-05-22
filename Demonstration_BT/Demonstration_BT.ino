#include <SoftwareSerial.h>
#include <Servo.h>
SoftwareSerial EEBlue(12, 13); // RX | TX
Servo myservo1, myservo3, myservo4, myservo5, myservo6, myservo8, myservo9, myservo10;
char incomingValue = 0;

void setup()
{
  Serial.begin(9600);
  EEBlue.begin(9600);

  myservo1.attach(2);
  myservo3.attach(3);
  myservo4.attach(4);
  myservo5.attach(5);
  myservo6.attach(6);
  myservo8.attach(7);
  myservo9.attach(8);
  myservo10.attach(9);
  
  myservo1.write(0);
  myservo3.write(0);
  myservo4.write(0);
  myservo5.write(0);
  myservo6.write(0);
  myservo8.write(0);
  myservo9.write(0);
  myservo10.write(0);
}

void loop()
{
  if (EEBlue.available()) {
    incomingValue = EEBlue.read();
    if (incomingValue == '1') {
      myservo1.write(90);
      Serial.println("Servo 1 On");
      delay(2000);
      myservo1.write(0);
    }
    if (incomingValue == '3') {
      myservo3.write(90);
      Serial.println("Servo 3 On");
      delay(2000);
      myservo3.write(0);
    }
    if (incomingValue == '4') {
      myservo4.write(90);
      Serial.println("Servo 4 On");
      delay(2000);
      myservo4.write(0);
    }
    if (incomingValue == '5') {
      myservo5.write(90);
      Serial.println("Servo 5 On");
      delay(2000);
      myservo5.write(0);
    }
    if (incomingValue == '6') {
      myservo6.write(90);
      Serial.println("Servo 6 On");
      delay(2000);
      myservo6.write(0);
    }
    if (incomingValue == '8') {
      myservo8.write(90);
      Serial.println("Servo 8 On");
      delay(2000);
      myservo8.write(0);
    }
    if (incomingValue == '9') {
      myservo9.write(90);
      Serial.println("Servo 9 On");
      delay(2000);
      myservo9.write(0);
    }
    if (incomingValue == 'w') {
      myservo10.write(90);
      Serial.println("WINNER");
      delay(2000);
      myservo10.write(0);
    }
  }
}
