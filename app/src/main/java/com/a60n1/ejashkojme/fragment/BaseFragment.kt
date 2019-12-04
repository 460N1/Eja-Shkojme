package com.a60n1.ejashkojme.fragment

import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import com.a60n1.ejashkojme.MainActivity

open class BaseFragment : Fragment() {
    @JvmField
    var mainActivity: MainActivity? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = try {
            context as MainActivity
        } catch (e: ClassCastException) {
            throw IllegalStateException(context.javaClass.simpleName
                    + " nuk eshte MainActivity", e)
        }
    }

    @Suppress("DEPRECATION")
    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            mainActivity = try {
                activity as MainActivity
            } catch (e: ClassCastException) {
                throw IllegalStateException(activity.javaClass.simpleName
                        + " nuk eshte MainActivity", e)
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        mainActivity = null
    }

    val uid: String
        get() = mainActivity!!.uid

    fun hideKeyboard() { // me hjek keyboard kur kemi nevoj
        val inputManager = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(activity!!.currentFocus!!.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS)
    }
}