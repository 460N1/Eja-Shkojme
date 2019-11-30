package com.a60n1.ejashkojme.models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Message {

    public String message;
    public long timestamp;
    public String from;
    private String type;
    private boolean seen;

    public Message() {
        // Default constructor required for calls to DataSnapshot.getValue(Message.class)
    }

    public Message(String message, String from, String type, long timestamp, boolean seen) {
        this.message = message;
        this.from = from;
        this.type = type;
        this.timestamp = timestamp;
        this.seen = seen;
    }
}