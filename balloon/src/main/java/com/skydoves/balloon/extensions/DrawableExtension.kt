package com.skydoves.balloon.extensions

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.Px
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat

fun Drawable.tint(context: Context, @ColorRes tintColorRes: Int?): Drawable =
        tintColorRes?.let { tintWithColor(ContextCompat.getColor(context, it)) } ?: this

fun Drawable.tintWithColor(@ColorInt tintColor: Int): Drawable = apply {
    DrawableCompat.wrap(this).apply { DrawableCompat.setTintList(this, ColorStateList.valueOf(tintColor)) }
}

fun Drawable.resize(context: Context, @Px width: Int?, @Px height: Int?): Drawable =
        if (width != null && height != null) {
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            setBounds(0, 0, width, height)
            draw(canvas)
            BitmapDrawable(context.resources, bitmap)
        } else this