package com.a60n1.ejashkojme.models

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
class Comment {
    @JvmField
    var uid: String? = null
    @JvmField
    var author: String? = null
    @JvmField
    var text: String? = null

    constructor() { // Default constructor required for calls to DataSnapshot.getValue(Comment.class)
    }

    constructor(uid: String?, author: String?, text: String?) {
        this.uid = uid
        this.author = author
        this.text = text
    }
}