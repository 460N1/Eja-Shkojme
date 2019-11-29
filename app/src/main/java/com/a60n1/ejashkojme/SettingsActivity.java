package com.a60n1.ejashkojme;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.a60n1.ejashkojme.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends BaseActivity {
    private static final String TAG = "SettingsActivity";
    private static final int SELECT_IMAGE = 1;
    private DatabaseReference mUserDatabase;
    private StorageReference mStorage;
    private CircleImageView mAvatar;
    private TextView mName, mStatus;
    private Button mStatusButton, mImageButton;
    private ProgressDialog mProgress;
    private String download_url = "default", thumb_downloadUrl = "default";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        final String userId = getUid();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
        mUserDatabase.keepSynced(true);
        mStorage = FirebaseStorage.getInstance().getReference();

        mAvatar = findViewById(R.id.settings_image);
        mName = findViewById(R.id.settings_name);
        mStatus = findViewById(R.id.settings_status);
        mStatusButton = findViewById(R.id.settings_status_btn);
        mImageButton = findViewById(R.id.settings_image_btn);

        mStatusButton.setOnClickListener(v -> {
            String status_value = mStatus.getText().toString();
            Intent intent = new Intent(SettingsActivity.this, ChangeStatusActivity.class);
            intent.putExtra("status_value", status_value);
            startActivity(intent);
        });

        mImageButton.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);

            startActivityForResult(Intent.createChooser(intent, "Select image"), SELECT_IMAGE);
        });

        // get user info from firebase
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Get user value
                User user = dataSnapshot.getValue(User.class);

                if (user == null) {
                    // User is null, error out
                    Log.e(TAG, "User " + userId + " is unexpectedly null");
                    Toast.makeText(SettingsActivity.this,
                            "Error: could not fetch user.",
                            Toast.LENGTH_SHORT).show();
                } else {
                    mName.setText(user.name);
                    mStatus.setText(user.status);
                    final String image = user.image;
                    if (!image.equals("default")) {
                        Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE)
                                .placeholder(R.drawable.default_avatar).into(mAvatar, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError(Exception e) {
                                Picasso.get().load(image).placeholder(R.drawable.default_avatar).into(mAvatar);
                            }
                        });
                    } else {
                        Picasso.get().load(image).placeholder(R.drawable.default_avatar).into(mAvatar);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // after cropping the image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_IMAGE && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            CropImage.activity(uri)
                    .setAspectRatio(1, 1)
                    .setMinCropWindowSize(500, 500)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mProgress = new ProgressDialog(SettingsActivity.this);
                mProgress.setTitle("Uploading image...");
                mProgress.setCanceledOnTouchOutside(false);
                mProgress.show();

                try {
                    Uri resultUri = result.getUri();

                    final File thumb_filePath = new File(Objects.requireNonNull(resultUri.getPath()));

                    Bitmap thumb_bitmap = new Compressor(this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(75)
                            .compressToBitmap(thumb_filePath);
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    final byte[] thumb_byte = outputStream.toByteArray();

                    String user_id = getUid();
                    final StorageReference image_path = mStorage.child("profile_images").child(user_id + ".jpg");
                    final StorageReference thumb_path = mStorage.child("profile_images").child("thumbnails").child(user_id + ".jpg");
                    image_path.putFile(resultUri).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            image_path.getDownloadUrl().addOnSuccessListener(uri -> download_url = uri.toString());
                            UploadTask uploadTask = thumb_path.putBytes(thumb_byte);
                            uploadTask.addOnCompleteListener(thumb_task -> {
                                if (thumb_task.isSuccessful()) {
                                    thumb_path.getDownloadUrl().addOnSuccessListener(uri -> {
                                        thumb_downloadUrl = uri.toString();
                                        Map image_map = new HashMap();
                                        image_map.put("image", download_url);
                                        image_map.put("thumb_image", thumb_downloadUrl);
                                        mUserDatabase.updateChildren(image_map).addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                mProgress.dismiss();
                                                Toast.makeText(SettingsActivity.this, "Successfully uploaded image", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    });
                                } else {
                                    mProgress.dismiss();
                                    Toast.makeText(SettingsActivity.this, "Error uploading thumbnail", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            mProgress.dismiss();
                            Toast.makeText(SettingsActivity.this, "Error uploaded image", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

}
