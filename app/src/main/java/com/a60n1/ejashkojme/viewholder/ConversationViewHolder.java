package com.a60n1.ejashkojme.viewholder;

import android.graphics.Typeface;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.a60n1.ejashkojme.R;
import com.a60n1.ejashkojme.models.Message;
import com.a60n1.ejashkojme.utils.TimeUtils;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConversationViewHolder extends RecyclerView.ViewHolder {
    public View mView;
    public TextView statusView, nameView, timestampView;
    public CircleImageView userAvatar;
    private ImageView statusIcon;

    public ConversationViewHolder(View itemView) {
        super(itemView);
        mView = itemView;

        statusView = mView.findViewById(R.id.conv_user_last_message);
        nameView = mView.findViewById(R.id.conv_user_name);
        timestampView = mView.findViewById(R.id.conv_timestamp);
        userAvatar = mView.findViewById(R.id.conv_user_photo);
        statusIcon = mView.findViewById(R.id.conv_status_icon);
    }

    public void setMessage(Message message, boolean seen) {
        statusView.setText(message.message);
        timestampView.setText(TimeUtils.getLastMessageTime(message.timestamp));

        if (!seen)
            statusView.setTypeface(statusView.getTypeface(), Typeface.BOLD);
        else
            statusView.setTypeface(statusView.getTypeface(), Typeface.NORMAL);
    }

    public void setUserStatus(String status) {
        if (status.equals("true"))
            statusIcon.setVisibility(View.VISIBLE);
        else
            statusIcon.setVisibility(View.INVISIBLE);
    }
}