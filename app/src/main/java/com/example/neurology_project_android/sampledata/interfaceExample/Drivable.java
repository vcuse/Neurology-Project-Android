package com.example.neurology_project_android.sampledata.interfaceExample;

public interface Drivable {

    // The contract: any class implementing Drivable must define this method.
    void drive();

    // You could also add other related behaviors that all drivable things share:
    void steer(String direction);
}