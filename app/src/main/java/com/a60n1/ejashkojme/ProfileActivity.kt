package com.a60n1.ejashkojme

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.a60n1.ejashkojme.models.Notification
import com.a60n1.ejashkojme.utils.TimeUtils.getDaysSince
import com.google.firebase.database.*
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*

class ProfileActivity : BaseActivity() {
    private var mDatabase: DatabaseReference? = null
    private var mUserDatabase: DatabaseReference? = null
    private var mFriendDatabase: DatabaseReference? = null
    private var mFriendReqDatabase: DatabaseReference? = null
    private var mProfileImage: ImageView? = null
    private var mName: TextView? = null
    private var mStatus: TextView? = null
    private var mDays: TextView? = null
    private var mSendReqButton: Button? = null
    private var mDeclineButton: Button? = null
    private var currentState: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        val userId = intent.getStringExtra("user_id")
        mDatabase = FirebaseDatabase.getInstance().reference
        mUserDatabase = FirebaseDatabase.getInstance().reference.child("users").child(userId!!)
        mFriendDatabase = FirebaseDatabase.getInstance().reference.child("friends")
        mFriendReqDatabase = FirebaseDatabase.getInstance().reference.child("friend_req")
        mProfileImage = findViewById(R.id.profile_image)
        mName = findViewById(R.id.profile_displayName)
        mStatus = findViewById(R.id.profile_status)
        mDays = findViewById(R.id.profile_friends_days)
        mSendReqButton = findViewById(R.id.profile_send_req_btn)
        mDeclineButton = findViewById(R.id.profile_decline_btn)
        currentState = "not_friends"
        mDeclineButton!!.visibility = View.INVISIBLE
        mDeclineButton!!.isEnabled = false
        showProgressDialog()
        mUserDatabase!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val name = dataSnapshot.child("name").value.toString()
                val status = dataSnapshot.child("status").value.toString()
                val image = dataSnapshot.child("image").value.toString()
                mName!!.text = name
                mStatus!!.text = status
                if (Objects.requireNonNull(image) != "default") {
                    Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_avatar).into(mProfileImage, object : Callback {
                        override fun onSuccess() {}
                        override fun onError(e: Exception) {
                            Picasso.get().load(image).placeholder(R.drawable.default_avatar).into(mProfileImage)
                        }
                    })
                } else {
                    Picasso.get().load(image).placeholder(R.drawable.default_avatar).into(mProfileImage)
                }
                if (uid == userId) {
                    mDays!!.visibility = View.INVISIBLE
                    mDeclineButton!!.isEnabled = false
                    mDeclineButton!!.visibility = View.INVISIBLE
                    mSendReqButton!!.isEnabled = false
                    mSendReqButton!!.visibility = View.INVISIBLE
                }
                // accept request
                mFriendReqDatabase!!.child(uid).addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.hasChild(userId)) {
                            val reqType = dataSnapshot.child(userId).child("request_type").value.toString()
                            if (Objects.requireNonNull(reqType) == "received") {
                                currentState = "req_received"
                                mSendReqButton!!.setText(R.string.accept_req_text)
                                mDays!!.visibility = View.INVISIBLE
                                mDeclineButton!!.visibility = View.INVISIBLE
                                mDeclineButton!!.isEnabled = true
                            } else if (reqType == "sent") {
                                currentState = "req_sent"
                                mSendReqButton!!.setText(R.string.cancel_req_text)
                                mDeclineButton!!.visibility = View.INVISIBLE
                                mDays!!.visibility = View.INVISIBLE
                                mDeclineButton!!.isEnabled = false
                            }
                            hideProgressDialog()
                        } else {
                            mFriendDatabase!!.child(uid).addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    if (dataSnapshot.hasChild(userId)) {
                                        currentState = "friends"
                                        mDays!!.visibility = View.VISIBLE
                                        val date = dataSnapshot.child(userId).child("date").value.toString()
                                        mDays!!.text = getDaysSince(date)
                                        mSendReqButton!!.setText(R.string.unfriend_text)
                                        mDeclineButton!!.visibility = View.INVISIBLE
                                        mDeclineButton!!.isEnabled = false
                                    }
                                    hideProgressDialog()
                                }

                                override fun onCancelled(databaseError: DatabaseError) {
                                    hideProgressDialog()
                                }
                            })
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {}
                })
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
        mSendReqButton!!.setOnClickListener {
            mSendReqButton!!.isEnabled = false
            // not friend state
            if (currentState == "not_friends") {
                val notificationDatabase = mDatabase!!.child("notifications").child(userId).push()
                val notificationId = notificationDatabase.key
                val notification = Notification(uid, "request")
                val requestMap: MutableMap<String, Any> = HashMap()
                requestMap["friend_req/$uid/$userId/request_type"] = "sent"
                requestMap["friend_req/$userId/$uid/request_type"] = "received"
                requestMap["notifications/$userId/$notificationId"] = notification
                mDatabase!!.updateChildren(requestMap) { databaseError: DatabaseError?, _: DatabaseReference? ->
                    if (databaseError != null) {
                        Toast.makeText(this@ProfileActivity, "Error sending request", Toast.LENGTH_SHORT).show()
                    } else {
                        currentState = "req_sent"
                        mSendReqButton!!.setText(R.string.cancel_req_text)
                    }
                    mSendReqButton!!.isEnabled = true
                }
            }
            // sent request state
            if (currentState == "req_sent") {
                mFriendReqDatabase!!.child(uid).child(userId).removeValue().addOnSuccessListener {
                    mFriendReqDatabase!!.child(userId).child(uid).removeValue().addOnSuccessListener {
                        mSendReqButton!!.isEnabled = true
                        currentState = "not_friends"
                        mSendReqButton!!.setText(R.string.send_req_text)
                        mDays!!.visibility = View.INVISIBLE
                        mDeclineButton!!.visibility = View.INVISIBLE
                        mDeclineButton!!.isEnabled = false
                    }
                }
            }
            // received request state
            if (currentState == "req_received") {
                @SuppressLint("SimpleDateFormat") val currentDate = SimpleDateFormat("M/dd/yyyy").format(Date())
                val friendsMap: MutableMap<String, Any?> = HashMap()
                friendsMap["friends/$uid/$userId/date"] = currentDate
                friendsMap["friends/$userId/$uid/date"] = currentDate
                friendsMap["friend_req/$uid/$userId"] = null
                friendsMap["friend_req/$userId/$uid"] = null
                mDatabase!!.updateChildren(friendsMap) { databaseError: DatabaseError?, _: DatabaseReference? ->
                    if (databaseError == null) {
                        mSendReqButton!!.isEnabled = true
                        currentState = "friends"
                        mSendReqButton!!.setText(R.string.unfriend_text)
                        mDeclineButton!!.visibility = View.INVISIBLE
                        mDays!!.text = getDaysSince(currentDate)
                        mDays!!.visibility = View.VISIBLE
                        mDeclineButton!!.isEnabled = false
                    } else {
                        val error = databaseError.message
                        Toast.makeText(this@ProfileActivity, error, Toast.LENGTH_SHORT).show()
                    }
                }
            }
            // friend state
            if (currentState == "friends") {
                val unfriendMap: MutableMap<String?, Any?> = HashMap()
                unfriendMap["friends/$uid/$userId"] = null
                unfriendMap["friends/$userId/$uid"] = null
                mDatabase!!.updateChildren(unfriendMap) { databaseError: DatabaseError?, _: DatabaseReference? ->
                    if (databaseError == null) {
                        currentState = "not_friends"
                        mSendReqButton!!.setText(R.string.send_req_text)
                        mDeclineButton!!.visibility = View.INVISIBLE
                        mDays!!.visibility = View.INVISIBLE
                        mDeclineButton!!.isEnabled = false
                    } else {
                        val error = databaseError.message
                        Toast.makeText(this@ProfileActivity, error, Toast.LENGTH_SHORT).show()
                    }
                    mSendReqButton!!.isEnabled = true
                }
            }
        }
    }
}