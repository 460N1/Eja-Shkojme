package com.a60n1.ejashkojme.viewholder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.a60n1.ejashkojme.R;
import com.a60n1.ejashkojme.models.Message;
import com.a60n1.ejashkojme.models.User;
import com.a60n1.ejashkojme.utils.TimeUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
    TextView messageText, timeText;
    CircleImageView profileAvatar;
    private DatabaseReference mUserDatabase;

    public ReceivedMessageViewHolder(View itemView) {
        super(itemView);
        messageText = itemView.findViewById(R.id.text_message_body);
        timeText = itemView.findViewById(R.id.text_message_time);
        profileAvatar = itemView.findViewById(R.id.image_message_profile);
    }

    public void bind(Message message) {
        messageText.setText(message.message);

        timeText.setText(TimeUtils.formatDateTime(message.timestamp));

        final String user_id = message.from;
        mUserDatabase = FirebaseDatabase.getInstance().getReference()
                .child("users").child(user_id);
        mUserDatabase.keepSynced(true);
        ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user == null)
                    return;
                final String thumbnail = user.thumb_image;
                if (!thumbnail.equals("default"))
                    Picasso.get().load(thumbnail).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.default_avatar).into(profileAvatar, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get().load(thumbnail).placeholder(R.drawable.default_avatar).into(profileAvatar);
                        }
                    });
                else
                    Picasso.get().load(thumbnail).placeholder(R.drawable.default_avatar).into(profileAvatar);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
        mUserDatabase.addValueEventListener(userListener);
    }
}
