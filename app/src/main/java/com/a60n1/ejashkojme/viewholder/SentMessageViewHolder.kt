package com.a60n1.ejashkojme.viewholder

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.a60n1.ejashkojme.R
import com.a60n1.ejashkojme.models.Message
import com.a60n1.ejashkojme.utils.TimeUtils.formatDateTime

class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    @Suppress("JoinDeclarationAndAssignment")
    var messageText: TextView
    private var timeText: TextView
    fun bind(message: Message) {
        messageText.text = message.message
        timeText.text = formatDateTime(message.timestamp)
    }

    init {
        messageText = itemView.findViewById(R.id.text_message_body)
        timeText = itemView.findViewById(R.id.text_message_time)
    }
}