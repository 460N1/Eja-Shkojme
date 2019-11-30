package com.a60n1.ejashkojme;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.a60n1.ejashkojme.adapter.MessageListAdapter;
import com.a60n1.ejashkojme.models.Message;
import com.a60n1.ejashkojme.utils.TimeUtils;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ChatActivity extends BaseActivity {

    private static final String TAG = "ChatActivity";
    private final List<Message> messagesList = new ArrayList<>();
    private String mChatUser;
    private EditText mChatbox;
    private ImageButton mSendButton;
    private TextView mTitle, mLastSeen;
    private DatabaseReference mDatabase;
    private RecyclerView mMessageRecycler;
    private LinearLayoutManager mLinearManager;
    private MessageListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Toolbar toolbar = findViewById(R.id.toolbar_chat);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams")
        View action_bar_view = Objects.requireNonNull(inflater).inflate(R.layout.chat_custom_app_bar, null);

        actionBar.setCustomView(action_bar_view);
        mTitle = findViewById(R.id.chat_bar_title);
        mLastSeen = findViewById(R.id.chat_bar_seen);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mChatUser = getIntent().getStringExtra("user_id");
        String name = getIntent().getStringExtra("user_name");

        mChatbox = findViewById(R.id.edittext_chatbox);

        mMessageRecycler = findViewById(R.id.recyclerview_message_list);
        mAdapter = new MessageListAdapter(this, messagesList, getUid());
        mLinearManager = new LinearLayoutManager((this));
        mLinearManager.setStackFromEnd(true);
        mMessageRecycler.setLayoutManager(mLinearManager);
        mMessageRecycler.setItemAnimator(new DefaultItemAnimator());

        mMessageRecycler.setHasFixedSize(true);
        mMessageRecycler.setAdapter(mAdapter);
        mDatabase.child("chat").child(getUid()).child(mChatUser).child("seen").setValue(true);

        loadMessages();
        mTitle.setText(name);
        mDatabase.child("users").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String online = dataSnapshot.child("online").getValue().toString();
                if (Objects.equals(online, "true")) {
                    mLastSeen.setText(R.string.online);
                } else {
                    long lastTime = Long.parseLong(Objects.requireNonNull(online));
                    mLastSeen.setText(TimeUtils.getLastSeenTime(lastTime));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        mDatabase.child("users").child(getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(mChatUser)) {
                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen", false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);
                    Map chatUserMap = new HashMap();
                    chatUserMap.put("chat/" + getUid() + "/" + mChatUser, chatAddMap);
                    chatUserMap.put("chat/" + mChatUser + "/" + getUid(), chatAddMap);
                    mDatabase.updateChildren(chatUserMap, (databaseError, databaseReference) -> {
                        if (databaseError != null) {
                            Log.d(TAG, databaseError.getMessage());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mMessageRecycler.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (bottom < oldBottom && Objects.requireNonNull(mMessageRecycler.getAdapter()).getItemCount() > 0) {
                mMessageRecycler.postDelayed(() -> mMessageRecycler.smoothScrollToPosition(mMessageRecycler.getAdapter().getItemCount() - 1), 100);
            }
        });

        mSendButton = findViewById(R.id.button_chatbox_send);
        mSendButton.setOnClickListener(v -> sentMessage());
    }

    private void loadMessages() {
        DatabaseReference messageRef = mDatabase.child("messages").child(getUid()).child(mChatUser);
        Query messageQuery = messageRef.orderByChild("timestamp").limitToLast(100);
        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                Message message = dataSnapshot.getValue(Message.class);
                messagesList.add(message);
                mAdapter.notifyDataSetChanged();
                mMessageRecycler.smoothScrollToPosition(messagesList.size() - 1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            hideKeyboard();
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sentMessage() {
        String message = mChatbox.getText().toString().trim();

        if (!message.isEmpty()) {
            String current_user_ref = "messages/" + getUid() + "/" + mChatUser;
            String chat_user_ref = "messages/" + mChatUser + "/" + getUid();

            DatabaseReference user_message_push = mDatabase.child("messages")
                    .child(getUid()).child(mChatUser).push();

            String push_id = user_message_push.getKey();

            Map messageMap = new HashMap();
            messageMap.put("message", message);
            messageMap.put("type", "text");
            messageMap.put("timestamp", ServerValue.TIMESTAMP);
            messageMap.put("from", getUid());
            messageMap.put("seen", false);
            Map messageUserMap = new HashMap();
            messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
            messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

            mChatbox.setText("");
            mDatabase.child("chat").child(getUid()).child(mChatUser).child("seen").setValue(true);
            mDatabase.child("chat").child(getUid()).child(mChatUser).child("timestamp").setValue(ServerValue.TIMESTAMP);
            mDatabase.child("chat").child(mChatUser).child(getUid()).child("seen").setValue(false);
            mDatabase.child("chat").child(mChatUser).child(getUid()).child("timestamp").setValue(ServerValue.TIMESTAMP);

            mDatabase.updateChildren(messageUserMap, (databaseError, databaseReference) -> {
                if (databaseError != null) {
                    Log.d(TAG, databaseError.getMessage());
                }
            });
        }
    }
}
