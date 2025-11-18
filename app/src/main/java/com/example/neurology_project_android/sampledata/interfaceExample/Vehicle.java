package com.example.neurology_project_android.sampledata.interfaceExample;

public abstract class Vehicle {
    // 1. Common State (Fields)
    // All vehicles have a make, model, and current speed.
    private String make;
    private String model;
    private int currentSpeed;
    private boolean isEngineRunning;

    // Constructor to initialize common fields
    public Vehicle(String make, String model) {
        this.make = make;
        this.model = model;
        this.currentSpeed = 0;
        this.isEngineRunning = false;
    }

    // 2. Common Concrete Behavior (Implemented Methods)
    // These methods have the same implementation for all subclasses
    public void startEngine() {
        this.isEngineRunning = true;
        System.out.println(make + " " + model + " engine started.");
    }

    public void stopEngine() {
        this.isEngineRunning = false;
        this.currentSpeed = 0; // Always stop moving when engine stops
        System.out.println(make + " " + model + " engine stopped.");
    }

    // Getter for common fields
    public String getModel() {
        return model;
    }

    // 3. Common Abstract Behavior (Unimplemented Methods)
    // All vehicles must define a way to accelerate, but the implementation differs
    // (e.g., a Car accelerates differently than a Bicycle or a Plane).
    public abstract void accelerate(int rate);
}
