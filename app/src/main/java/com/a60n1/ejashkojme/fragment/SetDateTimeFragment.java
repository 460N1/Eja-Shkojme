package com.a60n1.ejashkojme.fragment;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.a60n1.ejashkojme.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;
import java.util.Objects;


public class SetDateTimeFragment extends BaseFragment {

    private EditText mPickDateText, mPickTimeText;
    private FloatingActionButton mNextButton;
    private TextInputLayout mLayoutDate, mLayoutTime;

    public SetDateTimeFragment() {
        // Required empty public constructor
    }

    public static SetDateTimeFragment newInstance() {
        return new SetDateTimeFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_setdatetime, container, false);
        mPickDateText = view.findViewById(R.id.pickdate_text);
        mPickTimeText = view.findViewById(R.id.picktime_text);
        mNextButton = view.findViewById(R.id.fab_next_pickorigin);
        mLayoutDate = view.findViewById(R.id.field_pickdate_layout);
        mLayoutTime = view.findViewById(R.id.field_picktime_layout);
        mPickDateText.setOnClickListener(v -> {
            Calendar now = Calendar.getInstance();
            new DatePickerDialog(
                    Objects.requireNonNull(getActivity()),
                    (view1, year, month, dayOfMonth) -> {
                        Log.d("Orignal", "Got clicked");
                        month++;
                        mPickDateText.setText(year + "-" + month + "-" + dayOfMonth);
                    },
                    now.get(Calendar.YEAR),
                    now.get(Calendar.MONTH),
                    now.get(Calendar.DAY_OF_MONTH)
            ).show();
        });
        mPickTimeText.setOnClickListener(v -> {
            Calendar now = Calendar.getInstance();
            new android.app.TimePickerDialog(
                    getActivity(),
                    (view12, hour, minute) -> {
                        Log.d("Original", "Got clicked");
                        String formatedMinute = String.format("%02d", minute);
                        mPickTimeText.setText(hour + ":" + formatedMinute);

                    },
                    now.get(Calendar.HOUR_OF_DAY),
                    now.get(Calendar.MINUTE),
                    true
            ).show();
        });
        mNextButton.setOnClickListener(v -> goToNext());
        return view;
    }

    private void goToNext() {
        if (!validateDate()) {
            return;
        }

        if (!validateTime()) {
            return;
        }

        final String date = mPickDateText.getText().toString();
        final String time = mPickTimeText.getText().toString();
        mainActivity.onPickOriginDestinationBtnClicked(date, time);
    }

    private boolean validateDate() {
        String date = mPickDateText.getText().toString().trim();

        if (date.isEmpty()) {
            mLayoutDate.setError(getString(R.string.err_msg_date));
            return false;
        } else {
            mLayoutDate.setErrorEnabled(false);
        }
        return true;
    }

    private boolean validateTime() {
        String time = mPickTimeText.getText().toString().trim();

        if (time.isEmpty()) {
            mLayoutTime.setError(getString(R.string.err_msg_time));
            return false;
        } else {
            mLayoutTime.setErrorEnabled(false);
        }
        return true;
    }

}
