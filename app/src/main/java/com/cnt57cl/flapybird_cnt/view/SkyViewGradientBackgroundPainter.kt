
package com.cnt57cl.flapybird_cnt.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.TransitionDrawable
import android.os.Build
import android.os.Handler
import android.view.View
import androidx.core.content.ContextCompat
import java.util.*

class SkyViewGradientBackgroundPainter(
    private val target: View,
    private var drawables: IntArray
) {
    private val random: Random
    private val handler: Handler
    private val context: Context
    private var mFirst: Drawable? = null
    private var mSecond: Drawable? = null
    private var isChanged = false
    private fun animate(firstDrawable: Int, secondDrawable: Int, duration: Int) {
        var secondDrawable = secondDrawable
        if (secondDrawable >= drawables.size) {
            secondDrawable = 0
        }
        setDrawables(drawables, firstDrawable, secondDrawable)
        mFirst = ContextCompat.getDrawable(context, drawables[firstDrawable])
        mSecond = ContextCompat.getDrawable(context, drawables[secondDrawable])
        val mTransitionDrawable =
            TransitionDrawable(arrayOf(mFirst, mSecond))
        if (Build.VERSION.SDK_INT >= 17) {
            target.background = mTransitionDrawable
        } else {
            target.setBackgroundDrawable(mTransitionDrawable)
        }
        mTransitionDrawable.isCrossFadeEnabled = false
        mTransitionDrawable.startTransition(duration)
        val mLocalSecondDrawable = secondDrawable
        handler.postDelayed({
            animate(
                mLocalSecondDrawable,
                mLocalSecondDrawable + 1,
                randomInt(
                    MIN,
                    MAX
                )
            )
        }, duration.toLong())
    }

    private fun animate(d1: Drawable?, d2: Drawable?, duration: Int) {
        mFirst = d1
        mSecond = d2
        val mTransitionDrawable =
            TransitionDrawable(arrayOf(mFirst, mSecond))
        if (Build.VERSION.SDK_INT >= 17) {
            target.background = mTransitionDrawable
        } else {
            target.setBackgroundDrawable(mTransitionDrawable)
        }
        mTransitionDrawable.isCrossFadeEnabled = false
        mTransitionDrawable.startTransition(duration)
        if (isChanged) {
            isChanged = false
            animate(mFirst, mSecond, 2000)
            //animate(0, 1, randomInt(MIN, MAX));
            handler.postDelayed({
                animate(
                    0,
                    1,
                    randomInt(
                        MIN,
                        MAX
                    )
                )
            }, 2000)
        }
    }

    protected fun setDrawables(drawables: IntArray, i: Int, ii: Int) {
        this.drawables = drawables
        mFirst = ContextCompat.getDrawable(context, drawables[i])
        mSecond = ContextCompat.getDrawable(context, drawables[ii])
    }

    fun start() {
        val duration = randomInt(
            MIN,
            MAX
        )
        animate(0, 1, duration)
    }

    fun startAnimationInInternalPosition(
        drawables: IntArray,
        d1: Drawable?,
        d2: Drawable?
    ) {
        isChanged = true
        stop()
        setDrawables(drawables, 0, 1)
        animate(
            d1,
            d2,
            randomInt(
                MIN,
                MAX
            )
        )
    }

    fun stop() {
        handler.removeCallbacksAndMessages(null)
    }

    private fun randomInt(min: Int, max: Int): Int {
        return random.nextInt(max - min + 1) + min
    }

    companion object {
        private const val MIN = 2000
        private const val MAX = 4500
    }

    init {
        random = Random()
        handler = Handler()
        context = target.context.applicationContext
    }
}