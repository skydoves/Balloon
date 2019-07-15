/*
 * Copyright (C) 2019 skydoves
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress("unused")

package com.skydoves.balloon

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.RelativeLayout
import androidx.annotation.FloatRange
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.android.synthetic.main.layout_balloon.view.*

@DslMarker
annotation class BalloonDsl

/** creates an instance of [Balloon] by [Balloon.Builder] using kotlin dsl. */
fun createBalloon(context: Context, block: Balloon.Builder.() -> Unit): Balloon =
  Balloon.Builder(context).apply(block).build()

/** Balloon implements showing and dismissing text popup with arrow and animations. */
@Suppress("MemberVisibilityCanBePrivate")
@SuppressLint("InflateParams")
class Balloon(
  private val context: Context,
  private val builder: Builder
) : LifecycleObserver {

  private val bodyView: View
  private val bodyWindow: PopupWindow
  var isShowing = false
    private set
  var onBalloonClickListener: OnBalloonClickListener? = null
  var onBalloonDismissListener: OnBalloonDismissListener? = null
  private val balloonPreferenceManager = BalloonPreferenceManager(context).getInstance()

  init {
    val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    this.bodyView = inflater.inflate(R.layout.layout_balloon, null)
    val width = getMeasureWidth()
    val params = RelativeLayout.LayoutParams(width, builder.height)
    this.bodyView.layoutParams = params
    this.bodyWindow = PopupWindow(bodyView, width, builder.height)
    createByBuilder()
  }

  private fun createByBuilder() {
    initializeArrow()
    initializeBackground()
    initializeBalloonListeners()

    if (builder.layout == -1) {
      initializeBalloonContent()
      initializeIcon()
      initializeText()
    } else {
      initializeCustomLayout()
    }
    builder.lifecycleOwner?.lifecycle?.addObserver(this@Balloon)
  }

  private fun initializeArrow() {
    with(bodyView.balloon_arrow) {
      builder.arrowDrawable?.let { setImageDrawable(it) }
      val params = RelativeLayout.LayoutParams(builder.arrowSize, builder.arrowSize)
      when (builder.arrowOrientation) {
        ArrowOrientation.BOTTOM -> {
          params.addRule(RelativeLayout.ALIGN_BOTTOM, bodyView.balloon_content.id)
          rotation = 180f
        }
        ArrowOrientation.TOP -> {
          params.addRule(RelativeLayout.ALIGN_TOP, bodyView.balloon_content.id)
          rotation = 0f
        }
        ArrowOrientation.LEFT -> {
          params.addRule(RelativeLayout.ALIGN_LEFT, bodyView.balloon_content.id)
          rotation = -90f
        }
        ArrowOrientation.RIGHT -> {
          params.addRule(RelativeLayout.ALIGN_RIGHT, bodyView.balloon_content.id)
          rotation = 90f
        }
      }
      when (builder.arrowOrientation) {
        ArrowOrientation.BOTTOM, ArrowOrientation.TOP ->
          x = bodyWindow.width * builder.arrowPosition - (builder.arrowSize / 2)
        ArrowOrientation.LEFT, ArrowOrientation.RIGHT ->
          y = bodyWindow.height * builder.arrowPosition - (builder.arrowSize / 2)
      }
      layoutParams = params
      alpha = builder.alpha
      visible(builder.arrowVisible)
      ImageViewCompat.setImageTintList(this, ColorStateList.valueOf(builder.backgroundColor))
    }
  }

  private fun initializeBackground() {
    with(bodyView.balloon_background) {
      alpha = builder.alpha
      if (builder.backgroundDrawable == null) {
        val drawable = ContextCompat.getDrawable(context, R.drawable.rectangle_layout) as GradientDrawable
        drawable.setColor(builder.backgroundColor)
        drawable.cornerRadius = builder.cornerRadius
        background = drawable
      } else {
        background = builder.backgroundDrawable
      }
      alpha = builder.alpha
    }
  }

  private fun initializeBalloonListeners() {
    this.onBalloonClickListener = builder.onBalloonClickListener
    this.onBalloonDismissListener = builder.onBalloonDismissListener
    this.bodyView.setOnClickListener { this.onBalloonClickListener?.onBalloonClick() }
    this.bodyWindow.setOnDismissListener { this.onBalloonDismissListener?.onBalloonDismiss() }
  }

  private fun initializeBalloonContent() {
    with(bodyView.balloon_content) {
      when (builder.arrowOrientation) {
        ArrowOrientation.BOTTOM, ArrowOrientation.TOP ->
          setPadding(builder.arrowSize, builder.arrowSize, builder.arrowSize, builder.arrowSize)
        ArrowOrientation.LEFT, ArrowOrientation.RIGHT ->
          setPadding(builder.arrowSize, paddingTop, paddingBottom, builder.arrowSize)
      }
    }
  }

  private fun initializeIcon() {
    if (builder.iconDrawable != null) {
      with(bodyView.balloon_icon) {
        visible(true)
        setImageDrawable(builder.iconDrawable)
        val params = LinearLayout.LayoutParams(builder.iconSize, builder.iconSize)
        params.setMargins(0, 0, builder.iconSpace, 0)
        layoutParams = params
        builder.iconForm?.let { applyIconForm(it) }
      }
    }
  }

  private fun initializeText() {
    with(bodyView.balloon_text) {
      text = builder.text
      textSize = builder.textSize
      setTextColor(builder.textColor)
      setTypeface(typeface, builder.textTypeface)
      builder.textForm?.let { applyTextForm(it) }
    }
  }

  private fun initializeCustomLayout() {
    bodyView.balloon_detail.removeAllViews()
    val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    inflater.inflate(builder.layout, bodyView.balloon_detail)
  }

  private fun applyBalloonAnimation() {
    when (builder.balloonAnimation) {
      BalloonAnimation.ELASTIC -> bodyWindow.animationStyle = R.style.Elastic
      BalloonAnimation.CIRCULAR -> {
        bodyWindow.contentView.circularRevealed()
        bodyWindow.animationStyle = R.style.NormalDispose
      }
      BalloonAnimation.FADE -> bodyWindow.animationStyle = R.style.Fade
      else -> bodyWindow.animationStyle = R.style.Normal
    }
  }

  private inline fun show(crossinline block: () -> Unit) {
    if (!isShowing) {
      builder.preferenceName?.let {
        if (balloonPreferenceManager.shouldShowUP(it, builder.showTimes)) {
          balloonPreferenceManager.putIncrementedTimes(it)
        } else return
      }

      applyBalloonAnimation()
      isShowing = true
      block()
    }
  }

  /** shows the balloon on the center of an anchor view. */
  fun show(anchor: View) {
    show {
      bodyWindow.showAsDropDown(anchor, -(anchor.measuredWidth / 2),
        -builder.height - (anchor.measuredHeight / 2))
    }
  }

  /** shows the balloon on an anchor view with x-off and y-off. */
  fun show(anchor: View, xOff: Int, yOff: Int) {
    show { bodyWindow.showAsDropDown(anchor, xOff, yOff) }
  }

  /** shows the balloon on an anchor view as drop down. */
  fun showAsDropDown(anchor: View) {
    show { bodyWindow.showAsDropDown(anchor) }
  }

  /** shows the balloon on an anchor view as drop down with x-off and y-off. */
  fun showAsDropDown(anchor: View, xOff: Int, yOff: Int) {
    show { bodyWindow.showAsDropDown(anchor, xOff, yOff) }
  }

  /** shows the balloon on an anchor view as the top alignment. */
  fun showAlignTop(anchor: View) {
    show {
      bodyWindow.showAsDropDown(anchor,
        (anchor.measuredWidth / 2) - (getMeasureWidth() / 2),
        -builder.height - anchor.measuredHeight)
    }
  }

  /** shows the balloon on an anchor view as the top alignment with x-off and y-off. */
  fun showAlignTop(anchor: View, xOff: Int, yOff: Int) {
    show {
      bodyWindow.showAsDropDown(anchor,
        (anchor.measuredWidth / 2) - (getMeasureWidth() / 2) + xOff,
        -builder.height - anchor.measuredHeight + yOff)
    }
  }

  /** shows the balloon on an anchor view as the bottom alignment. */
  fun showAlignBottom(anchor: View) {
    show {
      bodyWindow.showAsDropDown(anchor,
        (anchor.measuredWidth / 2) - (getMeasureWidth() / 2),
        0)
    }
  }

  /** shows the balloon on an anchor view as the bottom alignment with x-off and y-off. */
  fun showAlignBottom(anchor: View, xOff: Int, yOff: Int) {
    show {
      bodyWindow.showAsDropDown(anchor,
        (anchor.measuredWidth / 2) - (getMeasureWidth() / 2) + xOff,
        yOff)
    }
  }

  /** shows the balloon on an anchor view as the right alignment. */
  fun showAlignRight(anchor: View) {
    show {
      bodyWindow.showAsDropDown(anchor, anchor.measuredWidth,
        -(builder.height / 2) - (anchor.measuredHeight / 2))
    }
  }

  /** shows the balloon on an anchor view as the right alignment with x-off and y-off. */
  fun showAlignRight(anchor: View, xOff: Int, yOff: Int) {
    show {
      bodyWindow.showAsDropDown(anchor, anchor.measuredWidth + xOff,
        -(builder.height / 2) - (anchor.measuredHeight / 2) + yOff)
    }
  }

  /** shows the balloon on an anchor view as the left alignment. */
  fun showAlignLeft(anchor: View) {
    show {
      bodyWindow.showAsDropDown(anchor, -(getMeasureWidth()),
        -(builder.height / 2) - (anchor.measuredHeight / 2))
    }
  }

  /** shows the balloon on an anchor view as the left alignment with x-off and y-off. */
  fun showAlignLeft(anchor: View, xOff: Int, yOff: Int) {
    show {
      bodyWindow.showAsDropDown(anchor, -(getMeasureWidth()) + xOff,
        -(builder.height / 2) - (anchor.measuredHeight / 2) + yOff)
    }
  }

  /** dismiss the popup menu. */
  fun dismiss() {
    if (isShowing) {
      bodyWindow.dismiss()
      isShowing = false
    }
  }

  /** gets measured width size of the balloon. */
  fun getMeasureWidth(): Int {
    if (builder.widthRatio != 0f) {
      return (context.displaySize().x * builder.widthRatio - builder.space).toInt()
    }
    return builder.width - builder.space
  }

  /** gets a content view of the balloon popup window. */
  fun getContentView(): View {
    return bodyView.balloon_detail
  }

  /** Builder class for creating [Balloon]. */
  @BalloonDsl
  class Builder(private val context: Context) {
    @JvmField
    var width: Int = context.displaySize().x
    @JvmField
    @FloatRange(from = 0.0, to = 1.0)
    var widthRatio: Float = 0f
    @JvmField
    var height: Int = context.dp2Px(60)
    @JvmField
    var space: Int = 0
    @JvmField
    var arrowVisible: Boolean = true
    @JvmField
    var arrowSize: Int = context.dp2Px(15)
    @JvmField
    @FloatRange(from = 0.0, to = 1.0)
    var arrowPosition: Float = 0.5f
    @JvmField
    var arrowOrientation: ArrowOrientation = ArrowOrientation.BOTTOM
    @JvmField
    var arrowDrawable: Drawable? = null
    @JvmField
    var backgroundColor: Int = Color.BLACK
    @JvmField
    var backgroundDrawable: Drawable? = null
    @JvmField
    var cornerRadius: Float = context.dp2Px(5).toFloat()
    @JvmField
    var text: String = ""
    @JvmField
    var textColor: Int = Color.WHITE
    @JvmField
    var textSize: Float = 12f
    @JvmField
    var textTypeface: Int = Typeface.NORMAL
    @JvmField
    var textForm: TextForm? = null
    @JvmField
    var iconDrawable: Drawable? = null
    @JvmField
    var iconSize: Int = context.dp2Px(28)
    @JvmField
    var iconSpace: Int = context.dp2Px(8)
    @JvmField
    var iconForm: IconForm? = null
    @FloatRange(from = 0.0, to = 1.0)
    var alpha: Float = 1f
    @JvmField
    @LayoutRes
    var layout: Int = -1
    @JvmField
    var onBalloonClickListener: OnBalloonClickListener? = null
    @JvmField
    var onBalloonDismissListener: OnBalloonDismissListener? = null
    @JvmField
    var lifecycleOwner: LifecycleOwner? = null
    @JvmField
    var balloonAnimation: BalloonAnimation = BalloonAnimation.FADE
    @JvmField
    var preferenceName: String? = null
    @JvmField
    var showTimes: Int = 1

    fun setWidth(value: Int): Builder = apply { this.width = context.dp2Px(value) }
    fun setWidthRatio(@FloatRange(from = 0.0, to = 1.0) value: Float): Builder = apply { this.widthRatio = value }
    fun setHeight(value: Int): Builder = apply { this.height = context.dp2Px(value) }
    fun setSpace(value: Int): Builder = apply { this.space = context.dp2Px(value) }
    fun setArrowVisible(value: Boolean): Builder = apply { this.arrowVisible = value }
    fun setArrowSize(value: Int): Builder = apply { this.arrowSize = context.dp2Px(value) }
    fun setArrowPosition(@FloatRange(from = 0.0, to = 1.0) value: Float): Builder = apply { this.arrowPosition = value }
    fun setArrowOrientation(value: ArrowOrientation): Builder = apply { this.arrowOrientation = value }
    fun setArrowDrawable(value: Drawable?): Builder = apply { this.arrowDrawable = value }
    fun setBackgroundColor(value: Int): Builder = apply { this.backgroundColor = value }
    fun setBackgroundDrawable(value: Drawable?) = apply { this.backgroundDrawable = value }
    fun setCornerRadius(value: Float) = apply { this.cornerRadius = context.dp2Px(value) }
    fun setText(value: String): Builder = apply { this.text = value }
    fun setTextColor(value: Int): Builder = apply { this.textColor = value }
    fun setTextSize(value: Float): Builder = apply { this.textSize = value }
    fun setTextTypeface(value: Int): Builder = apply { this.textTypeface = value }
    fun setTextForm(value: TextForm): Builder = apply { this.textForm = value }
    fun setIconDrawable(value: Drawable?) = apply { this.iconDrawable = value }
    fun setIconSize(value: Int) = apply { this.iconSize = context.dp2Px(value) }
    fun setIconSpace(value: Int) = apply { this.iconSpace = context.dp2Px(value) }
    fun setIconForm(value: IconForm) = apply { this.iconForm = value }
    fun setAlpha(@FloatRange(from = 0.0, to = 1.0) value: Float): Builder = apply { this.alpha = value }
    fun setLayout(@LayoutRes layout: Int): Builder = apply { this.layout = layout }
    fun setLifecycleOwner(value: LifecycleOwner): Builder = apply { this.lifecycleOwner = value }
    fun setBalloonAnimation(value: BalloonAnimation): Builder = apply { this.balloonAnimation = value }
    fun setOnBalloonClickListener(value: OnBalloonClickListener): Builder = apply { this.onBalloonClickListener = value }
    fun setOnBalloonDismissListener(value: OnBalloonDismissListener): Builder = apply { this.onBalloonDismissListener = value }
    fun setOnBalloonClickListener(unit: () -> Unit): Builder = apply {
      this.onBalloonClickListener = object : OnBalloonClickListener {
        override fun onBalloonClick() {
          unit()
        }
      }
    }

    fun setOnBalloonDismissListener(unit: () -> Unit): Builder = apply {
      this.onBalloonDismissListener = object : OnBalloonDismissListener {
        override fun onBalloonDismiss() {
          unit()
        }
      }
    }

    fun setPreferenceName(value: String): Builder = apply { this.preferenceName = value }
    fun setShowTime(value: Int): Builder = apply { this.showTimes = value }

    fun build(): Balloon {
      return Balloon(context, this@Builder)
    }
  }
}
