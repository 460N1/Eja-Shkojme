package com.a60n1.ejashkojme.models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Notification {

    public String from;
    private String type;

    public Notification() {

    }

    public Notification(String from, String type) {
        this.from = from;
        this.type = type;
    }
}
