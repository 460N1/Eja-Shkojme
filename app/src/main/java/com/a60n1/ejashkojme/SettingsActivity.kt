package com.a60n1.ejashkojme

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.a60n1.ejashkojme.models.User
import com.google.android.gms.tasks.Task
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import de.hdodenhof.circleimageview.CircleImageView
import id.zelory.compressor.Compressor
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.*

class SettingsActivity : BaseActivity() {
    private var mUserDatabase: DatabaseReference? = null
    private var mStorage: StorageReference? = null
    private var mAvatar: CircleImageView? = null
    private var mName: TextView? = null
    private var mStatus: TextView? = null
    private var mStatusButton: Button? = null
    private var mImageButton: Button? = null
    private var mProgress: ProgressDialog? = null
    private var downloadUrl = "default"
    private var thumbDownloadurl = "default"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        val userId = uid
        mUserDatabase = FirebaseDatabase.getInstance().reference.child("users").child(userId)
        mUserDatabase!!.keepSynced(true)
        mStorage = FirebaseStorage.getInstance().reference
        mAvatar = findViewById(R.id.settings_image)
        mName = findViewById(R.id.settings_name)
        mStatus = findViewById(R.id.settings_status)
        mStatusButton = findViewById(R.id.settings_status_btn)
        mImageButton = findViewById(R.id.settings_image_btn)
        mStatusButton!!.setOnClickListener {
            val statusValue = mStatus!!.text.toString()
            val intent = Intent(this@SettingsActivity, ChangeStatusActivity::class.java)
            intent.putExtra("status_value", statusValue)
            startActivity(intent)
        }
        mImageButton!!.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select image"), SELECT_IMAGE)
        }
        // get user info from firebase
        mUserDatabase!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) { // Get user value
                val user = dataSnapshot.getValue(User::class.java)
                if (user == null) { // User is null, error out
                    Log.e(TAG, "User $userId is unexpectedly null")
                    Toast.makeText(this@SettingsActivity,
                            "Error: could not fetch user.",
                            Toast.LENGTH_SHORT).show()
                } else {
                    mName!!.text = user.name
                    mStatus!!.text = user.status
                    val image = user.image
                    if (image != "default") {
                        Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE)
                                .placeholder(R.drawable.default_avatar).into(mAvatar, object : Callback {
                                    override fun onSuccess() {}
                                    override fun onError(e: Exception) {
                                        Picasso.get().load(image).placeholder(R.drawable.default_avatar).into(mAvatar)
                                    }
                                })
                    } else {
                        Picasso.get().load(image).placeholder(R.drawable.default_avatar).into(mAvatar)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    // after cropping the image
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SELECT_IMAGE && resultCode == Activity.RESULT_OK) {
            val uri = data!!.data
            CropImage.activity(uri)
                    .setAspectRatio(1, 1)
                    .setMinCropWindowSize(500, 500)
                    .start(this)
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                mProgress = ProgressDialog(this@SettingsActivity)
                mProgress!!.setTitle("Uploading image...")
                mProgress!!.setCanceledOnTouchOutside(false)
                mProgress!!.show()
                try {
                    val resultUri = result.uri
                    val thumbFilepath = File(resultUri.path!!)
                    val thumbBitmap = Compressor(this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(75)
                            .compressToBitmap(thumbFilepath)
                    val outputStream = ByteArrayOutputStream()
                    thumbBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                    val thumbByte = outputStream.toByteArray()
                    val userId = uid
                    val imagePath = mStorage!!.child("profile_images").child("$userId.jpg")
                    val thumbPath = mStorage!!.child("profile_images").child("thumbnails").child("$userId.jpg")
                    imagePath.putFile(resultUri).addOnCompleteListener { task: Task<UploadTask.TaskSnapshot?> ->
                        if (task.isSuccessful) {
                            imagePath.downloadUrl.addOnSuccessListener { uri: Uri -> downloadUrl = uri.toString() }
                            val uploadTask = thumbPath.putBytes(thumbByte)
                            uploadTask.addOnCompleteListener { thumb_task: Task<UploadTask.TaskSnapshot?> ->
                                if (thumb_task.isSuccessful) {
                                    thumbPath.downloadUrl.addOnSuccessListener { uri: Uri ->
                                        thumbDownloadurl = uri.toString()
                                        val imageMap: MutableMap<String, Any> = HashMap()
                                        imageMap["image"] = downloadUrl
                                        imageMap["thumb_image"] = thumbDownloadurl
                                        mUserDatabase!!.updateChildren(imageMap).addOnCompleteListener { task1: Task<*> ->
                                            if (task1.isSuccessful) {
                                                mProgress!!.dismiss()
                                                Toast.makeText(this@SettingsActivity, "Successfully uploaded image", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                } else {
                                    mProgress!!.dismiss()
                                    Toast.makeText(this@SettingsActivity, "Error uploading thumbnail", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            mProgress!!.dismiss()
                            Toast.makeText(this@SettingsActivity, "Error uploaded image", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
            }
        }
    }

    override fun onBackPressed() {
        val intent = Intent(this@SettingsActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    companion object {
        private const val TAG = "SettingsActivity"
        private const val SELECT_IMAGE = 1
    }
}