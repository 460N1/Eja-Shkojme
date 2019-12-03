package com.a60n1.ejashkojme.models

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
class Notification {
    var from: String? = null
    private var type: String? = null

    @Suppress("unused")
    constructor()

    constructor(from: String?, type: String?) {
        this.from = from
        this.type = type
    }
}