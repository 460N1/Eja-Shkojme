package com.a60n1.ejashkojme.models

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
class Friends {
    var date: String? = null

    @Suppress("unused")
    constructor()

    @Suppress("unused")
    constructor(date: String?) {
        this.date = date
    }
}