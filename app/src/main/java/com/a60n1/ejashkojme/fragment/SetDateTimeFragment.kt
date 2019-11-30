package com.a60n1.ejashkojme.fragment

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TimePicker
import com.a60n1.ejashkojme.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputLayout
import java.util.*

class SetDateTimeFragment : BaseFragment() {
    private var mPickDateText: EditText? = null
    private var mPickTimeText: EditText? = null
    private var mNextButton: FloatingActionButton? = null
    private var mLayoutDate: TextInputLayout? = null
    private var mLayoutTime: TextInputLayout? = null
    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? { // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_setdatetime, container, false)
        mPickDateText = view.findViewById(R.id.pickdate_text)
        mPickTimeText = view.findViewById(R.id.picktime_text)
        mNextButton = view.findViewById(R.id.fab_next_pickorigin)
        mLayoutDate = view.findViewById(R.id.field_pickdate_layout)
        mLayoutTime = view.findViewById(R.id.field_picktime_layout)
        mPickDateText!!.setOnClickListener {
            val now = Calendar.getInstance()
            DatePickerDialog(
                    activity!!,
                    DatePickerDialog.OnDateSetListener { _: DatePicker?, year: Int, month: Int, dayOfMonth: Int ->
                        Log.d("Orignal", "Got clicked")
                        mPickDateText!!.setText("""$year-${month + 1}-$dayOfMonth""")
                    },
                    now[Calendar.YEAR],
                    now[Calendar.MONTH],
                    now[Calendar.DAY_OF_MONTH]
            ).show()
        }
        mPickTimeText!!.setOnClickListener {
            val now = Calendar.getInstance()
            TimePickerDialog(
                    activity,
                    TimePickerDialog.OnTimeSetListener { _: TimePicker?, hour: Int, minute: Int ->
                        Log.d("Original", "Got clicked")
                        val formatedMinute = String.format("%02d", minute)
                        mPickTimeText!!.setText("$hour:$formatedMinute")
                    },
                    now[Calendar.HOUR_OF_DAY],
                    now[Calendar.MINUTE],
                    true
            ).show()
        }
        mNextButton!!.setOnClickListener { goToNext() }
        return view
    }

    private fun goToNext() {
        if (!validateDate()) {
            return
        }
        if (!validateTime()) {
            return
        }
        val date = mPickDateText!!.text.toString()
        val time = mPickTimeText!!.text.toString()
        mainActivity!!.onPickOriginDestinationBtnClicked(date, time)
    }

    private fun validateDate(): Boolean {
        val date = mPickDateText!!.text.toString().trim { it <= ' ' }
        if (date.isEmpty()) {
            mLayoutDate!!.error = getString(R.string.err_msg_date)
            return false
        } else {
            mLayoutDate!!.isErrorEnabled = false
        }
        return true
    }

    private fun validateTime(): Boolean {
        val time = mPickTimeText!!.text.toString().trim { it <= ' ' }
        if (time.isEmpty()) {
            mLayoutTime!!.error = getString(R.string.err_msg_time)
            return false
        } else {
            mLayoutTime!!.isErrorEnabled = false
        }
        return true
    }

    companion object {
        fun newInstance(): SetDateTimeFragment {
            return SetDateTimeFragment()
        }
    }
}