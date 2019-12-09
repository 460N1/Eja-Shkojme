package com.a60n1.ejashkojme.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.a60n1.ejashkojme.MainActivity
import com.a60n1.ejashkojme.ProfileActivity
import com.a60n1.ejashkojme.R
import com.a60n1.ejashkojme.models.Comment
import com.a60n1.ejashkojme.models.User
import com.a60n1.ejashkojme.viewholder.CommentViewHolder
import com.google.firebase.database.*
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import java.util.*

class CommentAdapter(private val mContext: Context, private val mDatabaseReference: DatabaseReference)
    : RecyclerView.Adapter<CommentViewHolder>() {
    @Suppress("JoinDeclarationAndAssignment")
    private val mainActivity: MainActivity
    private val mChildEventListener: ChildEventListener?
    private val mCommentIds: MutableList<String?> = ArrayList()
    private val mComments: MutableList<Comment> = ArrayList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val inflater = LayoutInflater.from(mContext)
        val view = inflater.inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = mComments[position]
        holder.authorView.text = comment.author
        holder.bodyView.text = comment.text
        holder.authorView.setOnClickListener {
            val popup = PopupMenu(mContext, holder.authorView)
            popup.inflate(R.menu.menu_user_action)
            popup.setOnMenuItemClickListener { item: MenuItem ->
                if (item.itemId == R.id.profile_action) {
                    val intent = Intent(mContext, ProfileActivity::class.java)
                    intent.putExtra("user_id", comment.uid)
                    mContext.startActivity(intent)
                }
                false
            }
            popup.show()
        }
        val uid = comment.uid
        val userDatabase = FirebaseDatabase.getInstance().reference
                .child("users").child(uid!!)
        userDatabase.keepSynced(true)
        val userListener: ValueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user = dataSnapshot.getValue(User::class.java) ?: return
                val thumbImage = user.thumbImage
                if (thumbImage != "default")
                    Picasso.get().load(thumbImage).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.default_avatar).into(holder.authorAvatar, object : Callback {
                                override fun onSuccess() {}
                                override fun onError(e: Exception) {
                                    Picasso.get().load(thumbImage).placeholder(R.drawable.default_avatar).into(holder.authorAvatar)
                                }
                            })
                else
                    Picasso.get().load(thumbImage).placeholder(R.drawable.default_avatar).into(holder.authorAvatar)
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        }
        userDatabase.addValueEventListener(userListener)
    }

    override fun getItemCount(): Int {
        return mComments.size
    }

    fun cleanupListener() {
        if (mChildEventListener != null)
            mDatabaseReference.removeEventListener(mChildEventListener)
    }

    companion object {
        private const val TAG = "460N_DEV_CommentAdapter"
    }

    init {
        mainActivity = mContext as MainActivity
        // Krijojme ChildEventListener qe prej events
        val childEventListener: ChildEventListener = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.key)
                // Kur ndodh shtimi i komentit
                val comment = dataSnapshot.getValue(Comment::class.java)!!
                // Shtohet ne RecyclerView
                mCommentIds.add(dataSnapshot.key)
                mComments.add(comment)
                notifyItemInserted(mComments.size - 1)
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
                Log.d(TAG, "onChildChanged:" + dataSnapshot.key)
                // Koment ka ndryshu
                // per te ne ardhmen kur edit comment shtohet si feature
                val newComment = dataSnapshot.getValue(Comment::class.java)!!
                val commentKey = dataSnapshot.key
                val commentIndex = mCommentIds.indexOf(commentKey)
                if (commentIndex > -1) { // zevendesimi
                    mComments[commentIndex] = newComment
                    // update UI
                    notifyItemChanged(commentIndex)
                } else
                    Log.w(TAG, "onChildChanged:unknown_child:$commentKey")
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                Log.d(TAG, "onChildRemoved:" + dataSnapshot.key)
                // Largimi i komenteve
                // per ne te ardhmen kur delete comment shtohet si feature
                val commentKey = dataSnapshot.key
                val commentIndex = mCommentIds.indexOf(commentKey)
                if (commentIndex > -1) { // largimi prej listes tkomenteve
                    mCommentIds.removeAt(commentIndex)
                    mComments.removeAt(commentIndex)
                    // update UI
                    notifyItemRemoved(commentIndex)
                } else
                    Log.w(TAG, "onChildRemoved:unknown_child:$commentKey")
            }

            @Suppress("UNUSED_VARIABLE")
            override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {
                Log.d(TAG, "onChildMoved:" + dataSnapshot.key)
                // Funksion i nevojshem
                // nuk perdoret, vetem template
                val movedComment = dataSnapshot.getValue(Comment::class.java)!!
                val commentKey = dataSnapshot.key
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "postComments:onCancelled", databaseError.toException())
                // deshtim i leximit per qfaredo arsye
                Toast.makeText(mContext, "Failed to load comments.",
                        Toast.LENGTH_SHORT).show()
            }
        }
        mDatabaseReference.addChildEventListener(childEventListener)
        // shtimi ne reference qe te ndalet kur aplikacioni te ndalet
        mChildEventListener = childEventListener
    }
}