package com.a60n1.ejashkojme.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import com.a60n1.ejashkojme.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputLayout

/**
 * fragment to hold new post
 */
class NewPostFragment : BaseFragment() {
    private var mTitleField: EditText? = null
    private var mBodyField: EditText? = null
    private var mNextButton: FloatingActionButton? = null
    private var mLayoutTitle: TextInputLayout? = null
    private var mLayoutBody: TextInputLayout? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_new_post, container, false)
        mTitleField = view.findViewById(R.id.field_title)
        mBodyField = view.findViewById(R.id.field_body)
        mNextButton = view.findViewById(R.id.fab_next_pickdate)
        mLayoutTitle = view.findViewById(R.id.field_title_layout)
        mLayoutBody = view.findViewById(R.id.field_body_layout)
        mTitleField!!.addTextChangedListener(MyTextWatcher(mTitleField!!))
        mBodyField!!.addTextChangedListener(MyTextWatcher(mBodyField!!))
        mNextButton!!.setOnClickListener {
            hideKeyboard()
            goToNext()
        }
        return view
    }

    private fun goToNext() {
        if (!validateTitle()) {
            return
        }
        if (!validateBody()) {
            return
        }
        val title = mTitleField!!.text.toString()
        val body = mBodyField!!.text.toString()
        mainActivity!!.onPickDateTimeBtnClicked(title, body)
    }

    private fun validateTitle(): Boolean {
        val title = mTitleField!!.text.toString().trim { it <= ' ' }
        if (title.isEmpty()) {
            mLayoutTitle!!.error = getString(R.string.err_msg_title)
            requestFocus(mTitleField)
            return false
        } else {
            mLayoutTitle!!.isErrorEnabled = false
        }
        return true
    }

    private fun validateBody(): Boolean {
        val body = mBodyField!!.text.toString().trim { it <= ' ' }
        if (body.isEmpty()) {
            mLayoutBody!!.error = getString(R.string.err_msg_body)
            requestFocus(mBodyField)
            return false
        } else {
            mLayoutBody!!.isErrorEnabled = false
        }
        return true
    }

    private fun requestFocus(view: View?) {
        if (view!!.requestFocus()) {
            activity!!.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }
    }

    private inner class MyTextWatcher internal constructor(private val view: View) : TextWatcher {
        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun afterTextChanged(editable: Editable) {
            when (view.id) {
                R.id.field_title -> validateTitle()
                R.id.field_body -> validateBody()
            }
        }

    }

    companion object {
        fun newInstance(): NewPostFragment {
            return NewPostFragment()
        }
    }
}