package com.example.neurology_project_android.sampledata.interfaceExample;

public class GoodController {
    // High-level module depends on the Movable abstraction.
    //This allows use to use GoodController on either the car or person!!
    public void moveItem(Movable item) {
        System.out.println("Controller received an item to move...");
        item.move(50, 100);
    }
}
