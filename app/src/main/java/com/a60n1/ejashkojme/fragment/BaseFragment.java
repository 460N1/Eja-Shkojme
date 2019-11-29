package com.a60n1.ejashkojme.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.a60n1.ejashkojme.MainActivity;

import java.util.Objects;

public class BaseFragment extends Fragment {
    MainActivity mainActivity;

    public BaseFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mainActivity = (MainActivity) context;
        } catch (ClassCastException e) {
            throw new IllegalStateException(context.getClass().getSimpleName()
                    + " is not MainActivity", e);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            try {
                mainActivity = (MainActivity) activity;
            } catch (ClassCastException e) {
                throw new IllegalStateException(activity.getClass().getSimpleName()
                        + " is not MainActivity", e);
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mainActivity = null;
    }

    public String getUid() {
        return mainActivity.getUid();
    }

    public void hideKeyboard() {
        // hides the keyboard if applicable
        InputMethodManager inputManager = (InputMethodManager)
                Objects.requireNonNull(getActivity()).getSystemService(Context.INPUT_METHOD_SERVICE);
        Objects.requireNonNull(inputManager).hideSoftInputFromWindow(Objects.requireNonNull(getActivity().getCurrentFocus()).getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }
}
