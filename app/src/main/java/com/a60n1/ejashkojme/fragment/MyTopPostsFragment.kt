package com.a60n1.ejashkojme.fragment

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.Query

class MyTopPostsFragment : PostListFragment() {
    override fun getQuery(databaseReference: DatabaseReference): Query { // My top posts by number of stars
        val myUserId = uid
        return databaseReference.child("user-posts").child(myUserId)
                .orderByChild("starCount")
    }
}