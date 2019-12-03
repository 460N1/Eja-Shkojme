package com.a60n1.ejashkojme.models

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
class Ride {
    var from: String? = null
    private var address: String? = null

    @Suppress("unused")
    constructor()

    constructor(from: String?, address: String?) {
        this.from = from
        this.address = address
    }
}