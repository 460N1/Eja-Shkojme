package com.a60n1.ejashkojme;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class ChangeStatusActivity extends BaseActivity {

    private DatabaseReference mDatabase;
    private TextInputLayout mLayoutStatus;
    private EditText mStatus;
    private Button mSaveButton;
    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_status);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        Toolbar toolbar = findViewById(R.id.toolbar_change_status);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Account Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String status_value = getIntent().getStringExtra("status_value");

        mLayoutStatus = findViewById(R.id.layout_status_input);
        mStatus = findViewById(R.id.status_input);
        mStatus.addTextChangedListener(new ChangeStatusActivity.MyTextWatcher(mStatus));
        mStatus.setText(status_value);

        mSaveButton = findViewById(R.id.status_save_btn);
        mSaveButton.setOnClickListener(v -> {
            hideKeyboard();
            changeStatus();
        });

    }

    public void changeStatus() {
        if (!validateStatus()) {
            return;
        }

        mProgress = new ProgressDialog(ChangeStatusActivity.this);
        mProgress.setTitle("Saving changes...");
        mProgress.show();

        final String status = mStatus.getText().toString();
        String user_id = getUid();
        mDatabase.child("users").child(user_id).child("status").setValue(status).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                mProgress.dismiss();
            } else {
                Toast.makeText(ChangeStatusActivity.this, "Error saving changes", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateStatus() {
        String status = mStatus.getText().toString().trim();

        if (status.isEmpty()) {
            mLayoutStatus.setError(getString(R.string.err_msg_status));
            requestFocus(mStatus);
            return false;
        } else {
            mLayoutStatus.setErrorEnabled(false);
        }
        return true;
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
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
            if (view.getId() == R.id.status_input) {
                validateStatus();
            }
        }
    }
}
