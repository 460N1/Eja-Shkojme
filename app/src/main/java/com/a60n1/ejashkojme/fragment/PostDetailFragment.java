package com.a60n1.ejashkojme.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.a60n1.ejashkojme.ProfileActivity;
import com.a60n1.ejashkojme.R;
import com.a60n1.ejashkojme.adapter.CommentAdapter;
import com.a60n1.ejashkojme.models.Comment;
import com.a60n1.ejashkojme.models.Post;
import com.a60n1.ejashkojme.models.User;
import com.a60n1.ejashkojme.utils.PermissionUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Fragment to hold post details
 */
public class PostDetailFragment extends BaseFragment implements GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback,
        OnMapReadyCallback {
    private static final String EXTRA_POST_KEY = "post_key";
    private static final String TAG = "PostDetailFragment";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final String LOG_TAG = "PostDetailFragment";
    private static String start;
    private static String end;
    private GoogleApiClient mGoogleApiClient;
    private LatLng start_position;
    private LatLng end_position;
    private DatabaseReference mPostDatabase, mCommentsDatabase, mUserDatabase;
    private ValueEventListener mPostListener, mUserListener;
    private String mPostKey, mPostUid;
    private CommentAdapter mAdapter;
    private CircleImageView mAuthorAvatar;
    private TextView mAuthorView;
    private TextView mTitleView;
    private TextView mBodyView;
    private TextView mDatetimeView;
    private TextView mOriginView;
    private TextView mDestinationView;
    private EditText mCommentField;
    private Button mCommentButton;
    private TextInputLayout mLayoutCommentField;
    private RecyclerView mCommentsRecycler;
    private GoogleMap mMap;
    private boolean mPermissionDenied = false;

    public PostDetailFragment() {
    }

    public static PostDetailFragment newInstance(String post_key) {
        PostDetailFragment fragment = new PostDetailFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_POST_KEY, post_key);
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGoogleApiClient = new GoogleApiClient.Builder(Objects.requireNonNull(getActivity()).getApplicationContext())
                .addApi(Places.GEO_DATA_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        MapsInitializer.initialize(getActivity().getApplicationContext());
        mGoogleApiClient.connect();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_detail, container, false);
        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager()
                .findFragmentById(R.id.mini_map);
        Objects.requireNonNull(mapFragment).getMapAsync(this);
        // Get post key from arguments
        mPostKey = Objects.requireNonNull(getArguments()).getString(EXTRA_POST_KEY);
        if (mPostKey == null) {
            throw new IllegalArgumentException("Must pass EXTRA_POST_KEY");
        }

        // Initialize Database
        mPostDatabase = FirebaseDatabase.getInstance().getReference()
                .child("posts").child(mPostKey);
        mCommentsDatabase = FirebaseDatabase.getInstance().getReference()
                .child("post-comments").child(mPostKey);

        // Initialize Views
        mAuthorAvatar = view.findViewById(R.id.post_author_photo);
        mAuthorView = view.findViewById(R.id.post_author);
        mTitleView = view.findViewById(R.id.post_title);
        mBodyView = view.findViewById(R.id.post_body);
        mDatetimeView = view.findViewById(R.id.post_datetime);
        mOriginView = view.findViewById(R.id.post_origin);
        mDestinationView = view.findViewById(R.id.post_destination);
        mCommentField = view.findViewById(R.id.field_comment_text);
        mCommentButton = view.findViewById(R.id.button_post_comment);
        mCommentsRecycler = view.findViewById(R.id.recycler_comments);
        mLayoutCommentField = view.findViewById(R.id.field_comment_text_layout);

        mAuthorView.setOnClickListener(view1 -> {
            PopupMenu popup = new PopupMenu(mainActivity, mAuthorView);
            popup.inflate(R.menu.menu_user_action);
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.profile_action) {
                    Intent intent = new Intent(getActivity(), ProfileActivity.class);
                    intent.putExtra("user_id", mPostUid);
                    startActivity(intent);
                }
                return false;
            });
            popup.show();
        });

        mCommentField.addTextChangedListener(new MyTextWatcher(mCommentField));

        mCommentButton.setOnClickListener(v -> {
            int i = v.getId();
            if (i == R.id.button_post_comment) {
                hideKeyboard();
                postComment();
            }
        });
        mCommentsRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        // Add value event listener to the post
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                Post post = dataSnapshot.getValue(Post.class);
                mAuthorView.setText(Objects.requireNonNull(post).author);
                mTitleView.setText(post.title);
                mBodyView.setText(post.body);
                mDatetimeView.setText("Date/time: " + post.date + " " + post.time);
                mOriginView.setText("From: " + post.origin);
                mDestinationView.setText("To: " + post.destination);
                if (mUserListener == null) {
                    mPostUid = post.uid;
                    Log.d(TAG, "user id is: " + mPostUid);
                    mUserDatabase = FirebaseDatabase.getInstance().getReference()
                            .child("users").child(mPostUid);
                    mUserDatabase.keepSynced(true);
                    ValueEventListener userListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            User user = dataSnapshot.getValue(User.class);
                            if (user == null) {
                                return;
                            }
                            final String thumb_image = user.thumb_image;
                            if (!thumb_image.equals("default")) {
                                Picasso.get().load(thumb_image).networkPolicy(NetworkPolicy.OFFLINE)
                                        .placeholder(R.drawable.default_avatar).into(mAuthorAvatar, new Callback() {
                                    @Override
                                    public void onSuccess() {

                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        Picasso.get().load(thumb_image).placeholder(R.drawable.default_avatar).into(mAuthorAvatar);
                                    }
                                });
                            } else {
                                Picasso.get().load(thumb_image).placeholder(R.drawable.default_avatar).into(mAuthorAvatar);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    };
                    mUserDatabase.addValueEventListener(userListener);
                    mUserListener = userListener;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                Toast.makeText(getActivity(), "Failed to load post.",
                        Toast.LENGTH_SHORT).show();
            }
        };
        mPostDatabase.addValueEventListener(postListener);

        // Keep copy of post listener so we can remove it when app stops
        mPostListener = postListener;

        // Listen for comments
        mAdapter = new CommentAdapter(getActivity(), mCommentsDatabase);
        mCommentsRecycler.setAdapter(mAdapter);
    }

    @Override
    public void onStop() {
        super.onStop();

        // Remove post value event listener
        if (mPostListener != null) {
            mPostDatabase.removeEventListener(mPostListener);
        }
        // Clean up comments listener
        mAdapter.cleanupListener();
    }

    private void postComment() {
        final String uid = getUid();
        FirebaseDatabase.getInstance().getReference().child("users").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // Get user information
                        User user = dataSnapshot.getValue(User.class);
                        if (user == null) {
                            return;
                        }
                        String authorName = user.name;

                        if (!validateComment()) {
                            return;
                        }
                        // Create new comment object
                        String commentText = mCommentField.getText().toString();
                        Comment comment = new Comment(uid, authorName, commentText);

                        // Push the comment, it will appear in the list
                        mCommentsDatabase.push().setValue(comment);

                        // Clear the field
                        mCommentField.setText(null);
                        mLayoutCommentField.setErrorEnabled(false);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private boolean validateComment() {
        if (mCommentField.getText().toString().trim().isEmpty()) {
            mLayoutCommentField.setError(getString(R.string.err_msg_post_detail));
            requestFocus(mCommentField);
            return false;
        } else {
            mLayoutCommentField.setErrorEnabled(false);
        }
        return true;
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            Objects.requireNonNull(getActivity()).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        //get the lat and lng of the position
        mPostDatabase = FirebaseDatabase.getInstance().getReference()
                .child("posts").child(mPostKey);
        mPostDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                Post post = dataSnapshot.getValue(Post.class);
                start = Objects.requireNonNull(post).origin;
                end = post.destination;
                boolean validAddress = false;
                try {
                    Geocoder geoCoder = new Geocoder(getActivity(), Locale.getDefault());
                    List<Address> address1 = geoCoder.getFromLocationName(start, 1);
                    if (!address1.isEmpty()) {
                        validAddress = true;
                        double latitude1 = address1.get(0).getLatitude();
                        double longitude1 = address1.get(0).getLongitude();
                        start_position = new LatLng(latitude1, longitude1);
                        List<Address> address2 = geoCoder.getFromLocationName(end, 1);
                        double latitude2 = address2.get(0).getLatitude();
                        double longitude2 = address2.get(0).getLongitude();
                        end_position = new LatLng(latitude2, longitude2);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (validAddress) {
                    mMap.addMarker(new MarkerOptions().position(start_position).title("Origin"));
                    mMap.addMarker(new MarkerOptions().position(end_position).title("Destination"));
                    LatLngBounds bounds = new LatLngBounds.Builder()
                            .include(start_position)
                            .include(end_position)
                            .build();
                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        enableMyLocation();

    }

    //=============================================
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()).getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            showMissingPermissionError();
        }
    }


    @Override
    public boolean onMyLocationButtonClick() {
//        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
//        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getChildFragmentManager(), "dialog");
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.v(LOG_TAG, connectionResult.toString());
    }

    private class MyTextWatcher implements TextWatcher {
        private View view;

        private MyTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void afterTextChanged(Editable editable) {
            if (view.getId() == R.id.field_comment_text) {
                validateComment();
            }
        }
    }
}
