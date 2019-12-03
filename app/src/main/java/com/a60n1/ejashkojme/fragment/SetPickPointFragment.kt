@file:Suppress("DEPRECATION")

package com.a60n1.ejashkojme.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.Toast
import com.a60n1.ejashkojme.R
import com.a60n1.ejashkojme.models.Post
import com.a60n1.ejashkojme.models.User
import com.google.android.gms.location.places.GeoDataClient
import com.google.android.gms.location.places.Places
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.*
import java.util.*

class SetPickPointFragment : BaseFragment() {
    private var mGeoDataClient: GeoDataClient? = null
    private var mSearchOriginText: AutoCompleteTextView? = null
    private var mSearchDestinationText: AutoCompleteTextView? = null
    private var mLayoutOrigin: TextInputLayout? = null
    private var mLayoutDestination: TextInputLayout? = null
    private var mSubmitButton: FloatingActionButton? = null
    private var mDatabase: DatabaseReference? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? { // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_setpickpoint, container, false)
        mDatabase = FirebaseDatabase.getInstance().reference
        mGeoDataClient = Places.getGeoDataClient(activity!!, null)
        mLayoutOrigin = view.findViewById(R.id.field_pickorigin_layout)
        mLayoutDestination = view.findViewById(R.id.field_pickdestination_layout)
        mSearchOriginText = view.findViewById(R.id.pickorigin_text)
        mSearchDestinationText = view.findViewById(R.id.pickdestination_text)
        mSubmitButton = view.findViewById(R.id.fab_submit_post)
        mSubmitButton!!.setOnClickListener { submitPost() }
        return view
    }

    private fun submitPost() {
        if (!validateOrigin()) {
            return
        }
        if (!validateDestination()) {
            return
        }
        val origin = mSearchOriginText!!.text.toString()
        val destination = mSearchDestinationText!!.text.toString()
        // Disable button so there are no multi-posts
        setEditingEnabled(false)
        Toast.makeText(activity, "Posting...", Toast.LENGTH_SHORT).show()
        val userId = uid
        mDatabase!!.child("users").child(userId).addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) { // Get user value
                        val user = dataSnapshot.getValue(User::class.java)
                        if (user == null) { // User is null, error out
                            Log.e(TAG, "User $userId is unexpectedly null")
                            Toast.makeText(activity,
                                    "Error: could not fetch user.",
                                    Toast.LENGTH_SHORT).show()
                        } else { // Write new post
                            writeNewPost(userId, user.name, mainActivity!!.currentTitle, mainActivity!!.currentBody, mainActivity!!.currentDate, mainActivity!!.currentTime, origin, destination)
                        }
                        // Finish this Activity, back to the stream
                        setEditingEnabled(true)
                        mainActivity!!.onSubmitPostBtnClicked()
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.w(TAG, "getUser:onCancelled", databaseError.toException())
                        setEditingEnabled(true)
                    }
                })
    }

    @SuppressLint("RestrictedApi")
    private fun setEditingEnabled(enabled: Boolean) {
        mSearchOriginText!!.isEnabled = enabled
        mSearchDestinationText!!.isEnabled = enabled
        if (enabled) {
            mSubmitButton!!.visibility = View.VISIBLE
        } else {
            mSubmitButton!!.visibility = View.GONE
        }
    }

    private fun writeNewPost(userId: String, author: String?, title: String, body: String, date: String, time: String, origin: String, destination: String) {
        val key = mDatabase!!.child("posts").push().key
        val post = Post(userId, author, title, body, date, time, origin, destination)
        val postValues = post.toMap()
        val childUpdates: MutableMap<String, Any> = HashMap()
        childUpdates["/posts/$key"] = postValues
        childUpdates["/user-posts/$userId/$key"] = postValues
        mDatabase!!.updateChildren(childUpdates)
    }

    private fun validateOrigin(): Boolean {
        val origin = mSearchOriginText!!.text.toString().trim { it <= ' ' }
        if (origin.isEmpty()) {
            mLayoutOrigin!!.error = getString(R.string.err_msg_origin)
            return false
        } else {
            mLayoutOrigin!!.isErrorEnabled = false
        }
        return true
    }

    private fun validateDestination(): Boolean {
        val destination = mSearchDestinationText!!.text.toString().trim { it <= ' ' }
        if (destination.isEmpty()) {
            mLayoutDestination!!.error = getString(R.string.err_msg_destination)
            return false
        } else {
            mLayoutDestination!!.isErrorEnabled = false
        }
        return true
    }

    companion object {
        private const val TAG = "460N1_DEV_PickPoint"
        fun newInstance(): SetPickPointFragment {
            return SetPickPointFragment()
        }
    }
}