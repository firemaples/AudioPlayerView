package com.firemaples.audioplayerview

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.View

class AudioPlayerView : View {
    private companion object {
        private const val progressButtonUpdateInterval = 1000L / 8L
        private val defaultProgressColor: Int = Color.parseColor("#4c5acff2")
        private val defaultButtonBgColor: Int = Color.parseColor("#5acff2")
        private val defaultButtonIconColor: Int = Color.parseColor("#ffffff")
        private val defaultTextColor: Int = Color.parseColor("#000000")
    }

    private var progressButtonFilterColor: Int = -1

    private var progressButtonDrawable: Drawable? = null

    private var buttonBgSize: Float = 22.toPx()
    private var buttonSize: Float = 10.toPx()
    private var buttonPaddingLeft: Float = 15.toPx()
    private var textPaddingLeft: Float = 9.toPx()
    private var textSize: Float = 16.toPx()

    private var progress: Float = 0.5f
    private var buttonState: ButtonState = ButtonState.Play
    private var displayText: String = ""

    private var drawingTopLeftRadius: Float = 0f
    private var drawingTopRightRadius: Float = 0f
    private var drawingBottomRightRadius: Float = 0f
    private var drawingBottomLeftRadius: Float = 0f

    private var progressPaint: Paint = Paint().apply {
        color = defaultProgressColor
    }
    private var buttonBgPaint: Paint = Paint().apply {
        color = defaultButtonBgColor
    }
    private var buttonIconPaint: Paint = Paint().apply {
        color = defaultButtonIconColor
    }
    private var textPaint: Paint = Paint().apply {
        color = defaultTextColor
        textSize = this@AudioPlayerView.textSize
        isAntiAlias = true
    }

    private var progressButtonDegree: Float = 0f
    private val mainHandler: Handler by lazy { Handler(Looper.getMainLooper()) }

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        if (context != null && attrs != null) initAttrs(context, attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        if (context != null && attrs != null) initAttrs(context, attrs)
    }

