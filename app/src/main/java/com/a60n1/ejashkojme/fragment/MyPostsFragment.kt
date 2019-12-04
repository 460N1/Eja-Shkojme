package com.a60n1.ejashkojme.fragment

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.Query

class MyPostsFragment : PostListFragment() {
    override fun getQuery(databaseReference: DatabaseReference): Query { // te gjitha postimet
        return databaseReference.child("user-posts")
                .child(uid)
    }
}