package com.skydoves.balloon.custom

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.Px
import androidx.appcompat.widget.AppCompatTextView
import com.skydoves.balloon.R
import com.skydoves.balloon.applyDrawable

open class VectorTextView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null
) : AppCompatTextView(context, attrs) {

    var drawableTextView: DrawableSimpleTextView? = null
        set(value) {
            field = value?.also { applyDrawable(it) }
        }

    init {
        initAttrs(context, attrs)
    }

    private fun initAttrs(context: Context, attrs: AttributeSet?) {
        if (attrs != null) {
            val attributeArray = context.obtainStyledAttributes(attrs, R.styleable.VectorTextView)
            drawableTextView = DrawableSimpleTextView(
                    drawableLeftRes = attributeArray.getResourceId(R.styleable.VectorTextView_drawableLeft, 0).takeIf { it != 0 },
                    drawableRightRes = attributeArray.getResourceId(R.styleable.VectorTextView_drawableRight, 0).takeIf { it != 0 },
                    drawableBottomRes = attributeArray.getResourceId(R.styleable.VectorTextView_drawableBottom, 0).takeIf { it != 0 },
                    drawableTopRes = attributeArray.getResourceId(R.styleable.VectorTextView_drawableTop, 0).takeIf { it != 0 },
                    compoundDrawablePaddingRes = attributeArray.getResourceId(R.styleable.VectorTextView_drawablePadding, 0).takeIf { it != 0 },
                    tintColorRes = attributeArray.getResourceId(R.styleable.VectorTextView_drawableTintColor, 0).takeIf { it != 0 },
                    widthRes = attributeArray.getResourceId(R.styleable.VectorTextView_drawableWidth, 0).takeIf { it != 0 },
                    heightRes = attributeArray.getResourceId(R.styleable.VectorTextView_drawableHeight, 0).takeIf { it != 0 },
                    squareSizeRes = attributeArray.getResourceId(R.styleable.VectorTextView_drawableSquareSize, 0).takeIf { it != 0 }
            )
            attributeArray.recycle()
        }
    }
}

data class DrawableSimpleTextView(
        var drawableLeftRes: Int? = null,
        var drawableRightRes: Int? = null,
        var drawableBottomRes: Int? = null,
        var drawableTopRes: Int? = null,
        var drawableLeft: Drawable? = null,
        var drawableRight: Drawable? = null,
        var drawableBottom: Drawable? = null,
        var drawableTop: Drawable? = null,
        @Px val compoundDrawablePadding: Int? = null,
        @Px val iconSize: Int? = null,
        @DimenRes var compoundDrawablePaddingRes: Int? = null,
        @ColorRes var tintColorRes: Int? = null,
        @DimenRes var widthRes: Int? = null,
        @DimenRes var heightRes: Int? = null,
        @DimenRes var squareSizeRes: Int? = null
)