package com.example.neurology_project_android.sampledata.interfaceExample;

public class Person implements Movable {
    private int currentX = 0;
    private int currentY = 0;

    // This method implementation fulfills the Movable contract
    @Override
    public void move(int x, int y) {
        this.currentX = x;
        this.currentY = y;
        System.out.println("🚶 Person walked to (" + x + ", " + y + ")");
    }

    // This method implementation fulfills the Movable contract
    @Override
    public void getPosition() {
        System.out.println("Person is at (" + currentX + ", " + currentY + ")");
    }

    public void printType(){
        System.out.println("I am a person!!!");
    }
}