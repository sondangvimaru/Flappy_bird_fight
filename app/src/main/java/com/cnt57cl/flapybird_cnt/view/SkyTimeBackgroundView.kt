
package com.cnt57cl.flapybird_cnt.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.os.Build
import android.os.Handler
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.annotation.IntRange
import androidx.annotation.IntegerRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import com.cnt57cl.flapybird_cnt.R
import com.cnt57cl.flapybird_cnt.view.particle.ParticlesDrawable
import com.cnt57cl.flapybird_cnt.view.utils.Point
import com.cnt57cl.flapybird_cnt.view.utils.SkyViewUtils
import java.util.*

class SkyTimeBackgroundView : RelativeLayout {
    private var mPainter: SkyViewGradientBackgroundPainter? = null

    enum class Time {
        AFTERNOON, EARLY_NIGHT, NIGHT, CUSTOM
    }

    enum class Planet {
        SUN, RED_SUN, MOON
    }

    private var mSkyTime =
        Time.AFTERNOON
    private var isAutoStart = false
    private var isPlanetVisible = false
    private var planetPosition = 0
    private var mPlanetSpeed = 0
    private var isPlanetAnimationOn = false
    private var mPlanetType: String? = null
    private val mDrawables = IntArray(3)
    //private int[] mCustomDrawables = new int[3];
    private var mAnimatorSet: AnimatorSet? = AnimatorSet()
    private var mPlanetView: AppCompatImageView? = null
    private var mStarView: AppCompatImageView? = null
    private var mStarDrawable: ParticlesDrawable? = null
    private var isChangeWait = false
    private var isStarVisible = false
    private var isStarLineVisible = false
    private var isStarLineVisibleAuto = false
    private val mHandler = Handler()
    private val mPathList: ArrayList<Point?> = ArrayList()
    var mAnimationIndex = 0

    constructor(context: Context?) : super(context) {}
    constructor(
        context: Context?,
        attrs: AttributeSet
    ) : super(context, attrs) {
        initBackground(attrs, 0)
        startAnimation()
    }

    constructor(
        context: Context?,
        attrs: AttributeSet,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        initBackground(attrs, defStyleAttr)
        startAnimation()
    }

    private fun setTime(t: Time) {
        mSkyTime = t
        if (mSkyTime == Time.AFTERNOON) {
            mDrawables[0] = R.drawable.morning
            mDrawables[1] = R.drawable.morning_before
            mDrawables[2] = R.drawable.morning_after
        } else if (mSkyTime == Time.EARLY_NIGHT) {
            mDrawables[0] = R.drawable.evening
            mDrawables[1] = R.drawable.evening_before
            mDrawables[2] = R.drawable.evening_after
        } else if (mSkyTime == Time.NIGHT) {
            mDrawables[0] = R.drawable.early_evening
            mDrawables[1] = R.drawable.early_evening_before
            mDrawables[2] = R.drawable.early_evening_after
        } else if (mSkyTime == Time.CUSTOM) {
        }
    }

    fun setBackgroundGradient(@IntegerRes i: Int, @IntegerRes ii: Int, @IntegerRes iii: Int) {
        mSkyTime = Time.CUSTOM
        mDrawables[0] = i
        mDrawables[1] = ii
        mDrawables[2] = iii
    }

    fun changeTime(t: Time) {
        if (mSkyTime != t) {
            setTime(t)
            if (t == Time.AFTERNOON) {
                mPainter!!.startAnimationInInternalPosition(
                    mDrawables,
                    background,
                    ContextCompat.getDrawable(context, R.drawable.morning_after)
                )
            } else if (t == Time.EARLY_NIGHT) {
                mPainter!!.startAnimationInInternalPosition(
                    mDrawables,
                    background,
                    ContextCompat.getDrawable(context, R.drawable.evening)
                )
            } else if (t == Time.NIGHT) {
                mPainter!!.startAnimationInInternalPosition(
                    mDrawables,
                    background,
                    ContextCompat.getDrawable(context, R.drawable.early_evening)
                )
            } else if (t == Time.CUSTOM) {
                mPainter!!.startAnimationInInternalPosition(
                    mDrawables,
                    background,
                    ContextCompat.getDrawable(context, mDrawables[0])
                )
            }
            isChangeWait = true
            if (!isPlanetAnimationOn) {
                changePlanet()
            }
        }
    }

