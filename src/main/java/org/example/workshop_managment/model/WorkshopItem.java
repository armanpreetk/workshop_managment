package org.example.workshop_managment.model;

/**
 * A simple Model class representing an item in the workshop.
 */
public class WorkshopItem {

    private String name;
    private int quantity;

    // Constructor
    public WorkshopItem(String name, int quantity) {
        this.name = name;
        this.quantity = quantity;
    }

    // Getters
    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}