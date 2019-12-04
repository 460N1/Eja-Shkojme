@file:Suppress("DEPRECATION")

package com.a60n1.ejashkojme

import android.app.ProgressDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.android.gms.tasks.Task
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ChangeStatusActivity : BaseActivity() {
    private var mDatabase: DatabaseReference? = null
    private var mLayoutStatus: TextInputLayout? = null
    private var mStatus: EditText? = null
    private var mSaveButton: Button? = null
    private var mProgress: ProgressDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_status)
        mDatabase = FirebaseDatabase.getInstance().reference
        val toolbar = findViewById<Toolbar>(R.id.toolbar_change_status)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = getString(R.string.account_status)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        val statusValue = intent.getStringExtra("status_value")
        mLayoutStatus = findViewById(R.id.layout_status_input)
        mStatus = findViewById(R.id.status_input)
        mStatus!!.addTextChangedListener(MyTextWatcher(mStatus!!))
        mStatus!!.setText(statusValue)
        mSaveButton = findViewById(R.id.status_save_btn)
        mSaveButton!!.setOnClickListener {
            hideKeyboard()
            changeStatus()
        }
    }

    private fun changeStatus() {
        if (!validateStatus())
            return
        mProgress = ProgressDialog(this@ChangeStatusActivity)
        mProgress!!.setTitle(getString(R.string.saving))
        mProgress!!.show()
        val status = mStatus!!.text.toString()
        val userId = uid
        mDatabase!!.child("users").child(userId).child("status").setValue(status).addOnCompleteListener { task: Task<Void?> ->
            if (task.isSuccessful)
                mProgress!!.dismiss()
            else
                Toast.makeText(this@ChangeStatusActivity, "Error saving changes", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateStatus(): Boolean {
        val status = mStatus!!.text.toString().trim { it <= ' ' }
        if (status.isEmpty()) {
            mLayoutStatus!!.error = getString(R.string.err_msg_status)
            requestFocus(mStatus)
            return false
        } else
            mLayoutStatus!!.isErrorEnabled = false
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
            if (view.id == R.id.status_input)
                validateStatus()
        }

    }
}