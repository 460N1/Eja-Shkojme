package com.a60n1.ejashkojme.viewholder

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.a60n1.ejashkojme.R
import com.a60n1.ejashkojme.models.Message
import com.a60n1.ejashkojme.models.User
import com.a60n1.ejashkojme.utils.TimeUtils.formatDateTime
import com.google.firebase.database.*
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    @Suppress("JoinDeclarationAndAssignment")
    var messageText: TextView
    private var timeText: TextView
    var profileAvatar: CircleImageView
    private var mUserDatabase: DatabaseReference? = null
    fun bind(message: Message) {
        messageText.text = message.message
        timeText.text = formatDateTime(message.timestamp)
        val userId = message.from
        mUserDatabase = FirebaseDatabase.getInstance().reference
                .child("users").child(userId!!)
        mUserDatabase!!.keepSynced(true)
        val userListener: ValueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user = dataSnapshot.getValue(User::class.java) ?: return
                val thumbnail = user.thumbImage
                if (thumbnail != "default")
                    Picasso.get().load(thumbnail).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.default_avatar).into(profileAvatar, object : Callback {
                                override fun onSuccess() {}
                                override fun onError(e: Exception) {
                                    Picasso.get().load(thumbnail).placeholder(R.drawable.default_avatar).into(profileAvatar)
                                }
                            })
                else
                    Picasso.get().load(thumbnail).placeholder(R.drawable.default_avatar).into(profileAvatar)
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        }
        mUserDatabase!!.addValueEventListener(userListener)
    }

    init {
        messageText = itemView.findViewById(R.id.text_message_body)
        timeText = itemView.findViewById(R.id.text_message_time)
        profileAvatar = itemView.findViewById(R.id.image_message_profile)
    }
}