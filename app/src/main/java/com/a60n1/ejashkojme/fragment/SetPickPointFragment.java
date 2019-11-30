package com.a60n1.ejashkojme.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.a60n1.ejashkojme.R;
import com.a60n1.ejashkojme.models.Post;
import com.a60n1.ejashkojme.models.User;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Places;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SetPickPointFragment extends BaseFragment {

    private static final String TAG = "SetPickPointFragment";

    private GeoDataClient mGeoDataClient;
    private AutoCompleteTextView mSearchOriginText, mSearchDestinationText;
    private TextInputLayout mLayoutOrigin, mLayoutDestination;
    private FloatingActionButton mSubmitButton;

    private DatabaseReference mDatabase;

    public SetPickPointFragment() {
        // Required empty public constructor
    }

    public static SetPickPointFragment newInstance() {
        return new SetPickPointFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_setpickpoint, container, false);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        mGeoDataClient = Places.getGeoDataClient(Objects.requireNonNull(getActivity()), null);
        mLayoutOrigin = view.findViewById(R.id.field_pickorigin_layout);
        mLayoutDestination = view.findViewById(R.id.field_pickdestination_layout);
        mSearchOriginText = view.findViewById(R.id.pickorigin_text);
        mSearchDestinationText = view.findViewById(R.id.pickdestination_text);

        // Set up the adapter that will retrieve suggestions from the Places Geo Data Client.
        //mAdapter = new PlaceAutocompleteAdapter(getContext(), mGeoDataClient, null, null);
        //mSearchOriginText.setAdapter(mAdapter);
        //mSearchDestinationText.setAdapter(mAdapter);

        mSubmitButton = view.findViewById(R.id.fab_submit_post);
        mSubmitButton.setOnClickListener(v -> submitPost());


        return view;
    }

    private void submitPost() {
        if (!validateOrigin())
            return;

        if (!validateDestination())
            return;

        final String origin = mSearchOriginText.getText().toString();
        final String destination = mSearchDestinationText.getText().toString();

        // Disable button so there are no multi-posts
        setEditingEnabled(false);
        Toast.makeText(getActivity(), "Posting...", Toast.LENGTH_SHORT).show();

        final String userId = getUid();
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // Get user value
                        User user = dataSnapshot.getValue(User.class);

                        if (user == null) {
                            // User is null, error out
                            Log.e(TAG, "User " + userId + " is unexpectedly null");
                            Toast.makeText(getActivity(),
                                    "Error: could not fetch user.",
                                    Toast.LENGTH_SHORT).show();
                        } else
                            // Write new post
                            writeNewPost(userId, user.name, mainActivity.getCurrentTitle(), mainActivity.getCurrentBody(), mainActivity.getCurrentDate(), mainActivity.getCurrentTime(), origin, destination);

                        // Finish this Activity, back to the stream
                        setEditingEnabled(true);
                        mainActivity.onSubmitPostBtnClicked();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                        setEditingEnabled(true);
                    }
                });
    }

    @SuppressLint("RestrictedApi")
    private void setEditingEnabled(boolean enabled) {
        mSearchOriginText.setEnabled(enabled);
        mSearchDestinationText.setEnabled(enabled);
        if (enabled)
            mSubmitButton.setVisibility(View.VISIBLE);
        else
            mSubmitButton.setVisibility(View.GONE);
    }

    private void writeNewPost(String userId, String author, String title, String body, String date, String time, String origin, String destination) {
        // Create new post at /user-posts/$userid/$postid and at
        // /posts/$postid simultaneously
        String key = mDatabase.child("posts").push().getKey();
        Post post = new Post(userId, author, title, body, date, time, origin, destination);
        Map<String, Object> postValues = post.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/posts/" + key, postValues);
        childUpdates.put("/user-posts/" + userId + "/" + key, postValues);

        mDatabase.updateChildren(childUpdates);
    }

    private boolean validateOrigin() {
        String origin = mSearchOriginText.getText().toString().trim();

        if (origin.isEmpty()) {
            mLayoutOrigin.setError(getString(R.string.err_msg_origin));
            return false;
        } else
            mLayoutOrigin.setErrorEnabled(false);
        return true;
    }

    private boolean validateDestination() {
        String destination = mSearchDestinationText.getText().toString().trim();

        if (destination.isEmpty()) {
            mLayoutDestination.setError(getString(R.string.err_msg_destination));
            return false;
        } else
            mLayoutDestination.setErrorEnabled(false);
        return true;
    }

}
