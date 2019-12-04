package com.a60n1.ejashkojme.models

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
class User {
    @JvmField
    var name: String? = null
    @JvmField
    var email: String? = null
    @JvmField
    var status: String? = null
    @JvmField
    var image: String? = null
    @JvmField
    var thumb_image: String? = null
    private var device_token: String? = null

    constructor()

    constructor(name: String?, email: String?, device_token: String?) {
        this.name = name
        this.email = email
        status = "Hi there, I'm using EjaShkojme."
        image = "default"
        thumb_image = "default"
        this.device_token = device_token
    }
}