package com.a60n1.ejashkojme.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.a60n1.ejashkojme.ChatActivity
import com.a60n1.ejashkojme.DriverMapActivity
import com.a60n1.ejashkojme.ProfileActivity
import com.a60n1.ejashkojme.R
import com.a60n1.ejashkojme.models.Friends
import com.a60n1.ejashkojme.models.Post
import com.a60n1.ejashkojme.models.Ride
import com.a60n1.ejashkojme.viewholder.FriendsViewHolder
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.*
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import java.util.*

class FriendsFragment : BaseFragment() {
    private var mAdapter: FirebaseRecyclerAdapter<Friends, FriendsViewHolder>? = null
    private var mRecycler: RecyclerView? = null
    private var mManager: LinearLayoutManager? = null
    private var mOrigin: String? = null
    private var mFriendsDatabase: DatabaseReference? = null
    private var mUserDatabase: DatabaseReference? = null
    private var mDatabase: DatabaseReference? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? { // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_friends, container, false)
        mRecycler = rootView.findViewById(R.id.friends_list)
        mRecycler!!.setHasFixedSize(true)
        val itemDecorator = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        itemDecorator.setDrawable(ContextCompat.getDrawable(context!!, R.drawable.divider_friends)!!)
        mRecycler!!.addItemDecoration(itemDecorator)
        val userId = uid
        mDatabase = FirebaseDatabase.getInstance().reference
        mUserDatabase = FirebaseDatabase.getInstance().reference.child("users")
        mFriendsDatabase = FirebaseDatabase.getInstance().reference.child("friends").child(userId)
        mUserDatabase!!.keepSynced(true)
        mFriendsDatabase!!.keepSynced(true)
        return rootView
    }

    @Suppress("UNCHECKED_CAST")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mManager = LinearLayoutManager(activity)
        mManager!!.reverseLayout = true
        mManager!!.stackFromEnd = true
        mRecycler!!.layoutManager = mManager
        val friendsQuery = getQuery(mFriendsDatabase)
        val options: FirebaseRecyclerOptions<*> = FirebaseRecyclerOptions.Builder<Friends>()
                .setQuery(friendsQuery!!, Friends::class.java)
                .build()
        mAdapter = object : FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(options as FirebaseRecyclerOptions<Friends>) {
            override fun onBindViewHolder(holder: FriendsViewHolder, position: Int, model: Friends) {
                val listUserId = getRef(position).key
                mUserDatabase!!.child(listUserId!!).addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val name = dataSnapshot.child("name").value.toString()
                        val thumbnail = dataSnapshot.child("thumb_image").value.toString()
                        val status = dataSnapshot.child("status").value.toString()
                        holder.statusView.text = status
                        holder.nameView.text = name
                        if (dataSnapshot.hasChild("online")) {
                            val userOnline = dataSnapshot.child("online").value.toString()
                            holder.setUserStatus(userOnline)
                        }
                        holder.mView.setOnClickListener {
                            val popup = PopupMenu(context, holder.mView)
                            popup.inflate(R.menu.menu_friends_action)
                            popup.setOnMenuItemClickListener { item: MenuItem ->
                                when (item.itemId) {
                                    R.id.friends_profile_action -> {
                                        val profileIntent = Intent(activity, ProfileActivity::class.java)
                                        profileIntent.putExtra("user_id", listUserId)
                                        startActivity(profileIntent)
                                    }
                                    R.id.friends_chat_action -> {
                                        val chatIntent = Intent(context, ChatActivity::class.java)
                                        chatIntent.putExtra("user_id", listUserId)
                                        chatIntent.putExtra("user_name", name)
                                        startActivity(chatIntent)
                                    }
                                    R.id.friends_ride_action -> {
                                        // get the pickup point address
                                        val userPostsDatabase = FirebaseDatabase.getInstance().reference.child("user-posts")
                                        val lastPost: Query = userPostsDatabase.child(listUserId)
                                        lastPost.addListenerForSingleValueEvent(object : ValueEventListener {
                                            override fun onDataChange(dataSnapshot1: DataSnapshot) {
                                                for (data in dataSnapshot1.children) {
                                                    val post = data.getValue(Post::class.java)!!
                                                    mOrigin = post.origin
                                                }
                                                val driverMapIntent = Intent(context, DriverMapActivity::class.java)
                                                driverMapIntent.putExtra("user_id", listUserId)
                                                driverMapIntent.putExtra("user_name", name)
                                                driverMapIntent.putExtra("thumb_image", thumbnail)
                                                driverMapIntent.putExtra("origin", mOrigin)
                                                val ridesDatabase = mDatabase!!.child("rides").child(listUserId).push()
                                                val ridesId = ridesDatabase.key
                                                val ride = Ride(uid, mOrigin)
                                                val rideMap: MutableMap<String, Any> = HashMap()
                                                rideMap["rides/$listUserId/$ridesId"] = ride
                                                mDatabase!!.updateChildren(rideMap) { databaseError: DatabaseError?, _: DatabaseReference? ->
                                                    if (databaseError != null)
                                                        Toast.makeText(context, "Error starting ride", Toast.LENGTH_SHORT).show()
                                                }
                                                startActivity(driverMapIntent)
                                            }

                                            override fun onCancelled(databaseError: DatabaseError) {}
                                        })
                                    }
                                }
                                false
                            }
                            popup.show()
                        }
                        if (thumbnail != "default")
                            Picasso.get().load(thumbnail).networkPolicy(NetworkPolicy.OFFLINE)
                                    .placeholder(R.drawable.default_avatar).into(holder.userAvatar, object : Callback {
                                        override fun onSuccess() {}
                                        override fun onError(e: Exception) {
                                            Picasso.get().load(thumbnail).placeholder(R.drawable.default_avatar).into(holder.userAvatar)
                                        }
                                    })
                        else
                            Picasso.get().load(thumbnail).placeholder(R.drawable.default_avatar).into(holder.userAvatar)
                    }

                    override fun onCancelled(databaseError: DatabaseError) {}
                })
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendsViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                return FriendsViewHolder(inflater.inflate(R.layout.item_user, parent, false))
            }
        }
        mRecycler!!.adapter = mAdapter
    }

    override fun onStart() {
        super.onStart()
        if (mAdapter != null)
            mAdapter!!.startListening()
    }

    override fun onStop() {
        super.onStop()
        if (mAdapter != null)
            mAdapter!!.stopListening()
    }

    private fun getQuery(databaseReference: DatabaseReference?): Query? { // All my friends
        return databaseReference
    }
}