package com.a60n1.ejashkojme.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.a60n1.ejashkojme.ChatActivity
import com.a60n1.ejashkojme.R
import com.a60n1.ejashkojme.models.Conversation
import com.a60n1.ejashkojme.models.Message
import com.a60n1.ejashkojme.viewholder.ConversationViewHolder
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.*
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso

class ConversationFragment : BaseFragment() {
    private var mRecycler: RecyclerView? = null
    private var mChatDatabase: DatabaseReference? = null
    private var mUserDatabase: DatabaseReference? = null
    private var mMessageDatabase: DatabaseReference? = null
    private var mManager: LinearLayoutManager? = null
    private var mAdapter: FirebaseRecyclerAdapter<Conversation, ConversationViewHolder>? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? { // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_conversation, container, false)
        mRecycler = rootView.findViewById(R.id.conv_list)
        mRecycler!!.setHasFixedSize(true)
        val userId = uid
        val itemDecorator = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        itemDecorator.setDrawable(ContextCompat.getDrawable(context!!, R.drawable.divider_friends)!!)
        mRecycler!!.addItemDecoration(itemDecorator)
        mChatDatabase = FirebaseDatabase.getInstance().reference.child("chat").child(userId)
        mChatDatabase!!.keepSynced(true)
        mUserDatabase = FirebaseDatabase.getInstance().reference.child("users")
        mMessageDatabase = FirebaseDatabase.getInstance().reference.child("messages").child(userId)
        mUserDatabase!!.keepSynced(true)
        return rootView
    }

    @Suppress("UNCHECKED_CAST")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mManager = LinearLayoutManager(activity)
        mManager!!.reverseLayout = true
        mManager!!.stackFromEnd = true
        mRecycler!!.layoutManager = mManager
        val convQuery = mChatDatabase!!.orderByChild("timestamp")
        val options: FirebaseRecyclerOptions<*> = FirebaseRecyclerOptions.Builder<Conversation>()
                .setQuery(convQuery, Conversation::class.java)
                .build()
        mAdapter = object : FirebaseRecyclerAdapter<Conversation, ConversationViewHolder>(options as FirebaseRecyclerOptions<Conversation>) {
            override fun onBindViewHolder(holder: ConversationViewHolder, position: Int, model: Conversation) {
                val listUserId = getRef(position).key
                val lastMessage = mMessageDatabase!!.child(listUserId!!).limitToLast(1)
                lastMessage.addChildEventListener(object : ChildEventListener {
                    override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                        val message = dataSnapshot.getValue(Message::class.java)!!
                        holder.setMessage(message, model.seen)
                    }

                    override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {}
                    override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
                    override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
                    override fun onCancelled(databaseError: DatabaseError) {}
                })
                mUserDatabase!!.child(listUserId).addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val name = dataSnapshot.child("name").value.toString()
                        val thumbnail = dataSnapshot.child("thumb_image").value.toString()
                        holder.nameView.text = name
                        if (dataSnapshot.hasChild("online")) {
                            val userOnline = dataSnapshot.child("online").value.toString()
                            holder.setUserStatus(userOnline)
                        }
                        holder.mView.setOnClickListener {
                            val chatIntent = Intent(context, ChatActivity::class.java)
                            chatIntent.putExtra("user_id", listUserId)
                            chatIntent.putExtra("user_name", name)
                            startActivity(chatIntent)
                        }
                        if (thumbnail != "default") {
                            Picasso.get().load(thumbnail).networkPolicy(NetworkPolicy.OFFLINE)
                                    .placeholder(R.drawable.default_avatar).into(holder.userAvatar, object : Callback {
                                        override fun onSuccess() {}
                                        override fun onError(e: Exception) {
                                            Picasso.get().load(thumbnail).placeholder(R.drawable.default_avatar).into(holder.userAvatar)
                                        }
                                    })
                        } else {
                            Picasso.get().load(thumbnail).placeholder(R.drawable.default_avatar).into(holder.userAvatar)
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {}
                })
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                return ConversationViewHolder(inflater.inflate(R.layout.item_conv, parent, false))
            }
        }
        mRecycler!!.adapter = mAdapter
    }

    override fun onStart() {
        super.onStart()
        if (mAdapter != null) {
            mAdapter!!.startListening()
        }
    }

    override fun onStop() {
        super.onStop()
        if (mAdapter != null) {
            mAdapter!!.stopListening()
        }
    }
}