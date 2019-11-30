package com.a60n1.ejashkojme.models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Conversation {

    public boolean seen;
    public long timestamp;

    public Conversation() {

    }

    public Conversation(boolean seen, long timestamp) {
        this.seen = seen;
        this.timestamp = timestamp;
    }
}
