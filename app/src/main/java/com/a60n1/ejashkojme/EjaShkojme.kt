package com.a60n1.ejashkojme

import android.app.Application
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import net.danlew.android.joda.JodaTimeAndroid

@Suppress("unused")
class EjaShkojme : Application() {
    private var mUserDatabase: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null
    override fun onCreate() {
        super.onCreate()
        JodaTimeAndroid.init(this)
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        val builder = Picasso.Builder(this)
        builder.downloader(OkHttp3Downloader(this, Long.MAX_VALUE))
        val built = builder.build()
        built.setIndicatorsEnabled(true)
        built.isLoggingEnabled = true
        Picasso.setSingletonInstance(built)
        mAuth = FirebaseAuth.getInstance()
        if (mAuth!!.currentUser != null) {
            mUserDatabase = FirebaseDatabase.getInstance()
                    .reference.child("users").child(mAuth!!.currentUser!!.uid)
            mUserDatabase!!.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    @Suppress("SENSELESS_COMPARISON")
                    if (dataSnapshot != null)
                        mUserDatabase!!.child("online").onDisconnect().setValue(ServerValue.TIMESTAMP)
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
        }
    }
}