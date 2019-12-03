/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.a60n1.ejashkojme.utils

import android.app.Dialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.a60n1.ejashkojme.R

/**
 * Utility class for access to runtime permissions.
 */
object PermissionUtils {
    /**
     * Checks if the result contains a [PackageManager.PERMISSION_GRANTED] result for a
     * permission from a runtime permissions request.
     *
     * @see androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
     */
    @JvmStatic
    fun isPermissionGranted(grantPermissions: Array<String>, grantResults: IntArray,
                            permission: String): Boolean {
        for (i in grantPermissions.indices)
            if (permission == grantPermissions[i])
                return grantResults[i] == PackageManager.PERMISSION_GRANTED
        return false
    }

    /**
     * A dialog that displays a permission denied message.
     */
    class PermissionDeniedDialog : DialogFragment() {
        private var mFinishActivity = false
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            mFinishActivity = arguments!!.getBoolean(ARGUMENT_FINISH_ACTIVITY)
            return AlertDialog.Builder(activity!!)
                    .setMessage(R.string.location_permission_denied)
                    .setPositiveButton(android.R.string.ok, null)
                    .create()
        }

        override fun onDismiss(dialog: DialogInterface) {
            super.onDismiss(dialog)
            if (mFinishActivity) {
                Toast.makeText(activity, R.string.permission_required_toast,
                        Toast.LENGTH_SHORT).show()
                activity!!.finish()
            }
        }

        companion object {
            private const val ARGUMENT_FINISH_ACTIVITY = "finish"
            /**
             * Creates a new instance of this dialog and optionally finishes the calling Activity
             * when the 'Ok' button is clicked.
             */
            @JvmStatic
            fun newInstance(finishActivity: Boolean): PermissionDeniedDialog {
                val arguments = Bundle()
                arguments.putBoolean(ARGUMENT_FINISH_ACTIVITY, finishActivity)
                val dialog = PermissionDeniedDialog()
                dialog.arguments = arguments
                return dialog
            }
        }
    }

}