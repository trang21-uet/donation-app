package com.homework.donation.models;

public class Donation {
    public String id;
    public int amount;
    public String paymentType;
    public int upvotes;
    public Donation (int amount, String method, int upvotes)
    {
        this.amount = amount;
        this.paymentType = method;
        this.upvotes = upvotes;
    }


    public Donation ()
    {
        this.amount = 0;
        this.paymentType = "";
        this.upvotes = 0;
    }
    public String toString()
    {
        return id + ", " + amount + ", " + paymentType + ", " + upvotes;
    }

}
