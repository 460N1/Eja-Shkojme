package com.a60n1.ejashkojme.fragment

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.Query

class RecentPostsFragment : PostListFragment() {
    override fun getQuery(databaseReference: DatabaseReference): Query { // shfaqja e 20 postimeve te fundit
        return databaseReference.child("posts")
                .limitToFirst(20)
    }
}