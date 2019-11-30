package com.a60n1.ejashkojme;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.Locale;

public class LoginActivity extends BaseActivity {
    private EditText mEmail, mPassword;
    private Button mLogin;
    private TextView mSignup;
    private TextInputLayout mLayoutEmail, mLayoutPassword;

    private FirebaseAuth mAuth;

    private static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Comment out in final release
        Locale locale = new Locale("sq");
        Locale.setDefault(locale);
        Configuration config = getBaseContext().getResources().getConfiguration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
        // Or add language toggle
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        FirebaseApp.initializeApp(getApplicationContext());

        mAuth = FirebaseAuth.getInstance();

        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);

        mLayoutEmail = findViewById(R.id.email_layout);
        mLayoutPassword = findViewById(R.id.password_layout);

        mLogin = findViewById(R.id.login_button);

        mSignup = findViewById(R.id.signup_button_text);

        mEmail.addTextChangedListener(new MyTextWatcher(mEmail));
        mPassword.addTextChangedListener(new MyTextWatcher(mPassword));

        mLogin.setOnClickListener(view -> {
            hideKeyboard();
            submitLoginInfo();
        });

        mSignup.setOnClickListener(view -> {
            hideKeyboard();
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
            finish();
        });

    }

    /**
     * Validating form inputs
     */
    private void submitLoginInfo() {
        if (!validateEmail()) {
            return;
        }

        if (!validatePassword()) {
            return;
        }

        showProgressDialog();
        // after validated info
        final String email = mEmail.getText().toString();
        final String password = mPassword.getText().toString();
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(LoginActivity.this, task -> {
            hideProgressDialog();
            if (task.isSuccessful()) {
                final String userId = getUid();
                final String deviceToken = FirebaseInstanceId.getInstance().getToken();
                FirebaseDatabase.getInstance().getReference().child("users").child(userId).child("device_token")
                        .setValue(deviceToken).addOnSuccessListener(aVoid -> {
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        });
            } else {
                Toast.makeText(LoginActivity.this, "Error logging in", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateEmail() {
        String email = mEmail.getText().toString().trim();

        if (email.isEmpty() || !isValidEmail(email)) {
            mLayoutEmail.setError(getString(R.string.err_msg_email));
            requestFocus(mEmail);
            return false;
        } else {
            mLayoutEmail.setErrorEnabled(false);
        }

        return true;
    }

    private boolean validatePassword() {
        if (mPassword.getText().toString().trim().isEmpty()) {
            mLayoutPassword.setError(getString(R.string.err_msg_password));
            requestFocus(mPassword);
            return false;
        } else {
            mLayoutPassword.setErrorEnabled(false);
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
            switch (view.getId()) {
                case R.id.email:
                    validateEmail();
                    break;
                case R.id.password:
                    validatePassword();
                    break;
            }
        }
    }

}
