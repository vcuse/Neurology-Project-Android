package com.example.neurology_project_android.sampledata.interfaceExample;

public class ElectricCar extends Car implements Movable, Drivable {

    private int chargeState;

    public ElectricCar(String make, String model, int doors) {
        // MUST call the parent's constructor first
        super(make, model, doors);
        chargeState = 100;
    }

    // 1. Fulfills Vehicle's abstract contract
    @Override
    public void accelerate(int rate) {
        // Car's specific acceleration logic
        System.out.println(getModel() + " is accelerating with 4 tires.");
        // Code to increase currentSpeed would go here...
    }

    // 2. Fulfills Movable's interface contract (from your previous example)
    @Override
    public void move(int x, int y) {
        chargeState = chargeState - 10;
        // Car's specific movement logic
        System.out.println(getModel() + " moved to new coordinates. (" + x + ", " + y + ")");
        System.out.println(getModel() + "charge dropped to " + chargeState);
    }

    @Override
    public void getPosition() {
        // Implementation for getting position
    }

    // Fulfills the contract from the Drivable interface
    @Override
    public void drive() {
        System.out.println(getModel() + " is being **driven** on the road by a human.");
    }

    // Fulfills the second contract from the Drivable interface
    @Override
    public void steer(String direction) {
        System.out.println(getModel() + " steering wheel turned " + direction + ".");
    }
}