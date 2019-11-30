package com.a60n1.ejashkojme.models

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
class Message {
    @JvmField
    var message: String? = null
    @JvmField
    var timestamp: Long = 0
    @JvmField
    var from: String? = null
    private var type: String? = null
    private var seen = false

    constructor() { // Default constructor required for calls to DataSnapshot.getValue(Message.class)
    }

    constructor(message: String?, from: String?, type: String?, timestamp: Long, seen: Boolean) {
        this.message = message
        this.from = from
        this.type = type
        this.timestamp = timestamp
        this.seen = seen
    }
}