package com.a60n1.ejashkojme.viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.a60n1.ejashkojme.R;
import com.a60n1.ejashkojme.models.Post;
import com.a60n1.ejashkojme.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostViewHolder extends RecyclerView.ViewHolder {

    private CircleImageView authorAvatar;
    private TextView titleView;
    public TextView authorView;
    public ImageView starView;
    private TextView numStarsView;
    private TextView bodyView;
    private TextView datetimeView;
    private TextView originView;
    private TextView destinationView;

    public PostViewHolder(View itemView) {
        super(itemView);
        authorAvatar = itemView.findViewById(R.id.post_author_photo);
        titleView = itemView.findViewById(R.id.post_title);
        authorView = itemView.findViewById(R.id.post_author);
        starView = itemView.findViewById(R.id.star);
        numStarsView = itemView.findViewById(R.id.post_num_stars);
        bodyView = itemView.findViewById(R.id.post_body);
        datetimeView = itemView.findViewById(R.id.post_datetime);
        originView = itemView.findViewById(R.id.post_origin);
        destinationView = itemView.findViewById(R.id.post_destination);
    }

    public void bindToPost(Post post, View.OnClickListener starClickListener) {
        String uid = post.uid;
        DatabaseReference userDatabase = FirebaseDatabase.getInstance().getReference()
                .child("users").child(uid);
        userDatabase.keepSynced(true);
        ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user == null)
                    return;
                final String thumb_image = user.thumb_image;
                if (!thumb_image.equals("default"))
                    Picasso.get().load(thumb_image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.default_avatar).into(authorAvatar, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get().load(thumb_image).placeholder(R.drawable.default_avatar).into(authorAvatar);
                        }
                    });
                else
                    Picasso.get().load(thumb_image).placeholder(R.drawable.default_avatar).into(authorAvatar);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
        userDatabase.addValueEventListener(userListener);

        authorView.setText(post.author);
        titleView.setText(post.title);
        numStarsView.setText(String.valueOf(post.starCount));
        bodyView.setText(post.body);
        datetimeView.setText("Date/time: " + post.date + " " + post.time);
        originView.setText("From: " + post.origin);
        destinationView.setText("To: " + post.destination);

        starView.setOnClickListener(starClickListener);
    }
}