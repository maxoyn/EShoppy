package com.simeonch.eshoppy;

import java.io.Serializable;

public class Item implements Serializable {
    private String itemName;
    private int itemQuantity;
    private double itemPrice;
    private String itemBrand;
    private int myStatus;
    //private boolean isBought;
    //private String[] itemCategory = {"Diary", "Sweets", "Meat", "Vegetables", "General Food", "Drinks", "Domestic", "None"};


    public Item() {
        super();
    }


    public Item(String itemName, int itemQuantity, double itemPrice, String itemBrand, int myStatus) {
        this.itemName = itemName;
        this.itemQuantity = itemQuantity;
        this.itemPrice = itemPrice;
        this.itemBrand = itemBrand;
        this.myStatus = myStatus;
        //this.isBought = isBought;
    }

    public String getItemName() {
        return itemName;
    }
    public void setItemName(String name) { this.itemName = name; }

    public int getItemQuantity() {
        return itemQuantity;
    }

    public double getItemPrice() {
        return itemPrice;
    }
    public void setItemPrice(double price) { this.itemPrice = price; }


    public String getItemBrand() {
        return itemBrand;
    }
    public void  setItemBrand(String b) {
        this.itemBrand = b;
    }

    public void increaseQuantity() {
        itemQuantity++;
    }
    public void decreaseQuantity() {
        if(itemQuantity > 1) {
            itemQuantity--;
            }
        }

//    public boolean getStatus() {
//        Boolean a = this.isBought;
//        if(a != null) {
//            return this.isBought;
//        }
//        else {
//            return false;
//        }
//    }
    //public boolean getStatus() { return this.isBought; }
    //public void changeStatus() { this.isBought = !isBought; }

    public int getMyStatus() { return this.myStatus; }
    public void changeMyStatus() {
        if(this.myStatus == 0) {
            this.myStatus = 1;
            return;
        }
        this.myStatus *= -1;
    }

    //for getting items from local db
    public void setMyStatus(int in) {
        this.myStatus = in;
    }

    public void setQuantity(Integer newQ) {
        this.itemQuantity = newQ;
    }


}
