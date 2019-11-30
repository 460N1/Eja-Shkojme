package com.a60n1.ejashkojme;

import android.content.Intent;
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

import com.a60n1.ejashkojme.models.User;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class SignupActivity extends BaseActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private EditText mFirstName, mLastName, mEmail, mPassword;
    private Button mSignup;
    private TextView mSignin;
    private TextInputLayout mLayoutFirstName, mLayoutLastName, mLayoutEmail, mLayoutPassword;

    private static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        FirebaseApp.initializeApp(getApplicationContext());

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mFirstName = findViewById(R.id.signup_firstname);
        mLastName = findViewById(R.id.signup_lastname);

        mEmail = findViewById(R.id.signup_email);
        mPassword = findViewById(R.id.signup_password);

        mFirstName.addTextChangedListener(new SignupActivity.MyTextWatcher(mFirstName));
        mLastName.addTextChangedListener(new SignupActivity.MyTextWatcher(mLastName));
        mEmail.addTextChangedListener(new SignupActivity.MyTextWatcher(mEmail));
        mPassword.addTextChangedListener(new SignupActivity.MyTextWatcher(mPassword));

        mLayoutFirstName = findViewById(R.id.signup_firstname_layout);
        mLayoutLastName = findViewById(R.id.signup_lastname_layout);
        mLayoutEmail = findViewById(R.id.signup_email_layout);
        mLayoutPassword = findViewById(R.id.signup_password_layout);

        mSignup = findViewById(R.id.signup_button);
        mSignup.setOnClickListener(view -> {
            hideKeyboard();
            submitRegisterInfo();
        });

        mSignin = findViewById(R.id.signin_button_text);
        mSignin.setOnClickListener(view -> {
            hideKeyboard();
            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    /**
     * Validating form inputs
     */
    private void submitRegisterInfo() {
        if (!validateFirstName()) {
            return;
        }

        if (!validateLastName()) {
            return;
        }

        if (!validateEmail()) {
            return;
        }

        if (!validatePassword()) {
            return;
        }

        showProgressDialog();
        final String name = mFirstName.getText().toString().trim() + " " + mLastName.getText().toString().trim();
        final String email = mEmail.getText().toString();
        final String password = mPassword.getText().toString();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(SignupActivity.this, task -> {
                    hideProgressDialog();
                    //check register is successful
                    if (!task.isSuccessful()) {
                        Toast.makeText(SignupActivity.this, "Error signing up", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(SignupActivity.this, "Successfully signed up", Toast.LENGTH_SHORT).show();
                        final String user_id = getUid();
                        final String device_token = FirebaseInstanceId.getInstance().getToken();
                        writeNewUser(user_id, name, email, device_token);
                    }
                });
    }

    private boolean validateFirstName() {
        if (mFirstName.getText().toString().trim().isEmpty()) {
            mLayoutFirstName.setError(getString(R.string.err_msg_firstname));
            requestFocus(mFirstName);
            return false;
        } else {
            mLayoutFirstName.setErrorEnabled(false);
        }
        return true;
    }

    private boolean validateLastName() {
        if (mLastName.getText().toString().trim().isEmpty()) {
            mLayoutLastName.setError(getString(R.string.err_msg_lastname));
            requestFocus(mLastName);
            return false;
        } else {
            mLayoutLastName.setErrorEnabled(false);
        }
        return true;
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

    private void writeNewUser(String userId, String name, String email, String device_token) {
        User user = new User(name, email, device_token);

        mDatabase.child("users").child(userId).setValue(user).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
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
                case R.id.signup_firstname:
                    validateFirstName();
                    break;
                case R.id.signup_lastname:
                    validateLastName();
                    break;
                case R.id.signup_email:
                    validateEmail();
                    break;
                case R.id.signup_password:
                    validatePassword();
                    break;
            }
        }
    }
}
