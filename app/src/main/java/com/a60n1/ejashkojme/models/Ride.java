package com.a60n1.ejashkojme.models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Ride {

    public String from;
    private String address;

    public Ride() {

    }

    public Ride(String from, String address) {
        this.from = from;
        this.address = address;
    }
}
