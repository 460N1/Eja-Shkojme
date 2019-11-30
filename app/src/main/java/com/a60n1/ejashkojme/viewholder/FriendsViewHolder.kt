package com.a60n1.ejashkojme.viewholder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.a60n1.ejashkojme.R
import de.hdodenhof.circleimageview.CircleImageView

class FriendsViewHolder(var mView: View) : RecyclerView.ViewHolder(mView) {
    @Suppress("JoinDeclarationAndAssignment")
    var userAvatar: CircleImageView
    var nameView: TextView
    var statusView: TextView
    private val statusIcon: ImageView
    fun setUserStatus(status: String) {
        if (status == "true") {
            statusIcon.visibility = View.VISIBLE
        } else {
            statusIcon.visibility = View.INVISIBLE
        }
    }

    init {
        userAvatar = mView.findViewById(R.id.user_photo)
        nameView = mView.findViewById(R.id.user_name)
        statusView = mView.findViewById(R.id.user_status)
        statusIcon = mView.findViewById(R.id.status_icon)
    }
}