    private fun initBackground(attrs: AttributeSet, defStyle: Int) {
        mPlanetView = AppCompatImageView(context)
        mStarView = AppCompatImageView(context)
        val mParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        mStarView!!.layoutParams = mParams
        addView(mStarView)
        mStarView!!.alpha = 0f
        mStarDrawable = ParticlesDrawable()
        mStarDrawable!!.setStepMultiplier(0.5f)
        //mStarDrawable.setFrameDelay(2000);
        mStarDrawable!!.setLineVisibility(false)
        if (Build.VERSION.SDK_INT >= 16) {
            mStarView!!.background = mStarDrawable
        } else {
            mStarView!!.setBackgroundDrawable(mStarDrawable)
        }
        mStarView!!.setImageResource(R.drawable.alpha_gradient)
        val mArray: TypedArray
        mArray = if (defStyle == 0) {
            context.obtainStyledAttributes(attrs, R.styleable.SkyTimeBackgroundView)
        } else {
            context.obtainStyledAttributes(
                attrs,
                R.styleable.SkyTimeBackgroundView,
                defStyle,
                0
            )
        }
        isAutoStart = mArray.getBoolean(R.styleable.SkyTimeBackgroundView_autoStart, false)
        isPlanetAnimationOn =
            mArray.getBoolean(R.styleable.SkyTimeBackgroundView_planetAnimation, false)
        isPlanetVisible = mArray.getBoolean(R.styleable.SkyTimeBackgroundView_planetVisible, true)
        isStarVisible = mArray.getBoolean(R.styleable.SkyTimeBackgroundView_starVisible, true)
        if (mArray.getString(R.styleable.SkyTimeBackgroundView_starLineVisible) != null) {
            if (mArray.getString(R.styleable.SkyTimeBackgroundView_starLineVisible) == "auto") {
                isStarLineVisible =
                    mArray.getBoolean(R.styleable.SkyTimeBackgroundView_starLineVisible, true)
                isStarLineVisibleAuto = true
            }
        } else {
            isStarLineVisible =
                mArray.getBoolean(R.styleable.SkyTimeBackgroundView_starLineVisible, true)
            isStarLineVisibleAuto = false
        }
        val s = mArray.getString(R.styleable.SkyTimeBackgroundView_planetType)
        if (s != null) {
            when (s) {
                SUN -> mPlanetType =
                    SUN
                RED_SUN -> mPlanetType =
                    RED_SUN
                MOON -> mPlanetType =
                    MOON
            }
        } else {
            mPlanetType = SUN
        }
        planetPosition = mArray.getInt(R.styleable.SkyTimeBackgroundView_planetPosition, 0)
        mPlanetSpeed = mArray.getInt(R.styleable.SkyTimeBackgroundView_planetSpeed, 300)
        mAnimationIndex = planetPosition
        mArray.recycle()
        if (mPlanetType == SUN) {
            setTime(Time.AFTERNOON)
        } else if (mPlanetType == RED_SUN) {
            setTime(Time.EARLY_NIGHT)
        } else if (mPlanetType == MOON) {
            setTime(Time.NIGHT)
        }
        mPainter = SkyViewGradientBackgroundPainter(this, mDrawables)
    }

    private fun calculateParabolaPath() {
        val mWidthSplit =
            SkyViewUtils.getScreenWidth(context) / 120.toDouble()
        for (i in 0..120) {
            var mX: Double
            mX = if (i == 0) {
                0.0
            } else {
                mWidthSplit * i
            }
            val mRadius = width / 2 + 200
            val mArea = mRadius - (width / 2 + 200).toDouble()
            var mY = Math.pow(mRadius.toDouble(), 2.0)
            mY = mY - Math.pow(width / 2 - mX + mArea, 2.0)
            mY =
                Math.abs(Math.sqrt(Math.abs(mY)) - height / 6 * 5)
            val mPoint =
                Point()
            mPoint.x = mX.toFloat()
            mPoint.y = mY.toFloat()
            mPathList.add(mPoint)
            //Log.e("TEST", mX + " / " + mY);
        }
        Collections.reverse(mPathList)
    }

