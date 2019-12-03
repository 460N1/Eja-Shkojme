package com.a60n1.ejashkojme.models

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
class Conversation {
    @JvmField
    var seen = false
    var timestamp: Long = 0

    @Suppress("unused")
    constructor()

    @Suppress("unused")
    constructor(seen: Boolean, timestamp: Long) {
        this.seen = seen
        this.timestamp = timestamp
    }
}