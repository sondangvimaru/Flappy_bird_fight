
package com.cnt57cl.flapybird_cnt.view.utils

import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager

object SkyViewUtils {
    fun getScreenWidth(c: Context): Int {
        val mWindowManager =
            c.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val mDisplay = mWindowManager.defaultDisplay
        val mMetrics = DisplayMetrics()
        mDisplay.getMetrics(mMetrics)
        return mMetrics.widthPixels
    }

    fun getScreenHeight(c: Context): Int {
        val mWindowManager =
            c.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val mDisplay = mWindowManager.defaultDisplay
        val mMetrics = DisplayMetrics()
        mDisplay.getMetrics(mMetrics)
        return mMetrics.heightPixels
    }
}