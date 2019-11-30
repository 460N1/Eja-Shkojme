package com.a60n1.ejashkojme;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;

import com.a60n1.ejashkojme.fragment.ChatFragment;
import com.a60n1.ejashkojme.fragment.ForumFragment;
import com.a60n1.ejashkojme.fragment.NewPostFragment;
import com.a60n1.ejashkojme.fragment.PostDetailFragment;
import com.a60n1.ejashkojme.fragment.SetDateTimeFragment;
import com.a60n1.ejashkojme.fragment.SetPickPointFragment;
import com.a60n1.ejashkojme.models.User;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    private static final String TAG = "MainActivity";
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private TextView mName, mEmail;
    private CircleImageView mAvatar;
    private DatabaseReference mUserDatabase;
    private String mTitle = "";
    private String mBody = "";
    private String mDate = "";
    private String mTime = "";
    private GoogleMap mMap;
    private Boolean mLocationPermissionsGranted = false;
    private NavigationView mNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(getApplicationContext());
        final String userId = getUid();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
        mUserDatabase.keepSynced(true);

        Toolbar toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                hideKeyboard();
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                hideKeyboard();
                invalidateOptionsMenu();
            }

        };
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        mNavigationView = findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        mNavigationView.setCheckedItem(R.id.nav_forum);

        if (findViewById(R.id.flContent) != null) {
            if (savedInstanceState != null)
                return;
            ForumFragment fragment = ForumFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.flContent, fragment).commit();
        }

        View headerView = mNavigationView.getHeaderView(0);

        mName = headerView.findViewById(R.id.nav_header_name);
        mEmail = headerView.findViewById(R.id.nav_header_email);
        mAvatar = headerView.findViewById(R.id.nav_header_avatar);
        setTitle("Forum");

        // get user info from firebase
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Get user value
                User user = dataSnapshot.getValue(User.class);

                if (user == null) {
                    // User is null, error out
                    Log.e(TAG, "User " + userId + " is unexpectedly null");
                    Toast.makeText(MainActivity.this,
                            "Error: could not fetch user.",
                            Toast.LENGTH_SHORT).show();
                } else {
                    mName.setText(user.name);
                    mEmail.setText(user.email);
                    final String thumb_image = user.thumb_image;
                    if (!thumb_image.equals("default"))
                        Picasso.get().load(thumb_image).networkPolicy(NetworkPolicy.OFFLINE)
                                .placeholder(R.drawable.default_avatar).into(mAvatar, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError(Exception e) {
                                Picasso.get().load(thumb_image).placeholder(R.drawable.default_avatar).into(mAvatar);
                            }
                        });
                    else
                        Picasso.get().load(thumb_image).placeholder(R.drawable.default_avatar).into(mAvatar);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        getLocationPermission();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null)
            mUserDatabase.child("online").setValue("true");
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START);
        else
            onReturnBtnClicked();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            finish();
            return true;
        } else if (id == R.id.action_logout) {
            mUserDatabase.child("online").setValue(ServerValue.TIMESTAMP);
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        hideKeyboard();
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_forum) {
            setTitle("Forum");
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.flContent, ForumFragment.newInstance()).commit();
        } else if (id == R.id.nav_chat) {
            setTitle("Chat");
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.flContent, ChatFragment.newInstance()).commit();
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //////////////// Fragment FloatingActionButton Routines ////////////////

    public void onNewPostBtnClicked() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.flContent, NewPostFragment.newInstance()).commit();
        hideKeyboard();
        mNavigationView.setCheckedItem(R.id.nav_forum);
        setTitle("New Post");
    }

    public void onSubmitPostBtnClicked() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.flContent, ForumFragment.newInstance()).commit();
        hideKeyboard();
        mNavigationView.setCheckedItem(R.id.nav_forum);
        setTitle("Forum");
    }

    public void onViewPostBtnClicked(String postKey) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.flContent, PostDetailFragment.newInstance(postKey)).commit();
        hideKeyboard();
    }

    public void onReturnBtnClicked() {
        if (Objects.requireNonNull(getSupportFragmentManager().findFragmentById(R.id.flContent)).getClass() == ForumFragment.class)
            return;
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.flContent, ForumFragment.newInstance()).commit();
        hideKeyboard();
        mNavigationView.setCheckedItem(R.id.nav_forum);
        setTitle("Forum");
    }

    public void onPickDateTimeBtnClicked(String title, String body) {
        mTitle = title;
        mBody = body;
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.flContent, SetDateTimeFragment.newInstance()).commit();
        hideKeyboard();
    }

    public void onPickOriginDestinationBtnClicked(String date, String time) {
        mDate = date;
        mTime = time;
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.flContent, SetPickPointFragment.newInstance()).commit();
        hideKeyboard();
    }

    public String getCurrentTitle() {
        return this.mTitle;
    }

    public String getCurrentBody() {
        return this.mBody;
    }

    public String getCurrentDate() {
        return this.mDate;
    }

    public String getCurrentTime() {
        return this.mTime;
    }

    //////////////// Google Maps Routines ////////////////

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Map is Ready", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: map is ready");
        mMap = googleMap;

        if (mLocationPermissionsGranted) {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                return;
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        }
    }

    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                mLocationPermissionsGranted = true;
            else
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
        } else
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called.");
        mLocationPermissionsGranted = false;

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE)
            if (grantResults.length > 0) {
                for (int grantResult : grantResults)
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        mLocationPermissionsGranted = false;
                        Log.d(TAG, "onRequestPermissionsResult: permission failed");
                        return;
                    }
                Log.d(TAG, "onRequestPermissionsResult: permission granted");
                mLocationPermissionsGranted = true;
            }
    }
}
