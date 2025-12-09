package com.example.neurology_project_android.sampledata.interfaceExample;

public class BadController {
    public void executeMove() {
        // Direct dependency on the low-level Car class. 🚨
        Car myCar = new Car("Honda", "Civic", 4);
        myCar.move(10, 20);
    }
}