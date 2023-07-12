# Pulseoximetr_android_app

## Purpose of the project:
1. To create a pulse oximeter system that receives pulse and saturation values from the sensor and transmits them to an app.
2. To create a mobile device application receiving pulse and saturation values, presenting the data to the user and informing when they exceed safe ranges.
3. Calculating and presenting statistics from the measurements.


## Hardware:
1. MAX30100 pulse sensor
2. HC-05 bluetooth module
3. Arduino Uno
4. Mobile device

## Schema:
![Opera Zrzut ekranu_2023-07-12_220704_docs google com](https://github.com/annasli378/Pulseoximetr_android_app/assets/86146614/2e325c97-2c1a-429a-a85a-cd1b67f8f745)

Diagram of the connection of individual components


## Finished layout
![Opera Zrzut ekranu_2023-07-12_220724_docs google com](https://github.com/annasli378/Pulseoximetr_android_app/assets/86146614/f7dd20fe-b713-4267-bf20-c14577a94892)

Finished layout after assembly


## Specification
1. Kotlin 1.2.0
2. Android Studio 4.1.0
3. MPAndroidCharts 3.1.0
4. SQLite 3.33.0

## App
Layouts:

![Opera Zrzut ekranu_2023-07-12_221327_docs google com](https://github.com/annasli378/Pulseoximetr_android_app/assets/86146614/d30469f7-af40-488e-963a-e7e9c5554f77)

Database:

The measurements are stored in a database created in SQLite, which supports SQL (Structured Query Language) and consists of a single table of measurements storing the values of pulse, saturation and the date of each measurement stored in the appropriate format.

![Opera Zrzut ekranu_2023-07-12_221442_docs google com](https://github.com/annasli378/Pulseoximetr_android_app/assets/86146614/f1c177ac-ce3e-4f36-a455-646e39a23f71)

