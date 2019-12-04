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
import com.a60n1.ejashkojme.SignupActivity
import com.google.android.gms.tasks.Task
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId

@Suppress("DEPRECATION")
class LoginActivity : BaseActivity() {
    private var mEmail: EditText? = null
    private var mPassword: EditText? = null
    private var mLogin: Button? = null
    private var mSignup: TextView? = null
    private var mLayoutEmail: TextInputLayout? = null
    private var mLayoutPassword: TextInputLayout? = null
    private var mAuth: FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        //        val locale = Locale("sq")
        //        Locale.setDefault(locale)
        //        val config = baseContext.resources.configuration
        //        config.locale = locale
        //        baseContext.resources.updateConfiguration(config,
        //                baseContext.resources.displayMetrics)
        // hard-set gjuhen ne shqip pavarsisht prej device language
        // ^^^
        // language toggle duhet me u shtu
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        FirebaseApp.initializeApp(applicationContext)
        mAuth = FirebaseAuth.getInstance()
        mEmail = findViewById(R.id.email)
        mPassword = findViewById(R.id.password)
        mLayoutEmail = findViewById(R.id.email_layout)
        mLayoutPassword = findViewById(R.id.password_layout)
        mLogin = findViewById(R.id.login_button)
        mSignup = findViewById(R.id.signup_button_text)
        mEmail!!.addTextChangedListener(MyTextWatcher(mEmail!!))
        mPassword!!.addTextChangedListener(MyTextWatcher(mPassword!!))
        mLogin!!.setOnClickListener {
            hideKeyboard()
            submitLoginInfo()
        }
        mSignup!!.setOnClickListener {
            hideKeyboard()
            val intent = Intent(this@LoginActivity, SignupActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun submitLoginInfo() {
        if (!validateEmail())
            return
        if (!validatePassword())
            return
        showProgressDialog()
        // nese validimi me sukses
        val email = mEmail!!.text.toString()
        val password = mPassword!!.text.toString()
        mAuth!!.signInWithEmailAndPassword(email, password).addOnCompleteListener(this@LoginActivity) { task: Task<AuthResult?> ->
            hideProgressDialog()
            if (task.isSuccessful) {
                val userId = uid
                val deviceToken = FirebaseInstanceId.getInstance().token
                FirebaseDatabase.getInstance().reference.child("users").child(userId).child("device_token")
                        .setValue(deviceToken).addOnSuccessListener {
                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
            } else
                Toast.makeText(this@LoginActivity, "Error logging in", Toast.LENGTH_SHORT).show()
        }
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

    private inner class MyTextWatcher(private val view: View) : TextWatcher {
        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun afterTextChanged(editable: Editable) {
            when (view.id) {
                R.id.email -> validateEmail()
                R.id.password -> validatePassword()
            }
        }

    }

    companion object {
        private fun isValidEmail(email: String): Boolean {
            return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()
        }
    }
}