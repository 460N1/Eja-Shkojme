package com.a60n1.ejashkojme.models

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import java.util.*

@IgnoreExtraProperties
class Post {
    @JvmField
    var uid: String? = null
    @JvmField
    var author: String? = null
    @JvmField
    var title: String? = null
    @JvmField
    var body: String? = null
    @JvmField
    var date: String? = null
    @JvmField
    var time: String? = null
    @JvmField
    var origin: String? = null
    @JvmField
    var destination: String? = null
    @JvmField
    var starCount = 0
    @JvmField
    var stars: Map<String, Boolean> = HashMap()

    @Suppress("unused")
    constructor() { // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    constructor(uid: String?, author: String?, title: String?, body: String?, date: String?, time: String?, origin: String?, destination: String?) {
        this.uid = uid
        this.author = author
        this.title = title
        this.body = body
        this.date = date
        this.time = time
        this.origin = origin
        this.destination = destination
    }

    @Exclude
    fun toMap(): Map<String, Any?> {
        val result = HashMap<String, Any?>()
        result["uid"] = uid
        result["author"] = author
        result["title"] = title
        result["body"] = body
        result["date"] = date
        result["time"] = time
        result["origin"] = origin
        result["destination"] = destination
        result["starCount"] = starCount
        result["stars"] = stars
        return result
    }
}