    var isT = false
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        /*Paint mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.RED);
        mPaint.setStrokeWidth(1);

        double mWidthSplit = SkyViewUtils.getScreenWidth(getContext()) / (double)100;

        if(!isT) {
            for(int i = 0; i < 100; i++) {
                double mX = mWidthSplit * (i + 1);
                int mRadius = getWidth() / 2 + 200;

                double mArea = mRadius - (getWidth() / 2 + 200);

                double mY = Math.pow(mRadius, 2);
                mY = mY - Math.pow(((getWidth() / 2 - mX) + mArea), 2);
                mY = Math.abs(Math.sqrt(Math.abs(mY)) - getHeight() / 6 * 5);

                canvas.drawCircle((float)mX, (float)mY, 1, mPaint);

                Log.e("TEST", mX + " / " + mY);
            }

            isT = true;
        }*/if (!isT) {
            calculateParabolaPath()
            planetAnimationStart()
            isT = true
        }
    }

    private fun startAnimation() {
        if (isAutoStart) {
            backgroundAnimationStart()
        }
    }

    private fun changePlanet() {
        if (mSkyTime == Time.AFTERNOON) {
            mPlanetView!!.setBackgroundResource(R.drawable.sunny)
            hideStar()
        } else if (mSkyTime == Time.EARLY_NIGHT) {
            mPlanetView!!.setBackgroundResource(R.drawable.moon)
            if (isStarVisible) {
                if (isStarLineVisibleAuto || isStarLineVisible) { //AUTO LINE
                    mStarDrawable!!.setLineVisibility(false)
                }
                showStar()
            } else {
                hideStar()
            }
        } else if (mSkyTime == Time.NIGHT) {
            mPlanetView!!.setBackgroundResource(R.drawable.moon_c)
            if (isStarVisible) {
                if (isStarLineVisibleAuto) {
                    mStarDrawable!!.setLineVisibility(true)
                } else if (isStarLineVisible) {
                    mStarDrawable!!.setLineVisibility(true)
                } else {
                    mStarDrawable!!.setLineVisibility(false)
                }
                showStar()
            } else {
                hideStar()
            }
        } else if (mSkyTime == Time.CUSTOM) {
            if (mPlanetType == SUN) {
                mPlanetView!!.setImageResource(R.drawable.sunny)
            } else if (mPlanetType == RED_SUN) {
                mPlanetView!!.setImageResource(R.drawable.moon)
            } else if (mPlanetType == MOON) {
                mPlanetView!!.setImageResource(R.drawable.moon_c)
            }
        }
    }

    private fun planetAnimationStart() {
        if (isPlanetVisible) {
            if (mAnimatorSet!!.isRunning) {
                mAnimatorSet!!.cancel()
                mAnimatorSet = null
            }
            try {
                removeView(mPlanetView)
            } catch (e: Exception) {
            }
            mPlanetView = null
            mPlanetView = AppCompatImageView(context)
            val mParams = LinearLayout.LayoutParams(200, 200)
            mPlanetView!!.layoutParams = mParams
            mPlanetView!!.x = mPathList[mAnimationIndex]!!.x
            mPlanetView!!.y = mPathList[mAnimationIndex]!!.y
            changePlanet()
            addView(mPlanetView)
            if (isPlanetAnimationOn) {
                mAnimatorSet = AnimatorSet()
                val mXAnimator = ObjectAnimator.ofFloat(
                    mPlanetView,
                    View.TRANSLATION_X,
                    mPlanetView!!.x,
                    mPathList[mAnimationIndex]!!.x
                )
                val mYAnimator = ObjectAnimator.ofFloat(
                    mPlanetView,
                    View.TRANSLATION_Y,
                    mPlanetView!!.y,
                    mPathList[mAnimationIndex]!!.y
                )
                //mAnimatorSet.setInterpolator(null);
                mAnimatorSet!!.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        if (mAnimationIndex >= 500) {
                            mAnimationIndex = 0
                            mHandler.post {
                                isChangeWait = false
                                planetAnimationStart()
                            }
                            return
                        }
                        if (mAnimationIndex >= mPathList.size - 1) {
                            if (mSkyTime == Time.AFTERNOON) {
                                changeTime(Time.EARLY_NIGHT)
                            } else if (mSkyTime == Time.EARLY_NIGHT) {
                                changeTime(Time.NIGHT)
                            } else if (mSkyTime == Time.NIGHT) {
                                changeTime(Time.AFTERNOON)
                            }
                            val mXAnimator = ObjectAnimator.ofFloat(
                                mPlanetView,
                                View.TRANSLATION_X,
                                mPlanetView!!.x,
                                mPathList[120]!!.x - 200
                            )
                            val mYAnimator = ObjectAnimator.ofFloat(
                                mPlanetView,
                                View.TRANSLATION_Y,
                                mPlanetView!!.y,
                                mPathList[120]!!.y + 200
                            )
                            mAnimatorSet!!.playTogether(mXAnimator, mYAnimator)
                            mAnimatorSet!!.duration = 2000
                            mHandler.post { mAnimatorSet!!.start() }
                            mAnimationIndex = 500
                        } else {
                            val mXAnimator: ObjectAnimator
                            val mYAnimator: ObjectAnimator
                            if (isChangeWait) {
                                mXAnimator = ObjectAnimator.ofFloat(
                                    mPlanetView,
                                    View.TRANSLATION_X,
                                    mPlanetView!!.x,
                                    mPathList[mAnimationIndex]!!.x
                                )
                                mYAnimator = ObjectAnimator.ofFloat(
                                    mPlanetView,
                                    View.TRANSLATION_Y,
                                    mPlanetView!!.y,
                                    height + 300.toFloat()
                                )
                                mAnimatorSet!!.duration = 1000
                                mAnimationIndex = 500
                            } else {
                                mXAnimator = ObjectAnimator.ofFloat(
                                    mPlanetView,
                                    View.TRANSLATION_X,
                                    mPlanetView!!.x,
                                    mPathList[mAnimationIndex]!!.x
                                )
                                mYAnimator = ObjectAnimator.ofFloat(
                                    mPlanetView,
                                    View.TRANSLATION_Y,
                                    mPlanetView!!.y,
                                    mPathList[mAnimationIndex]!!.y
                                )
                            }
                            mAnimatorSet!!.playTogether(mXAnimator, mYAnimator)
                            mAnimationIndex++
                            mHandler.post { mAnimatorSet!!.start() }
                        }
                    }

                    override fun onAnimationRepeat(animation: Animator) {}
                    override fun onAnimationStart(animation: Animator) {}
                })
                mAnimatorSet!!.playTogether(mXAnimator, mYAnimator)
                mAnimatorSet!!.duration = mPlanetSpeed.toLong()
                mAnimatorSet!!.start()
            }
        }
    }

    fun setStarVisibility(b: Boolean) {
        isStarVisible = b
    }

    fun showStar() { //isStarVisible = true;
        if (!mStarDrawable!!.isRunning) {
            mHandler.post {
                val mAnimator =
                    ObjectAnimator.ofFloat(mStarView!!,"alpha", 0f, 1f)
                mAnimator.duration = 2000
                mAnimator.start()
                mStarDrawable!!.start()
            }
        }
    }

    fun hideStar() { //isStarVisible = false;
        if (mStarDrawable!!.isRunning) {
            if (mStarView!!.alpha == 1f) {
                mHandler.post {
                    val mAnimator =
                        ObjectAnimator.ofFloat(mStarView!!, "alpha", 1f, 0f)
                    mAnimator.duration = 2000
                    mAnimator.addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            mStarDrawable!!.stop()
                        }
                    })
                    mAnimator.start()
                }
            }
        }
    }

    fun setPlanetVisiblility(b: Boolean) {
        isPlanetVisible = b
    }

    fun setPlanetPosition(@IntRange(from = 0, to = 120) i: Int) {
        planetPosition = i
        mAnimationIndex = if (i > 0 && i < 121) {
            i
        } else {
            throw IllegalArgumentException("Position must be range 0 to 120")
        }
    }

    fun setPlanetSpeed(millisec: Int) {
        mPlanetSpeed = millisec
    }

    fun usePlanetAnimation(b: Boolean) {
        isPlanetAnimationOn = b
        if (isPlanetAnimationOn) {
            if (mAnimatorSet != null) {
                if (!mAnimatorSet!!.isRunning) {
                    mAnimatorSet!!.start()
                }
            }
        } else {
            if (mAnimatorSet != null) {
                if (mAnimatorSet!!.isRunning) {
                    mAnimatorSet!!.cancel()
                }
            }
        }
    }

    fun setStarLineVisibility(b: Boolean) {
        if (isStarVisible) {
            mStarDrawable!!.setLineVisibility(b)
        }
    }

    fun setPlanet(p: Planet) {
        mPlanetType = if (p == Planet.SUN) {
            SUN
        } else if (p == Planet.RED_SUN) {
            RED_SUN
        } else {
            MOON
        }
        mHandler.post {
            if (mPlanetType == SUN) {
                mPlanetView!!.setImageResource(R.drawable.sunny)
            } else if (mPlanetType == RED_SUN) {
                mPlanetView!!.setImageResource(R.drawable.moon)
            } else if (mPlanetType == MOON) {
                mPlanetView!!.setImageResource(R.drawable.moon_c)
            }
        }
        setTime(Time.CUSTOM)
    }

    fun backgroundAnimationStart() {
        mPainter!!.start()
    }

    fun backgroundAnimationStop() {
        mPainter!!.stop()
    }

    companion object {
        const val SUN = "sun"
        const val RED_SUN = "redSun"
        const val MOON = "moon"
    }
}