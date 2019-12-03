package com.a60n1.ejashkojme

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.a60n1.ejashkojme.adapter.MessageListAdapter
import com.a60n1.ejashkojme.models.Message
import com.a60n1.ejashkojme.utils.TimeUtils.getLastSeenTime
import com.google.firebase.database.*
import java.util.*

class ChatActivity : BaseActivity() {
    private val messagesList: MutableList<Message> = ArrayList()
    private var mChatUser: String? = null
    private var mChatbox: EditText? = null
    private var mSendButton: ImageButton? = null
    private var mTitle: TextView? = null
    private var mLastSeen: TextView? = null
    private var mDatabase: DatabaseReference? = null
    private var mMessageRecycler: RecyclerView? = null
    private var mLinearManager: LinearLayoutManager? = null
    private var mAdapter: MessageListAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        val toolbar = findViewById<Toolbar>(R.id.toolbar_chat)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        actionBar.setDisplayShowCustomEnabled(true)
        val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        @SuppressLint("InflateParams") val actionBarView = Objects.requireNonNull(inflater).inflate(R.layout.chat_custom_app_bar, null)
        actionBar.customView = actionBarView
        mTitle = findViewById(R.id.chat_bar_title)
        mLastSeen = findViewById(R.id.chat_bar_seen)
        mDatabase = FirebaseDatabase.getInstance().reference
        mChatUser = intent.getStringExtra("user_id")
        val name = intent.getStringExtra("user_name")
        mChatbox = findViewById(R.id.edittext_chatbox)
        mMessageRecycler = findViewById(R.id.recyclerview_message_list)
        mAdapter = MessageListAdapter(this, messagesList, uid)
        mLinearManager = LinearLayoutManager(this)
        mLinearManager!!.stackFromEnd = true
        mMessageRecycler!!.layoutManager = mLinearManager
        mMessageRecycler!!.itemAnimator = DefaultItemAnimator()
        mMessageRecycler!!.setHasFixedSize(true)
        mMessageRecycler!!.adapter = mAdapter
        mDatabase!!.child("chat").child(uid).child(mChatUser!!).child("seen").setValue(true)
        loadMessages()
        mTitle!!.text = name
        mDatabase!!.child("users").child(mChatUser!!).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val online = dataSnapshot.child("online").value.toString()
                if (online == "true")
                    mLastSeen!!.setText(R.string.online)
                else {
                    val lastTime = Objects.requireNonNull(online).toLong()
                    mLastSeen!!.text = getLastSeenTime(lastTime)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
        mDatabase!!.child("users").child(uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (!dataSnapshot.hasChild(mChatUser!!)) {
                    val chatAddMap: MutableMap<String?, Any?> = HashMap()
                    chatAddMap["seen"] = false
                    chatAddMap["timestamp"] = ServerValue.TIMESTAMP
                    val chatUserMap: MutableMap<String?, Any?> = HashMap()
                    chatUserMap["chat/$uid/$mChatUser"] = chatAddMap
                    chatUserMap["chat/$mChatUser/$uid"] = chatAddMap
                    mDatabase!!.updateChildren(chatUserMap) { databaseError: DatabaseError?, _: DatabaseReference? ->
                        if (databaseError != null)
                            Log.d(TAG, databaseError.message)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
        mMessageRecycler!!.addOnLayoutChangeListener { _: View?, _: Int, _: Int, _: Int, bottom: Int, _: Int, _: Int, _: Int, oldBottom: Int ->
            if (bottom < oldBottom && mMessageRecycler!!.adapter!!.itemCount > 0)
                mMessageRecycler!!.postDelayed({ mMessageRecycler!!.smoothScrollToPosition(mMessageRecycler!!.adapter!!.itemCount - 1) }, 100)
        }
        mSendButton = findViewById(R.id.button_chatbox_send)
        mSendButton!!.setOnClickListener { sentMessage() }
    }

    private fun loadMessages() {
        val messageRef = mDatabase!!.child("messages").child(uid).child(mChatUser!!)
        val messageQuery = messageRef.orderByChild("timestamp").limitToLast(100)
        messageQuery.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                val message = dataSnapshot.getValue(Message::class.java)!!
                messagesList.add(message)
                mAdapter!!.notifyDataSetChanged()
                mMessageRecycler!!.smoothScrollToPosition(messagesList.size - 1)
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {}
            override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
            override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            hideKeyboard()
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun sentMessage() {
        val message = mChatbox!!.text.toString().trim { it <= ' ' }
        if (message.isNotEmpty()) {
            val currentUserRef = "messages/$uid/$mChatUser"
            val chatUserRef = "messages/$mChatUser/$uid"
            val userMessagePush = mDatabase!!.child("messages")
                    .child(uid).child(mChatUser!!).push()
            val pushId = userMessagePush.key
            val messageMap: MutableMap<String, Any?> = HashMap()
            messageMap["message"] = message
            messageMap["type"] = "text"
            messageMap["timestamp"] = ServerValue.TIMESTAMP
            messageMap["from"] = uid
            messageMap["seen"] = false
            val messageUserMap: MutableMap<String, Any?> = HashMap()
            messageUserMap["$currentUserRef/$pushId"] = messageMap
            messageUserMap["$chatUserRef/$pushId"] = messageMap
            mChatbox!!.setText("")
            mDatabase!!.child("chat").child(uid).child(mChatUser!!).child("seen").setValue(true)
            mDatabase!!.child("chat").child(uid).child(mChatUser!!).child("timestamp").setValue(ServerValue.TIMESTAMP)
            mDatabase!!.child("chat").child(mChatUser!!).child(uid).child("seen").setValue(false)
            mDatabase!!.child("chat").child(mChatUser!!).child(uid).child("timestamp").setValue(ServerValue.TIMESTAMP)
            mDatabase!!.updateChildren(messageUserMap) { databaseError: DatabaseError?, _: DatabaseReference? ->
                if (databaseError != null)
                    Log.d(TAG, databaseError.message)
            }
        }
    }

    companion object {
        private const val TAG = "460N1_DEV_ChatActivity"
    }
}