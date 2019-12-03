package com.a60n1.ejashkojme.viewholder

import android.annotation.SuppressLint
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.a60n1.ejashkojme.R
import com.a60n1.ejashkojme.models.Post
import com.a60n1.ejashkojme.models.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    @Suppress("JoinDeclarationAndAssignment")
    private val authorAvatar: CircleImageView
    private val titleView: TextView
    @JvmField
    var authorView: TextView
    @JvmField
    var starView: ImageView
    private val numStarsView: TextView
    private val bodyView: TextView
    private val datetimeView: TextView
    private val originView: TextView
    private val destinationView: TextView
    @SuppressLint("SetTextI18n")
    fun bindToPost(post: Post, starClickListener: View.OnClickListener?) {
        val uid = post.uid
        val userDatabase = FirebaseDatabase.getInstance().reference
                .child("users").child(uid!!)
        userDatabase.keepSynced(true)
        val userListener: ValueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user = dataSnapshot.getValue(User::class.java) ?: return
                val thumbImage = user.thumb_image
                if (thumbImage != "default")
                    Picasso.get().load(thumbImage).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.default_avatar).into(authorAvatar, object : Callback {
                                override fun onSuccess() {}
                                override fun onError(e: Exception) {
                                    Picasso.get().load(thumbImage).placeholder(R.drawable.default_avatar).into(authorAvatar)
                                }
                            })
                else
                    Picasso.get().load(thumbImage).placeholder(R.drawable.default_avatar).into(authorAvatar)
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        }
        userDatabase.addValueEventListener(userListener)
        authorView.text = post.author
        titleView.text = post.title
        numStarsView.text = post.starCount.toString()
        bodyView.text = post.body
        datetimeView.text = "Date/time: " + post.date + " " + post.time
        originView.text = "From: " + post.origin
        destinationView.text = "To: " + post.destination
        starView.setOnClickListener(starClickListener)
    }

    init {
        authorAvatar = itemView.findViewById(R.id.post_author_photo)
        titleView = itemView.findViewById(R.id.post_title)
        authorView = itemView.findViewById(R.id.post_author)
        starView = itemView.findViewById(R.id.star)
        numStarsView = itemView.findViewById(R.id.post_num_stars)
        bodyView = itemView.findViewById(R.id.post_body)
        datetimeView = itemView.findViewById(R.id.post_datetime)
        originView = itemView.findViewById(R.id.post_origin)
        destinationView = itemView.findViewById(R.id.post_destination)
    }
}