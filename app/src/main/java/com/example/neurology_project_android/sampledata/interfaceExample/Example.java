package com.example.neurology_project_android.sampledata.interfaceExample;

public class Example {
    public static void main(String[] args) {
        // 1. Setup Abstractions and Details
        Movable myCar = new Car("Toyota", "Corolla", 4);
        Movable myPerson = new Person(); // Assuming Person also implements Movable
        Movable myElectricCar = new ElectricCar("Toyota", "PriusPrime", 4);

        // 2. High-level Controller (GoodMover) is instantiated
        GoodController mover = new GoodController();

        System.out.println("\n--- Moving the Car --- with Good Controller");
        // The controller doesn't know it's a Car, only that it's Movable.
        mover.moveItem(myCar);


        System.out.println("\n--- Moving the Electric Car --- with Good Controller");
        mover.moveItem(myElectricCar);
        mover.moveItem(myElectricCar);
        mover.moveItem(myElectricCar);
        mover.moveItem(myElectricCar);

        System.out.println("\n--- Moving the Person ---also with Good Controller");
        // The controller can swap to a completely different type of object!
        mover.moveItem(myPerson);

        //notice that bad controller cannot move a person at all
        //because it depends directly on the car
        System.out.println("\n--- Moving the person with bad Controller");
        BadController badController = new BadController();
        badController.executeMove();
    }
}

