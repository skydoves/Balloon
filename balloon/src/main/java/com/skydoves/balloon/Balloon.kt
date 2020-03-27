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
import android.annotation.TargetApi
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Handler
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.PopupWindow
import android.widget.RelativeLayout
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.FloatRange
import androidx.annotation.LayoutRes
import androidx.annotation.MainThread
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import androidx.core.widget.ImageViewCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.skydoves.balloon.annotations.Dp
import com.skydoves.balloon.annotations.Sp
import kotlinx.android.synthetic.main.layout_balloon.view.balloon_arrow
import kotlinx.android.synthetic.main.layout_balloon.view.balloon_card
import kotlinx.android.synthetic.main.layout_balloon.view.balloon_content
import kotlinx.android.synthetic.main.layout_balloon.view.balloon_detail
import kotlinx.android.synthetic.main.layout_balloon.view.balloon_icon
import kotlinx.android.synthetic.main.layout_balloon.view.balloon_text

@DslMarker
annotation class BalloonDsl

/** creates an instance of [Balloon] by [Balloon.Builder] using kotlin dsl. */
@BalloonDsl
inline fun createBalloon(context: Context, block: Balloon.Builder.() -> Unit): Balloon =
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
  var onBalloonOutsideTouchListener: OnBalloonOutsideTouchListener? = null
  private var supportRtlLayoutFactor: Int = LTR.unaryMinus(builder.isRtlSupport)
  private val balloonPersistence = BalloonPersistence.getInstance(context)

  init {
    val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    this.bodyView = inflater.inflate(R.layout.layout_balloon, null)
    this.bodyWindow = PopupWindow(bodyView, RelativeLayout.LayoutParams.WRAP_CONTENT,
      RelativeLayout.LayoutParams.WRAP_CONTENT)
    createByBuilder()
  }

  private fun createByBuilder() {
    initializeArrow()
    initializeBackground()
    initializeBalloonWindow()
    initializeBalloonListeners()

    if (builder.layout == NO_INT_VALUE) {
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
      bodyView.post {
        when (builder.arrowOrientation) {
          ArrowOrientation.BOTTOM, ArrowOrientation.TOP ->
            x = bodyView.width * builder.arrowPosition - (builder.arrowSize / 2)
          ArrowOrientation.LEFT, ArrowOrientation.RIGHT ->
            y = bodyView.height * builder.arrowPosition - (builder.arrowSize / 2)
        }
      }
      layoutParams = params
      alpha = builder.alpha
      visible(builder.arrowVisible)
      if (builder.arrowDrawable != null) {
        setImageDrawable(builder.arrowDrawable)
      } else {
        ImageViewCompat.setImageTintList(this, ColorStateList.valueOf(builder.backgroundColor))
      }
    }
  }

  private fun initializeBackground() {
    with(bodyView.balloon_card) {
      alpha = builder.alpha
      if (builder.padding != NO_INT_VALUE) {
        setPadding(builder.padding, builder.padding, builder.padding, builder.padding)
      } else {
        setPadding(builder.paddingLeft, builder.paddingTop,
          builder.paddingRight, builder.paddingBottom)
      }
      if (builder.backgroundDrawable == null) {
        setCardBackgroundColor(builder.backgroundColor)
        radius = builder.cornerRadius
      } else {
        background = builder.backgroundDrawable
      }
    }
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  private fun initializeBalloonWindow() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      bodyWindow.elevation = builder.elevation
    }
  }

  private fun initializeBalloonListeners() {
    this.onBalloonClickListener = builder.onBalloonClickListener
    this.onBalloonDismissListener = builder.onBalloonDismissListener
    this.onBalloonOutsideTouchListener = builder.onBalloonOutsideTouchListener
    this.bodyView.setOnClickListener {
      this.onBalloonClickListener?.onBalloonClick(it)
      if (builder.dismissWhenClicked) dismiss()
    }
    with(this.bodyWindow) {
      setOnDismissListener { onBalloonDismissListener?.onBalloonDismiss() }
      setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
      isOutsideTouchable = true
      setTouchInterceptor(object : View.OnTouchListener {
        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(view: View, event: MotionEvent): Boolean {
          if (event.action == MotionEvent.ACTION_OUTSIDE) {
            if (builder.dismissWhenTouchOutside) {
              this@Balloon.dismiss()
            }
            onBalloonOutsideTouchListener?.onBalloonOutsideTouch(view, event)
            return true
          }
          return false
        }
      })
    }
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
    with(bodyView.balloon_icon) {
      builder.iconForm?.let {
        applyIconForm(it)
      } ?: applyIconForm(iconForm(context) {
        setDrawable(builder.iconDrawable)
        setIconSize(builder.iconSize)
        setIconColor(builder.iconColor)
        setIconSpace(builder.iconSpace)
      })
    }
  }

  private fun initializeText() {
    with(bodyView.balloon_text) {
      builder.textForm?.let {
        applyTextForm(it)
      } ?: applyTextForm(textForm(context) {
        setText(builder.text)
        setTextSize(builder.textSize)
        setTextColor(builder.textColor)
        setTextTypeface(builder.textTypeface)
        setTextTypeface(builder.textTypefaceObject)
      })
    }
  }

  private fun initializeCustomLayout() {
    bodyView.balloon_detail.removeAllViews()
    val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    inflater.inflate(builder.layout, bodyView.balloon_detail)
  }

  private fun applyBalloonAnimation() {
    if (builder.balloonAnimationStyle == NO_INT_VALUE) {
      when (builder.balloonAnimation) {
        BalloonAnimation.ELASTIC -> bodyWindow.animationStyle = R.style.Elastic
        BalloonAnimation.CIRCULAR -> {
          bodyWindow.contentView.circularRevealed()
          bodyWindow.animationStyle = R.style.NormalDispose
        }
        BalloonAnimation.FADE -> bodyWindow.animationStyle = R.style.Fade
        BalloonAnimation.OVERSHOOT -> bodyWindow.animationStyle = R.style.Overshoot
        else -> bodyWindow.animationStyle = R.style.Normal
      }
    } else {
      bodyWindow.animationStyle = builder.balloonAnimationStyle
    }
  }

  @MainThread
  private inline fun show(anchor: View, crossinline block: () -> Unit) {
    if (!this.isShowing) {
      this.isShowing = true
      this.builder.preferenceName?.let {
        if (balloonPersistence.shouldShowUP(it, builder.showTimes)) {
          balloonPersistence.putIncrementedTimes(it)
        } else return
      }

      val dismissDelay = this.builder.autoDismissDuration
      if (dismissDelay != NO_LONG_VALUE) {
        dismissWithDelay(dismissDelay)
      }

      anchor.post {
        this.bodyView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        this.bodyWindow.width = getMeasureWidth()
        this.bodyWindow.height = getMeasureHeight()
        this.bodyView.balloon_detail.layoutParams = FrameLayout.LayoutParams(
          FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)

        applyBalloonAnimation()
        block()
      }
    } else if (builder.dismissWhenShowAgain) {
      dismiss()
    }
  }

  @MainThread
  private inline fun relay(
    balloon: Balloon,
    crossinline block: (balloon: Balloon) -> Unit
  ): Balloon {
    this.setOnBalloonDismissListener { block(balloon) }
    return balloon
  }

  /** shows the balloon on the center of an anchor view. */
  fun show(anchor: View) {
    show(anchor) {
      bodyWindow.showAsDropDown(anchor, -(anchor.measuredWidth / 2),
        -getMeasureHeight() - (anchor.measuredHeight / 2))
    }
  }

  /**
   * shows the balloon on the center of an anchor view
   * and shows the next balloon sequentially.
   * This function returns the next balloon.
   */
  fun relayShow(balloon: Balloon, anchor: View) = relay(balloon) { it.show(anchor) }

  /** shows the balloon on an anchor view with x-off and y-off. */
  fun show(anchor: View, xOff: Int, yOff: Int) {
    show(anchor) { bodyWindow.showAsDropDown(anchor, xOff, yOff) }
  }

  /**
   * shows the balloon on an anchor view with x-off and y-off
   * and shows the next balloon sequentially.
   * This function returns the next balloon.
   */
  fun relayShow(balloon: Balloon, anchor: View, xOff: Int, yOff: Int) = relay(balloon) {
    it.show(anchor, xOff, yOff)
  }

  /** shows the balloon on an anchor view as drop down. */
  fun showAsDropDown(anchor: View) {
    show(anchor) { bodyWindow.showAsDropDown(anchor) }
  }

  /**
   * shows the balloon on an anchor view as drop down
   * and shows the next balloon sequentially.
   * This function returns the next balloon.
   */
  fun relayShowAsDropDown(balloon: Balloon, anchor: View) = relay(balloon) {
    it.showAsDropDown(anchor)
  }

  /** shows the balloon on an anchor view as drop down with x-off and y-off. */
  fun showAsDropDown(anchor: View, xOff: Int, yOff: Int) {
    show(anchor) { bodyWindow.showAsDropDown(anchor, xOff, yOff) }
  }

  /**
   * shows the balloon on an anchor view as drop down with x-off and y-off
   * and shows the next balloon sequentially.
   * This function returns the next balloon.
   */
  fun relayShowAsDropDown(balloon: Balloon, anchor: View, xOff: Int, yOff: Int) = relay(balloon) {
    it.showAsDropDown(anchor, xOff, yOff)
  }

  /** shows the balloon on an anchor view as the top alignment. */
  fun showAlignTop(anchor: View) {
    show(anchor) {
      bodyWindow.showAsDropDown(anchor,
        supportRtlLayoutFactor * ((anchor.measuredWidth / 2) - (getMeasureWidth() / 2)),
        -getMeasureHeight() - anchor.measuredHeight)
    }
  }

  /**
   * shows the balloon on an anchor view as the top alignment
   * and shows the next balloon sequentially.
   * This function returns the next balloon.
   */
  fun relayShowAlignTop(balloon: Balloon, anchor: View) = relay(balloon) {
    it.showAlignTop(anchor)
  }

  /** shows the balloon on an anchor view as the top alignment with x-off and y-off. */
  fun showAlignTop(anchor: View, xOff: Int, yOff: Int) {
    show(anchor) {
      bodyWindow.showAsDropDown(anchor,
        supportRtlLayoutFactor * ((anchor.measuredWidth / 2) - (getMeasureWidth() / 2) + xOff),
        -getMeasureHeight() - anchor.measuredHeight + yOff)
    }
  }

  /**
   * shows the balloon on an anchor view as the top alignment with x-off and y-off
   * and shows the next balloon sequentially.
   * This function returns the next balloon.
   */
  fun relayShowAlignTop(balloon: Balloon, anchor: View, xOff: Int, yOff: Int) = relay(balloon) {
    it.showAlignTop(anchor, xOff, yOff)
  }

  /** shows the balloon on an anchor view as the bottom alignment. */
  fun showAlignBottom(anchor: View) {
    show(anchor) {
      bodyWindow.showAsDropDown(anchor,
        supportRtlLayoutFactor * ((anchor.measuredWidth / 2) - (getMeasureWidth() / 2)),
        0)
    }
  }

  /**
   * shows the balloon on an anchor view as the bottom alignment
   * and shows the next balloon sequentially.
   * This function returns the next balloon.
   */
  fun relayShowAlignBottom(balloon: Balloon, anchor: View) = relay(balloon) {
    it.showAlignBottom(anchor)
  }

  /** shows the balloon on an anchor view as the bottom alignment with x-off and y-off. */
  fun showAlignBottom(anchor: View, xOff: Int, yOff: Int) {
    show(anchor) {
      bodyWindow.showAsDropDown(anchor,
        supportRtlLayoutFactor * ((anchor.measuredWidth / 2) - (getMeasureWidth() / 2) + xOff),
        yOff)
    }
  }

  /**
   * shows the balloon on an anchor view as the bottom alignment with x-off and y-off
   * and shows the next balloon sequentially.
   * This function returns the next balloon.
   */
  fun relayShowAlignBottom(balloon: Balloon, anchor: View, xOff: Int, yOff: Int) = relay(balloon) {
    it.showAlignBottom(anchor, xOff, yOff)
  }

  /** shows the balloon on an anchor view as the right alignment. */
  fun showAlignRight(anchor: View) {
    show(anchor) {
      bodyWindow.showAsDropDown(anchor, anchor.measuredWidth,
        -(getMeasureHeight() / 2) - (anchor.measuredHeight / 2))
    }
  }

  /**
   * shows the balloon on an anchor view as the right alignment
   * and shows the next balloon sequentially.
   * This function returns the next balloon.
   */
  fun relayShowAlignRight(balloon: Balloon, anchor: View) = relay(balloon) {
    it.showAlignRight(anchor)
  }

  /** shows the balloon on an anchor view as the right alignment with x-off and y-off. */
  fun showAlignRight(anchor: View, xOff: Int, yOff: Int) {
    show(anchor) {
      bodyWindow.showAsDropDown(anchor, anchor.measuredWidth + xOff,
        -(getMeasureHeight() / 2) - (anchor.measuredHeight / 2) + yOff)
    }
  }

  /**
   * shows the balloon on an anchor view as the right alignment with x-off and y-off
   * and shows the next balloon sequentially.
   * This function returns the next balloon.
   */
  fun relayShowAlignRight(balloon: Balloon, anchor: View, xOff: Int, yOff: Int) = relay(balloon) {
    it.showAlignRight(anchor, xOff, yOff)
  }

  /** shows the balloon on an anchor view as the left alignment. */
  fun showAlignLeft(anchor: View) {
    show(anchor) {
      bodyWindow.showAsDropDown(anchor, -(getMeasureWidth()),
        -(getMeasureHeight() / 2) - (anchor.measuredHeight / 2))
    }
  }

  /**
   * shows the balloon on an anchor view as the left alignment
   * and shows the next balloon sequentially.
   * This function returns the next balloon.
   */
  fun relayShowAlignLeft(balloon: Balloon, anchor: View) = relay(balloon) {
    it.showAlignLeft(anchor)
  }

  /** shows the balloon on an anchor view as the left alignment with x-off and y-off. */
  fun showAlignLeft(anchor: View, xOff: Int, yOff: Int) {
    show(anchor) {
      bodyWindow.showAsDropDown(anchor, -(getMeasureWidth()) + xOff,
        -(getMeasureHeight() / 2) - (anchor.measuredHeight / 2) + yOff)
    }
  }

  /**
   * shows the balloon on an anchor view as the left alignment with x-off and y-off
   * and shows the next balloon sequentially.
   * This function returns the next balloon.
   */
  fun relayShowAlignLeft(balloon: Balloon, anchor: View, xOff: Int, yOff: Int) = relay(balloon) {
    it.showAlignLeft(anchor, xOff, yOff)
  }

  /** dismiss the popup menu. */
  fun dismiss() {
    if (this.isShowing) {
      this.isShowing = false

      val dismissWindow: () -> Unit = { this.bodyWindow.dismiss() }
      if (this.builder.balloonAnimation == BalloonAnimation.CIRCULAR) {
        this.bodyWindow.contentView.circularUnRevealed {
          dismissWindow()
        }
      } else {
        dismissWindow()
      }
    }
  }

  /** dismiss the popup menu with milliseconds delay. */
  fun dismissWithDelay(delay: Long) {
    Handler().postDelayed({ dismiss() }, delay)
  }

  /** sets a [OnBalloonClickListener] to the popup using lambda. */
  fun setOnBalloonClickListener(unit: (View) -> Unit) {
    this.onBalloonClickListener = object : OnBalloonClickListener {
      override fun onBalloonClick(view: View) {
        unit(view)
      }
    }
  }

  /** sets a [OnBalloonDismissListener] to the popup using lambda. */
  fun setOnBalloonDismissListener(unit: () -> Unit) {
    this.onBalloonDismissListener = object : OnBalloonDismissListener {
      override fun onBalloonDismiss() {
        unit()
      }
    }
  }

  /** sets a [OnBalloonOutsideTouchListener] to the popup using lambda. */
  fun setOnBalloonOutsideTouchListener(unit: (View, MotionEvent) -> Unit) {
    this.onBalloonOutsideTouchListener = object : OnBalloonOutsideTouchListener {
      override fun onBalloonOutsideTouch(
        view: View,
        event: MotionEvent
      ) {
        unit(view, event)
      }
    }
  }

  /** gets measured width size of the balloon popup. */
  fun getMeasureWidth(): Int {
    if (builder.widthRatio != NO_Float_VALUE) {
      return (context.displaySize().x * builder.widthRatio - builder.space).toInt()
    } else if (builder.width != NO_INT_VALUE) {
      return builder.width
    }
    return this.bodyView.measuredWidth
  }

  /** gets measured height size of the balloon popup. */
  fun getMeasureHeight(): Int {
    if (builder.height != NO_INT_VALUE) {
      return builder.height
    }
    return this.bodyView.measuredHeight
  }

  /** gets a content view of the balloon popup window. */
  fun getContentView(): View {
    return bodyView.balloon_detail
  }

  /** dismiss automatically when lifecycle owner is destroyed. */
  @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
  fun onDestroy() {
    dismiss()
  }

  /** Builder class for creating [Balloon]. */
  @BalloonDsl
  class Builder(private val context: Context) {
    @JvmField @Dp
    var width: Int = NO_INT_VALUE
    @JvmField @FloatRange(from = 0.0, to = 1.0)
    var widthRatio: Float = NO_Float_VALUE
    @JvmField @Dp
    var height: Int = NO_INT_VALUE
    @JvmField @Dp
    var padding: Int = NO_INT_VALUE
    @JvmField @Dp
    var paddingLeft: Int = 0
    @JvmField @Dp
    var paddingTop: Int = 0
    @JvmField @Dp
    var paddingRight: Int = 0
    @JvmField @Dp
    var paddingBottom: Int = 0
    @JvmField @Dp
    var space: Int = 0
    @JvmField
    var arrowVisible: Boolean = true
    @JvmField @Dp
    var arrowSize: Int = context.dp2Px(15)
    @JvmField @FloatRange(from = 0.0, to = 1.0)
    var arrowPosition: Float = 0.5f
    @JvmField
    var arrowOrientation: ArrowOrientation = ArrowOrientation.BOTTOM
    @JvmField
    var arrowDrawable: Drawable? = null
    @JvmField @ColorInt
    var backgroundColor: Int = Color.BLACK
    @JvmField
    var backgroundDrawable: Drawable? = null
    @JvmField @Dp
    var cornerRadius: Float = context.dp2Px(5).toFloat()
    @JvmField
    var text: String = ""
    @JvmField @ColorInt
    var textColor: Int = Color.WHITE
    @JvmField @Sp
    var textSize: Float = 12f
    @JvmField
    var textTypeface: Int = Typeface.NORMAL
    @JvmField
    var textTypefaceObject: Typeface? = null
    @JvmField
    var textForm: TextForm? = null
    @JvmField
    var iconDrawable: Drawable? = null
    @JvmField @Dp
    var iconSize: Int = context.dp2Px(28)
    @JvmField @Dp
    var iconSpace: Int = context.dp2Px(8)
    @JvmField @ColorInt
    var iconColor: Int = Color.WHITE
    @JvmField
    var iconForm: IconForm? = null
    @JvmField @FloatRange(from = 0.0, to = 1.0)
    var alpha: Float = 1f
    @JvmField
    var elevation: Float = context.dp2Px(2f)
    @JvmField @LayoutRes
    var layout: Int = NO_INT_VALUE
    @JvmField
    var onBalloonClickListener: OnBalloonClickListener? = null
    @JvmField
    var onBalloonDismissListener: OnBalloonDismissListener? = null
    @JvmField
    var onBalloonOutsideTouchListener: OnBalloonOutsideTouchListener? = null
    @JvmField
    var dismissWhenTouchOutside: Boolean = false
    @JvmField
    var dismissWhenShowAgain: Boolean = false
    @JvmField
    var dismissWhenClicked: Boolean = false
    @JvmField
    var autoDismissDuration: Long = NO_LONG_VALUE
    @JvmField
    var lifecycleOwner: LifecycleOwner? = null
    @JvmField @StyleRes
    var balloonAnimationStyle: Int = NO_INT_VALUE
    @JvmField
    var balloonAnimation: BalloonAnimation = BalloonAnimation.FADE
    @JvmField
    var preferenceName: String? = null
    @JvmField
    var showTimes: Int = 1
    @JvmField
    var isRtlSupport: Boolean = false

    /** sets the width size. */
    fun setWidth(@Dp value: Int): Builder = apply { this.width = context.dp2Px(value) }

    /** sets the width size by the display screen size ratio. */
    fun setWidthRatio(
      @FloatRange(from = 0.0, to = 1.0) value: Float
    ): Builder = apply { this.widthRatio = value }

    /** sets the height size. */
    fun setHeight(@Dp value: Int): Builder = apply { this.height = context.dp2Px(value) }

    /** sets the padding on all directions. */
    fun setPadding(@Dp value: Int): Builder = apply { this.padding = context.dp2Px(value) }

    /** sets the left padding on all directions. */
    fun setPaddingLeft(@Dp value: Int): Builder = apply { this.paddingLeft = context.dp2Px(value) }

    /** sets the top padding on all directions. */
    fun setPaddingTop(@Dp value: Int): Builder = apply { this.paddingTop = context.dp2Px(value) }

    /** sets the right padding on all directions. */
    fun setPaddingRight(@Dp value: Int): Builder = apply {
      this.paddingRight = context.dp2Px(value)
    }

    /** sets the bottom padding on all directions. */
    fun setPaddingBottom(@Dp value: Int): Builder = apply {
      this.paddingBottom = context.dp2Px(value)
    }

    /** sets the side space between popup and display. */
    fun setSpace(@Dp value: Int): Builder = apply { this.space = context.dp2Px(value) }

    /** sets the visibility of the arrow. */
    fun setArrowVisible(value: Boolean): Builder = apply { this.arrowVisible = value }

    /** sets the size of the arrow. */
    fun setArrowSize(@Dp value: Int): Builder = apply { this.arrowSize = context.dp2Px(value) }

    /** sets the arrow position by popup size ration. The popup size depends on [arrowOrientation]. */
    fun setArrowPosition(
      @FloatRange(from = 0.0, to = 1.0) value: Float
    ): Builder = apply { this.arrowPosition = value }

    /** sets the arrow orientation using [ArrowOrientation]. */
    fun setArrowOrientation(value: ArrowOrientation): Builder = apply {
      this.arrowOrientation = value
    }

    /** sets a custom drawable of the arrow. */
    fun setArrowDrawable(value: Drawable?): Builder = apply {
      this.arrowDrawable = value?.mutate()
    }

    /** sets a custom drawable of the arrow using the resource. */
    fun setArrowDrawableResource(@DrawableRes value: Int): Builder = apply {
      this.arrowDrawable = context.contextDrawable(value)?.mutate()
    }

    /** sets the background color of the arrow and popup. */
    fun setBackgroundColor(@ColorInt value: Int): Builder = apply { this.backgroundColor = value }

    /** sets the background color of the arrow and popup using the resource color. */
    fun setBackgroundColorResource(@ColorRes value: Int): Builder = apply {
      this.backgroundColor = context.contextColor(value)
    }

    /** sets the background drawable of the popup. */
    fun setBackgroundDrawable(value: Drawable?): Builder = apply {
      this.backgroundDrawable = value?.mutate()
    }

    /** sets the background drawable of the popup by the resource. */
    fun setBackgroundDrawableResource(@DrawableRes value: Int): Builder = apply {
      this.backgroundDrawable = context.contextDrawable(value)?.mutate()
    }

    /** sets the corner radius of the popup. */
    fun setCornerRadius(@Dp value: Float): Builder = apply {
      this.cornerRadius = context.dp2Px(value)
    }

    /** sets the main text content of the popup. */
    fun setText(value: String): Builder = apply { this.text = value }

    /** sets the main text content of the popup using resource. */
    fun setTextResource(@StringRes value: Int): Builder = apply {
      this.text = context.getString(value)
    }

    /** sets the color of the main text content. */
    fun setTextColor(@ColorInt value: Int): Builder = apply { this.textColor = value }

    /** sets the color of the main text content using the resource color. */
    fun setTextColorResource(@ColorRes value: Int): Builder = apply {
      this.textColor = context.contextColor(value)
    }

    /** sets the size of the main text content. */
    fun setTextSize(@Sp value: Float): Builder = apply { this.textSize = value }

    /** sets the typeface of the main text content. */
    fun setTextTypeface(value: Int): Builder = apply { this.textTypeface = value }

    /** sets the typeface of the main text content. */
    fun setTextTypeface(value: Typeface): Builder = apply { this.textTypefaceObject = value }

    /** applies [TextForm] attributes to the main text content. */
    fun setTextForm(value: TextForm): Builder = apply { this.textForm = value }

    /** sets the icon drawable of the popup. */
    fun setIconDrawable(value: Drawable?): Builder = apply { this.iconDrawable = value?.mutate() }

    /** sets the icon drawable of the popup using the resource. */
    fun setIconDrawableResource(@DrawableRes value: Int) = apply {
      this.iconDrawable = context.contextDrawable(value)?.mutate()
    }

    /** sets the size of the icon drawable. */
    fun setIconSize(@Dp value: Int): Builder = apply { this.iconSize = context.dp2Px(value) }

    /** sets the color of the icon drawable. */
    fun setIconColor(@ColorInt value: Int): Builder = apply { this.iconColor = value }

    /** sets the color of the icon drawable using the resource color. */
    fun setIconColorResource(@ColorInt value: Int): Builder = apply {
      this.iconColor = context.contextColor(value)
    }

    /** sets the space between the icon and the main text content. */
    fun setIconSpace(@Dp value: Int): Builder = apply { this.iconSpace = context.dp2Px(value) }

    /** applies [IconForm] attributes to the icon. */
    fun setIconForm(value: IconForm): Builder = apply { this.iconForm = value }

    /** sets the alpha value to the popup. */
    fun setAlpha(@FloatRange(from = 0.0, to = 1.0) value: Float): Builder = apply {
      this.alpha = value
    }

    /** sets the elevation to the popup. */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    fun setElevation(value: Float): Builder = apply {
      this.elevation = value
    }

    /** sets the custom layout resource to the popup content. */
    fun setLayout(@LayoutRes layout: Int): Builder = apply { this.layout = layout }

    /**
     * sets the [LifecycleOwner] for dismissing automatically when the [LifecycleOwner] is destroyed.
     * It will prevents memory leak : [Avoid Memory Leak](https://github.com/skydoves/balloon#avoid-memory-leak)
     */
    fun setLifecycleOwner(value: LifecycleOwner?): Builder = apply { this.lifecycleOwner = value }

    /** sets the balloon showing animation using [BalloonAnimation]. */
    fun setBalloonAnimation(value: BalloonAnimation): Builder = apply {
      this.balloonAnimation = value
    }

    /** sets the balloon showing animation using custom xml animation style. */
    fun setBalloonAnimationStyle(@StyleRes value: Int): Builder = apply {
      this.balloonAnimationStyle = value
    }

    /** sets a [OnBalloonClickListener] to the popup. */
    fun setOnBalloonClickListener(value: OnBalloonClickListener): Builder = apply {
      this.onBalloonClickListener = value
    }

    /** sets a [OnBalloonDismissListener] to the popup. */
    fun setOnBalloonDismissListener(value: OnBalloonDismissListener): Builder = apply {
      this.onBalloonDismissListener = value
    }

    /** sets a [OnBalloonOutsideTouchListener] to the popup. */
    fun setOnBalloonOutsideTouchListener(value: OnBalloonOutsideTouchListener): Builder = apply {
      this.onBalloonOutsideTouchListener = value
    }

    /** sets a [OnBalloonClickListener] to the popup using lambda. */
    fun setOnBalloonClickListener(unit: (View) -> Unit): Builder = apply {
      this.onBalloonClickListener = object : OnBalloonClickListener {
        override fun onBalloonClick(view: View) {
          unit(view)
        }
      }
    }

    /** sets a [OnBalloonDismissListener] to the popup using lambda. */
    fun setOnBalloonDismissListener(unit: () -> Unit): Builder = apply {
      this.onBalloonDismissListener = object : OnBalloonDismissListener {
        override fun onBalloonDismiss() {
          unit()
        }
      }
    }

    /** sets a [OnBalloonOutsideTouchListener] to the popup using lambda. */
    fun setOnBalloonOutsideTouchListener(unit: (View, MotionEvent) -> Unit): Builder = apply {
      this.onBalloonOutsideTouchListener = object : OnBalloonOutsideTouchListener {
        override fun onBalloonOutsideTouch(
          view: View,
          event: MotionEvent
        ) {
          unit(view, event)
        }
      }
    }

    /** dismisses when touch outside. */
    fun setDismissWhenTouchOutside(value: Boolean): Builder = apply {
      this.dismissWhenTouchOutside = value
    }

    /** dismisses when invoked show function again. */
    fun setDismissWhenShowAgain(value: Boolean): Builder = apply {
      this.dismissWhenShowAgain = value
    }

    /** dismisses when the popup clicked. */
    fun setDismissWhenClicked(value: Boolean): Builder = apply { this.dismissWhenClicked = value }

    /** dismisses automatically some milliseconds later when the popup is shown. */
    fun setAutoDismissDuration(value: Long): Builder = apply { this.autoDismissDuration = value }

    /** sets the preference name for persisting showing times([showTimes]).  */
    fun setPreferenceName(value: String): Builder = apply { this.preferenceName = value }

    /** sets the show times. */
    fun setShowTime(value: Int): Builder = apply { this.showTimes = value }

    /** sets flag for enabling rtl support */
    fun isRtlSupport(value: Boolean): Builder = apply { this.isRtlSupport = value }

    fun build(): Balloon = Balloon(context, this@Builder)
  }

  /**
   * An abstract factory class for creating [Balloon] instance.
   *
   * A factory implementation class must have a non-argument constructor.
   */
  abstract class Factory {

    /** returns an instance of [Balloon]. */
    abstract fun create(context: Context, lifecycle: LifecycleOwner?): Balloon
  }
}
