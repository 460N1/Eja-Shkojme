@file:Suppress("DEPRECATION")

package com.a60n1.ejashkojme.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.*
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.a60n1.ejashkojme.ProfileActivity
import com.a60n1.ejashkojme.R
import com.a60n1.ejashkojme.adapter.CommentAdapter
import com.a60n1.ejashkojme.models.Comment
import com.a60n1.ejashkojme.models.Post
import com.a60n1.ejashkojme.models.User
import com.a60n1.ejashkojme.utils.PermissionUtils.PermissionDeniedDialog.Companion.newInstance
import com.a60n1.ejashkojme.utils.PermissionUtils.isPermissionGranted
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.places.Places
import com.google.android.gms.maps.*
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener
import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.*
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*

class PostDetailFragment : BaseFragment(), GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, OnMyLocationButtonClickListener, OnMyLocationClickListener, OnRequestPermissionsResultCallback, OnMapReadyCallback {
    private var mGoogleApiClient: GoogleApiClient? = null
    private var startPosition: LatLng? = null
    private var endPosition: LatLng? = null
    private var mPostDatabase: DatabaseReference? = null
    private var mCommentsDatabase: DatabaseReference? = null
    private var mUserDatabase: DatabaseReference? = null
    private var mPostListener: ValueEventListener? = null
    private var mUserListener: ValueEventListener? = null
    private var mPostKey: String? = null
    private var mPostUid: String? = null
    private var mAdapter: CommentAdapter? = null
    private var mAuthorAvatar: CircleImageView? = null
    private var mAuthorView: TextView? = null
    private var mTitleView: TextView? = null
    private var mBodyView: TextView? = null
    private var mDatetimeView: TextView? = null
    private var mOriginView: TextView? = null
    private var mDestinationView: TextView? = null
    private var mCommentField: EditText? = null
    private var mCommentButton: Button? = null
    private var mLayoutCommentField: TextInputLayout? = null
    private var mCommentsRecycler: RecyclerView? = null
    private var mMap: GoogleMap? = null
    private var mPermissionDenied = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mGoogleApiClient = GoogleApiClient.Builder(activity!!.applicationContext)
                .addApi(Places.GEO_DATA_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build()
        MapsInitializer.initialize(activity!!.applicationContext)
        mGoogleApiClient!!.connect()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_post_detail, container, false)
        val mapFragment = this.childFragmentManager
                .findFragmentById(R.id.mini_map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)
        // marrja e postkey
        mPostKey = arguments!!.getString(EXTRA_POST_KEY)
        requireNotNull(mPostKey) { "Must pass EXTRA_POST_KEY" }
        // init databazen
        mPostDatabase = FirebaseDatabase.getInstance().reference
                .child("posts").child(mPostKey!!)
        mCommentsDatabase = FirebaseDatabase.getInstance().reference
                .child("post-comments").child(mPostKey!!)
        // init views
        mAuthorAvatar = view.findViewById(R.id.post_author_photo)
        mAuthorView = view.findViewById(R.id.post_author)
        mTitleView = view.findViewById(R.id.post_title)
        mBodyView = view.findViewById(R.id.post_body)
        mDatetimeView = view.findViewById(R.id.post_datetime)
        mOriginView = view.findViewById(R.id.post_origin)
        mDestinationView = view.findViewById(R.id.post_destination)
        mCommentField = view.findViewById(R.id.field_comment_text)
        mCommentButton = view.findViewById(R.id.button_post_comment)
        mCommentsRecycler = view.findViewById(R.id.recycler_comments)
        mLayoutCommentField = view.findViewById(R.id.field_comment_text_layout)
        mAuthorView!!.setOnClickListener {
            val popup = PopupMenu(mainActivity, mAuthorView)
            popup.inflate(R.menu.menu_user_action)
            popup.setOnMenuItemClickListener { item: MenuItem ->
                if (item.itemId == R.id.profile_action) {
                    val intent = Intent(activity, ProfileActivity::class.java)
                    intent.putExtra("user_id", mPostUid)
                    startActivity(intent)
                }
                false
            }
            popup.show()
        }
        mCommentField!!.addTextChangedListener(MyTextWatcher(mCommentField!!))
        mCommentButton!!.setOnClickListener { v: View ->
            val i = v.id
            if (i == R.id.button_post_comment) {
                hideKeyboard()
                postComment()
            }
        }
        mCommentsRecycler!!.layoutManager = LinearLayoutManager(activity)
        return view
    }

    override fun onStart() {
        super.onStart()
        // krijimi i listener per postime
        val postListener: ValueEventListener = object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(dataSnapshot: DataSnapshot) { // marrja e objekteve, aplikimi i rregullave per ui
                val post = dataSnapshot.getValue(Post::class.java)!!
                mAuthorView!!.text = post.author
                mTitleView!!.text = post.title
                mBodyView!!.text = post.body
                mDatetimeView!!.text = "Date/time: " + post.date + " " + post.time
                mOriginView!!.text = "From: " + post.origin
                mDestinationView!!.text = "To: " + post.destination
                if (mUserListener == null) {
                    mPostUid = post.uid
                    Log.d(TAG, "user id is: $mPostUid")
                    mUserDatabase = FirebaseDatabase.getInstance().reference
                            .child("users").child(mPostUid!!)
                    mUserDatabase!!.keepSynced(true)
                    val userListener: ValueEventListener = object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val user = dataSnapshot.getValue(User::class.java) ?: return
                            val thumbImage = user.thumbImage
                            if (thumbImage != "default")
                                Picasso.get().load(thumbImage).networkPolicy(NetworkPolicy.OFFLINE)
                                        .placeholder(R.drawable.default_avatar).into(mAuthorAvatar, object : Callback {
                                            override fun onSuccess() {}
                                            override fun onError(e: Exception) {
                                                Picasso.get().load(thumbImage).placeholder(R.drawable.default_avatar).into(mAuthorAvatar)
                                            }
                                        })
                            else
                                Picasso.get().load(thumbImage).placeholder(R.drawable.default_avatar).into(mAuthorAvatar)
                        }

                        override fun onCancelled(databaseError: DatabaseError) {}
                    }
                    mUserDatabase!!.addValueEventListener(userListener)
                    mUserListener = userListener
                }
            }

            override fun onCancelled(databaseError: DatabaseError) { // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                Toast.makeText(activity, "Failed to load post.",
                        Toast.LENGTH_SHORT).show()
            }
        }
        mPostDatabase!!.addValueEventListener(postListener)
        // ruajtja e listener qe te mund t'e ndalim kur app te ndalet
        mPostListener = postListener
        // listening per komente
        mAdapter = CommentAdapter(activity!!, mCommentsDatabase!!)
        mCommentsRecycler!!.adapter = mAdapter
    }

    override fun onStop() {
        super.onStop()
        // largimi i listener
        if (mPostListener != null)
            mPostDatabase!!.removeEventListener(mPostListener!!)
        // pastrim i adapterit
        mAdapter!!.cleanupListener()
    }

    private fun postComment() {
        val uid = uid
        FirebaseDatabase.getInstance().reference.child("users").child(uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) { // Get user information
                        val user = dataSnapshot.getValue(User::class.java) ?: return
                        val authorName = user.name
                        if (!validateComment())
                            return
                        // Create new comment object
                        val commentText = mCommentField!!.text.toString()
                        val comment = Comment(uid, authorName, commentText)
                        // Push the comment, it will appear in the list
                        mCommentsDatabase!!.push().setValue(comment)
                        // Clear the field
                        mCommentField!!.text = null
                        mLayoutCommentField!!.isErrorEnabled = false
                    }

                    override fun onCancelled(databaseError: DatabaseError) {}
                })
    }

    private fun validateComment(): Boolean {
        if (mCommentField!!.text.toString().trim { it <= ' ' }.isEmpty()) {
            mLayoutCommentField!!.error = getString(R.string.err_msg_post_detail)
            requestFocus(mCommentField)
            return false
        } else
            mLayoutCommentField!!.isErrorEnabled = false
        return true
    }

    private fun requestFocus(view: View?) {
        if (view!!.requestFocus())
            activity!!.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
    }

    override fun onMapReady(map: GoogleMap) {
        mMap = map
        //lat, long e pozites
        mPostDatabase = FirebaseDatabase.getInstance().reference
                .child("posts").child(mPostKey!!)
        mPostDatabase!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) { // funksioni kryesor per shfaqje te lok.
                val post = dataSnapshot.getValue(Post::class.java)!!
                start = post.origin
                end = post.destination
                var validAddress = false
                try {
                    val geoCoder = Geocoder(activity, Locale.getDefault())
                    val address1 = geoCoder.getFromLocationName(start, 1)
                    if (address1.isNotEmpty()) {
                        validAddress = true
                        val latitude1 = address1[0].latitude
                        val longitude1 = address1[0].longitude
                        startPosition = LatLng(latitude1, longitude1)
                        val address2 = geoCoder.getFromLocationName(end, 1)
                        val latitude2 = address2[0].latitude
                        val longitude2 = address2[0].longitude
                        endPosition = LatLng(latitude2, longitude2)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                if (validAddress) {
                    mMap!!.addMarker(MarkerOptions().position(startPosition!!).title("Origin"))
                    mMap!!.addMarker(MarkerOptions().position(endPosition!!).title("Destination"))
                    val bounds = LatLngBounds.Builder()
                            .include(startPosition)
                            .include(endPosition)
                            .build()
                    mMap!!.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50))
                }
            }

            override fun onCancelled(error: DatabaseError) { // deshtim i qfaredoshem
                Log.w(TAG, "Failed to read value.", error.toException())
            }
        })
        mMap!!.setOnMyLocationButtonClickListener(this)
        mMap!!.setOnMyLocationClickListener(this)
        enableMyLocation()
    }

    //============================================= // MARRJA E PERMISSIONS
    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(activity!!.applicationContext, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
            mMap!!.isMyLocationEnabled = true
        else
            showMissingPermissionError()
    }

    override fun onMyLocationButtonClick(): Boolean {
        return false
    }

    override fun onMyLocationClick(location: Location) {}

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE)
            return
        if (isPermissionGranted(permissions, grantResults,
                        Manifest.permission.ACCESS_FINE_LOCATION)) // qasje ne lokacion nese eshte dhene leja
            enableMyLocation()
        else
            mPermissionDenied = true // pergaditje per showMissingPermissionError
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private fun showMissingPermissionError() {
        newInstance(true).show(childFragmentManager, "dialog")
    }

    override fun onConnected(bundle: Bundle?) {}
    override fun onConnectionSuspended(i: Int) {}
    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.v(TAG, connectionResult.toString())
    }

    private inner class MyTextWatcher(private val view: View) : TextWatcher {
        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun afterTextChanged(editable: Editable) {
            if (view.id == R.id.field_comment_text)
                validateComment()
        }

    }

    companion object {
        private const val EXTRA_POST_KEY = "post_key"
        private const val TAG = "460N1_DEV_PostDetail"
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private var start: String? = null
        private var end: String? = null
        fun newInstance(post_key: String?): PostDetailFragment {
            val fragment = PostDetailFragment()
            val args = Bundle()
            args.putString(EXTRA_POST_KEY, post_key)
            fragment.arguments = args
            return fragment
        }
    }
}