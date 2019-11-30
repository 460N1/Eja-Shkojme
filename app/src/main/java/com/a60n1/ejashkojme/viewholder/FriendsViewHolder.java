package com.a60n1.ejashkojme.viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.a60n1.ejashkojme.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsViewHolder extends RecyclerView.ViewHolder {

    public View mView;
    public CircleImageView userAvatar;
    public TextView nameView;
    public TextView statusView;
    private ImageView statusIcon;

    public FriendsViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        userAvatar = itemView.findViewById(R.id.user_photo);
        nameView = itemView.findViewById(R.id.user_name);
        statusView = itemView.findViewById(R.id.user_status);
        statusIcon = itemView.findViewById(R.id.status_icon);
    }

    public void setUserStatus(String status) {
        if (status.equals("true")) {
            statusIcon.setVisibility(View.VISIBLE);
        } else {
            statusIcon.setVisibility(View.INVISIBLE);
        }
    }

}
