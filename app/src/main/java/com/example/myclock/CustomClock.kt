package com.example.myclock

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import java.time.LocalDateTime
import kotlin.math.cos
import kotlin.math.sin

class CustomClock @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr){
    companion object {
        private const val mainRadiusScale = 0.9f
        private const val borderScale  = 0.02f
        private const val pointRadiusScale = 0.85f
        private const val pointScale = 0.01f
        private const val numberScale = 0.75f

        private const val hourArrowScale = 0.4f
        private const val minutesArrowScale = 0.65f
        private const val secondsArrowScale = 0.8f

        private const val centerRadiusScale = 0.03f

        private const val hourArrowWidth = 0.03f
        private const val minuteArrowWidth = 0.015f
        private const val secondsArrowWidth = 0.01f

        private const val shadowLayerSize = 0.02f

    }
    private var distToCenter = 0f

    private val DEFAULT_PRIMARY_COLOR = ContextCompat.getColor(context, R.color.grey)
    private val DEFAULT_SECONDARY_COLOR = Color.WHITE
    private val DEFAULT_TERTIARY_COLOR = Color.GRAY

    private var primaryColor = DEFAULT_PRIMARY_COLOR
    private var secondaryColor = DEFAULT_SECONDARY_COLOR
    private var tertiaryColor = DEFAULT_TERTIARY_COLOR



    private val clockPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        color = primaryColor
    }

    private val arrowPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = tertiaryColor
    }
    private val pointsPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        color = tertiaryColor
    }
    init {
        setupAttributes(attrs)
    }
    private fun setupAttributes(attrs: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomClock)

        with(typedArray) {
            primaryColor = getColor(R.styleable.CustomClock_primaryColor, DEFAULT_PRIMARY_COLOR)
                .also {
                    clockPaint.color = it
                    arrowPaint.color = it
                }

            secondaryColor = getColor(R.styleable.CustomClock_secondaryColor, DEFAULT_SECONDARY_COLOR)
            tertiaryColor = getColor(R.styleable.CustomClock_tertiaryColor, DEFAULT_TERTIARY_COLOR)
        }

        typedArray.recycle()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        distToCenter = measuredHeight.coerceAtMost(measuredWidth) / 2f

        canvas.apply {
            setBackgroundColor(secondaryColor)
            drawClockShape()
            drawArrows()
            drawPoints()
            drawCenter()
            drawNumbers()
        }
        postInvalidateDelayed(1000)
    }

    private fun Canvas.drawClockShape() {
        clockPaint.style = Paint.Style.STROKE
        clockPaint.strokeWidth = distToCenter * borderScale
        clockPaint.setShadowLayer(
            shadowLayerSize * distToCenter,
            shadowLayerSize * distToCenter,
            shadowLayerSize * distToCenter,
            Color.BLACK)
        drawCircle(
            distToCenter,
            distToCenter,
            distToCenter * mainRadiusScale,
            clockPaint
        )
    }
    private fun Canvas.drawCenter() {
        clockPaint.style = Paint.Style.FILL
        drawCircle(
            distToCenter,
            distToCenter,
            distToCenter * centerRadiusScale,
            clockPaint
        )
    }
    private fun Canvas.drawPoints() {
        for(i in 0 ..59){
            val angle = Math.PI * i / 30 - Math.PI / 2
            if(i % 5 == 0) {
                pointsPaint.strokeWidth = pointScale * distToCenter + 5f
            } else {
                pointsPaint.strokeWidth = pointScale * distToCenter
            }
            drawPoint(
                distToCenter + cos(angle).toFloat() * distToCenter * pointRadiusScale,
                distToCenter + sin(angle).toFloat() * distToCenter * pointRadiusScale,
                pointsPaint)
        }
    }
    private fun Canvas.drawNumbers() {
        val rect = Rect()
        clockPaint.textSize = 0.15f * distToCenter
        clockPaint.setShadowLayer(0f, 0f, 0f, Color.BLACK)
        for (i in 1..12) {
            val title = i.toString()
            clockPaint.getTextBounds(title, 0, title.length, rect)
            val angle = Math.PI / 6 * (i - 3)
            val x = distToCenter - rect.width() / 2 + numberScale * cos(angle) * distToCenter
            val y = distToCenter + rect.height() / 2 + numberScale * sin(angle) * distToCenter

            drawText(title, x.toFloat(), y.toFloat(), clockPaint)
        }
    }
    private fun Canvas.drawArrow(value: Double, lengthScale: Float, strokeWidth: Float) {
        val angle = Math.PI * value / 30 - Math.PI / 2
        arrowPaint.strokeWidth = strokeWidth
        arrowPaint.setShadowLayer(
            shadowLayerSize * distToCenter,
            shadowLayerSize * distToCenter,
            shadowLayerSize * distToCenter,
            Color.BLACK)
        drawLine(
            distToCenter,
            distToCenter,
            distToCenter + cos(angle).toFloat() * distToCenter * lengthScale,
            distToCenter + sin(angle).toFloat() * distToCenter * lengthScale,
            arrowPaint
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun Canvas.drawArrows() = with(LocalDateTime.now()) {
        drawArrow(
            value = (hour % 12 + minute / 60f) * 5.0,
            lengthScale = hourArrowScale,
            strokeWidth = hourArrowWidth * distToCenter
        )

        drawArrow(
            value = minute.toDouble(),
            lengthScale = minutesArrowScale,
            strokeWidth = minuteArrowWidth * distToCenter
        )

        drawArrow(
            value = second.toDouble(),
            lengthScale = secondsArrowScale,
            strokeWidth = secondsArrowWidth * distToCenter
        )
    }
}