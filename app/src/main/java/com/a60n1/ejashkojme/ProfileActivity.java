package com.a60n1.ejashkojme;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.a60n1.ejashkojme.models.Notification;
import com.a60n1.ejashkojme.utils.TimeUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ProfileActivity extends BaseActivity {

    private DatabaseReference mDatabase, mUserDatabase, mFriendDatabase, mFriendReqDatabase;
    private ImageView mProfileImage;
    private TextView mName, mStatus, mDays;
    private Button mSendReqButton, mDeclineButton;
    private String currentState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        final String user_id = getIntent().getStringExtra("user_id");
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(user_id);
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("friends");
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("friend_req");

        mProfileImage = findViewById(R.id.profile_image);
        mName = findViewById(R.id.profile_displayName);
        mStatus = findViewById(R.id.profile_status);
        mDays = findViewById(R.id.profile_friends_days);
        mSendReqButton = findViewById(R.id.profile_send_req_btn);
        mDeclineButton = findViewById(R.id.profile_decline_btn);
        currentState = "not_friends";

        mDeclineButton.setVisibility(View.INVISIBLE);
        mDeclineButton.setEnabled(false);

        showProgressDialog();
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue(String.class);
                String status = dataSnapshot.child("status").getValue(String.class);
                final String image = dataSnapshot.child("image").getValue(String.class);

                mName.setText(name);
                mStatus.setText(status);

                if (!Objects.requireNonNull(image).equals("default")) {
                    Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_avatar).into(mProfileImage, new Callback() {
                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get().load(image).placeholder(R.drawable.default_avatar).into(mProfileImage);
                        }
                    });
                } else {
                    Picasso.get().load(image).placeholder(R.drawable.default_avatar).into(mProfileImage);
                }
                if (getUid().equals(user_id)) {
                    mDays.setVisibility(View.INVISIBLE);
                    mDeclineButton.setEnabled(false);
                    mDeclineButton.setVisibility(View.INVISIBLE);
                    mSendReqButton.setEnabled(false);
                    mSendReqButton.setVisibility(View.INVISIBLE);
                }

                // accept request
                mFriendReqDatabase.child(getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(user_id)) {
                            String req_type = dataSnapshot.child(user_id).child("request_type").getValue(String.class);
                            if (Objects.requireNonNull(req_type).equals("received")) {
                                currentState = "req_received";
                                mSendReqButton.setText(R.string.accept_req_text);
                                mDays.setVisibility(View.INVISIBLE);
                                mDeclineButton.setVisibility(View.INVISIBLE);
                                mDeclineButton.setEnabled(true);
                            } else if (req_type.equals("sent")) {
                                currentState = "req_sent";
                                mSendReqButton.setText(R.string.cancel_req_text);
                                mDeclineButton.setVisibility(View.INVISIBLE);
                                mDays.setVisibility(View.INVISIBLE);
                                mDeclineButton.setEnabled(false);
                            }
                            hideProgressDialog();
                        } else {
                            mFriendDatabase.child(getUid()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(user_id)) {
                                        currentState = "friends";
                                        mDays.setVisibility(View.VISIBLE);
                                        final String date = dataSnapshot.child(user_id).child("date").getValue(String.class);
                                        mDays.setText(TimeUtils.getDaysSince(date));
                                        mSendReqButton.setText(R.string.unfriend_text);
                                        mDeclineButton.setVisibility(View.INVISIBLE);
                                        mDeclineButton.setEnabled(false);
                                    }
                                    hideProgressDialog();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    hideProgressDialog();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mSendReqButton.setOnClickListener(v -> {
            mSendReqButton.setEnabled(false);
            // not friend state
            if (currentState.equals("not_friends")) {
                DatabaseReference notificationDatabase = mDatabase.child("notifications").child(user_id).push();
                String notificationId = notificationDatabase.getKey();

                Notification notification = new Notification(getUid(), "request");
                Map requestMap = new HashMap();
                requestMap.put("friend_req/" + getUid() + "/" + user_id + "/request_type", "sent");
                requestMap.put("friend_req/" + user_id + "/" + getUid() + "/request_type", "received");
                requestMap.put("notifications/" + user_id + "/" + notificationId, notification);
                mDatabase.updateChildren(requestMap, (databaseError, databaseReference) -> {
                    if (databaseError != null) {
                        Toast.makeText(ProfileActivity.this, "Error sending request", Toast.LENGTH_SHORT).show();
                    } else {
                        currentState = "req_sent";
                        mSendReqButton.setText(R.string.cancel_req_text);
                    }
                    mSendReqButton.setEnabled(true);
                });
            }
            // sent request state
            if (currentState.equals("req_sent")) {
                mFriendReqDatabase.child(getUid()).child(user_id).removeValue().addOnSuccessListener(aVoid -> mFriendReqDatabase.child(user_id).child(getUid()).removeValue().addOnSuccessListener(aVoid1 -> {
                    mSendReqButton.setEnabled(true);
                    currentState = "not_friends";
                    mSendReqButton.setText(R.string.send_req_text);
                    mDays.setVisibility(View.INVISIBLE);
                    mDeclineButton.setVisibility(View.INVISIBLE);
                    mDeclineButton.setEnabled(false);
                }));
            }
            // received request state
            if (currentState.equals("req_received")) {
                @SuppressLint("SimpleDateFormat") final String currentDate = new SimpleDateFormat("M/dd/yyyy").format(new Date());
                Map friendsMap = new HashMap();
                friendsMap.put("friends/" + getUid() + "/" + user_id + "/date", currentDate);
                friendsMap.put("friends/" + user_id + "/" + getUid() + "/date", currentDate);
                friendsMap.put("friend_req/" + getUid() + "/" + user_id, null);
                friendsMap.put("friend_req/" + user_id + "/" + getUid(), null);

                mDatabase.updateChildren(friendsMap, (databaseError, databaseReference) -> {
                    if (databaseError == null) {
                        mSendReqButton.setEnabled(true);
                        currentState = "friends";
                        mSendReqButton.setText(R.string.unfriend_text);
                        mDeclineButton.setVisibility(View.INVISIBLE);
                        mDays.setText(TimeUtils.getDaysSince(currentDate));
                        mDays.setVisibility(View.VISIBLE);
                        mDeclineButton.setEnabled(false);
                    } else {
                        String error = databaseError.getMessage();
                        Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
            // friend state
            if (currentState.equals("friends")) {
                Map unfriendMap = new HashMap();
                unfriendMap.put("friends/" + getUid() + "/" + user_id, null);
                unfriendMap.put("friends/" + user_id + "/" + getUid(), null);
                mDatabase.updateChildren(unfriendMap, (databaseError, databaseReference) -> {
                    if (databaseError == null) {
                        currentState = "not_friends";
                        mSendReqButton.setText(R.string.send_req_text);
                        mDeclineButton.setVisibility(View.INVISIBLE);
                        mDays.setVisibility(View.INVISIBLE);
                        mDeclineButton.setEnabled(false);
                    } else {
                        String error = databaseError.getMessage();
                        Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                    mSendReqButton.setEnabled(true);
                });
            }
        });
    }
}
