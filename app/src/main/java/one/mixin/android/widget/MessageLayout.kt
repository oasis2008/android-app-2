package one.mixin.android.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import one.mixin.android.R
import one.mixin.android.extension.dpToPx
import org.jetbrains.anko.dip
import kotlin.math.max

class MessageLayout : ViewGroup {
    private val offset: Int
    private var lastLineWidth: Float = 0.toFloat()
    private var maxWidth: Int = 0

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        offset = context.dpToPx(8f)
        val ta = context.obtainStyledAttributes(attrs, R.styleable.MessageLayout, defStyleAttr, 0)
        ta?.let {
            maxWidth = ta.getDimensionPixelSize(R.styleable.MessageLayout_max_width, context.dip(300))
            ta.recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val childCount = childCount
        if (childCount < 2) {
            throw RuntimeException("CustomLayout child count must >= 2")
        }
        if (getChildAt(0) !is TextView) {
            throw RuntimeException("CustomLayout first child view not a TextView")
        }
        val paddingWidth = paddingStart + paddingEnd
        val paddingHeight = paddingTop + paddingBottom

        measureChildren(MeasureSpec.makeMeasureSpec(maxWidth - paddingWidth, MeasureSpec.AT_MOST),
            heightMeasureSpec)
        val firstView = getChildAt(0) as TextView
        val secondView = getChildAt(1)
        val third = getThird()
        initTextParams(firstView)

        var layoutHeight: Int
        var layoutWidth: Int

        if (lastLineWidth + offset + secondView.measuredWidth <= firstView.measuredWidth) {
            layoutWidth = firstView.measuredWidth
            layoutHeight = firstView.measuredHeight
        } else if (lastLineWidth == firstView.measuredWidth.toFloat() && lastLineWidth + offset + secondView.measuredWidth < maxWidth - paddingWidth) {
            layoutWidth = (lastLineWidth + offset + secondView.measuredWidth).toInt()
            layoutHeight = firstView.measuredHeight
        } else if (third != null && lastLineWidth + offset + secondView.measuredWidth <= third.measuredWidth) {
            layoutWidth = third.measuredWidth
            layoutHeight = firstView.measuredHeight
        } else {
            layoutWidth = firstView.measuredWidth
            layoutHeight = firstView.measuredHeight + secondView.measuredHeight
        }
        if (third != null) {
            val lp = third.layoutParams as MarginLayoutParams
            setMeasuredDimension(max(layoutWidth, third.measuredWidth) + paddingWidth,
                layoutHeight + third.measuredHeight + paddingHeight + lp.topMargin + lp.bottomMargin)
            if (third is ViewGroup && third.measuredWidth < measuredWidth) {
                third.measure(MeasureSpec.makeMeasureSpec(measuredWidth, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(third.measuredHeight, MeasureSpec.EXACTLY))
            }
        } else {
            setMeasuredDimension(layoutWidth + paddingWidth, layoutHeight + paddingHeight)
        }
    }

    private fun getThird(): View? {
        return if (childCount > 2) {
            val view = getChildAt(2)
            if (view.visibility == View.VISIBLE) {
                view
            } else {
                null
            }
        } else {
            null
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val firstView = getChildAt(0) as TextView
        val secondView = getChildAt(1)
        val thirdView = getThird()
        if (thirdView == null) {
            firstView.layout(paddingStart, paddingTop, firstView.measuredWidth + paddingStart,
                firstView.measuredHeight + paddingTop)
        } else {
            val lp = thirdView.layoutParams as MarginLayoutParams
            thirdView.layout(paddingStart, paddingTop + lp.topMargin, thirdView.measuredWidth + paddingStart,
                thirdView.measuredHeight + paddingTop + lp.bottomMargin)
            firstView.layout(paddingStart, paddingTop + thirdView.measuredHeight + lp.topMargin + lp.bottomMargin,
                firstView.measuredWidth + paddingStart,
                firstView.measuredHeight + paddingTop + thirdView.measuredHeight + lp.topMargin + lp.bottomMargin)
        }
        val top = measuredHeight - paddingBottom - secondView.measuredHeight
        val left = measuredWidth - paddingEnd - secondView.measuredWidth
        secondView.layout(left, top, left + secondView.measuredWidth, top + secondView.measuredHeight)
    }

    private fun initTextParams(textView: TextView) {
        val layout = textView.layout
        val lastLineIndex = textView.lineCount - 1
        lastLineWidth = layout.getLineRight(lastLineIndex) - layout.getLineLeft(lastLineIndex)
    }

    override fun generateDefaultLayoutParams(): MarginLayoutParams {
        return MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
    }

    override fun generateLayoutParams(p: LayoutParams): MarginLayoutParams {
        return MarginLayoutParams(p)
    }

    override fun generateLayoutParams(attrs: AttributeSet): MarginLayoutParams {
        return MarginLayoutParams(context, attrs)
    }
}