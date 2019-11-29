package com.a60n1.ejashkojme.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.a60n1.ejashkojme.ChatActivity;
import com.a60n1.ejashkojme.DriverMapActivity;
import com.a60n1.ejashkojme.ProfileActivity;
import com.a60n1.ejashkojme.R;
import com.a60n1.ejashkojme.models.Friends;
import com.a60n1.ejashkojme.models.Post;
import com.a60n1.ejashkojme.models.Ride;
import com.a60n1.ejashkojme.viewholder.FriendsViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FriendsFragment extends BaseFragment {

    private FirebaseRecyclerAdapter<Friends, FriendsViewHolder> mAdapter;
    private RecyclerView mRecycler;
    private LinearLayoutManager mManager;
    private String mOrigin;

    private DatabaseReference mFriendsDatabase, mUserDatabase, mDatabase;

    public FriendsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_friends, container, false);

        mRecycler = rootView.findViewById(R.id.friends_list);
        mRecycler.setHasFixedSize(true);
        DividerItemDecoration itemDecorator = new DividerItemDecoration(Objects.requireNonNull(getContext()), DividerItemDecoration.VERTICAL);
        itemDecorator.setDrawable(Objects.requireNonNull(ContextCompat.getDrawable(getContext(), R.drawable.divider_friends)));
        mRecycler.addItemDecoration(itemDecorator);

        final String userId = getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("friends").child(userId);
        mUserDatabase.keepSynced(true);
        mFriendsDatabase.keepSynced(true);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mManager = new LinearLayoutManager(getActivity());
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(mManager);

        Query friendsQuery = getQuery(mFriendsDatabase);

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Friends>()
                .setQuery(friendsQuery, Friends.class)
                .build();

        mAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final FriendsViewHolder holder, int position, @NonNull Friends model) {
                final String list_user_id = getRef(position).getKey();

                mUserDatabase.child(Objects.requireNonNull(list_user_id)).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final String name = dataSnapshot.child("name").getValue(String.class);
                        final String thumbnail = dataSnapshot.child("thumb_image").getValue(String.class);
                        final String status = dataSnapshot.child("status").getValue(String.class);
                        holder.statusView.setText(status);
                        holder.nameView.setText(name);
                        if (dataSnapshot.hasChild("online")) {
                            String userOnline = dataSnapshot.child("online").getValue(String.class);
                            holder.setUserStatus(userOnline);
                        }
                        holder.mView.setOnClickListener(v -> {
                            PopupMenu popup = new PopupMenu(getContext(), holder.mView);
                            popup.inflate(R.menu.menu_friends_action);
                            popup.setOnMenuItemClickListener(item -> {
                                switch (item.getItemId()) {
                                    case R.id.friends_profile_action:
                                        Intent profileIntent = new Intent(getActivity(), ProfileActivity.class);
                                        profileIntent.putExtra("user_id", list_user_id);
                                        startActivity(profileIntent);
                                        break;
                                    case R.id.friends_chat_action:
                                        Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                        chatIntent.putExtra("user_id", list_user_id);
                                        chatIntent.putExtra("user_name", name);
                                        startActivity(chatIntent);
                                        break;
                                    case R.id.friends_ride_action:
                                        // get the pickup point address
                                        DatabaseReference userPostsDatabase = FirebaseDatabase.getInstance().getReference().child("user-posts");
                                        Query lastPost = userPostsDatabase.child(list_user_id);
                                        lastPost.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot1) {
                                                for (DataSnapshot data : dataSnapshot1.getChildren()) {
                                                    Post post = data.getValue(Post.class);
                                                    mOrigin = Objects.requireNonNull(post).origin;
                                                }
                                                Intent driverMapIntent = new Intent(getContext(), DriverMapActivity.class);
                                                driverMapIntent.putExtra("user_id", list_user_id);
                                                driverMapIntent.putExtra("user_name", name);
                                                driverMapIntent.putExtra("thumb_image", thumbnail);
                                                driverMapIntent.putExtra("origin", mOrigin);
                                                DatabaseReference ridesDatabase = mDatabase.child("rides").child(list_user_id).push();
                                                String ridesId = ridesDatabase.getKey();
                                                Ride ride = new Ride(getUid(), mOrigin);
                                                Map rideMap = new HashMap();
                                                rideMap.put("rides/" + list_user_id + "/" + ridesId, ride);
                                                mDatabase.updateChildren(rideMap, (databaseError, databaseReference) -> {
                                                    if (databaseError != null) {
                                                        Toast.makeText(getContext(), "Error starting ride", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                                startActivity(driverMapIntent);
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                        break;
                                }
                                return false;
                            });
                            popup.show();
                        });
                        if (!thumbnail.equals("default")) {
                            Picasso.get().load(thumbnail).networkPolicy(NetworkPolicy.OFFLINE)
                                    .placeholder(R.drawable.default_avatar).into(holder.userAvatar, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError(Exception e) {
                                    Picasso.get().load(thumbnail).placeholder(R.drawable.default_avatar).into(holder.userAvatar);
                                }
                            });
                        } else {
                            Picasso.get().load(thumbnail).placeholder(R.drawable.default_avatar).into(holder.userAvatar);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                return new FriendsViewHolder(inflater.inflate(R.layout.item_user, parent, false));
            }
        };
        mRecycler.setAdapter(mAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mAdapter != null) {
            mAdapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAdapter != null) {
            mAdapter.stopListening();
        }
    }

    private Query getQuery(DatabaseReference databaseReference) {
        // All my friends
        return databaseReference;
    }

}
