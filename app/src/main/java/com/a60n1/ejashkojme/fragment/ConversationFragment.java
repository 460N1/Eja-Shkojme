package com.a60n1.ejashkojme.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.a60n1.ejashkojme.ChatActivity;
import com.a60n1.ejashkojme.R;
import com.a60n1.ejashkojme.models.Conversation;
import com.a60n1.ejashkojme.models.Message;
import com.a60n1.ejashkojme.viewholder.ConversationViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class ConversationFragment extends BaseFragment {
    private RecyclerView mRecycler;
    private DatabaseReference mChatDatabase, mUserDatabase, mMessageDatabase;
    private LinearLayoutManager mManager;
    private FirebaseRecyclerAdapter<Conversation, ConversationViewHolder> mAdapter;

    public ConversationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_conversation, container, false);
        mRecycler = rootView.findViewById(R.id.conv_list);
        mRecycler.setHasFixedSize(true);
        final String user_id = getUid();
        DividerItemDecoration itemDecorator = new DividerItemDecoration(Objects.requireNonNull(getContext()), DividerItemDecoration.VERTICAL);
        itemDecorator.setDrawable(Objects.requireNonNull(ContextCompat.getDrawable(getContext(), R.drawable.divider_friends)));
        mRecycler.addItemDecoration(itemDecorator);

        mChatDatabase = FirebaseDatabase.getInstance().getReference().child("chat").child(user_id);
        mChatDatabase.keepSynced(true);
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        mMessageDatabase = FirebaseDatabase.getInstance().getReference().child("messages").child(user_id);
        mUserDatabase.keepSynced(true);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mManager = new LinearLayoutManager(getActivity());
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(mManager);

        Query convQuery = mChatDatabase.orderByChild("timestamp");

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Conversation>()
                .setQuery(convQuery, Conversation.class)
                .build();

        mAdapter = new FirebaseRecyclerAdapter<Conversation, ConversationViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ConversationViewHolder holder, int position, @NonNull final Conversation model) {
                final String list_user_id = getRef(position).getKey();
                Query lastMessage = mMessageDatabase.child(Objects.requireNonNull(list_user_id)).limitToLast(1);

                lastMessage.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        Message message = dataSnapshot.getValue(Message.class);
                        holder.setMessage(Objects.requireNonNull(message), model.seen);
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                mUserDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final String name = dataSnapshot.child("name").getValue(String.class);
                        final String thumbnail = dataSnapshot.child("thumb_image").getValue(String.class);

                        holder.nameView.setText(name);
                        if (dataSnapshot.hasChild("online")) {
                            String userOnline = dataSnapshot.child("online").getValue(String.class);
                            holder.setUserStatus(Objects.requireNonNull(userOnline));
                        }
                        holder.mView.setOnClickListener(v -> {
                            Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                            chatIntent.putExtra("user_id", list_user_id);
                            chatIntent.putExtra("user_name", name);
                            startActivity(chatIntent);
                        });
                        if (!Objects.requireNonNull(thumbnail).equals("default"))
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
                        else
                            Picasso.get().load(thumbnail).placeholder(R.drawable.default_avatar).into(holder.userAvatar);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                return new ConversationViewHolder(inflater.inflate(R.layout.item_conv, parent, false));
            }
        };
        mRecycler.setAdapter(mAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mAdapter != null)
            mAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAdapter != null)
            mAdapter.stopListening();
    }

}
