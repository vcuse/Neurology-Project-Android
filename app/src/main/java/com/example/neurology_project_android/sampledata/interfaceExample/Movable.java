package com.example.neurology_project_android.sampledata.interfaceExample;

public interface Movable {
    // This method signature is the contract.
    // Any class implementing Movable MUST define this method.
    void move(int x, int y);

    // Another method for the contract
    void getPosition();
}