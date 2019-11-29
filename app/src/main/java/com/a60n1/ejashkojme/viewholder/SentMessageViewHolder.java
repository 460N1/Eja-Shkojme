package com.a60n1.ejashkojme.viewholder;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.a60n1.ejashkojme.R;
import com.a60n1.ejashkojme.models.Message;
import com.a60n1.ejashkojme.utils.TimeUtils;

public class SentMessageViewHolder extends RecyclerView.ViewHolder {
    TextView messageText, timeText;

    public SentMessageViewHolder(View itemView) {
        super(itemView);

        messageText = itemView.findViewById(R.id.text_message_body);
        timeText = itemView.findViewById(R.id.text_message_time);
    }

    public void bind(Message message) {
        messageText.setText(message.message);

        timeText.setText(TimeUtils.formatDateTime(message.timestamp));
    }
}