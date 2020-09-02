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
import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.FloatRange
import androidx.annotation.LayoutRes
import androidx.annotation.MainThread
import androidx.annotation.Px
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import androidx.core.widget.ImageViewCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.skydoves.balloon.annotations.Dp
import com.skydoves.balloon.annotations.Sp
import com.skydoves.balloon.databinding.LayoutBalloonBinding
import com.skydoves.balloon.extensions.applyIconForm
import com.skydoves.balloon.extensions.applyTextForm
import com.skydoves.balloon.extensions.circularRevealed
import com.skydoves.balloon.extensions.circularUnRevealed
import com.skydoves.balloon.extensions.contextColor
import com.skydoves.balloon.extensions.contextDrawable
import com.skydoves.balloon.extensions.dimen
import com.skydoves.balloon.extensions.displaySize
import com.skydoves.balloon.extensions.dp2Px
import com.skydoves.balloon.extensions.visible

@DslMarker
annotation class BalloonDsl

/** creates an instance of [Balloon] by [Balloon.Builder] using kotlin dsl. */
@BalloonDsl
inline fun createBalloon(context: Context, block: Balloon.Builder.() -> Unit): Balloon =
  Balloon.Builder(context).apply(block).build()

/** Balloon implements showing and dismissing text popup with arrow and animations. */
@Suppress("MemberVisibilityCanBePrivate")
class Balloon(
  private val context: Context,
  private val builder: Builder
) : LifecycleObserver {

  private val binding: LayoutBalloonBinding =
    LayoutBalloonBinding.inflate(LayoutInflater.from(context), null, false)
  private val bodyWindow: PopupWindow
  var isShowing = false
    private set
  private var destroyed: Boolean = false
  var onBalloonClickListener: OnBalloonClickListener? = null
  var onBalloonDismissListener: OnBalloonDismissListener? = null
  var onBalloonInitializedListener: OnBalloonInitializedListener? = null
  var onBalloonOutsideTouchListener: OnBalloonOutsideTouchListener? = null
  private var supportRtlLayoutFactor: Int = LTR.unaryMinus(builder.isRtlSupport)
  private val balloonPersistence = BalloonPersistence.getInstance(context)

  init {
    this.bodyWindow = PopupWindow(
      binding.root,
      RelativeLayout.LayoutParams.WRAP_CONTENT,
      RelativeLayout.LayoutParams.WRAP_CONTENT
    )
    createByBuilder()
  }

  private fun createByBuilder() {
    initializeBackground()
    initializeBalloonRoot()
    initializeBalloonWindow()
    initializeBalloonContent()
    initializeBalloonListeners()

    if (builder.layoutRes != NO_INT_VALUE) {
      initializeCustomLayoutWithResource()
    } else if (builder.layout != null) {
      initializeCustomLayoutWithView()
    } else {
      initializeIcon()
      initializeText()
    }
    builder.lifecycleOwner?.lifecycle?.addObserver(this@Balloon)
  }

  private fun initializeArrow(anchor: View) {
    with(binding.balloonArrow) {
      visible(false)
      val params = RelativeLayout.LayoutParams(builder.arrowSize, builder.arrowSize)
      when (builder.arrowOrientation) {
        ArrowOrientation.BOTTOM -> {
          params.addRule(RelativeLayout.ALIGN_BOTTOM, binding.balloonContent.id)
          rotation = 180f
        }
        ArrowOrientation.TOP -> {
          params.addRule(RelativeLayout.ALIGN_TOP, binding.balloonContent.id)
          rotation = 0f
        }
        ArrowOrientation.LEFT -> {
          params.addRule(RelativeLayout.ALIGN_LEFT, binding.balloonContent.id)
          rotation = -90f
        }
        ArrowOrientation.RIGHT -> {
          params.addRule(RelativeLayout.ALIGN_RIGHT, binding.balloonContent.id)
          rotation = 90f
        }
      }
      layoutParams = params
      alpha = builder.alpha
      builder.arrowDrawable?.let { setImageDrawable(it) }
      setPadding(
        builder.arrowLeftPadding,
        builder.arrowTopPadding,
        builder.arrowRightPadding,
        builder.arrowBottomPadding
      )
      if (builder.arrowColor != NO_INT_VALUE) {
        ImageViewCompat.setImageTintList(this, ColorStateList.valueOf(builder.arrowColor))
      } else {
        ImageViewCompat.setImageTintList(this, ColorStateList.valueOf(builder.backgroundColor))
      }
      binding.root.post {
        binding.balloonArrow.visible(builder.arrowVisible)
        onBalloonInitializedListener?.onBalloonInitialized(getContentView())
        when (builder.arrowOrientation) {
          ArrowOrientation.BOTTOM, ArrowOrientation.TOP -> {
            x = getArrowConstraintPositionX(anchor)
          }
          ArrowOrientation.LEFT, ArrowOrientation.RIGHT -> {
            y = getArrowConstraintPositionY(anchor)
          }
        }
      }
    }
  }

  private fun getMinArrowPosition(): Float {
    return (builder.arrowSize.toFloat() * builder.arrowAlignAnchorPaddingRatio) +
      builder.arrowAlignAnchorPadding
  }

  private fun getWindowBodyScreenLocation(view: View): IntArray {
    val location: IntArray = intArrayOf(0, 0)
    view.getLocationOnScreen(location)
    return location
  }

  fun getStatusBarHeight(): Int {
    val rectangle = Rect()
    return if (context is Activity && builder.isStatusBarVisible) {
      context.window.decorView.getWindowVisibleDisplayFrame(rectangle)
      rectangle.top
    } else 0
  }

  fun getDoubleArrowSize(): Int {
    return builder.arrowSize * 2
  }

  private fun getArrowConstraintPositionX(anchor: View): Float {
    val balloonX: Int = getWindowBodyScreenLocation(binding.balloonContent)[0]
    val anchorX: Int = getWindowBodyScreenLocation(anchor)[0]
    val minPosition = getMinArrowPosition()
    val maxPosition = getMeasureWidth() - minPosition - builder.marginRight - builder.marginLeft
    val arrowHalfSize = builder.arrowSize / 2f
    return when (builder.arrowConstraints) {
      ArrowConstraints.ALIGN_BALLOON -> binding.balloonWrapper.width * builder.arrowPosition - arrowHalfSize
      ArrowConstraints.ALIGN_ANCHOR -> {
        when {
          anchorX + anchor.width < balloonX -> minPosition
          balloonX + getMeasureWidth() < anchorX -> maxPosition
          else -> {
            val position =
              (anchor.width) * builder.arrowPosition + anchorX - balloonX - arrowHalfSize
            when {
              position <= getDoubleArrowSize() -> minPosition
              position > getMeasureWidth() - getDoubleArrowSize() -> maxPosition
              else -> position
            }
          }
        }
      }
    }
  }

  private fun getArrowConstraintPositionY(anchor: View): Float {
    val balloonY: Int =
      getWindowBodyScreenLocation(binding.balloonContent)[1] - getStatusBarHeight()
    val anchorY: Int = getWindowBodyScreenLocation(anchor)[1] - getStatusBarHeight()
    val minPosition = getMinArrowPosition()
    val maxPosition = getMeasureHeight() - minPosition - builder.marginTop - builder.marginBottom
    val arrowHalfSize = builder.arrowSize / 2
    return when (builder.arrowConstraints) {
      ArrowConstraints.ALIGN_BALLOON -> binding.balloonWrapper.height * builder.arrowPosition - arrowHalfSize
      ArrowConstraints.ALIGN_ANCHOR -> {
        when {
          anchorY + anchor.height < balloonY -> minPosition
          balloonY + getMeasureHeight() < anchorY -> maxPosition
          else -> {
            val position =
              (anchor.height) * builder.arrowPosition + anchorY - balloonY - arrowHalfSize
            when {
              position <= getDoubleArrowSize() -> minPosition
              position > getMeasureHeight() - getDoubleArrowSize() -> maxPosition
              else -> position
            }
          }
        }
      }
    }
  }

  private fun initializeBackground() {
    with(binding.balloonCard) {
      alpha = builder.alpha
      cardElevation = builder.elevation
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
    with(this.bodyWindow) {
      isFocusable = builder.isFocusable
      setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        elevation = builder.elevation
      }
    }
  }

  private fun initializeBalloonListeners() {
    this.onBalloonClickListener = builder.onBalloonClickListener
    this.onBalloonDismissListener = builder.onBalloonDismissListener
    this.onBalloonInitializedListener = builder.onBalloonInitializedListener
    this.onBalloonOutsideTouchListener = builder.onBalloonOutsideTouchListener
    this.binding.balloonWrapper.setOnClickListener {
      this.onBalloonClickListener?.onBalloonClick(it)
      if (builder.dismissWhenClicked) dismiss()
    }
    with(this.bodyWindow) {
      isOutsideTouchable = builder.dismissWhenTouchOutside
      setOnDismissListener {
        this@Balloon.dismiss()
        onBalloonDismissListener?.onBalloonDismiss()
      }
      setTouchInterceptor(
        object : View.OnTouchListener {
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
        }
      )
    }
  }

  private fun initializeBalloonRoot() {
    with(binding.balloonWrapper) {
      (layoutParams as ViewGroup.MarginLayoutParams).setMargins(
        builder.marginLeft,
        builder.marginTop,
        builder.marginRight,
        builder.marginBottom
      )
    }
  }

  private fun initializeBalloonContent() {
    val paddingSize = builder.arrowSize * 2 - 2
    with(binding.balloonContent) {
      when (builder.arrowOrientation) {
        ArrowOrientation.LEFT -> setPadding(paddingSize, 0, 0, 0)
        ArrowOrientation.TOP -> setPadding(0, paddingSize, 0, 0)
        ArrowOrientation.RIGHT -> setPadding(0, 0, paddingSize, 0)
        ArrowOrientation.BOTTOM -> setPadding(0, 0, 0, paddingSize)
      }
    }
    with(binding.balloonText) {
      setPadding(
        builder.paddingLeft,
        builder.paddingTop,
        builder.paddingRight,
        builder.paddingBottom
      )
    }
  }

  private fun initializeIcon() {
    with(binding.balloonText) {
      builder.iconForm?.let {
        applyIconForm(it)
      } ?: applyIconForm(
        iconForm(context) {
          setDrawable(builder.iconDrawable)
          setIconSize(builder.iconSize)
          setIconColor(builder.iconColor)
          setIconSpace(builder.iconSpace)
          setDrawableGravity(builder.iconGravity)
        }
      )
    }
  }

  private fun initializeText() {
    with(binding.balloonText) {
      builder.textForm?.let {
        applyTextForm(it)
      } ?: applyTextForm(
        textForm(context) {
          setText(builder.text)
          setTextSize(builder.textSize)
          setTextColor(builder.textColor)
          setTextIsHtml(builder.textIsHtml)
          setTextGravity(builder.textGravity)
          setTextTypeface(builder.textTypeface)
          setTextTypeface(builder.textTypefaceObject)
        }
      )
      measureTextWidth(this)
    }
  }

  private fun initializeCustomLayoutWithResource() {
    binding.balloonCard.removeAllViews()
    val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    inflater.inflate(builder.layoutRes, binding.balloonCard)
  }

  private fun initializeCustomLayoutWithView() {
    binding.balloonCard.removeAllViews()
    binding.balloonCard.addView(builder.layout)
  }

  /**
   * measures the width of a [TextView] and set the measured with.
   * If the width of parent XML layout is wrapped content, and also
   * the widths of [TextView]s in the parent layout is wrapped content, this functionality
   * will measure the width exactly.
   */
  fun measureTextWidth(textView: TextView) {
    with(textView) {
      val widthSpec =
        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
      val heightSpec =
        View.MeasureSpec.makeMeasureSpec(context.displaySize().y, View.MeasureSpec.UNSPECIFIED)
      measure(widthSpec, heightSpec)
      layoutParams.width = getMeasureTextWidth(measuredWidth)
    }
  }

  private fun applyBalloonAnimation() {
    if (builder.balloonAnimationStyle == NO_INT_VALUE) {
      when (builder.balloonAnimation) {
        BalloonAnimation.ELASTIC -> bodyWindow.animationStyle = R.style.Elastic
        BalloonAnimation.CIRCULAR -> {
          bodyWindow.contentView.circularRevealed(builder.circularDuration)
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
    if (!this.isShowing && !this.destroyed) {
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
        initializeText()
        this.binding.root.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        this.bodyWindow.width = getMeasureWidth()
        this.bodyWindow.height = getMeasureHeight()
        this.binding.balloonText.layoutParams = FrameLayout.LayoutParams(
          FrameLayout.LayoutParams.MATCH_PARENT,
          FrameLayout.LayoutParams.MATCH_PARENT
        )
        initializeArrow(anchor)
        initializeBalloonContent()

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
    this.setOnBalloonDismissListener {
      if (!destroyed) {
        block(balloon)
      }
    }
    return balloon
  }

  /** shows the balloon on the center of an anchor view. */
  fun show(anchor: View) {
    show(anchor) {
      bodyWindow.showAsDropDown(
        anchor,
        supportRtlLayoutFactor * ((anchor.measuredWidth / 2) - (getMeasureWidth() / 2)),
        -getMeasureHeight() - (anchor.measuredHeight / 2)
      )
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
      bodyWindow.showAsDropDown(
        anchor,
        supportRtlLayoutFactor * ((anchor.measuredWidth / 2) - (getMeasureWidth() / 2)),
        -getMeasureHeight() - anchor.measuredHeight
      )
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
      bodyWindow.showAsDropDown(
        anchor,
        supportRtlLayoutFactor * ((anchor.measuredWidth / 2) - (getMeasureWidth() / 2) + xOff),
        -getMeasureHeight() - anchor.measuredHeight + yOff
      )
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
      bodyWindow.showAsDropDown(
        anchor,
        supportRtlLayoutFactor * ((anchor.measuredWidth / 2) - (getMeasureWidth() / 2)),
        0
      )
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
      bodyWindow.showAsDropDown(
        anchor,
        supportRtlLayoutFactor * ((anchor.measuredWidth / 2) - (getMeasureWidth() / 2) + xOff),
        yOff
      )
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
      bodyWindow.showAsDropDown(
        anchor,
        anchor.measuredWidth,
        -(getMeasureHeight() / 2) - (anchor.measuredHeight / 2)
      )
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
      bodyWindow.showAsDropDown(
        anchor,
        anchor.measuredWidth + xOff,
        -(getMeasureHeight() / 2) - (anchor.measuredHeight / 2) + yOff
      )
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
      bodyWindow.showAsDropDown(
        anchor,
        -(getMeasureWidth()),
        -(getMeasureHeight() / 2) - (anchor.measuredHeight / 2)
      )
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
      bodyWindow.showAsDropDown(
        anchor,
        -(getMeasureWidth()) + xOff,
        -(getMeasureHeight() / 2) - (anchor.measuredHeight / 2) + yOff
      )
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

  /** updates popup and arrow position of the popup based on a new target anchor view. */
  fun update(anchor: View) {
    update(anchor = anchor) {
      this.bodyWindow.update(anchor, getMeasureWidth(), getMeasureHeight())
    }
  }

  /**
   * updates popup and arrow position of the popup based on
   * a new target anchor view with additional x-off and y-off.
   */
  fun update(anchor: View, xOff: Int, yOff: Int) {
    update(anchor = anchor) {
      this.bodyWindow.update(anchor, xOff, yOff, getMeasureWidth(), getMeasureHeight())
    }
  }

  /** updates popup and arrow position of the popup based on a new target anchor view. */
  @MainThread
  private inline fun update(anchor: View, crossinline block: () -> Unit) {
    if (isShowing) {
      initializeArrow(anchor)
      block()
    }
  }

  /** dismiss the popup menu. */
  fun dismiss() {
    if (this.isShowing) {
      val dismissWindow: () -> Unit = {
        this.isShowing = false
        this.bodyWindow.dismiss()
      }
      if (this.builder.balloonAnimation == BalloonAnimation.CIRCULAR) {
        this.bodyWindow.contentView.circularUnRevealed(builder.circularDuration) {
          dismissWindow()
        }
      } else {
        dismissWindow()
      }
    }
  }

  /** dismiss the popup menu with milliseconds delay. */
  fun dismissWithDelay(delay: Long) {
    Handler(Looper.getMainLooper()).postDelayed({ dismiss() }, delay)
  }

  /** sets a [OnBalloonClickListener] to the popup using lambda. */
  fun setOnBalloonClickListener(unit: (View) -> Unit) {
    this.onBalloonClickListener = OnBalloonClickListener { view -> unit(view) }
  }

  /** sets a [OnBalloonDismissListener] to the popup using lambda. */
  fun setOnBalloonDismissListener(unit: () -> Unit) {
    this.onBalloonDismissListener = OnBalloonDismissListener { unit() }
  }

  /** sets a [OnBalloonOutsideTouchListener] to the popup using lambda. */
  fun setOnBalloonOutsideTouchListener(unit: (View, MotionEvent) -> Unit) {
    this.onBalloonOutsideTouchListener =
      OnBalloonOutsideTouchListener { view, event -> unit(view, event) }
  }

  /** gets measured width size of the balloon popup. */
  fun getMeasureWidth(): Int {
    val displayWidth = context.displaySize().x
    return when {
      builder.widthRatio != NO_Float_VALUE ->
        (displayWidth * builder.widthRatio).toInt()
      builder.width != NO_INT_VALUE && builder.width < displayWidth -> builder.width
      binding.root.measuredWidth > displayWidth -> displayWidth
      else -> this.binding.root.measuredWidth
    }
  }

  /** gets measured width size of the balloon popup text label. */
  private fun getMeasureTextWidth(measuredWidth: Int): Int {
    val displayWidth = context.displaySize().x
    val spaces = builder.paddingLeft + builder.paddingRight + context.dp2Px(24) +
      if (builder.iconDrawable != null) {
        builder.iconSize + builder.iconSpace
      } else 0

    return when {
      builder.widthRatio != NO_Float_VALUE ->
        (displayWidth * builder.widthRatio).toInt() - spaces
      builder.width != NO_INT_VALUE && builder.width <= displayWidth ->
        builder.width - spaces
      measuredWidth < displayWidth - spaces -> measuredWidth
      measuredWidth > displayWidth - spaces -> displayWidth - spaces
      else -> displayWidth - spaces
    }
  }

  /** gets measured height size of the balloon popup. */
  fun getMeasureHeight(): Int {
    if (builder.height != NO_INT_VALUE) {
      return builder.height
    }
    return this.binding.root.measuredHeight
  }

  /** gets a content view of the balloon popup window. */
  fun getContentView(): View {
    return binding.balloonCard
  }

  /** dismiss when the [LifecycleOwner] be on paused. */
  @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
  fun onPause() {
    if (builder.dismissWhenLifecycleOnPause) {
      dismiss()
    }
  }

  /** dismiss automatically when lifecycle owner is destroyed. */
  @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
  fun onDestroy() {
    destroyed = true
    dismiss()
  }

  /** Builder class for creating [Balloon]. */
  @BalloonDsl
  class Builder(private val context: Context) {
    @JvmField @Px
    var width: Int = NO_INT_VALUE

    @JvmField @FloatRange(from = 0.0, to = 1.0)
    var widthRatio: Float = NO_Float_VALUE

    @JvmField @Px
    var height: Int = NO_INT_VALUE

    @JvmField @Px
    var paddingLeft: Int = 0

    @JvmField @Px
    var paddingTop: Int = 0

    @JvmField @Px
    var paddingRight: Int = 0

    @JvmField @Px
    var paddingBottom: Int = 0

    @JvmField @Px
    var marginRight: Int = 0

    @JvmField @Px
    var marginLeft: Int = 0

    @JvmField @Px
    var marginTop: Int = 0

    @JvmField @Px
    var marginBottom: Int = 0

    @JvmField
    var arrowVisible: Boolean = true

    @JvmField @ColorInt
    var arrowColor: Int = NO_INT_VALUE

    @JvmField @Px
    var arrowSize: Int = context.dp2Px(12)

    @JvmField @FloatRange(from = 0.0, to = 1.0)
    var arrowPosition: Float = 0.5f

    @JvmField
    var arrowConstraints: ArrowConstraints = ArrowConstraints.ALIGN_BALLOON

    @JvmField
    var arrowOrientation: ArrowOrientation = ArrowOrientation.BOTTOM

    @JvmField
    var arrowDrawable: Drawable? = null

    @JvmField
    var arrowLeftPadding: Int = 0

    @JvmField
    var arrowRightPadding: Int = 0

    @JvmField
    var arrowTopPadding: Int = 0

    @JvmField
    var arrowBottomPadding: Int = 0

    @JvmField
    var arrowAlignAnchorPadding: Int = 0

    @JvmField
    var arrowAlignAnchorPaddingRatio: Float = 2.5f

    @JvmField @ColorInt
    var backgroundColor: Int = Color.BLACK

    @JvmField
    var backgroundDrawable: Drawable? = null

    @JvmField @Px
    var cornerRadius: Float = context.dp2Px(5).toFloat()

    @JvmField
    var text: CharSequence = ""

    @JvmField @ColorInt
    var textColor: Int = Color.WHITE

    @JvmField
    var textIsHtml: Boolean = false

    @JvmField @Sp
    var textSize: Float = 12f

    @JvmField
    var textTypeface: Int = Typeface.NORMAL

    @JvmField
    var textTypefaceObject: Typeface? = null

    @JvmField
    var textGravity: Int = Gravity.CENTER

    @JvmField
    var textForm: TextForm? = null

    @JvmField
    var iconDrawable: Drawable? = null

    @JvmField
    var iconGravity = IconGravity.LEFT

    @JvmField @Px
    var iconSize: Int = context.dp2Px(28)

    @JvmField @Px
    var iconSpace: Int = context.dp2Px(8)

    @JvmField @ColorInt
    var iconColor: Int = Color.WHITE

    @JvmField
    var iconForm: IconForm? = null

    @JvmField @FloatRange(from = 0.0, to = 1.0)
    var alpha: Float = 1f

    @JvmField
    var elevation: Float = context.dp2Px(2f)

    @JvmField
    var layout: View? = null

    @JvmField
    @LayoutRes
    var layoutRes: Int = NO_INT_VALUE

    @JvmField
    var onBalloonClickListener: OnBalloonClickListener? = null

    @JvmField
    var onBalloonDismissListener: OnBalloonDismissListener? = null

    @JvmField
    var onBalloonInitializedListener: OnBalloonInitializedListener? = null

    @JvmField
    var onBalloonOutsideTouchListener: OnBalloonOutsideTouchListener? = null

    @JvmField
    var dismissWhenTouchOutside: Boolean = true

    @JvmField
    var dismissWhenShowAgain: Boolean = false

    @JvmField
    var dismissWhenClicked: Boolean = false

    @JvmField
    var dismissWhenLifecycleOnPause: Boolean = false

    @JvmField
    var autoDismissDuration: Long = NO_LONG_VALUE

    @JvmField
    var lifecycleOwner: LifecycleOwner? = null

    @JvmField @StyleRes
    var balloonAnimationStyle: Int = NO_INT_VALUE

    @JvmField
    var balloonAnimation: BalloonAnimation = BalloonAnimation.FADE

    @JvmField
    var circularDuration: Long = 500L

    @JvmField
    var preferenceName: String? = null

    @JvmField
    var showTimes: Int = 1

    @JvmField
    var isRtlSupport: Boolean = false

    @JvmField
    var isFocusable: Boolean = true

    @JvmField
    var isStatusBarVisible: Boolean = true

    /** sets the width size. */
    fun setWidth(@Dp value: Int): Builder = apply {
      require(value > 0) { "The width of the balloon must bigger than zero." }
      this.width = context.dp2Px(value)
    }

    /** sets the width size using dimension resources. */
    fun setWidthResource(@DimenRes value: Int): Builder = apply {
      this.width = context.dimen(value)
    }

    /** sets the width size by the display screen size ratio. */
    fun setWidthRatio(
      @FloatRange(from = 0.0, to = 1.0) value: Float
    ): Builder = apply { this.widthRatio = value }

    /** sets the height size. */
    fun setHeight(@Dp value: Int): Builder = apply { this.height = context.dp2Px(value) }

    /** sets the height size using dimension resource. */
    fun setHeightResource(@DimenRes value: Int): Builder = apply {
      this.height = context.dimen(value)
    }

    /** sets the padding on all directions. */
    fun setPadding(@Dp value: Int): Builder = apply {
      setPaddingLeft(value)
      setPaddingTop(value)
      setPaddingRight(value)
      setPaddingBottom(value)
    }

    /** sets the padding on all directions using dimension resource. */
    fun setPaddingResource(@DimenRes value: Int): Builder = apply {
      setPadding(value)
    }

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

    /** sets the visibility of the arrow. */
    fun setArrowVisible(value: Boolean): Builder = apply { this.arrowVisible = value }

    /** sets a color of the arrow. */
    fun setArrowColor(@ColorInt value: Int): Builder = apply { this.arrowColor = value }

    /** sets a color of the arrow using a resource. */
    fun setArrowColorResource(@ColorRes value: Int): Builder = apply {
      this.arrowColor = context.contextColor(value)
    }

    /** sets the size of the arrow. */
    fun setArrowSize(@Dp value: Int): Builder = apply { this.arrowSize = context.dp2Px(value) }

    /** sets the size of the arrow using dimension resource. */
    fun setArrowSizeResource(@DimenRes value: Int): Builder = apply {
      this.arrowSize = context.dimen(value)
    }

    /** sets the arrow position by popup size ration. The popup size depends on [arrowOrientation]. */
    fun setArrowPosition(
      @FloatRange(from = 0.0, to = 1.0) value: Float
    ): Builder = apply { this.arrowPosition = value }

    /**
     * sets the constraints of the arrow positioning.
     * [ArrowConstraints.ALIGN_BALLOON]: aligning based on the balloon.
     * [ArrowConstraints.ALIGN_ANCHOR]: aligning based on the anchor.
     */
    fun setArrowConstraints(value: ArrowConstraints) = apply { this.arrowConstraints = value }

    /** sets is status bar is visible or not in your screen. */
    fun setIsStatusBarVisible(value: Boolean) = apply {
      this.isStatusBarVisible = value
    }

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

    /** sets the left padding of the arrow. */
    fun setArrowLeftPadding(@Dp value: Int): Builder = apply {
      this.arrowLeftPadding = context.dp2Px(value)
    }

    /** sets the right padding of the arrow. */
    fun setArrowRightPadding(@Dp value: Int): Builder = apply {
      this.arrowRightPadding = context.dp2Px(value)
    }

    /** sets the top padding of the arrow. */
    fun setArrowTopPadding(@Dp value: Int): Builder = apply {
      this.arrowTopPadding = context.dp2Px(value)
    }

    /** sets the bottom padding of the arrow. */
    fun setArrowBottomPadding(@Dp value: Int): Builder = apply {
      this.arrowBottomPadding = context.dp2Px(value)
    }

    /** sets the padding of the arrow when aligning anchor using with [ArrowConstraints.ALIGN_ANCHOR]. */
    fun setArrowAlignAnchorPadding(@Dp value: Int): Builder = apply {
      this.arrowAlignAnchorPadding = context.dp2Px(value)
    }

    /** sets the padding ratio of the arrow when aligning anchor using with [ArrowConstraints.ALIGN_ANCHOR]. */
    fun setArrowAlignAnchorPaddingRatio(value: Float): Builder = apply {
      this.arrowAlignAnchorPaddingRatio = value
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

    /** sets the corner radius of the popup using dimension resource. */
    fun setCornerRadiusResource(@DimenRes value: Int): Builder = apply {
      this.cornerRadius = context.dimen(value).toFloat()
    }

    /** sets the main text content of the popup. */
    fun setText(value: CharSequence): Builder = apply { this.text = value }

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

    /** sets whether the text will be parsed as HTML (using Html.fromHtml(..)) */
    fun setTextIsHtml(value: Boolean): Builder = apply { this.textIsHtml = value }

    /** sets the size of the main text content. */
    fun setTextSize(@Sp value: Float): Builder = apply { this.textSize = value }

    /** sets the typeface of the main text content. */
    fun setTextTypeface(value: Int): Builder = apply { this.textTypeface = value }

    /** sets the typeface of the main text content. */
    fun setTextTypeface(value: Typeface): Builder = apply { this.textTypefaceObject = value }

    /**
     * sets gravity of the text.
     * this only works when the width or setWidthRatio set explicitly.
     */
    fun setTextGravity(value: Int): Builder = apply {
      this.textGravity = value
    }

    /** applies [TextForm] attributes to the main text content. */
    fun setTextForm(value: TextForm): Builder = apply { this.textForm = value }

    /** sets the icon drawable of the popup. */
    fun setIconDrawable(value: Drawable?): Builder = apply { this.iconDrawable = value?.mutate() }

    /** sets the icon drawable of the popup using the resource. */
    fun setIconDrawableResource(@DrawableRes value: Int) = apply {
      this.iconDrawable = context.contextDrawable(value)?.mutate()
    }

    /** sets the icon gravity of the popup using the resource. */
    fun setIconGravity(value: IconGravity) = apply {
      this.iconGravity = value
    }

    /** sets the size of the icon drawable. */
    fun setIconSize(@Dp value: Int): Builder = apply { this.iconSize = context.dp2Px(value) }

    /** sets the size of the icon drawable using dimension resource. */
    fun setIconSizeResource(@DimenRes value: Int): Builder = apply {
      this.iconSize = context.dimen(value)
    }

    /** sets the color of the icon drawable. */
    fun setIconColor(@ColorInt value: Int): Builder = apply { this.iconColor = value }

    /** sets the color of the icon drawable using the resource color. */
    fun setIconColorResource(@ColorInt value: Int): Builder = apply {
      this.iconColor = context.contextColor(value)
    }

    /** sets the space between the icon and the main text content. */
    fun setIconSpace(@Dp value: Int): Builder = apply { this.iconSpace = context.dp2Px(value) }

    /** sets the space between the icon and the main text content using dimension resource. */
    fun setIconSpaceResource(@DimenRes value: Int): Builder = apply {
      this.iconSpace = context.dimen(value)
    }

    /** applies [IconForm] attributes to the icon. */
    fun setIconForm(value: IconForm): Builder = apply { this.iconForm = value }

    /** sets the alpha value to the popup. */
    fun setAlpha(@FloatRange(from = 0.0, to = 1.0) value: Float): Builder = apply {
      this.alpha = value
    }

    /** sets the elevation to the popup. */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    fun setElevation(@Dp value: Int): Builder = apply {
      this.elevation = context.dp2Px(value).toFloat()
    }

    /** sets the elevation to the popup using dimension resource. */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    fun setElevationResource(@DimenRes value: Int): Builder = apply {
      this.elevation = context.dimen(value).toFloat()
    }

    /** sets the custom layout resource to the popup content. */
    fun setLayout(@LayoutRes layoutRes: Int): Builder = apply { this.layoutRes = layoutRes }

    /** sets the custom layout view to the popup content. */
    fun setLayout(layout: View): Builder = apply { this.layout = layout }

    /**
     * sets the [LifecycleOwner] for dismissing automatically when the [LifecycleOwner] is destroyed.
     * It will prevents memory leak : [Avoid Memory Leak](https://github.com/skydoves/balloon#avoid-memory-leak)
     */
    fun setLifecycleOwner(value: LifecycleOwner?): Builder = apply { this.lifecycleOwner = value }

    /** sets the balloon showing animation using [BalloonAnimation]. */
    fun setBalloonAnimation(value: BalloonAnimation): Builder = apply {
      this.balloonAnimation = value
      if (value == BalloonAnimation.CIRCULAR) {
        setFocusable(false)
      }
    }

    /** sets the balloon showing animation using custom xml animation style. */
    fun setBalloonAnimationStyle(@StyleRes value: Int): Builder = apply {
      this.balloonAnimationStyle = value
    }

    /**
     * sets the duration of the circular animation.
     * this option only works with [BalloonAnimation.CIRCULAR] value in [setBalloonAnimation].
     */
    fun setCircularDuration(value: Long): Builder = apply {
      this.circularDuration = value
    }

    /** sets a [OnBalloonClickListener] to the popup. */
    fun setOnBalloonClickListener(value: OnBalloonClickListener): Builder = apply {
      this.onBalloonClickListener = value
    }

    /** sets a [OnBalloonDismissListener] to the popup. */
    fun setOnBalloonDismissListener(value: OnBalloonDismissListener): Builder = apply {
      this.onBalloonDismissListener = value
    }

    /** sets a [OnBalloonInitializedListener] to the popup. */
    fun setOnBalloonInitializedListener(value: OnBalloonInitializedListener): Builder = apply {
      this.onBalloonInitializedListener = value
    }

    /** sets a [OnBalloonOutsideTouchListener] to the popup. */
    fun setOnBalloonOutsideTouchListener(value: OnBalloonOutsideTouchListener): Builder = apply {
      this.onBalloonOutsideTouchListener = value
    }

    /** sets a [OnBalloonClickListener] to the popup using lambda. */
    fun setOnBalloonClickListener(unit: (View) -> Unit): Builder = apply {
      this.onBalloonClickListener = OnBalloonClickListener { view -> unit(view) }
    }

    /** sets a [OnBalloonDismissListener] to the popup using lambda. */
    fun setOnBalloonDismissListener(unit: () -> Unit): Builder = apply {
      this.onBalloonDismissListener = OnBalloonDismissListener { unit() }
    }

    /** sets a [OnBalloonInitializedListener] to the popup using lambda. */
    fun setOnBalloonInitializedListener(unit: (View) -> Unit): Builder = apply {
      this.onBalloonInitializedListener =
        OnBalloonInitializedListener { contentView -> unit(contentView) }
    }

    /** sets a [OnBalloonOutsideTouchListener] to the popup using lambda. */
    fun setOnBalloonOutsideTouchListener(unit: (View, MotionEvent) -> Unit): Builder = apply {
      this.onBalloonOutsideTouchListener =
        OnBalloonOutsideTouchListener { view, event -> unit(view, event) }
    }

    /** dismisses when touch outside. */
    fun setDismissWhenTouchOutside(value: Boolean): Builder = apply {
      this.dismissWhenTouchOutside = value
      if (!value) {
        setFocusable(value)
      }
    }

    /** dismisses when invoked show function again. */
    fun setDismissWhenShowAgain(value: Boolean): Builder = apply {
      this.dismissWhenShowAgain = value
    }

    /** dismisses when the popup clicked. */
    fun setDismissWhenClicked(value: Boolean): Builder = apply { this.dismissWhenClicked = value }

    /** dismisses when the [LifecycleOwner] be on paused. */
    fun setDismissWhenLifecycleOnPause(value: Boolean): Builder = apply {
      this.dismissWhenLifecycleOnPause = value
    }

    /** dismisses automatically some milliseconds later when the popup is shown. */
    fun setAutoDismissDuration(value: Long): Builder = apply { this.autoDismissDuration = value }

    /** sets the preference name for persisting showing times([showTimes]).  */
    fun setPreferenceName(value: String): Builder = apply { this.preferenceName = value }

    /** sets the show times. */
    fun setShowTime(value: Int): Builder = apply { this.showTimes = value }

    /** sets flag for enabling rtl support */
    fun isRtlSupport(value: Boolean): Builder = apply { this.isRtlSupport = value }

    /**
     * sets isFocusable option to the body window.
     * if true when the balloon is showing, can not touch other views and
     * onBackPressed will be fired to the balloon.
     * */
    fun setFocusable(value: Boolean): Builder = apply { this.isFocusable = value }

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
