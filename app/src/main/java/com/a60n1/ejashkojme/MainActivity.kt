package com.a60n1.ejashkojme

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.a60n1.ejashkojme.fragment.*
import com.a60n1.ejashkojme.models.User
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.material.navigation.NavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {
    private var mName: TextView? = null
    private var mEmail: TextView? = null
    private var mAvatar: CircleImageView? = null
    private var mUserDatabase: DatabaseReference? = null
    var currentTitle = ""
        private set
    var currentBody = ""
        private set
    var currentDate = ""
        private set
    var currentTime = ""
        private set
    private var mMap: GoogleMap? = null
    private var mLocationPermissionsGranted = false
    private var mNavigationView: NavigationView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        FirebaseApp.initializeApp(applicationContext)
        val userId = uid
        mUserDatabase = FirebaseDatabase.getInstance().reference.child("users").child(userId)
        mUserDatabase?.keepSynced(true)
        val toolbar = findViewById<Toolbar>(R.id.toolbar_main)
        setSupportActionBar(toolbar)
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toggle: ActionBarDrawerToggle = object : ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            override fun onDrawerClosed(view: View) {
                super.onDrawerClosed(view)
                hideKeyboard()
                invalidateOptionsMenu()
            }

            override fun onDrawerOpened(drawerView: View) {
                super.onDrawerOpened(drawerView)
                hideKeyboard()
                invalidateOptionsMenu()
            }
        }
        drawer.addDrawerListener(toggle)
        toggle.syncState()
        mNavigationView = findViewById(R.id.nav_view)
        mNavigationView?.setNavigationItemSelectedListener(this)
        mNavigationView?.setCheckedItem(R.id.nav_forum)
        if (findViewById<View?>(R.id.flContent) != null) {
            if (savedInstanceState != null) {
                return
            }
            val fragment = ForumFragment.newInstance()
            supportFragmentManager.beginTransaction()
                    .add(R.id.flContent, fragment).commit()
        }
        val headerView = mNavigationView!!.getHeaderView(0)
        mName = headerView.findViewById(R.id.nav_header_name)
        mEmail = headerView.findViewById(R.id.nav_header_email)
        mAvatar = headerView.findViewById(R.id.nav_header_avatar)
        title = "Forum"
        // user prej firebase
        mUserDatabase!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) { // Get user value
                val user = dataSnapshot.getValue(User::class.java)
                if (user == null) { // User is null, error out
                    Log.e(TAG, "User $userId is unexpectedly null")
                    Toast.makeText(this@MainActivity,
                            "Error: could not fetch user.",
                            Toast.LENGTH_SHORT).show()
                } else {
                    mName!!.text = user.name
                    mEmail!!.text = user.email
                    val thumbImage = user.thumbImage
                    if (thumbImage != "default")
                        Picasso.get().load(thumbImage).networkPolicy(NetworkPolicy.OFFLINE)
                                .placeholder(R.drawable.default_avatar).into(mAvatar, object : Callback {
                                    override fun onSuccess() {}
                                    override fun onError(e: Exception) {
                                        Picasso.get().load(thumbImage).placeholder(R.drawable.default_avatar).into(mAvatar)
                                    }
                                })
                    else
                        Picasso.get().load(thumbImage).placeholder(R.drawable.default_avatar).into(mAvatar)
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
        locationPermission
    }

    override fun onStart() {
        super.onStart()
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null)
            mUserDatabase!!.child("online").setValue("true")
    }

    override fun onBackPressed() {
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        if (drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START)
        else
            onReturnBtnClicked()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean { // inflate menu
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean { // handlin klikimet e options
        // back handled ne manifest
        when (item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(this@MainActivity, SettingsActivity::class.java)
                startActivity(intent)
                finish()
                return true
            }
            R.id.action_logout -> {
                mUserDatabase!!.child("online").setValue(ServerValue.TIMESTAMP)
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        hideKeyboard()
        // handling klikimet ne Navigation
        val id = item.itemId
        if (id == R.id.nav_forum) {
            title = getString(R.string.forum)
            val fragmentManager = supportFragmentManager
            fragmentManager.beginTransaction().replace(R.id.flContent, ForumFragment.newInstance()).commit()
        } else if (id == R.id.nav_chat) {
            title = getString(R.string.chat)
            val fragmentManager = supportFragmentManager
            fragmentManager.beginTransaction().replace(R.id.flContent, ChatFragment.newInstance()).commit()
        }
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    //////////////// FAB ////////////////
    fun onNewPostBtnClicked() {
        supportFragmentManager.beginTransaction()
                .replace(R.id.flContent, NewPostFragment.newInstance()).commit()
        hideKeyboard()
        mNavigationView!!.setCheckedItem(R.id.nav_forum)
        title = getString(R.string.new_post)
    }

    fun onSubmitPostBtnClicked() {
        supportFragmentManager.beginTransaction()
                .replace(R.id.flContent, ForumFragment.newInstance()).commit()
        hideKeyboard()
        mNavigationView!!.setCheckedItem(R.id.nav_forum)
        title = getString(R.string.forum)
    }

    fun onViewPostBtnClicked(postKey: String?) {
        supportFragmentManager.beginTransaction()
                .replace(R.id.flContent, PostDetailFragment.newInstance(postKey)).commit()
        hideKeyboard()
    }

    private fun onReturnBtnClicked() {
        if (supportFragmentManager.findFragmentById(R.id.flContent)!!.javaClass == ForumFragment::class.java) {
            return
        }
        supportFragmentManager.beginTransaction()
                .replace(R.id.flContent, ForumFragment.newInstance()).commit()
        hideKeyboard()
        mNavigationView!!.setCheckedItem(R.id.nav_forum)
        title = getString(R.string.forum)
    }

    fun onPickDateTimeBtnClicked(title: String, body: String) {
        currentTitle = title
        currentBody = body
        supportFragmentManager.beginTransaction()
                .replace(R.id.flContent, SetDateTimeFragment.newInstance()).commit()
        hideKeyboard()
    }

    fun onPickOriginDestinationBtnClicked(date: String, time: String) {
        currentDate = date
        currentTime = time
        supportFragmentManager.beginTransaction()
                .replace(R.id.flContent, SetPickPointFragment.newInstance()).commit()
        hideKeyboard()
    }

    // Google Maps pergaditja
    override fun onMapReady(googleMap: GoogleMap) {
        Toast.makeText(this, getString(R.string.map_ready), Toast.LENGTH_SHORT).show()
        Log.d(TAG, "onMapReady: map is ready")
        mMap = googleMap
        if (mLocationPermissionsGranted) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                return
            mMap!!.isMyLocationEnabled = true
            mMap!!.uiSettings.isMyLocationButtonEnabled = false
        }
    }

    private val locationPermission: Unit
        get() {
            Log.d(TAG, "getLocationPermission: getting location permissions")
            val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION)
            if (ContextCompat.checkSelfPermission(this.applicationContext,
                            FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this.applicationContext,
                                COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                    mLocationPermissionsGranted = true
                else
                    ActivityCompat.requestPermissions(this,
                            permissions,
                            LOCATION_PERMISSION_REQUEST_CODE)
            } else
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE)
        }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        Log.d(TAG, "onRequestPermissionsResult: called.")
        mLocationPermissionsGranted = false
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE)
            if (grantResults.isNotEmpty())
                for (grantResult in grantResults)
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        mLocationPermissionsGranted = false
                        Log.d(TAG, "onRequestPermissionsResult: permission failed")
                        return
                    }
                Log.d(TAG, "onRequestPermissionsResult: permission granted")
                mLocationPermissionsGranted = true
    }

    companion object {
        private const val TAG = "460N1_DEV_Main"
        private const val FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION
        private const val COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1234
    }
}