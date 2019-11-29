package com.a60n1.ejashkojme.models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {

    public String name;
    public String email;
    public String status;
    public String image;
    public String thumb_image;
    private String device_token;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String name, String email, String device_token) {
        this.name = name;
        this.email = email;
        this.status = "Hi there, I'm using EjaShkojme.";
        this.image = "default";
        this.thumb_image = "default";
        this.device_token = device_token;
    }

}
