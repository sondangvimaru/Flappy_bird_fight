
package com.cnt57cl.flapybird_cnt.view.particle

import android.content.res.Resources
import android.content.res.Resources.Theme
import android.content.res.TypedArray
import android.graphics.*
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.os.SystemClock
import android.util.AttributeSet
import android.util.TypedValue
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import com.cnt57cl.flapybird_cnt.R
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.util.*

class ParticlesDrawable : Drawable(), Animatable, Runnable {
    private val mPoints: MutableList<ParticleDot> =
        ArrayList(DEFAULT_DOT_NUMBER)
    private val mRandom = Random()
    val paint =
        Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG)
    private var mPointsInited = false
    private var mMinDotRadius = DEFAULT_MIN_DOT_RADIUS
    private var mMaxDotRadius = DEFAULT_MAX_DOT_RADIUS
    private var mLineThickness = DEFAULT_LINE_THICKNESS
    private var mLineDistance = DEFAULT_LINE_DISTANCE
    private var mNumDots = DEFAULT_DOT_NUMBER
    @ColorInt
    private var mDotColor = DEFAULT_DOT_COLOR
    @ColorInt
    private var mLineColor = DEFAULT_LINE_COLOR
    private var mDelay = DEFAULT_DELAY
    private var mStepMultiplier = DEFAULT_STEP_MULTIPLIER
    private var mLastFrameTime: Long = 0
    private var mAnimating = false
    private var isLineVisible = false
    // The alpha value of this Drawable
    private var mAlpha = 255

    @Throws(XmlPullParserException::class, IOException::class)
    override fun inflate(
        r: Resources,
        parser: XmlPullParser,
        attrs: AttributeSet,
        theme: Theme?
    ) {
        super.inflate(r, parser, attrs, theme)
        val a = r.obtainAttributes(attrs, R.styleable.ParticlesDrawable)
        try {
            handleAttrs(a)
        } finally {
            a.recycle()
        }
    }

    fun handleAttrs(a: TypedArray) {
        val count = a.indexCount
        var minDotRadius = DEFAULT_MIN_DOT_RADIUS
        var maxDotRadius = DEFAULT_MAX_DOT_RADIUS
        for (i in 0 until count) {
            val attr = a.getIndex(i)
            if (attr == R.styleable.ParticlesDrawable_minDotRadius) {
                minDotRadius =
                    a.getDimension(attr, DEFAULT_MIN_DOT_RADIUS)
            } else if (attr == R.styleable.ParticlesDrawable_maxDotRadius) {
                maxDotRadius =
                    a.getDimension(attr, DEFAULT_MAX_DOT_RADIUS)
            } else if (attr == R.styleable.ParticlesDrawable_lineThickness) {
                setLineThickness(
                    a.getDimension(attr, DEFAULT_LINE_THICKNESS)
                )
            } else if (attr == R.styleable.ParticlesDrawable_lineDistance) {
                setLineDistance(
                    a.getDimension(
                        attr,
                        DEFAULT_LINE_DISTANCE
                    )
                )
            } else if (attr == R.styleable.ParticlesDrawable_numDots) {
                setNumDots(a.getInteger(attr, DEFAULT_DOT_NUMBER))
            } else if (attr == R.styleable.ParticlesDrawable_dotColor) {
                setDotColor(a.getColor(attr, DEFAULT_DOT_COLOR))
            } else if (attr == R.styleable.ParticlesDrawable_lineColor) {
                setLineColor(a.getColor(attr, DEFAULT_LINE_COLOR))
            } else if (attr == R.styleable.ParticlesDrawable_frameDelayMillis) {
                frameDelay = a.getInteger(attr, DEFAULT_DELAY)
            } else if (attr == R.styleable.ParticlesDrawable_stepMultiplier) {
                setStepMultiplier(
                    a.getFloat(
                        attr,
                        DEFAULT_STEP_MULTIPLIER
                    )
                )
            }
        }
        setDotRadiusRange(minDotRadius, maxDotRadius)
        isLineVisible = true
    }

    fun resetLastFrameTime() {
        mLastFrameTime = 0
    }

    override fun draw(canvas: Canvas) {
        if (mNumDots > 0) {
            val pointsSize = mPoints.size
            for (i in 0 until pointsSize) {
                val p1 = mPoints[i]
                // Draw connection lines for eligible points
                for (c in 0 until pointsSize) {
                    if (c != i) {
                        val p2 = mPoints[c]
                        val distance =
                            distance(p1.x, p1.y, p2.x, p2.y)
                        if (distance < mLineDistance) {
                            if (isLineVisible) {
                                drawLine(canvas, p1, p2, distance)
                            }
                        }
                    }
                }
            }
            // The dots are drawn above the lines
// As an optimization, we can exclude point radius when drawing a line and then move point
// drawing to the loop above
            for (i in 0 until pointsSize) {
                val p1 = mPoints[i]
                drawDot(canvas, p1)
            }
        }
    }

    private fun gotoNextFrameAndSchedule() {
        nextFrame()
        scheduleSelf(this, SystemClock.uptimeMillis() + mDelay)
    }

    override fun setAlpha(alpha: Int) {
        mAlpha = alpha
    }

    override fun getAlpha(): Int {
        return mAlpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    override fun start() {
        if (!mAnimating) {
            mAnimating = true
            gotoNextFrameAndSchedule()
        }
    }

    override fun stop() {
        if (mAnimating) {
            mAnimating = false
            unscheduleSelf(this)
        }
    }

    override fun isRunning(): Boolean {
        return mAnimating
    }

    override fun run() {
        if (mAnimating) {
            gotoNextFrameAndSchedule()
        } else {
            mLastFrameTime = 0
        }
    }

    private val width: Int
        private get() = bounds.width()

    private val height: Int
        private get() = bounds.height()

    /**
     * Resets and makes new random frame. This is useful for re-generating new fancy static
     * backgrounds when not using animations.
     */
    fun makeBrandNewFrame() {
        val numDots = mNumDots
        setNumDots(0)
        setNumDots(numDots)
        if (width != 0 && height != 0) {
            initPoints()
        }
    }

    fun setLineVisibility(b: Boolean) {
        isLineVisible = b
    }

    /**
     * Set a delay per frame in milliseconds.
     *
     * @param delay delay between frames
     * @throws IllegalArgumentException if delay is a negative number
     */
    var frameDelay: Int
        get() = mDelay
        set(delay) {
            require(delay >= 0) { "delay must not be nagative" }
            mDelay = delay
        }

    /**
     * Sets step multiplier. Use this to control speed.
     *
     * @param stepMultiplier step multiplier
     */
    fun setStepMultiplier(@FloatRange stepMultiplier: Float) {
        require(stepMultiplier >= 0) { "step multiplier must not be nagative" }
        mStepMultiplier = stepMultiplier
    }

    /**
     * Set dot radius range
     *
     * @param minRadius smallest dot radius
     * @param maxRadius largest dot radius
     */
    fun setDotRadiusRange(
        @FloatRange minRadius: Float,
        @FloatRange  maxRadius: Float
    ) {
        require(!(minRadius < 0.5f || maxRadius < 0.5f)) { "Dot radius must not be lass than 0.5" }
        require(minRadius <= maxRadius) {
            String.format(
                Locale.US,
                "Min radius must not be greater than max, but min = %f, max = %f",
                minRadius, maxRadius
            )
        }
        mMinDotRadius = minRadius
        mMaxDotRadius = maxRadius
    }

    /**
     * Set a line thickness
     *
     * @param lineThickness line thickness
     */
    fun setLineThickness(@FloatRange  lineThickness: Float) {
        mLineThickness = lineThickness
    }

    /**
     * Set the maximum distance when the connection line is still drawn between points
     *
     * @param lineDistance maximum distance for connection lines
     */
    fun setLineDistance(@FloatRange lineDistance: Float) {
        require(lineDistance >= 0) { "line distance must not be negative" }
        mLineDistance = lineDistance
    }

    /**
     * Set number of points to draw
     *
     * @param newNum the number of points
     * @throws IllegalArgumentException if number of points is negative
     */
    fun setNumDots(@IntRange(from = 0) newNum: Int) {
        require(newNum >= 0) { "numPoints must not be negative" }
        if (newNum != mNumDots) {
            if (mPointsInited) {
                if (newNum > mNumDots) {
                    for (i in mNumDots until newNum) {
                        mPoints.add(makeNewPoint(false))
                    }
                } else {
                    for (i in 0 until mNumDots - newNum) {
                        mPoints.removeAt(0)
                    }
                }
            }
            mNumDots = newNum
        }
    }

    /**
     * Set the dot color
     *
     * @param dotColor dot color to use
     */
    fun setDotColor(@ColorInt dotColor: Int) {
        mDotColor = dotColor
    }

    /**
     * Set the line color. Note that the color alpha is ignored and will be calculated depending on
     * distance between points
     *
     * @param lineColor line color to use
     */
    fun setLineColor(@ColorInt lineColor: Int) {
        mLineColor = lineColor
    }

    override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        super.setBounds(left, top, right, bottom)
        if (right - left > 0 && bottom - top > 0) {
            if (!mPointsInited) {
                mPointsInited = true
                initPoints()
            }
        }
    }

    private fun initPoints() {
        check(!(width == 0 || height == 0)) { "Cannot init points if width or height is 0" }
        mPoints.clear()
        for (i in 0 until mNumDots) {
            mPoints.add(makeNewPoint(i % 2 == 0))
        }
    }

    private fun makeNewPoint(onScreen: Boolean): ParticleDot {
        check(!(width == 0 || height == 0)) { "Cannot make new point if width or height is 0" }
        val point = ParticleDot()
        if (onScreen) {
            applyFreshPointOnScreen(point)
        } else {
            applyFreshPointOffScreen(point)
        }
        return point
    }

    /**
     * Calculates values for the next frame
     */
    private fun nextFrame() {
        val step =
            if (mLastFrameTime == 0L) 1f else (SystemClock.uptimeMillis() - mLastFrameTime) * STEP_PER_MS
        val pointsSize = mPoints.size
        for (i in 0 until pointsSize) {
            val p = mPoints[i]
            p.x += step * mStepMultiplier * p.stepMultiplier * p.dCos
            p.y += step * mStepMultiplier * p.stepMultiplier * p.dSin
            if (pointOutOfBounds(p.x, p.y)) {
                applyFreshPointOffScreen(p)
            }
        }
        mLastFrameTime = SystemClock.uptimeMillis()
        invalidateSelf()
    }

    /**
     * Set new point coordinates somewhere on screen and apply new direction
     *
     * @param p [ParticleDot] to apply new values to
     */
    private fun applyFreshPointOnScreen(p: ParticleDot) {
        val w = width
        val h = height
        check(!(w == 0 || h == 0)) { "Cannot apply points if width or height is 0" }
        val direction = Math.toRadians(mRandom.nextInt(360).toDouble())
        p.dCos = Math.cos(direction).toFloat()
        p.dSin = Math.sin(direction).toFloat()
        p.x = mRandom.nextInt(w).toFloat()
        p.y = mRandom.nextInt(h).toFloat()
        p.stepMultiplier = newRandomIndividualDotStepMultiplier()
        p.radius = newRandomIndividualDotRadius()
    }

    /**
     * Generates new step multiplier for individual dot.
     * The value is in [0.5:1.5] range
     *
     * @return new step multiplier for individual dot
     */
    private fun newRandomIndividualDotStepMultiplier(): Float {
        return 1f + 0.1f * (mRandom.nextInt(11) - 5)
    }

    /**
     * Generates new individual dot radius based on min and max radius setting
     *
     * @return new dot radius
     */
    private fun newRandomIndividualDotRadius(): Float {
        return if (mMinDotRadius == mMaxDotRadius) mMinDotRadius else mMinDotRadius
                + mRandom.nextInt((mMaxDotRadius - mMinDotRadius).toInt() * 100) / 100f
    }

    /**
     * Set new point coordinates somewhere off screen and apply new direction towards the screen
     *
     * @param p [ParticleDot] to apply new values to
     */
    private fun applyFreshPointOffScreen(p: ParticleDot) {
        val w = width
        val h = height
        check(!(w == 0 || h == 0)) { "Cannot apply points if width or height is 0" }
        p.x = mRandom.nextInt(w).toFloat()
        p.y = mRandom.nextInt(h).toFloat()
        // The offset to make when creating point of out bounds
        val offset = mMinDotRadius + mLineDistance
        // Point angle range
        val startAngle: Float
        var endAngle: Float
        when (mRandom.nextInt(4)) {
            0 -> {
                // offset to left
                p.x = -offset
                startAngle = angleDeg(
                    PCC,
                    PCC,
                    p.x,
                    p.y
                )
                endAngle = angleDeg(
                    PCC,
                    h - PCC,
                    p.x,
                    p.y
                )
            }
            1 -> {
                // offset to top
                p.y = -offset
                startAngle = angleDeg(
                    w - PCC,
                    PCC,
                    p.x,
                    p.y
                )
                endAngle = angleDeg(
                    PCC,
                    PCC,
                    p.x,
                    p.y
                )
            }
            2 -> {
                // offset to right
                p.x = w + offset
                startAngle = angleDeg(
                    w - PCC,
                    h - PCC,
                    p.x,
                    p.y
                )
                endAngle = angleDeg(
                    w - PCC,
                    PCC,
                    p.x,
                    p.y
                )
            }
            3 -> {
                // offset to bottom
                p.y = h + offset
                startAngle = angleDeg(
                    PCC,
                    h - PCC,
                    p.x,
                    p.y
                )
                endAngle = angleDeg(
                    w - PCC,
                    h - PCC,
                    p.x,
                    p.y
                )
            }
            else -> throw IllegalArgumentException("Supplied value out of range")
        }
        if (endAngle < startAngle) {
            endAngle += 360f
        }
        // Get random angle from angle range
        val randomAngleInRange = startAngle + mRandom
            .nextInt(Math.abs(endAngle - startAngle).toInt())
        val direction = Math.toRadians(randomAngleInRange.toDouble())
        p.dCos = Math.cos(direction).toFloat()
        p.dSin = Math.sin(direction).toFloat()
        p.stepMultiplier = newRandomIndividualDotStepMultiplier()
        p.radius = newRandomIndividualDotRadius()
    }

    /**
     * Used for checking if the point is off-screen and farther than line distance
     *
     * @param x the point x
     * @param y the point y
     * @return true if the point is off-screen and guaranteed not to be used to draw a line to the
     * closest point on-screen
     */
    private fun pointOutOfBounds(x: Float, y: Float): Boolean {
        val offset = mMinDotRadius + mLineDistance
        return x + offset < 0 || x - offset > width || y + offset < 0 || y - offset > height
    }

    /**
     * Draw a point on the [Canvas]
     *
     * @param canvas the [Canvas] to draw on
     * @param p      the [ParticleDot] to draw
     */
    private fun drawDot(
        canvas: Canvas,
        p: ParticleDot
    ) {
        val alpha = Color.alpha(mDotColor) * mAlpha / 255
        paint.color = mDotColor and 0x00FFFFFF or (alpha shl 24)
        canvas.drawCircle(p.x, p.y, p.radius, paint)
    }

    /**
     * Draw a line between two [ParticleDot]s on [Canvas]
     *
     * @param canvas   the [Canvas] to draw on
     * @param p1       the neighbour [ParticleDot]
     * @param p2       the neighbour [ParticleDot]
     * @param distance the distance between p1 and p2
     */
    private fun drawLine(
        canvas: Canvas,
        p1: ParticleDot,
        p2: ParticleDot,
        distance: Float
    ) {
        val alphaPercent = 1f - distance / mLineDistance
        var alpha = (255f * alphaPercent).toInt()
        alpha = alpha * mAlpha / 255
        // Set line color alpha
        paint.strokeWidth = mLineThickness
        paint.color = mLineColor and 0x00FFFFFF or alpha shl 24
        // TODO exclude radius for better performance?
        canvas.drawLine(p1.x, p1.y, p2.x, p2.y, paint)
    }

    /**
     * Represents a dot by holding x and y coordinates, travel direction and step multiplier
     */
    private class ParticleDot {
        /**
         * Direction cosine
         */
        var dCos = 0f
        /**
         * Direction sine
         */
        var dSin = 0f
        /**
         * Current X
         */
        var x = 0f
        /**
         * Current Y
         */
        var y = 0f
        /**
         * Step multiplier for this dot
         */
        var stepMultiplier = 0f
        /**
         * Radius multiplier for this dot
         */
        var radius = 0f
    }

    companion object {
        /**
         * Path calculation padding.
         *
         * @see .applyFreshPointOffScreen
         */
        private val PCC = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            18f,
            Resources.getSystem().displayMetrics
        )
        private const val STEP_PER_MS = 0.05f
        private const val DEFAULT_DOT_NUMBER = 60
        private val DEFAULT_MAX_DOT_RADIUS = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            3f,
            Resources.getSystem().displayMetrics
        )
        private val DEFAULT_MIN_DOT_RADIUS = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            1f,
            Resources.getSystem().displayMetrics
        )
        private val DEFAULT_LINE_THICKNESS = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            1f,
            Resources.getSystem().displayMetrics
        )
        @ColorInt
        private val DEFAULT_DOT_COLOR = Color.WHITE
        @ColorInt
        private val DEFAULT_LINE_COLOR = Color.WHITE
        private val DEFAULT_LINE_DISTANCE = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            86f,
            Resources.getSystem().displayMetrics
        )
        private const val DEFAULT_STEP_MULTIPLIER = 1f
        private const val DEFAULT_DELAY = 10
        /**
         * Calculates the distance between two points
         *
         * @return distance between two points
         */
        private fun distance(
            ax: Float, ay: Float,
            bx: Float, by: Float
        ): Float {
            return Math.sqrt(
                (ax - bx) * (ax - bx) +
                        (ay - by) * (ay - by)
                    .toDouble()
            ).toFloat()
        }

        /**
         * Returns angle in degrees between two points
         *
         * @param ax x of the point 1
         * @param ay y of the point 1
         * @param bx x of the point 2
         * @param by y of the point 2
         * @return angle in degrees between two points
         */
        private fun angleDeg(
            ax: Float, ay: Float,
            bx: Float, by: Float
        ): Float {
            val angleRad =
                Math.atan2(ay - by.toDouble(), ax - bx.toDouble())
            var angle = Math.toDegrees(angleRad)
            if (angleRad < 0) {
                angle += 360.0
            }
            return angle.toFloat()
        }
    }
}