    private fun initAttrs(context: Context, attrs: AttributeSet) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.AudioPlayerView)

        progressPaint.color =
            a.getColor(R.styleable.AudioPlayerView_ap_progressColor, defaultProgressColor)
        buttonBgPaint.color =
            a.getColor(R.styleable.AudioPlayerView_ap_buttonBackgroundColor, defaultButtonBgColor)
        buttonIconPaint.color =
            a.getColor(R.styleable.AudioPlayerView_ap_buttonIconColor, defaultButtonIconColor)
        textPaint.color = a.getColor(R.styleable.AudioPlayerView_ap_textColor, defaultTextColor)


        progressButtonFilterColor =
            a.getColor(
                R.styleable.AudioPlayerView_ap_progressButtonFilterColor,
                progressButtonFilterColor
            )
        a.getDrawable(R.styleable.AudioPlayerView_ap_progressButtonDrawable)?.also {
            setProgressDrawable(it, progressButtonFilterColor)
        }

        drawingTopLeftRadius =
            a.getDimensionPixelSize(R.styleable.AudioPlayerView_ap_drawingTopLeftRadius, 0)
                .toFloat()
        drawingTopRightRadius =
            a.getDimensionPixelSize(R.styleable.AudioPlayerView_ap_drawingTopRightRadius, 0)
                .toFloat()
        drawingBottomLeftRadius =
            a.getDimensionPixelSize(R.styleable.AudioPlayerView_ap_drawingBottomLeftRadius, 0)
                .toFloat()
        drawingBottomRightRadius =
            a.getDimensionPixelSize(R.styleable.AudioPlayerView_ap_drawingBottomRightRadius, 0)
                .toFloat()

        displayText = a.getString(R.styleable.AudioPlayerView_ap_displayText) ?: ""

        a.recycle()
    }

    fun setProgressDrawable(progressDrawable: Drawable, filterColor: Int = -1) {
        this.progressButtonDrawable = progressDrawable.apply {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                colorFilter = PorterDuffColorFilter(
                    filterColor,
                    PorterDuff.Mode.SRC_ATOP
                )
            } else {
                setColorFilter(filterColor, PorterDuff.Mode.SRC_ATOP)
            }
        }
    }

    fun updateProgress(progress: Float) {
        this.progress = progress.coerceAtMost(1f).coerceAtLeast(0f)
        postInvalidate()
    }

    fun getCurrentButtonState(): ButtonState = buttonState

    fun updateButtonState(buttonState: ButtonState) {
        this.buttonState = buttonState
        postInvalidate()
        if (buttonState == ButtonState.Progress) {
            startProgressAnimation()
        }
    }

    fun updateRadius(
        topLeftRadius: Int,
        topRightRadius: Int,
        bottomRightRadius: Int,
        bottomLeftRadius: Int
    ) {
        this.drawingTopLeftRadius = topLeftRadius.toPx()
        this.drawingTopRightRadius = topRightRadius.toPx()
        this.drawingBottomRightRadius = bottomRightRadius.toPx()
        this.drawingBottomLeftRadius = bottomLeftRadius.toPx()
        postInvalidate()
    }

    fun updateDisplayText(displayText: String) {
        this.displayText = displayText
        postInvalidate()
    }

    private fun startProgressAnimation() {
        mainHandler.postDelayed({
            if (getCurrentButtonState() != ButtonState.Progress) return@postDelayed

            progressButtonDegree += 360 / 8
            if (progressButtonDegree >= 360) progressButtonDegree %= 360

            postInvalidate()

            if (getCurrentButtonState() == ButtonState.Progress) {
                startProgressAnimation()
            }
        }, progressButtonUpdateInterval)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        clipRegion(canvas)

        drawProgressBar(canvas, progress)

        drawButton(canvas)

        drawText(canvas, displayText)
    }

    private fun clipRegion(canvas: Canvas) {
        val path = getPathOfRoundedRectF(
            rect = RectF(0f, 0f, width.toFloat(), height.toFloat()),
            topLeftRadius = drawingTopLeftRadius,
            topRightRadius = drawingTopRightRadius,
            bottomLeftRadius = drawingBottomLeftRadius,
            bottomRightRadius = drawingBottomRightRadius
        )
        canvas.clipPath(path)
    }

    private fun drawProgressBar(canvas: Canvas, progress: Float) {
        if (progress <= 0) return

        val rect = RectF(0f, 0f, width * progress, height.toFloat())

        val path = when {
            progress >= 1 -> {
                getPathOfRoundedRectF(
                    rect = rect
                )
            }
            else -> {
                getPathOfRoundedRectF(
                    rect = rect,
                    topRightRadius = height / 2f,
                    bottomRightRadius = height / 2f
                )
            }
        }

        canvas.drawPath(path, progressPaint)
    }

    private fun drawButton(canvas: Canvas) {
        //draw button background
        val bgPadding = (height - buttonBgSize) / 2
        val bgRect = RectF(
            buttonPaddingLeft,
            bgPadding,
            buttonPaddingLeft + buttonBgSize,
            height - bgPadding
        )
        canvas.drawOval(bgRect, buttonBgPaint)

        //draw button
        val btPadding = (bgRect.width() - buttonSize) / 2
        val btRect = RectF(
            bgRect.left + btPadding,
            bgRect.top + btPadding,
            bgRect.right - btPadding,
            bgRect.bottom - btPadding
        )


        when (buttonState) {
            ButtonState.Play -> {
                val delta = btRect.width() / 6f
                with(Path()) {
                    moveTo(btRect.left + delta, btRect.top)
                    lineTo(btRect.right, btRect.centerY())
                    lineTo(btRect.left + delta, btRect.bottom)
                    close()

                    canvas.drawPath(this, buttonIconPaint)
                }
            }
            ButtonState.Stop -> {
                val path = getPathOfRoundedRectF(
                    btRect,
                    topLeftRadius = 2.toPx(),
                    topRightRadius = 2.toPx(),
                    bottomRightRadius = 2.toPx(),
                    bottomLeftRadius = 2.toPx()
                )

                canvas.drawPath(path, buttonIconPaint)
            }
            ButtonState.Progress -> {
                val progressDrawable = progressButtonDrawable
                if (progressDrawable != null) {
                    progressDrawable.setBounds(
                        btRect.left.toInt(),
                        btRect.top.toInt(),
                        btRect.right.toInt(),
                        btRect.bottom.toInt()
                    )
                    canvas.save()
                    canvas.rotate(progressButtonDegree, btRect.centerX(), btRect.centerY())
                    progressDrawable.draw(canvas)
                    canvas.restore()
                }
            }
        }
    }

    private fun drawText(canvas: Canvas, text: String) {
        val bound = Rect()
        textPaint.getTextBounds(text, 0, text.length, bound)

        canvas.drawText(
            text,
            buttonPaddingLeft + buttonBgSize + textPaddingLeft,
            (height - bound.height()) / 2f + bound.height(),
            textPaint
        )
    }

    private fun getPathOfRoundedRectF(
        rect: RectF,
        topLeftRadius: Float = 0f,
        topRightRadius: Float = 0f,
        bottomRightRadius: Float = 0f,
        bottomLeftRadius: Float = 0f
    ): Path {
        val tlRadius = topLeftRadius.coerceAtLeast(0f)
        val trRadius = topRightRadius.coerceAtLeast(0f)
        val brRadius = bottomRightRadius.coerceAtLeast(0f)
        val blRadius = bottomLeftRadius.coerceAtLeast(0f)

        with(Path()) {
            moveTo(rect.left + tlRadius, rect.top)

            //setup top border
            lineTo(rect.right - trRadius, rect.top)

            //setup top-right corner
            arcTo(
                RectF(
                    rect.right - trRadius * 2f,
                    rect.top,
                    rect.right,
                    rect.top + trRadius * 2f
                ), -90f, 90f
            )

            //setup right border
            lineTo(rect.right, rect.bottom - trRadius)

            //setup bottom-right corner
            arcTo(
                RectF(
                    rect.right - brRadius * 2f,
                    rect.bottom - brRadius * 2f,
                    rect.right,
                    rect.bottom
                ), 0f, 90f
            )

            //setup bottom border
            lineTo(rect.left + blRadius, rect.bottom)

            //setup bottom-left corner
            arcTo(
                RectF(
                    rect.left,
                    rect.bottom - blRadius * 2f,
                    rect.left + blRadius * 2f,
                    rect.bottom
                ), 90f, 90f
            )

            //setup left border
            lineTo(rect.left, rect.top + tlRadius)

            //setup top-left corner
            arcTo(
                RectF(
                    rect.left,
                    rect.top,
                    rect.left + tlRadius * 2f,
                    rect.top + tlRadius * 2f
                ),
                180f,
                90f
            )

            close()

            return this
        }
    }

    private fun Int.toPx(): Float = this * Resources.getSystem().displayMetrics.density

    enum class ButtonState {
        Play, Stop, Progress
    }
}