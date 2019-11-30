package com.a60n1.ejashkojme.models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Friends {

    public String date;

    public Friends() {

    }

    public Friends(String date) {
        this.date = date;
    }
}
