package com.a60n1.ejashkojme.viewholder

import android.graphics.Typeface
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.a60n1.ejashkojme.R
import com.a60n1.ejashkojme.models.Message
import com.a60n1.ejashkojme.utils.TimeUtils.getLastMessageTime
import de.hdodenhof.circleimageview.CircleImageView

class ConversationViewHolder(var mView: View) : RecyclerView.ViewHolder(mView) {
    @Suppress("JoinDeclarationAndAssignment")
    var statusView: TextView
    var nameView: TextView
    var timestampView: TextView
    var userAvatar: CircleImageView
    private val statusIcon: ImageView
    fun setMessage(message: Message, seen: Boolean) {
        statusView.text = message.message
        timestampView.text = getLastMessageTime(message.timestamp)
        if (!seen)
            statusView.setTypeface(statusView.typeface, Typeface.BOLD)
        else
            statusView.setTypeface(statusView.typeface, Typeface.NORMAL)
    }

    fun setUserStatus(status: String) {
        if (status == "true")
            statusIcon.visibility = View.VISIBLE
        else
            statusIcon.visibility = View.INVISIBLE
    }

    init {
        statusView = mView.findViewById(R.id.conv_user_last_message)
        nameView = mView.findViewById(R.id.conv_user_name)
        timestampView = mView.findViewById(R.id.conv_timestamp)
        userAvatar = mView.findViewById(R.id.conv_user_photo)
        statusIcon = mView.findViewById(R.id.conv_status_icon)
    }
}