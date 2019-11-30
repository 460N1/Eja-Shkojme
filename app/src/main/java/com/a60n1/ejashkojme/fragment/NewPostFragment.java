package com.a60n1.ejashkojme.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;

import com.a60n1.ejashkojme.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

/**
 * fragment to hold new post
 */
public class NewPostFragment extends BaseFragment {

    private EditText mTitleField;
    private EditText mBodyField;
    private FloatingActionButton mNextButton;
    private TextInputLayout mLayoutTitle, mLayoutBody;

    public NewPostFragment() {
    }

    public static NewPostFragment newInstance() {
        return new NewPostFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_post, container, false);

        mTitleField = view.findViewById(R.id.field_title);
        mBodyField = view.findViewById(R.id.field_body);
        mNextButton = view.findViewById(R.id.fab_next_pickdate);
        mLayoutTitle = view.findViewById(R.id.field_title_layout);
        mLayoutBody = view.findViewById(R.id.field_body_layout);

        mTitleField.addTextChangedListener(new MyTextWatcher(mTitleField));
        mBodyField.addTextChangedListener(new MyTextWatcher(mBodyField));

        mNextButton.setOnClickListener(v -> {
            hideKeyboard();
            goToNext();
        });
        return view;
    }

    private void goToNext() {
        if (!validateTitle())
            return;

        if (!validateBody())
            return;

        final String title = mTitleField.getText().toString();
        final String body = mBodyField.getText().toString();
        mainActivity.onPickDateTimeBtnClicked(title, body);

    }

    private boolean validateTitle() {
        String title = mTitleField.getText().toString().trim();

        if (title.isEmpty()) {
            mLayoutTitle.setError(getString(R.string.err_msg_title));
            requestFocus(mTitleField);
            return false;
        } else
            mLayoutTitle.setErrorEnabled(false);
        return true;
    }

    private boolean validateBody() {
        String body = mBodyField.getText().toString().trim();

        if (body.isEmpty()) {
            mLayoutBody.setError(getString(R.string.err_msg_body));
            requestFocus(mBodyField);
            return false;
        } else
            mLayoutBody.setErrorEnabled(false);
        return true;
    }

    private void requestFocus(View view) {
        if (view.requestFocus())
            Objects.requireNonNull(getActivity()).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
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
                case R.id.field_title:
                    validateTitle();
                    break;
                case R.id.field_body:
                    validateBody();
                    break;
            }
        }
    }

}
