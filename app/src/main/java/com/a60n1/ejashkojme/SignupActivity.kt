package com.a60n1.ejashkojme

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.a60n1.ejashkojme.models.User
import com.google.android.gms.tasks.Task
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId

class SignupActivity : BaseActivity() {
    private var mAuth: FirebaseAuth? = null
    private var mDatabase: DatabaseReference? = null
    private var mFirstName: EditText? = null
    private var mLastName: EditText? = null
    private var mEmail: EditText? = null
    private var mPassword: EditText? = null
    private var mSignup: Button? = null
    private var mSignin: TextView? = null
    private var mLayoutFirstName: TextInputLayout? = null
    private var mLayoutLastName: TextInputLayout? = null
    private var mLayoutEmail: TextInputLayout? = null
    private var mLayoutPassword: TextInputLayout? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        FirebaseApp.initializeApp(applicationContext)
        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        mFirstName = findViewById(R.id.signup_firstname)
        mLastName = findViewById(R.id.signup_lastname)
        mEmail = findViewById(R.id.signup_email)
        mPassword = findViewById(R.id.signup_password)
        mFirstName!!.addTextChangedListener(MyTextWatcher(mFirstName!!))
        mLastName!!.addTextChangedListener(MyTextWatcher(mLastName!!))
        mEmail!!.addTextChangedListener(MyTextWatcher(mEmail!!))
        mPassword!!.addTextChangedListener(MyTextWatcher(mPassword!!))
        mLayoutFirstName = findViewById(R.id.signup_firstname_layout)
        mLayoutLastName = findViewById(R.id.signup_lastname_layout)
        mLayoutEmail = findViewById(R.id.signup_email_layout)
        mLayoutPassword = findViewById(R.id.signup_password_layout)
        mSignup = findViewById(R.id.signup_button)
        mSignup!!.setOnClickListener {
            hideKeyboard()
            submitRegisterInfo()
        }
        mSignin = findViewById(R.id.signin_button_text)
        mSignin!!.setOnClickListener {
            hideKeyboard()
            val intent = Intent(this@SignupActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    private fun submitRegisterInfo() {
        if (!validateFirstName())
            return
        if (!validateLastName())
            return
        if (!validateEmail())
            return
        if (!validatePassword())
            return
        showProgressDialog()
        val name = mFirstName!!.text.toString().trim { it <= ' ' } + " " + mLastName!!.text.toString().trim { it <= ' ' }
        val email = mEmail!!.text.toString()
        val password = mPassword!!.text.toString()
        mAuth!!.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this@SignupActivity) { task: Task<AuthResult?> ->
                    hideProgressDialog()
                    // check nese sukses
                    if (!task.isSuccessful)
                        Toast.makeText(this@SignupActivity, getString(R.string.error_signup), Toast.LENGTH_SHORT).show()
                    else {
                        Toast.makeText(this@SignupActivity, getString(R.string.success_signup), Toast.LENGTH_SHORT).show()
                        val userId = uid
                        @Suppress("DEPRECATION") val deviceToken = FirebaseInstanceId.getInstance().token
                        writeNewUser(userId, name, email, deviceToken)
                    }
                }
    }

    private fun validateFirstName(): Boolean {
        if (mFirstName!!.text.toString().trim { it <= ' ' }.isEmpty()) {
            mLayoutFirstName!!.error = getString(R.string.err_msg_firstname)
            requestFocus(mFirstName)
            return false
        } else
            mLayoutFirstName!!.isErrorEnabled = false
        return true
    }

    private fun validateLastName(): Boolean {
        if (mLastName!!.text.toString().trim { it <= ' ' }.isEmpty()) {
            mLayoutLastName!!.error = getString(R.string.err_msg_lastname)
            requestFocus(mLastName)
            return false
        } else
            mLayoutLastName!!.isErrorEnabled = false
        return true
    }

    private fun validateEmail(): Boolean {
        val email = mEmail!!.text.toString().trim { it <= ' ' }
        if (email.isEmpty() || !isValidEmail(email)) {
            mLayoutEmail!!.error = getString(R.string.err_msg_email)
            requestFocus(mEmail)
            return false
        } else
            mLayoutEmail!!.isErrorEnabled = false
        return true
    }

    private fun validatePassword(): Boolean {
        if (mPassword!!.text.toString().trim { it <= ' ' }.isEmpty()) {
            mLayoutPassword!!.error = getString(R.string.err_msg_password)
            requestFocus(mPassword)
            return false
        } else
            mLayoutPassword!!.isErrorEnabled = false
        return true
    }

    private fun requestFocus(view: View?) {
        if (view!!.requestFocus())
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
    }

    private fun writeNewUser(userId: String, name: String, email: String, device_token: String?) {
        val user = User(name, email, device_token)
        mDatabase!!.child("users").child(userId).setValue(user).addOnCompleteListener { task: Task<Void?> ->
            if (task.isSuccessful) {
                val intent = Intent(this@SignupActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private inner class MyTextWatcher(private val view: View) : TextWatcher {
        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun afterTextChanged(editable: Editable) {
            when (view.id) {
                R.id.signup_firstname -> validateFirstName()
                R.id.signup_lastname -> validateLastName()
                R.id.signup_email -> validateEmail()
                R.id.signup_password -> validatePassword()
            }
        }

    }

    companion object {
        private fun isValidEmail(email: String): Boolean {
            return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()
        }
    }
}