package com.a60n1.ejashkojme.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.a60n1.ejashkojme.ProfileActivity;
import com.a60n1.ejashkojme.R;
import com.a60n1.ejashkojme.models.Post;
import com.a60n1.ejashkojme.viewholder.PostViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;

import java.util.Objects;

public abstract class PostListFragment extends BaseFragment {

    private static final String TAG = "460N1_DEV_ListFragment";

    private DatabaseReference mDatabase;

    private FirebaseRecyclerAdapter<Post, PostViewHolder> mAdapter;
    private RecyclerView mRecycler;
    @SuppressWarnings("FieldCanBeLocal")
    private LinearLayoutManager mManager;

    public PostListFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_all_posts, container, false);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        mRecycler = rootView.findViewById(R.id.messages_list);
        mRecycler.setHasFixedSize(true);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // setup i layouts
        mManager = new LinearLayoutManager(getActivity());
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(mManager);

        // firebase eshte ready
        Query postsQuery = getQuery(mDatabase);

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Post>()
                .setQuery(postsQuery, Post.class)
                .build();

        //noinspection unchecked
        mAdapter = new FirebaseRecyclerAdapter<Post, PostViewHolder>(options) {

            @NonNull
            @Override
            public PostViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                return new PostViewHolder(inflater.inflate(R.layout.item_post, viewGroup, false));
            }

            @Override
            protected void onBindViewHolder(@NonNull final PostViewHolder viewHolder, int position, @NonNull final Post model) {
                final DatabaseReference postRef = getRef(position);

                // onclick per postimin
                final String postKey = postRef.getKey();
                viewHolder.itemView.setOnClickListener(v -> Objects.requireNonNull(mainActivity).onViewPostBtnClicked(postKey));
                viewHolder.authorView.setOnClickListener(v -> {
                    PopupMenu popup = new PopupMenu(getContext(), viewHolder.authorView);
                    popup.inflate(R.menu.menu_user_action);
                    popup.setOnMenuItemClickListener(item -> {
                        if (item.getItemId() == R.id.profile_action) {
                            Intent intent = new Intent(getActivity(), ProfileActivity.class);
                            intent.putExtra("user_id", model.uid);
                            startActivity(intent);
                        }
                        return false;
                    });
                    popup.show();
                });

                // useri e ka ba like apo jo
                if (model.stars.containsKey(getUid()))
                    viewHolder.starView.setImageResource(R.drawable.baseline_star_24);
                else
                    viewHolder.starView.setImageResource(R.drawable.baseline_star_border_24);

                // binding post me viewholder, setonclick per like
                viewHolder.bindToPost(model, starView -> {
                    // postimi ruhet edhe te te gjitha postimet, edhe te posts te userit
                    DatabaseReference globalPostRef = mDatabase.child("posts").child(Objects.requireNonNull(postRef.getKey()));
                    DatabaseReference userPostRef = mDatabase.child("user-posts").child(Objects.requireNonNull(model.uid)).child(postRef.getKey());

                    // percaktimi i funksioneve per on starclicked
                    onStarClicked(globalPostRef);
                    onStarClicked(userPostRef);
                });
            }
        };
        mRecycler.setAdapter(mAdapter);
    }

    private void onStarClicked(DatabaseReference postRef) {
        postRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                Post p = mutableData.getValue(Post.class);
                if (p == null)
                    return Transaction.success(mutableData);

                if (p.stars.containsKey(getUid())) {
                    // unlike
                    p.starCount = p.starCount - 1;
                    p.stars.remove(getUid());
                } else {
                    // like
                    p.starCount = p.starCount + 1;
                    p.stars.put(getUid(), true);
                }

                // cakto vleren per likes
                mutableData.setValue(p);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
                // Transaction completed
                Log.d(TAG, "postTransaction:onComplete:" + databaseError);
            }
        });
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

    public abstract Query getQuery(DatabaseReference databaseReference);

}