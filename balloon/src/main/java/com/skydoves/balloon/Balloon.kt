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
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Point
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.method.MovementMethod
import android.util.LayoutDirection
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.PopupWindow
import androidx.annotation.AnimRes
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
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.ViewCompat
import androidx.core.view.forEach
import androidx.core.widget.ImageViewCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.skydoves.balloon.annotations.Dp
import com.skydoves.balloon.annotations.Sp
import com.skydoves.balloon.databinding.LayoutBalloonLibrarySkydovesBinding
import com.skydoves.balloon.databinding.LayoutBalloonOverlayLibrarySkydovesBinding
import com.skydoves.balloon.extensions.applyIconForm
import com.skydoves.balloon.extensions.applyTextForm
import com.skydoves.balloon.extensions.circularRevealed
import com.skydoves.balloon.extensions.circularUnRevealed
import com.skydoves.balloon.extensions.contextColor
import com.skydoves.balloon.extensions.contextDrawable
import com.skydoves.balloon.extensions.dimen
import com.skydoves.balloon.extensions.dimenPixel
import com.skydoves.balloon.extensions.displaySize
import com.skydoves.balloon.extensions.dp
import com.skydoves.balloon.extensions.getIntrinsicHeight
import com.skydoves.balloon.extensions.getStatusBarHeight
import com.skydoves.balloon.extensions.getSumOfIntrinsicWidth
import com.skydoves.balloon.extensions.getViewPointOnScreen
import com.skydoves.balloon.extensions.isExistHorizontalDrawable
import com.skydoves.balloon.extensions.isFinishing
import com.skydoves.balloon.extensions.px2Sp
import com.skydoves.balloon.extensions.runOnAfterSDK21
import com.skydoves.balloon.extensions.sumOfCompoundPadding
import com.skydoves.balloon.extensions.visible
import com.skydoves.balloon.overlay.BalloonOverlayAnimation
import com.skydoves.balloon.overlay.BalloonOverlayOval
import com.skydoves.balloon.overlay.BalloonOverlayShape
import kotlin.math.max

@DslMarker
internal annotation class BalloonInlineDsl

/**
 * Creates an instance of the [Balloon] by scope of the [Balloon.Builder] using kotlin dsl.
 *
 * @param context A context for creating resources of the [Balloon].
 * @param block A dsl scope lambda from the [Balloon.Builder].
 * */
@MainThread
@JvmSynthetic
@BalloonInlineDsl
inline fun createBalloon(context: Context, crossinline block: Balloon.Builder.() -> Unit): Balloon =
  Balloon.Builder(context).apply(block).build()

/**
 * Balloon implements a customizable tooltips popup with and arrow and animations.
 *
 * @see [Balloon](https://github.com/skydoves/balloon)
 *
 * @param context A context for creating and accessing internal resources.
 * @param builder A [Balloon.Builder] for creating an instance of the [Balloon].
 */
@Suppress("MemberVisibilityCanBePrivate")
class Balloon(
  private val context: Context,
  private val builder: Builder
) : LifecycleObserver {

  /** A main content view of the popup. */
  private val binding: LayoutBalloonLibrarySkydovesBinding =
    LayoutBalloonLibrarySkydovesBinding.inflate(LayoutInflater.from(context), null, false)

  /** An overlay view of the background for highlighting the popup and an anchor. */
  private val overlayBinding: LayoutBalloonOverlayLibrarySkydovesBinding =
    LayoutBalloonOverlayLibrarySkydovesBinding.inflate(LayoutInflater.from(context), null, false)

  /** A main content window of the popup. */
  private val bodyWindow: PopupWindow

  /** An overlay window of the background popup. */
  private val overlayWindow: PopupWindow

  /** Denotes the popup is showing or not. */
  var isShowing = false
    private set

  /** Denotes the popup is already destroyed internally. */
  private var destroyed: Boolean = false

  /** Interface definition for a callback to be invoked when a balloon view is initialized. */
  @JvmField
  @set:JvmSynthetic
  var onBalloonInitializedListener: OnBalloonInitializedListener? =
    builder.onBalloonInitializedListener

  /** A handler for running [autoDismissRunnable]. */
  private val handler: Handler by lazy(LazyThreadSafetyMode.NONE) {
    Handler(Looper.getMainLooper())
  }

  /** A runnable for dismissing the balloon with the [Builder.autoDismissDuration]. */
  private val autoDismissRunnable: AutoDismissRunnable by lazy(
    LazyThreadSafetyMode.NONE
  ) { AutoDismissRunnable(this) }

  /** A persistence helper for showing the popup a limited number of times. */
  private val balloonPersistence: BalloonPersistence by lazy(LazyThreadSafetyMode.NONE) {
    BalloonPersistence.getInstance(context)
  }

  init {
    this.bodyWindow = PopupWindow(
      binding.root,
      FrameLayout.LayoutParams.WRAP_CONTENT,
      FrameLayout.LayoutParams.WRAP_CONTENT
    )
    this.overlayWindow = PopupWindow(
      overlayBinding.root,
      ViewGroup.LayoutParams.MATCH_PARENT,
      ViewGroup.LayoutParams.MATCH_PARENT
    )
    createByBuilder()
  }

  private fun createByBuilder() {
    initializeBackground()
    initializeBalloonRoot()
    initializeBalloonWindow()
    initializeBalloonLayout()
    initializeBalloonContent()
    initializeBalloonOverlay()
    initializeBalloonListeners()

    adjustFitsSystemWindows(binding.root)

    if (builder.lifecycleOwner == null && context is LifecycleOwner) {
      builder.setLifecycleOwner(context)
      context.lifecycle.addObserver(this@Balloon)
    } else {
      builder.lifecycleOwner?.lifecycle?.addObserver(this@Balloon)
    }
  }

  private fun adjustFitsSystemWindows(parent: ViewGroup) {
    parent.fitsSystemWindows = false
    (0 until parent.childCount).map { parent.getChildAt(it) }.forEach { child ->
      child.fitsSystemWindows = false
      if (child is ViewGroup) {
        adjustFitsSystemWindows(child)
      }
    }
  }

  private fun getMinArrowPosition(): Float {
    return (builder.arrowSize.toFloat() * builder.arrowAlignAnchorPaddingRatio) +
      builder.arrowAlignAnchorPadding
  }

  private fun getDoubleArrowSize(): Int {
    return builder.arrowSize * 2
  }

  private fun initializeArrow(anchor: View) {
    with(binding.balloonArrow) {
      layoutParams = FrameLayout.LayoutParams(builder.arrowSize, builder.arrowSize)
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
      runOnAfterSDK21 {
        outlineProvider = ViewOutlineProvider.BOUNDS
      }
      binding.balloonCard.post {
        onBalloonInitializedListener?.onBalloonInitialized(getContentView())

        adjustArrowOrientationByRules(anchor)

        @SuppressLint("NewApi")
        when (builder.arrowOrientation) {
          ArrowOrientation.BOTTOM -> {
            rotation = 180f
            x = getArrowConstraintPositionX(anchor)
            y = binding.balloonCard.y + binding.balloonCard.height - SIZE_ARROW_BOUNDARY
            ViewCompat.setElevation(this, builder.arrowElevation)
            if (builder.arrowColorMatchBalloon) {
              foreground = BitmapDrawable(
                resources,
                adjustArrowColorByMatchingCardBackground(
                  this, x,
                  binding.balloonCard.height.toFloat()
                )
              )
            }
          }
          ArrowOrientation.TOP -> {
            rotation = 0f
            x = getArrowConstraintPositionX(anchor)
            y = binding.balloonCard.y - builder.arrowSize + SIZE_ARROW_BOUNDARY
            if (builder.arrowColorMatchBalloon) {
              foreground =
                BitmapDrawable(resources, adjustArrowColorByMatchingCardBackground(this, x, 0f))
            }
          }
          ArrowOrientation.LEFT -> {
            rotation = -90f
            x = binding.balloonCard.x - builder.arrowSize + SIZE_ARROW_BOUNDARY
            y = getArrowConstraintPositionY(anchor)
            if (builder.arrowColorMatchBalloon) {
              foreground =
                BitmapDrawable(resources, adjustArrowColorByMatchingCardBackground(this, 0f, y))
            }
          }
          ArrowOrientation.RIGHT -> {
            rotation = 90f
            x = binding.balloonCard.x + binding.balloonCard.width - SIZE_ARROW_BOUNDARY
            y = getArrowConstraintPositionY(anchor)
            if (builder.arrowColorMatchBalloon) {
              foreground = BitmapDrawable(
                resources,
                adjustArrowColorByMatchingCardBackground(
                  this, binding.balloonCard.width.toFloat(),
                  y
                )
              )
            }
          }
        }
        visible(builder.isVisibleArrow)
      }
    }
  }

  /**
   * Calculate the color at arrow position from balloonCard. The color is then set as a foreground to the arrow.
   *
   * @param imageView the arrow imageview containing the drawable.
   * @param x x position of the point where the middle of the arrow is connected to the balloon
   * @param y y position of the point where the middle of the arrow is connected to the balloon
   *
   * @throws IllegalArgumentException Throws an exception when the arrow is attached outside the balloon.
   *
   */
  private fun adjustArrowColorByMatchingCardBackground(
    imageView: AppCompatImageView,
    x: Float,
    y: Float
  ): Bitmap {
    imageView.setColorFilter(builder.backgroundColor, PorterDuff.Mode.SRC_IN)
    val oldBitmap = drawableToBitmap(
      imageView.drawable, imageView.drawable.intrinsicWidth,
      imageView.drawable.intrinsicHeight
    )
    val colors: Pair<Int, Int>
    try {
      colors = getColorsFromBalloonCard(x, y)
    } catch (e: IllegalArgumentException) {
      throw IllegalArgumentException(
        "Arrow attached outside balloon. Could not get a matching color."
      )
    }
    val startColor = colors.first
    val endColor = colors.second

    val updatedBitmap =
      Bitmap.createBitmap(oldBitmap.width, oldBitmap.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(updatedBitmap)
    canvas.drawBitmap(oldBitmap, 0f, 0f, null)
    val paint = Paint()
    val shader: LinearGradient = when (builder.arrowOrientation) {
      ArrowOrientation.BOTTOM, ArrowOrientation.LEFT -> {
        LinearGradient(
          oldBitmap.width.toFloat() / 2 - builder.arrowSize / 2, 0f,
          oldBitmap.width.toFloat(), 0f, startColor, endColor, Shader.TileMode.CLAMP
        )
      }
      ArrowOrientation.RIGHT, ArrowOrientation.TOP -> {
        LinearGradient(
          oldBitmap.width.toFloat() / 2 + builder.arrowSize / 2, 0f, 0f, 0f,
          startColor, endColor, Shader.TileMode.CLAMP
        )
      }
    }
    paint.shader = shader
    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    canvas.drawRect(0f, 0f, oldBitmap.width.toFloat(), oldBitmap.height.toFloat(), paint)
    imageView.setColorFilter(Color.TRANSPARENT, PorterDuff.Mode.SRC_IN)
    return updatedBitmap
  }

  private fun getColorsFromBalloonCard(x: Float, y: Float): Pair<Int, Int> {
    val bitmap = drawableToBitmap(
      binding.balloonCard.background, binding.balloonCard.width + 1,
      binding.balloonCard.height + 1
    )
    val startColor: Int
    val endColor: Int
    when (builder.arrowOrientation) {
      ArrowOrientation.BOTTOM, ArrowOrientation.TOP -> {
        startColor = bitmap.getPixel((x + builder.arrowSize / 2f).toInt(), y.toInt())
        endColor = bitmap.getPixel((x - builder.arrowSize / 2f).toInt(), y.toInt())
      }
      ArrowOrientation.LEFT, ArrowOrientation.RIGHT -> {
        startColor = bitmap.getPixel(x.toInt(), (y + builder.arrowSize / 2f).toInt())
        endColor = bitmap.getPixel(x.toInt(), (y - builder.arrowSize / 2f).toInt())
      }
    }
    return Pair(startColor, endColor)
  }

  private fun drawableToBitmap(drawable: Drawable, width: Int, height: Int): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
  }

  /**
   * Adjust the orientation of the arrow depending on the [ArrowOrientationRules].
   *
   * @param anchor A target anchor to be shown under the balloon.
   */
  private fun adjustArrowOrientationByRules(anchor: View) {
    if (builder.arrowOrientationRules == ArrowOrientationRules.ALIGN_FIXED) return

    val anchorRect = Rect()
    anchor.getGlobalVisibleRect(anchorRect)

    val location: IntArray = intArrayOf(0, 0)
    bodyWindow.contentView.getLocationOnScreen(location)

    if (builder.arrowOrientation == ArrowOrientation.TOP &&
      location[1] < anchorRect.bottom
    ) {
      builder.setArrowOrientation(ArrowOrientation.BOTTOM)
    } else if (builder.arrowOrientation == ArrowOrientation.BOTTOM &&
      location[1] > anchorRect.top
    ) {
      builder.setArrowOrientation(ArrowOrientation.TOP)
    }

    initializeBalloonContent()
  }

  private fun getArrowConstraintPositionX(anchor: View): Float {
    val balloonX: Int = binding.balloonContent.getViewPointOnScreen().x
    val anchorX: Int = anchor.getViewPointOnScreen().x
    val minPosition = getMinArrowPosition()
    val maxPosition = getMeasuredWidth() - minPosition - builder.marginRight - builder.marginLeft
    val arrowHalfSize = builder.arrowSize / 2f
    return when (builder.arrowPositionRules) {
      ArrowPositionRules.ALIGN_BALLOON -> binding.balloonWrapper.width * builder.arrowPosition - arrowHalfSize
      ArrowPositionRules.ALIGN_ANCHOR -> {
        when {
          anchorX + anchor.width < balloonX -> minPosition
          balloonX + getMeasuredWidth() < anchorX -> maxPosition
          else -> {
            val position =
              (anchor.width) * builder.arrowPosition + anchorX - balloonX - arrowHalfSize
            when {
              position <= getDoubleArrowSize() -> minPosition
              position > getMeasuredWidth() - getDoubleArrowSize() -> maxPosition
              else -> position
            }
          }
        }
      }
    }
  }

  private fun getArrowConstraintPositionY(anchor: View): Float {
    val statusBarHeight = anchor.getStatusBarHeight(builder.isStatusBarVisible)
    val balloonY: Int = binding.balloonContent.getViewPointOnScreen().y - statusBarHeight
    val anchorY: Int = anchor.getViewPointOnScreen().y - statusBarHeight
    val minPosition = getMinArrowPosition()
    val maxPosition = getMeasuredHeight() - minPosition - builder.marginTop - builder.marginBottom
    val arrowHalfSize = builder.arrowSize / 2
    return when (builder.arrowPositionRules) {
      ArrowPositionRules.ALIGN_BALLOON -> binding.balloonWrapper.height * builder.arrowPosition - arrowHalfSize
      ArrowPositionRules.ALIGN_ANCHOR -> {
        when {
          anchorY + anchor.height < balloonY -> minPosition
          balloonY + getMeasuredHeight() < anchorY -> maxPosition
          else -> {
            val position =
              (anchor.height) * builder.arrowPosition + anchorY - balloonY - arrowHalfSize
            when {
              position <= getDoubleArrowSize() -> minPosition
              position > getMeasuredHeight() - getDoubleArrowSize() -> maxPosition
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
      radius = builder.cornerRadius
      ViewCompat.setElevation(this, builder.elevation)
      background = builder.backgroundDrawable ?: GradientDrawable().apply {
        setColor(builder.backgroundColor)
        cornerRadius = builder.cornerRadius
      }
      setPadding(
        builder.paddingLeft,
        builder.paddingTop,
        builder.paddingRight,
        builder.paddingBottom
      )
    }
  }

  private fun initializeBalloonWindow() {
    with(this.bodyWindow) {
      isOutsideTouchable = true
      isFocusable = builder.isFocusable
      setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
      runOnAfterSDK21 {
        elevation = builder.elevation
      }
    }
  }

  private fun initializeBalloonListeners() {
    setOnBalloonClickListener(builder.onBalloonClickListener)
    setOnBalloonDismissListener(builder.onBalloonDismissListener)
    setOnBalloonOutsideTouchListener(builder.onBalloonOutsideTouchListener)
    setOnBalloonTouchListener(builder.onBalloonTouchListener)
    setOnBalloonOverlayClickListener(builder.onBalloonOverlayClickListener)
    setOnBalloonOverlayTouchListener(builder.onBalloonOverlayTouchListener)
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
    val paddingSize = builder.arrowSize - SIZE_ARROW_BOUNDARY
    val elevation = builder.elevation.toInt()
    with(binding.balloonContent) {
      when (builder.arrowOrientation) {
        ArrowOrientation.LEFT -> setPadding(paddingSize, elevation, paddingSize, elevation)
        ArrowOrientation.RIGHT -> setPadding(paddingSize, elevation, paddingSize, elevation)
        ArrowOrientation.TOP ->
          setPadding(elevation, paddingSize, elevation, paddingSize.coerceAtLeast(elevation))
        ArrowOrientation.BOTTOM ->
          setPadding(elevation, paddingSize, elevation, paddingSize.coerceAtLeast(elevation))
      }
    }
  }

  private fun initializeIcon() {
    with(binding.balloonText) {
      builder.iconForm?.let {
        applyIconForm(it)
      } ?: applyIconForm(
        iconForm(context) {
          setDrawable(builder.iconDrawable)
          setIconWidth(builder.iconWidth)
          setIconHeight(builder.iconHeight)
          setIconColor(builder.iconColor)
          setIconSpace(builder.iconSpace)
          setDrawableGravity(builder.iconGravity)
        }
      )
      isRtlSupport(builder.isRtlLayout)
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
          setMovementMethod(builder.movementMethod)
        }
      )
      measureTextWidth(this, binding.balloonCard)
    }
  }

  private fun initializeBalloonLayout() {
    if (hasCustomLayout()) {
      initializeCustomLayout()
    } else {
      initializeIcon()
      initializeText()
    }
  }

  /** Check the [Balloon.Builder] has a custom layout [Balloon.Builder.layoutRes] or [Balloon.Builder.layout]. */
  private fun hasCustomLayout(): Boolean {
    return builder.layoutRes != null || builder.layout != null
  }

  /** Initializes the Balloon content using the custom layout. */
  private fun initializeCustomLayout() {
    val layout = builder.layoutRes?.let {
      LayoutInflater.from(context).inflate(it, binding.balloonCard, false)
    } ?: builder.layout ?: throw IllegalArgumentException("The custom layout is null.")
    binding.balloonCard.removeAllViews()
    binding.balloonCard.addView(layout)
    traverseAndMeasureTextWidth(binding.balloonCard)
  }

  private fun initializeBalloonOverlay() {
    if (builder.isVisibleOverlay) with(overlayBinding.balloonOverlayView) {
      overlayColor = builder.overlayColor
      overlayPadding = builder.overlayPadding
      overlayPosition = builder.overlayPosition
      balloonOverlayShape = builder.overlayShape
      overlayPaddingColor = builder.overlayPaddingColor
      overlayWindow.isClippingEnabled = false
    }
  }

  private fun applyBalloonAnimation() {
    if (builder.balloonAnimationStyle == NO_INT_VALUE) {
      when (builder.balloonAnimation) {
        BalloonAnimation.ELASTIC -> bodyWindow.animationStyle = R.style.Elastic_Balloon_Library
        BalloonAnimation.CIRCULAR -> {
          bodyWindow.contentView.circularRevealed(builder.circularDuration)
          bodyWindow.animationStyle = R.style.NormalDispose_Balloon_Library
        }
        BalloonAnimation.FADE -> bodyWindow.animationStyle = R.style.Fade_Balloon_Library
        BalloonAnimation.OVERSHOOT -> bodyWindow.animationStyle = R.style.Overshoot_Balloon_Library
        BalloonAnimation.NONE -> bodyWindow.animationStyle = R.style.Normal_Balloon_Library
      }
    } else {
      bodyWindow.animationStyle = builder.balloonAnimationStyle
    }
  }

  private fun applyBalloonOverlayAnimation() {
    if (builder.balloonOverlayAnimationStyle == NO_INT_VALUE) {
      when (builder.balloonOverlayAnimation) {
        BalloonOverlayAnimation.FADE -> overlayWindow.animationStyle = R.style.Fade_Balloon_Library
        else -> overlayWindow.animationStyle = R.style.Normal_Balloon_Library
      }
    } else {
      overlayWindow.animationStyle = builder.balloonAnimationStyle
    }
  }

  private fun getBalloonHighlightAnimation(): Animation? {
    val animRes = if (builder.balloonHighlightAnimationStyle == NO_INT_VALUE) {
      when (builder.balloonHighlightAnimation) {
        BalloonHighlightAnimation.HEARTBEAT -> {
          if (builder.isVisibleArrow) {
            when (builder.arrowOrientation) {
              ArrowOrientation.TOP -> R.anim.heartbeat_bottom_balloon_library
              ArrowOrientation.BOTTOM -> R.anim.heartbeat_top_balloon_library
              ArrowOrientation.LEFT -> R.anim.heartbeat_right_balloon_library
              ArrowOrientation.RIGHT -> R.anim.heartbeat_left_balloon_library
            }
          } else {
            R.anim.heartbeat_center_balloon_library
          }
        }
        else -> return null
      }
    } else {
      builder.balloonHighlightAnimationStyle
    }

    return AnimationUtils.loadAnimation(context, animRes)
  }

  private fun startBalloonHighlightAnimation() {
    binding.balloon.post {
      Handler(Looper.getMainLooper()).postDelayed(
        {
          getBalloonHighlightAnimation()?.let { animation ->
            binding.balloon.startAnimation(animation)
          }
        },
        builder.balloonHighlightAnimationStartDelay
      )
    }
  }

  private fun stopBalloonHighlightAnimation() {
    binding.balloon.apply {
      animation?.apply {
        cancel()
        reset()
      }
      clearAnimation()
    }
  }

  /**
   * Checks if the balloon should show up.
   */
  fun shouldShowUp(): Boolean {
    return this.builder.preferenceName?.let {
      this.balloonPersistence.shouldShowUp(it, builder.showTimes)
    } ?: true
  }

  /**
   * Shows [Balloon] tooltips on the [anchor] with some initializations related to arrow, content, and overlay.
   * The balloon will be shown with the [overlayWindow] if the anchorView's parent window is in a valid state.
   * The size of the content will be measured internally, and it will affect calculating the popup size.
   *
   * @param block A lambda block for showing the [bodyWindow].
   */
  @MainThread
  private inline fun show(anchor: View, crossinline block: () -> Unit) {
    if (!isShowing &&
      // If the balloon is already destroyed depending on the lifecycle,
      // We should not allow showing the popupWindow, it's related to `relay()` method. (#46)
      !destroyed &&
      // We should check the current Activity is running.
      // If the Activity is finishing, we can't attach the popupWindow to the Activity's window. (#92)
      !context.isFinishing() &&
      // We should check the contentView is already attached to the decorView or backgroundView in the popupWindow.
      // Sometimes there is a concurrency issue between show and dismiss the popupWindow. (#149)
      bodyWindow.contentView.parent == null &&
      // we should check the anchor view is attached to the parent's window.
      ViewCompat.isAttachedToWindow(anchor)
    ) {
      anchor.post {
        this.builder.preferenceName?.let {
          if (balloonPersistence.shouldShowUp(it, builder.showTimes)) {
            balloonPersistence.putIncrementedCounts(it)
          } else {
            this.builder.runIfReachedShowCounts?.invoke()
            return@post
          }
        }

        this.isShowing = true

        val dismissDelay = this.builder.autoDismissDuration
        if (dismissDelay != NO_LONG_VALUE) {
          dismissWithDelay(dismissDelay)
        }

        if (hasCustomLayout()) {
          traverseAndMeasureTextWidth(binding.balloonCard)
        } else {
          measureTextWidth(binding.balloonText, binding.balloonCard)
        }
        this.binding.root.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        this.bodyWindow.width = getMeasuredWidth()
        this.bodyWindow.height = getMeasuredHeight()
        this.binding.balloonText.layoutParams = FrameLayout.LayoutParams(
          FrameLayout.LayoutParams.MATCH_PARENT,
          FrameLayout.LayoutParams.MATCH_PARENT
        )
        initializeArrow(anchor)
        initializeBalloonContent()

        applyBalloonOverlayAnimation()
        showOverlayWindow(anchor)

        applyBalloonAnimation()
        startBalloonHighlightAnimation()
        block()
      }
    } else if (builder.dismissWhenShowAgain) {
      dismiss()
    }
  }

  private fun showOverlayWindow(anchor: View) {
    if (builder.isVisibleOverlay) {
      overlayBinding.balloonOverlayView.anchorView = anchor
      overlayWindow.showAtLocation(anchor, Gravity.CENTER, 0, 0)
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

  /**
   * Shows the balloon on the center of an anchor view.
   *
   * @param anchor A target view which popup will be shown to.
   */
  fun show(anchor: View) {
    show(anchor) {
      bodyWindow.showAsDropDown(
        anchor,
        builder.supportRtlLayoutFactor * ((anchor.measuredWidth / 2) - (getMeasuredWidth() / 2)),
        -getMeasuredHeight() - (anchor.measuredHeight / 2)
      )
    }
  }

  /**
   * Shows the balloon on the center of an anchor view.
   *
   * @param anchor A target view which popup will be shown to.
   * @param xOff A horizontal offset from the anchor in pixels.
   * @param yOff A vertical offset from the anchor in pixels.
   */
  fun show(anchor: View, xOff: Int, yOff: Int) {
    show(anchor) { bodyWindow.showAsDropDown(anchor, xOff, yOff) }
  }

  /**
   * Shows the balloon on the center of an anchor view and shows the next balloon sequentially.
   * This function returns the next balloon.
   *
   * @param balloon A next [Balloon] that will be shown sequentially after dismissing this popup.
   * @param anchor A target view which popup will be shown to.
   * @param xOff A horizontal offset from the anchor in pixels.
   * @param yOff A vertical offset from the anchor in pixels.
   *
   * @return A next [balloon].
   *
   * @see [Show sequentially](https://github.com/skydoves/Balloon#show-sequentially)
   */
  @JvmOverloads
  fun relayShow(balloon: Balloon, anchor: View, xOff: Int = 0, yOff: Int = 0) =
    relay(balloon) { it.show(anchor, xOff, yOff) }

  /**
   * Shows the balloon on an anchor view as drop down and shows the next balloon sequentially.
   * This function returns the next balloon.
   *
   * @param balloon A next [Balloon] that will be shown sequentially after dismissing this popup.
   * @param anchor A target view which popup will be shown to.
   *
   * @return A next [balloon].
   *
   * @see [Show sequentially](https://github.com/skydoves/Balloon#show-sequentially)
   */
  fun relayShowAsDropDown(balloon: Balloon, anchor: View) =
    relay(balloon) { it.showAsDropDown(anchor) }

  /**
   * Shows the balloon on an anchor view as drop down with x-off and y-off.
   *
   * @param anchor A target view which popup will be shown to.
   * @param xOff A horizontal offset from the anchor in pixels.
   * @param yOff A vertical offset from the anchor in pixels.
   */
  @JvmOverloads
  fun showAsDropDown(anchor: View, xOff: Int = 0, yOff: Int = 0) {
    show(anchor) { bodyWindow.showAsDropDown(anchor, xOff, yOff) }
  }

  /**
   * Shows the balloon on an anchor view as drop down with x-off and y-off and shows the next balloon sequentially.
   * This function returns the next balloon.
   *
   * @param balloon A next [Balloon] that will be shown sequentially after dismissing this popup.
   * @param anchor A target view which popup will be shown to.
   * @param xOff A horizontal offset from the anchor in pixels.
   * @param yOff A vertical offset from the anchor in pixels.
   *
   * @return A next [balloon].
   *
   * @see [Show sequentially](https://github.com/skydoves/Balloon#show-sequentially)
   */
  fun relayShowAsDropDown(balloon: Balloon, anchor: View, xOff: Int, yOff: Int) =
    relay(balloon) { it.showAsDropDown(anchor, xOff, yOff) }

  /**
   * Shows the balloon on an anchor view as the top alignment with x-off and y-off.
   *
   * @param anchor A target view which popup will be shown to.
   * @param xOff A horizontal offset from the anchor in pixels.
   * @param yOff A vertical offset from the anchor in pixels.
   */
  @JvmOverloads
  fun showAlignTop(anchor: View, xOff: Int = 0, yOff: Int = 0) {
    show(anchor) {
      bodyWindow.showAsDropDown(
        anchor,
        builder.supportRtlLayoutFactor * ((anchor.measuredWidth / 2) - (getMeasuredWidth() / 2) + xOff),
        -getMeasuredHeight() - anchor.measuredHeight + yOff
      )
    }
  }

  /**
   * Shows the balloon on an anchor view as the top alignment with x-off and y-off and shows the next balloon sequentially.
   * This function returns the next balloon.
   *
   * @param balloon A next [Balloon] that will be shown sequentially after dismissing this popup.
   * @param anchor A target view which popup will be shown to.
   * @param xOff A horizontal offset from the anchor in pixels.
   * @param yOff A vertical offset from the anchor in pixels.
   *
   * @return A next [balloon].
   *
   * @see [Show sequentially](https://github.com/skydoves/Balloon#show-sequentially)
   */
  @JvmOverloads
  fun relayShowAlignTop(balloon: Balloon, anchor: View, xOff: Int = 0, yOff: Int = 0) =
    relay(balloon) { it.showAlignTop(anchor, xOff, yOff) }

  /**
   * Shows the balloon on an anchor view as the bottom alignment with x-off and y-off.
   *
   * @param anchor A target view which popup will be shown to.
   * @param xOff A horizontal offset from the anchor in pixels.
   * @param yOff A vertical offset from the anchor in pixels.
   */
  @JvmOverloads
  fun showAlignBottom(anchor: View, xOff: Int = 0, yOff: Int = 0) {
    show(anchor) {
      bodyWindow.showAsDropDown(
        anchor,
        builder.supportRtlLayoutFactor * ((anchor.measuredWidth / 2) - (getMeasuredWidth() / 2) + xOff),
        yOff
      )
    }
  }

  /**
   * Shows the balloon on an anchor view as the bottom alignment with x-off and y-off
   * and shows the next balloon sequentially.
   * This function returns the next balloon.
   *
   * @param balloon A next [Balloon] that will be shown sequentially after dismissing this popup.
   * @param anchor A target view which popup will be shown to.
   * @param xOff A horizontal offset from the anchor in pixels.
   * @param yOff A vertical offset from the anchor in pixels.
   *
   * @return A next [balloon].
   *
   * @see [Show sequentially](https://github.com/skydoves/Balloon#show-sequentially)
   */
  @JvmOverloads
  fun relayShowAlignBottom(balloon: Balloon, anchor: View, xOff: Int = 0, yOff: Int = 0) =
    relay(balloon) { it.showAlignBottom(anchor, xOff, yOff) }

  /**
   * Shows the balloon on an anchor view as the right alignment with x-off and y-off.
   *
   * @param anchor A target view which popup will be shown to.
   * @param xOff A horizontal offset from the anchor in pixels.
   * @param yOff A vertical offset from the anchor in pixels.
   */
  @JvmOverloads
  fun showAlignRight(anchor: View, xOff: Int = 0, yOff: Int = 0) {
    show(anchor) {
      bodyWindow.showAsDropDown(
        anchor,
        anchor.measuredWidth + xOff,
        -(getMeasuredHeight() / 2) - (anchor.measuredHeight / 2) + yOff
      )
    }
  }

  /**
   * Shows the balloon on an anchor view as the right alignment with x-off and y-off
   * and shows the next balloon sequentially.
   * This function returns the next balloon.
   *
   * @param balloon A next [Balloon] that will be shown sequentially after dismissing this popup.
   * @param anchor A target view which popup will be shown to.
   * @param xOff A horizontal offset from the anchor in pixels.
   * @param yOff A vertical offset from the anchor in pixels.
   *
   * @return A next [balloon].
   *
   * @see [Show sequentially](https://github.com/skydoves/Balloon#show-sequentially)
   */
  @JvmOverloads
  fun relayShowAlignRight(balloon: Balloon, anchor: View, xOff: Int = 0, yOff: Int = 0) = relay(
    balloon
  ) {
    it.showAlignRight(anchor, xOff, yOff)
  }

  /**
   * Shows the balloon on an anchor view as the left alignment with x-off and y-off.
   *
   * @param anchor A target view which popup will be shown to.
   * @param xOff A horizontal offset from the anchor in pixels.
   * @param yOff A vertical offset from the anchor in pixels.
   */
  @JvmOverloads
  fun showAlignLeft(anchor: View, xOff: Int = 0, yOff: Int = 0) {
    show(anchor) {
      bodyWindow.showAsDropDown(
        anchor,
        -(getMeasuredWidth()) + xOff,
        -(getMeasuredHeight() / 2) - (anchor.measuredHeight / 2) + yOff
      )
    }
  }

  /**
   * Shows the balloon on an anchor view as the left alignment with x-off and y-off
   * and shows the next balloon sequentially.
   * This function returns the next balloon.
   *
   * @param balloon A next [Balloon] that will be shown sequentially after dismissing this popup.
   * @param anchor A target view which popup will be shown to.
   * @param xOff A horizontal offset from the anchor in pixels.
   * @param yOff A vertical offset from the anchor in pixels.
   *
   * @return A next [balloon].
   *
   * @see [Show sequentially](https://github.com/skydoves/Balloon#show-sequentially)
   */
  @JvmOverloads
  fun relayShowAlignLeft(balloon: Balloon, anchor: View, xOff: Int = 0, yOff: Int = 0) =
    relay(balloon) { it.showAlignLeft(anchor, xOff, yOff) }

  /**
   * updates popup and arrow position of the popup based on
   * a new target anchor view with additional x-off and y-off.
   *
   * @param anchor A target view which popup will be shown to.
   * @param xOff A horizontal offset from the anchor in pixels.
   * @param yOff A vertical offset from the anchor in pixels.
   */
  @JvmOverloads
  fun update(anchor: View, xOff: Int = 0, yOff: Int = 0) {
    update(anchor = anchor) {
      this.bodyWindow.update(anchor, xOff, yOff, getMeasuredWidth(), getMeasuredHeight())
      if (builder.isVisibleOverlay) {
        overlayBinding.balloonOverlayView.forceInvalidate()
      }
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
        this.overlayWindow.dismiss()
        this.handler.removeCallbacks(autoDismissRunnable)
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
  fun dismissWithDelay(delay: Long) =
    handler.postDelayed(autoDismissRunnable, delay)

  /** sets a [OnBalloonClickListener] to the popup. */
  fun setOnBalloonClickListener(onBalloonClickListener: OnBalloonClickListener?) {
    this.binding.balloonWrapper.setOnClickListener {
      onBalloonClickListener?.onBalloonClick(it)
      if (builder.dismissWhenClicked) dismiss()
    }
  }

  /** sets a [OnBalloonClickListener] to the popup using lambda. */
  @JvmSynthetic
  fun setOnBalloonClickListener(block: (View) -> Unit) {
    setOnBalloonClickListener(OnBalloonClickListener(block))
  }

  /**
   * sets a [OnBalloonInitializedListener] to the popup.
   * The [OnBalloonInitializedListener.onBalloonInitialized] will be invoked when inflating the
   * body content of the balloon is finished.
   */
  fun setOnBalloonInitializedListener(onBalloonInitializedListener: OnBalloonInitializedListener?) {
    this.onBalloonInitializedListener = onBalloonInitializedListener
  }

  /**
   * sets a [OnBalloonInitializedListener] to the popup using a lambda.
   * The [OnBalloonInitializedListener.onBalloonInitialized] will be invoked when inflating the
   * body content of the balloon is finished.
   */
  @JvmSynthetic
  fun setOnBalloonInitializedListener(block: (View) -> Unit) {
    setOnBalloonInitializedListener(OnBalloonInitializedListener(block))
  }

  /** sets a [OnBalloonDismissListener] to the popup. */
  fun setOnBalloonDismissListener(onBalloonDismissListener: OnBalloonDismissListener?) {
    this.bodyWindow.setOnDismissListener {
      stopBalloonHighlightAnimation()
      this@Balloon.dismiss()
      onBalloonDismissListener?.onBalloonDismiss()
    }
  }

  /** sets a [OnBalloonDismissListener] to the popup using lambda. */
  @JvmSynthetic
  fun setOnBalloonDismissListener(block: () -> Unit) {
    setOnBalloonDismissListener(OnBalloonDismissListener(block))
  }

  /** sets a [OnBalloonOutsideTouchListener] to the popup. */
  fun setOnBalloonOutsideTouchListener(onBalloonOutsideTouchListener: OnBalloonOutsideTouchListener?) {
    this.bodyWindow.setTouchInterceptor(
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

  /** sets a [OnBalloonOutsideTouchListener] to the popup using lambda. */
  @JvmSynthetic
  fun setOnBalloonOutsideTouchListener(block: (View, MotionEvent) -> Unit) {
    setOnBalloonOutsideTouchListener(
      OnBalloonOutsideTouchListener(block)
    )
  }

  /** sets a [View.OnTouchListener] to the popup. */
  fun setOnBalloonTouchListener(onTouchListener: View.OnTouchListener?) {
    if (onTouchListener != null) {
      this.bodyWindow.setTouchInterceptor(onTouchListener)
    }
  }

  /** sets a [View.OnTouchListener] to the overlay popup */
  fun setOnBalloonOverlayTouchListener(onTouchListener: View.OnTouchListener?) {
    if (onTouchListener != null) {
      this.overlayWindow.setTouchInterceptor(onTouchListener)
    }
  }

  /** sets a [View.OnTouchListener] to the overlay popup using lambda. */
  fun setOnBalloonOverlayTouchListener(block: (View, MotionEvent) -> Boolean) {
    setOnBalloonOverlayTouchListener(
      View.OnTouchListener(block)
    )
  }

  /** sets a [OnBalloonOverlayClickListener] to the overlay popup. */
  fun setOnBalloonOverlayClickListener(onBalloonOverlayClickListener: OnBalloonOverlayClickListener?) {
    this.overlayBinding.root.setOnClickListener {
      onBalloonOverlayClickListener?.onBalloonOverlayClick()
      if (builder.dismissWhenOverlayClicked) dismiss()
    }
  }

  /** sets a [OnBalloonOverlayClickListener] to the overlay popup using lambda. */
  @JvmSynthetic
  fun setOnBalloonOverlayClickListener(block: () -> Unit) {
    setOnBalloonOverlayClickListener(
      OnBalloonOverlayClickListener(block)
    )
  }

  /** gets measured width size of the balloon popup. */
  fun getMeasuredWidth(): Int {
    val displayWidth = context.displaySize().x
    return when {
      builder.widthRatio != NO_Float_VALUE ->
        (displayWidth * builder.widthRatio).toInt()
      builder.minWidthRatio != NO_Float_VALUE || builder.maxWidthRatio != NO_Float_VALUE -> {
        val max = if (builder.maxWidthRatio != NO_Float_VALUE) builder.maxWidthRatio else 1f
        binding.root.measuredWidth.coerceIn((displayWidth * builder.minWidthRatio).toInt(), (displayWidth * max).toInt())
      }
      builder.width != BalloonSizeSpec.WRAP -> builder.width.coerceAtMost(displayWidth)
      else -> binding.root.measuredWidth.coerceIn(builder.minWidth, builder.maxWidth)
    }
  }

  /**
   * Measures the width of a [AppCompatTextView] and set the measured with.
   * If the width of the parent XML layout is the `WRAP_CONTENT`, and the width of [AppCompatTextView]
   * in the parent layout is `WRAP_CONTENT`, this method will measure the size of the width exactly.
   *
   * @param textView a target textView for measuring text width.
   */
  private fun measureTextWidth(textView: AppCompatTextView, rootView: View) {
    with(textView) {
      var measuredTextWidth = textView.paint.measureText(textView.text.toString()).toInt()
      if (compoundDrawablesRelative.isExistHorizontalDrawable()) {
        minHeight = compoundDrawablesRelative.getIntrinsicHeight()
        measuredTextWidth += compoundDrawablesRelative.getSumOfIntrinsicWidth() + sumOfCompoundPadding
      } else if (compoundDrawables.isExistHorizontalDrawable()) {
        minHeight = compoundDrawables.getIntrinsicHeight()
        measuredTextWidth += compoundDrawables.getSumOfIntrinsicWidth() + sumOfCompoundPadding
      }
      maxWidth = getMeasuredTextWidth(measuredTextWidth, rootView)
    }
  }

  /**
   * Traverse a [ViewGroup]'s view hierarchy and measure each [AppCompatTextView] for measuring
   * the specific height of the [AppCompatTextView] and calculating the proper height size of the balloon.
   *
   * @param parent a parent view for traversing and measuring.
   */
  private fun traverseAndMeasureTextWidth(parent: ViewGroup) {
    parent.forEach { child ->
      if (child is AppCompatTextView) {
        measureTextWidth(child, parent)
      } else if (child is ViewGroup) {
        traverseAndMeasureTextWidth(child)
      }
    }
  }

  /** gets measured width size of the balloon popup text label. */
  private fun getMeasuredTextWidth(measuredWidth: Int, rootView: View): Int {
    val displayWidth = context.displaySize().x
    val spaces = rootView.paddingLeft + rootView.paddingRight + if (builder.iconDrawable != null) {
      builder.iconWidth + builder.iconSpace
    } else 0 + builder.marginRight + builder.marginLeft + (builder.arrowSize * 2)
    val maxTextWidth = displayWidth - spaces

    return when {
      builder.widthRatio != NO_Float_VALUE ->
        (displayWidth * builder.widthRatio).toInt() - spaces
      builder.minWidthRatio != NO_Float_VALUE || builder.maxWidthRatio != NO_Float_VALUE -> {
        val max = if (builder.maxWidthRatio != NO_Float_VALUE) builder.maxWidthRatio else 1f
        measuredWidth.coerceIn(
          (displayWidth * builder.minWidthRatio).toInt(),
          (displayWidth * max).toInt()
        ) - spaces
      }
      builder.width != BalloonSizeSpec.WRAP && builder.width <= displayWidth ->
        builder.width - spaces
      else -> measuredWidth.coerceAtMost(maxTextWidth)
    }
  }

  /** gets measured height size of the balloon popup. */
  fun getMeasuredHeight(): Int {
    if (builder.height != BalloonSizeSpec.WRAP) {
      return builder.height
    }
    return this.binding.root.measuredHeight
  }

  /** gets a content view of the balloon popup window. */
  fun getContentView(): ViewGroup {
    return binding.balloonCard
  }

  /** gets a arrow view of the balloon popup window. */
  fun getBalloonArrowView(): View {
    return binding.balloonArrow
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
    this.destroyed = true
    this.overlayWindow.dismiss()
    this.bodyWindow.dismiss()
  }

  /** Builder class for creating [Balloon]. */
  @BalloonInlineDsl
  class Builder(private val context: Context) {
    @JvmField @Px
    @set:JvmSynthetic
    var width: Int = BalloonSizeSpec.WRAP

    @JvmField @Px
    @set:JvmSynthetic
    var minWidth: Int = 0

    @JvmField @Px
    @set:JvmSynthetic
    var maxWidth: Int = context.displaySize().x

    @JvmField @FloatRange(from = 0.0, to = 1.0)
    @set:JvmSynthetic
    var widthRatio: Float = NO_Float_VALUE

    @JvmField @FloatRange(from = 0.0, to = 1.0)
    @set:JvmSynthetic
    var minWidthRatio: Float = NO_Float_VALUE

    @JvmField @FloatRange(from = 0.0, to = 1.0)
    @set:JvmSynthetic
    var maxWidthRatio: Float = NO_Float_VALUE

    @JvmField @Px
    @set:JvmSynthetic
    var height: Int = BalloonSizeSpec.WRAP

    @JvmField @Px
    @set:JvmSynthetic
    var paddingLeft: Int = 0

    @JvmField @Px
    @set:JvmSynthetic
    var paddingTop: Int = 0

    @JvmField @Px
    @set:JvmSynthetic
    var paddingRight: Int = 0

    @JvmField @Px
    @set:JvmSynthetic
    var paddingBottom: Int = 0

    @JvmField @Px
    @set:JvmSynthetic
    var marginRight: Int = 0

    @JvmField @Px
    @set:JvmSynthetic
    var marginLeft: Int = 0

    @JvmField @Px
    @set:JvmSynthetic
    var marginTop: Int = 0

    @JvmField @Px
    @set:JvmSynthetic
    var marginBottom: Int = 0

    @JvmField
    @set:JvmSynthetic
    var isVisibleArrow: Boolean = true

    @JvmField @ColorInt
    @set:JvmSynthetic
    var arrowColor: Int = NO_INT_VALUE

    @JvmField
    @set:JvmSynthetic
    var arrowColorMatchBalloon: Boolean = false

    @JvmField @Px
    @set:JvmSynthetic
    var arrowSize: Int = 12.dp

    @JvmField @FloatRange(from = 0.0, to = 1.0)
    @set:JvmSynthetic
    var arrowPosition: Float = 0.5f

    @JvmField
    @set:JvmSynthetic
    var arrowPositionRules: ArrowPositionRules = ArrowPositionRules.ALIGN_BALLOON

    @JvmField
    @set:JvmSynthetic
    var arrowOrientationRules: ArrowOrientationRules =
      ArrowOrientationRules.ALIGN_ANCHOR

    @JvmField
    @set:JvmSynthetic
    var arrowOrientation: ArrowOrientation = ArrowOrientation.BOTTOM

    @JvmField
    @set:JvmSynthetic
    var arrowDrawable: Drawable? = null

    @JvmField
    @set:JvmSynthetic
    var arrowLeftPadding: Int = 0

    @JvmField
    @set:JvmSynthetic
    var arrowRightPadding: Int = 0

    @JvmField
    @set:JvmSynthetic
    var arrowTopPadding: Int = 0

    @JvmField
    @set:JvmSynthetic
    var arrowBottomPadding: Int = 0

    @JvmField
    @set:JvmSynthetic
    var arrowAlignAnchorPadding: Int = 0

    @JvmField
    @set:JvmSynthetic
    var arrowAlignAnchorPaddingRatio: Float = 2.5f

    @JvmField
    @set:JvmSynthetic
    var arrowElevation: Float = 0f

    @JvmField @ColorInt
    @set:JvmSynthetic
    var backgroundColor: Int = Color.BLACK

    @JvmField
    @set:JvmSynthetic
    var backgroundDrawable: Drawable? = null

    @JvmField @Px
    @set:JvmSynthetic
    var cornerRadius: Float = 5f.dp

    @JvmField
    @set:JvmSynthetic
    var text: CharSequence = ""

    @JvmField @ColorInt
    @set:JvmSynthetic
    var textColor: Int = Color.WHITE

    @JvmField
    @set:JvmSynthetic
    var textIsHtml: Boolean = false

    @JvmField
    @set:JvmSynthetic
    var movementMethod: MovementMethod? = null

    @JvmField @Sp
    @set:JvmSynthetic
    var textSize: Float = 12f

    @JvmField
    @set:JvmSynthetic
    var textTypeface: Int = Typeface.NORMAL

    @JvmField
    @set:JvmSynthetic
    var textTypefaceObject: Typeface? = null

    @JvmField
    @set:JvmSynthetic
    var textGravity: Int = Gravity.CENTER

    @JvmField
    @set:JvmSynthetic
    var textForm: TextForm? = null

    @JvmField
    @set:JvmSynthetic
    var iconDrawable: Drawable? = null

    @JvmField
    @set:JvmSynthetic
    var iconGravity = IconGravity.START

    @JvmField @Px
    @set:JvmSynthetic
    var iconWidth: Int = 28.dp

    @JvmField @Px
    @set:JvmSynthetic
    var iconHeight: Int = 28.dp

    @JvmField @Px
    @set:JvmSynthetic
    var iconSpace: Int = 8.dp

    @JvmField @ColorInt
    @set:JvmSynthetic
    var iconColor: Int = NO_INT_VALUE

    @JvmField
    @set:JvmSynthetic
    var iconForm: IconForm? = null

    @JvmField @FloatRange(from = 0.0, to = 1.0)
    @set:JvmSynthetic
    var alpha: Float = 1f

    @JvmField
    @set:JvmSynthetic
    var elevation: Float = 2f.dp

    @JvmField
    @set:JvmSynthetic
    var layout: View? = null

    @JvmField @LayoutRes
    @set:JvmSynthetic
    var layoutRes: Int? = null

    @JvmField
    @set:JvmSynthetic
    var isVisibleOverlay: Boolean = false

    @JvmField @ColorInt
    @set:JvmSynthetic
    var overlayColor: Int = Color.TRANSPARENT

    @JvmField @Px
    @set:JvmSynthetic
    var overlayPadding: Float = 0f

    @JvmField @ColorInt
    @set:JvmSynthetic
    var overlayPaddingColor: Int = Color.TRANSPARENT

    @JvmField
    @set:JvmSynthetic
    var overlayPosition: Point? = null

    @JvmField
    @set:JvmSynthetic
    var overlayShape: BalloonOverlayShape = BalloonOverlayOval

    @JvmField
    @set:JvmSynthetic
    var onBalloonClickListener: OnBalloonClickListener? = null

    @JvmField
    @set:JvmSynthetic
    var onBalloonDismissListener: OnBalloonDismissListener? = null

    @JvmField
    @set:JvmSynthetic
    var onBalloonInitializedListener: OnBalloonInitializedListener? = null

    @JvmField
    @set:JvmSynthetic
    var onBalloonOutsideTouchListener: OnBalloonOutsideTouchListener? = null

    @JvmField
    @set:JvmSynthetic
    var onBalloonTouchListener: View.OnTouchListener? = null

    @JvmField
    @set:JvmSynthetic
    var onBalloonOverlayTouchListener: View.OnTouchListener? = null

    @JvmField
    @set:JvmSynthetic
    var onBalloonOverlayClickListener: OnBalloonOverlayClickListener? = null

    @JvmField
    @set:JvmSynthetic
    var dismissWhenTouchOutside: Boolean = true

    @JvmField
    @set:JvmSynthetic
    var dismissWhenShowAgain: Boolean = false

    @JvmField
    @set:JvmSynthetic
    var dismissWhenClicked: Boolean = false

    @JvmField
    @set:JvmSynthetic
    var dismissWhenOverlayClicked: Boolean = true

    @JvmField
    @set:JvmSynthetic
    var dismissWhenLifecycleOnPause: Boolean = false

    @JvmField
    @set:JvmSynthetic
    var autoDismissDuration: Long = NO_LONG_VALUE

    @JvmField
    @set:JvmSynthetic
    var lifecycleOwner: LifecycleOwner? = null

    @JvmField @StyleRes
    @set:JvmSynthetic
    var balloonAnimationStyle: Int = NO_INT_VALUE

    @JvmField @StyleRes
    @set:JvmSynthetic
    var balloonOverlayAnimationStyle: Int = NO_INT_VALUE

    @JvmField
    @set:JvmSynthetic
    var balloonAnimation: BalloonAnimation = BalloonAnimation.FADE

    @JvmField
    @set:JvmSynthetic
    var balloonOverlayAnimation: BalloonOverlayAnimation = BalloonOverlayAnimation.FADE

    @JvmField
    @set:JvmSynthetic
    var circularDuration: Long = 500L

    @JvmField
    @set:JvmSynthetic
    var balloonHighlightAnimation: BalloonHighlightAnimation = BalloonHighlightAnimation.NONE

    @JvmField @StyleRes
    @set:JvmSynthetic
    var balloonHighlightAnimationStyle: Int = NO_INT_VALUE

    @JvmField
    @set:JvmSynthetic
    var balloonHighlightAnimationStartDelay: Long = 0L

    @JvmField
    @set:JvmSynthetic
    var preferenceName: String? = null

    @JvmField
    @set:JvmSynthetic
    var showTimes: Int = 1

    @JvmField
    @set:JvmSynthetic
    var runIfReachedShowCounts: (() -> Unit)? = null

    @JvmField
    @set:JvmSynthetic
    var isRtlLayout: Boolean =
      context.resources.configuration.layoutDirection == LayoutDirection.RTL

    @JvmField
    @set:JvmSynthetic
    var supportRtlLayoutFactor: Int = LTR.unaryMinus(isRtlLayout)

    @JvmField
    @set:JvmSynthetic
    var isFocusable: Boolean = true

    @JvmField
    @set:JvmSynthetic
    var isStatusBarVisible: Boolean = true

    /** sets the width size. */
    fun setWidth(@Dp value: Int): Builder = apply {
      require(
        value > 0 || value == BalloonSizeSpec.WRAP
      ) { "The width of the balloon must bigger than zero." }
      this.width = value.dp
    }

    /** sets the width size using a dimension resource. */
    fun setWidthResource(@DimenRes value: Int): Builder = apply {
      this.width = context.dimenPixel(value)
    }

    /**
     * sets the minimum size of the width.
     * this functionality works only with the [BalloonSizeSpec.WRAP].
     */
    fun setMinWidth(@Dp value: Int): Builder = apply {
      this.minWidth = value.dp
    }

    /**
     * sets the minimum size of the width using a dimension resource.
     * this functionality works only with the [BalloonSizeSpec.WRAP].
     */
    fun setMinWidthResource(@DimenRes value: Int): Builder = apply {
      this.minWidth = context.dimenPixel(value)
    }

    /**
     * sets the maximum size of the width.
     * this functionality works only with the [BalloonSizeSpec.WRAP].
     */
    fun setMaxWidth(@Dp value: Int): Builder = apply {
      this.maxWidth = value.dp
    }

    /**
     * sets the maximum size of the width using a dimension resource.
     * this functionality works only with the [BalloonSizeSpec.WRAP].
     */
    fun setMaxWidthResource(@DimenRes value: Int): Builder = apply {
      this.maxWidth = context.dimenPixel(value)
    }

    /** sets the width size by the display screen size ratio. */
    fun setWidthRatio(
      @FloatRange(from = 0.0, to = 1.0) value: Float
    ): Builder = apply { this.widthRatio = value }

    /** sets the minimum width size by the display screen size ratio. */
    fun setMinWidthRatio(
      @FloatRange(from = 0.0, to = 1.0) value: Float
    ): Builder = apply { this.minWidthRatio = value }

    /** sets the maximum width size by the display screen size ratio. */
    fun setMaxWidthRatio(
      @FloatRange(from = 0.0, to = 1.0) value: Float
    ): Builder = apply { this.maxWidthRatio = value }

    /** sets the height size. */
    fun setHeight(@Dp value: Int): Builder = apply {
      require(
        value > 0 || value == BalloonSizeSpec.WRAP
      ) { "The height of the balloon must bigger than zero." }
      this.height = value.dp
    }

    /** sets the height size using a dimension resource. */
    fun setHeightResource(@DimenRes value: Int): Builder = apply {
      this.height = context.dimenPixel(value)
    }

    /** sets the width and height sizes of the balloon. */
    fun setSize(@Dp width: Int, @Dp height: Int): Builder = apply {
      setWidth(width)
      setHeight(height)
    }

    /** sets the width and height sizes of the balloon using a dimension resource. */
    fun setSizeResource(@DimenRes width: Int, @DimenRes height: Int): Builder = apply {
      setWidthResource(width)
      setHeightResource(height)
    }

    /** sets the padding on the balloon content all directions. */
    fun setPadding(@Dp value: Int): Builder = apply {
      setPaddingLeft(value)
      setPaddingTop(value)
      setPaddingRight(value)
      setPaddingBottom(value)
    }

    /** sets the padding on the balloon content all directions using dimension resource. */
    fun setPaddingResource(@DimenRes value: Int): Builder = apply {
      val padding = context.dimenPixel(value)
      this.paddingLeft = padding
      this.paddingTop = padding
      this.paddingRight = padding
      this.paddingBottom = padding
    }

    /** sets the horizontal (right and left) padding on the balloon content. */
    fun setPaddingHorizontal(@Dp value: Int): Builder = apply {
      setPaddingLeft(value)
      setPaddingRight(value)
    }

    /** sets the horizontal (right and left) padding on the balloon content using dimension resource. */
    fun setPaddingHorizontalResource(@DimenRes value: Int): Builder = apply {
      setPaddingLeftResource(value)
      setPaddingRightResource(value)
    }

    /** sets the vertical (top and bottom) padding on the balloon content. */
    fun setPaddingVertical(@Dp value: Int): Builder = apply {
      setPaddingTop(value)
      setPaddingBottom(value)
    }

    /** sets the vertical (top and bottom) padding on the balloon content using dimension resource. */
    fun setPaddingVerticalResource(@DimenRes value: Int): Builder = apply {
      setPaddingTopResource(value)
      setPaddingBottomResource(value)
    }

    /** sets the left padding on the balloon content. */
    fun setPaddingLeft(@Dp value: Int): Builder = apply { this.paddingLeft = value.dp }

    /** sets the left padding on the balloon content using dimension resource. */
    fun setPaddingLeftResource(@DimenRes value: Int): Builder = apply {
      this.paddingLeft = context.dimenPixel(value)
    }

    /** sets the top padding on the balloon content. */
    fun setPaddingTop(@Dp value: Int): Builder = apply { this.paddingTop = value.dp }

    /** sets the top padding on the balloon content using dimension resource. */
    fun setPaddingTopResource(@DimenRes value: Int): Builder = apply {
      this.paddingTop = context.dimenPixel(value)
    }

    /** sets the right padding on the balloon content. */
    fun setPaddingRight(@Dp value: Int): Builder = apply {
      this.paddingRight = value.dp
    }

    /** sets the right padding on the balloon content using dimension resource. */
    fun setPaddingRightResource(@DimenRes value: Int): Builder = apply {
      this.paddingRight = context.dimenPixel(value)
    }

    /** sets the bottom padding on the balloon content. */
    fun setPaddingBottom(@Dp value: Int): Builder = apply {
      this.paddingBottom = value.dp
    }

    /** sets the bottom padding on the balloon content using dimension resource. */
    fun setPaddingBottomResource(@DimenRes value: Int): Builder = apply {
      this.paddingBottom = context.dimenPixel(value)
    }

    /** sets the margin on the balloon all directions. */
    fun setMargin(@Dp value: Int): Builder = apply {
      setMarginLeft(value)
      setMarginTop(value)
      setMarginRight(value)
      setMarginBottom(value)
    }

    /** sets the margin on the balloon all directions using a dimension resource. */
    fun setMarginResource(@DimenRes value: Int): Builder = apply {
      val margin = context.dimenPixel(value)
      this.marginLeft = margin
      this.marginTop = margin
      this.marginRight = margin
      this.marginBottom = margin
    }

    /** sets the horizontal (left and right) margins on the balloon. */
    fun setMarginHorizontal(@Dp value: Int): Builder = apply {
      setMarginLeft(value)
      setMarginRight(value)
    }

    /** sets the horizontal (left and right) margins on the balloon using a dimension resource. */
    fun setMarginHorizontalResource(@DimenRes value: Int): Builder = apply {
      setMarginLeftResource(value)
      setMarginRightResource(value)
    }

    /** sets the vertical (top and bottom) margins on the balloon. */
    fun setMarginVertical(@Dp value: Int): Builder = apply {
      setMarginTop(value)
      setMarginBottom(value)
    }

    /** sets the vertical (top and bottom) margins on the balloon using a dimension resource. */
    fun setMarginVerticalResource(@DimenRes value: Int): Builder = apply {
      setMarginTopResource(value)
      setMarginBottomResource(value)
    }

    /** sets the left margin on the balloon. */
    fun setMarginLeft(@Dp value: Int): Builder = apply {
      this.marginLeft = value.dp
    }

    /** sets the left margin on the balloon using dimension resource. */
    fun setMarginLeftResource(@DimenRes value: Int): Builder = apply {
      this.marginLeft = context.dimenPixel(value)
    }

    /** sets the top margin on the balloon. */
    fun setMarginTop(@Dp value: Int): Builder = apply {
      this.marginTop = value.dp
    }

    /** sets the top margin on the balloon using dimension resource. */
    fun setMarginTopResource(@DimenRes value: Int): Builder = apply {
      this.marginTop = context.dimenPixel(value)
    }

    /** sets the right margin on the balloon. */
    fun setMarginRight(@Dp value: Int): Builder = apply {
      this.marginRight = value.dp
    }

    /** sets the right margin on the balloon using dimension resource. */
    fun setMarginRightResource(@DimenRes value: Int): Builder = apply {
      this.marginRight = context.dimenPixel(value)
    }

    /** sets the bottom margin on the balloon. */
    fun setMarginBottom(@Dp value: Int): Builder = apply {
      this.marginBottom = value.dp
    }

    /** sets the bottom margin on the balloon using dimension resource. */
    fun setMarginBottomResource(@DimenRes value: Int): Builder = apply {
      this.marginBottom = context.dimenPixel(value)
    }

    /** sets the visibility of the arrow. */
    fun setIsVisibleArrow(value: Boolean): Builder = apply { this.isVisibleArrow = value }

    /** sets a color of the arrow. */
    fun setArrowColor(@ColorInt value: Int): Builder = apply { this.arrowColor = value }

    /** sets if arrow color should match the color of the balloon card. Overrides [arrowColor].
     * Does not work with custom arrows.
     */
    fun setArrowColorMatchBalloon(value: Boolean): Builder = apply {
      this.arrowColorMatchBalloon = value
    }

    /** sets a color of the arrow using a resource. */
    fun setArrowColorResource(@ColorRes value: Int): Builder = apply {
      this.arrowColor = context.contextColor(value)
    }

    /** sets the size of the arrow. */
    fun setArrowSize(@Dp value: Int): Builder = apply {
      this.arrowSize =
        if (value == BalloonSizeSpec.WRAP) {
          BalloonSizeSpec.WRAP
        } else {
          value.dp
        }
    }

    /** sets the size of the arrow using dimension resource. */
    fun setArrowSizeResource(@DimenRes value: Int): Builder = apply {
      this.arrowSize = context.dimenPixel(value)
    }

    /** sets the arrow position by popup size ration. The popup size depends on [arrowOrientation]. */
    fun setArrowPosition(
      @FloatRange(from = 0.0, to = 1.0) value: Float
    ): Builder = apply { this.arrowPosition = value }

    /**
     * ArrowPositionRules determines the position of the arrow depending on the aligning rules.
     *
     * [ArrowPositionRules.ALIGN_BALLOON]: Align the arrow position depending on the balloon popup body.
     * [ArrowPositionRules.ALIGN_ANCHOR]: Align the arrow position depending on an anchor.
     */
    fun setArrowPositionRules(value: ArrowPositionRules) = apply { this.arrowPositionRules = value }

    /** sets the arrow orientation using [ArrowOrientation]. */
    fun setArrowOrientation(value: ArrowOrientation): Builder = apply {
      this.arrowOrientation = value
    }

    /**
     * ArrowOrientationRules determines the orientation of the arrow depending on the aligning rules.
     *
     * [ArrowOrientationRules.ALIGN_ANCHOR]: Align depending on the position of an anchor.
     * [ArrowOrientationRules.ALIGN_FIXED]: Align to fixed [ArrowOrientation].
     */
    fun setArrowOrientationRules(value: ArrowOrientationRules) = apply {
      this.arrowOrientationRules = value
    }

    /** sets a custom drawable of the arrow. */
    fun setArrowDrawable(value: Drawable?): Builder = apply {
      this.arrowDrawable = value?.mutate()
      if (value != null && arrowSize == BalloonSizeSpec.WRAP) {
        arrowSize = max(value.intrinsicWidth, value.intrinsicHeight)
      }
    }

    /** sets a custom drawable of the arrow using the resource. */
    fun setArrowDrawableResource(@DrawableRes value: Int): Builder = apply {
      setArrowDrawable(context.contextDrawable(value))
    }

    /** sets the left padding of the arrow. */
    fun setArrowLeftPadding(@Dp value: Int): Builder = apply {
      this.arrowLeftPadding = value.dp
    }

    /** sets the left padding of the arrow using the resource. */
    fun setArrowLeftPaddingResource(@DimenRes value: Int): Builder = apply {
      this.arrowLeftPadding = context.dimenPixel(value)
    }

    /** sets the right padding of the arrow. */
    fun setArrowRightPadding(@Dp value: Int): Builder = apply {
      this.arrowRightPadding = value.dp
    }

    /** sets the right padding of the arrow using the resource. */
    fun setArrowRightPaddingResource(@DimenRes value: Int): Builder = apply {
      this.arrowRightPadding = context.dimenPixel(value)
    }

    /** sets the top padding of the arrow. */
    fun setArrowTopPadding(@Dp value: Int): Builder = apply {
      this.arrowTopPadding = value.dp
    }

    /** sets the top padding of the arrow using the resource. */
    fun setArrowTopPaddingResource(@DimenRes value: Int): Builder = apply {
      this.arrowTopPadding = context.dimenPixel(value)
    }

    /** sets the bottom padding of the arrow. */
    fun setArrowBottomPadding(@Dp value: Int): Builder = apply {
      this.arrowBottomPadding = value.dp
    }

    /** sets the bottom padding of the arrow using the resource. */
    fun setArrowBottomPaddingResource(@DimenRes value: Int): Builder = apply {
      this.arrowBottomPadding = context.dimenPixel(value)
    }

    /** sets the padding of the arrow when aligning anchor using with [ArrowPositionRules.ALIGN_ANCHOR]. */
    fun setArrowAlignAnchorPadding(@Dp value: Int): Builder = apply {
      this.arrowAlignAnchorPadding = value.dp
    }

    /** sets the padding of the arrow the resource when aligning anchor using with [ArrowPositionRules.ALIGN_ANCHOR]. */
    fun setArrowAlignAnchorPaddingResource(@DimenRes value: Int): Builder = apply {
      this.arrowAlignAnchorPadding = context.dimenPixel(value)
    }

    /** sets the padding ratio of the arrow when aligning anchor using with [ArrowPositionRules.ALIGN_ANCHOR]. */
    fun setArrowAlignAnchorPaddingRatio(value: Float): Builder = apply {
      this.arrowAlignAnchorPaddingRatio = value
    }

    /** sets the elevation of the arrow. */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    fun setArrowElevation(@Dp value: Int): Builder = apply {
      this.arrowElevation = value.dp.toFloat()
    }

    /** sets the elevation of the arrow using dimension resource. */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    fun setArrowElevationResource(@DimenRes value: Int): Builder = apply {
      this.arrowElevation = context.dimen(value)
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
      this.cornerRadius = value.dp
    }

    /** sets the corner radius of the popup using dimension resource. */
    fun setCornerRadiusResource(@DimenRes value: Int): Builder = apply {
      this.cornerRadius = context.dimen(value)
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

    /** sets the movement method for TextView. */
    fun setMovementMethod(value: MovementMethod): Builder = apply { this.movementMethod = value }

    /** sets the size of the main text content. */
    fun setTextSize(@Sp value: Float): Builder = apply { this.textSize = value }

    /** sets the size of the main text content using dimension resource. */
    fun setTextSizeResource(@DimenRes value: Int) = apply {
      this.textSize = context.px2Sp(context.dimen(value))
    }

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

    /** sets the width size of the icon drawable. */
    fun setIconWidth(@Dp value: Int): Builder = apply {
      this.iconWidth = value.dp
    }

    /** sets the width size of the icon drawable using the dimension resource. */
    fun setIconWidthResource(@DimenRes value: Int): Builder = apply {
      this.iconWidth = context.dimenPixel(value)
    }

    /** sets the height size of the icon drawable. */
    fun setIconHeight(@Dp value: Int): Builder = apply {
      this.iconHeight = value.dp
    }

    /** sets the height size of the icon drawable using the dimension resource. */
    fun setIconHeightResource(@DimenRes value: Int): Builder = apply {
      this.iconHeight = context.dimenPixel(value)
    }

    /** sets the size of the icon drawable. */
    fun setIconSize(@Dp value: Int): Builder = apply {
      setIconWidth(value)
      setIconHeight(value)
    }

    /** sets the size of the icon drawable using the dimension resource. */
    fun setIconSizeResource(@DimenRes value: Int): Builder = apply {
      setIconWidthResource(value)
      setIconHeightResource(value)
    }

    /** sets the color of the icon drawable. */
    fun setIconColor(@ColorInt value: Int): Builder = apply { this.iconColor = value }

    /** sets the color of the icon drawable using the resource color. */
    fun setIconColorResource(@ColorRes value: Int): Builder = apply {
      this.iconColor = context.contextColor(value)
    }

    /** sets the space between the icon and the main text content. */
    fun setIconSpace(@Dp value: Int): Builder = apply { this.iconSpace = value.dp }

    /** sets the space between the icon and the main text content using dimension resource. */
    fun setIconSpaceResource(@DimenRes value: Int): Builder = apply {
      this.iconSpace = context.dimenPixel(value)
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
      this.elevation = value.dp.toFloat()
    }

    /** sets the elevation to the popup using dimension resource. */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    fun setElevationResource(@DimenRes value: Int): Builder = apply {
      this.elevation = context.dimen(value)
    }

    /** sets the custom layout resource to the popup content. */
    fun setLayout(@LayoutRes layoutRes: Int): Builder = apply { this.layoutRes = layoutRes }

    /** sets the custom layout view to the popup content. */
    fun setLayout(layout: View): Builder = apply { this.layout = layout }

    /** sets the visibility of the overlay for highlighting an anchor. */
    fun setIsVisibleOverlay(value: Boolean) = apply { this.isVisibleOverlay = value }

    /** background color of the overlay. */
    fun setOverlayColor(@ColorInt value: Int) = apply { this.overlayColor = value }

    /** background color of the overlay using a color resource. */
    fun setOverlayColorResource(@ColorRes value: Int) = apply {
      this.overlayColor = context.contextColor(value)
    }

    /** sets a padding value of the overlay shape internally. */
    fun setOverlayPadding(@Dp value: Float) = apply { this.overlayPadding = value.dp }

    /** sets a padding value of the overlay shape internally using dimension resource.. */
    fun setOverlayPaddingResource(@DimenRes value: Int) = apply {
      this.overlayPadding = context.dimen(value)
    }

    /** color of the overlay padding. */
    fun setOverlayPaddingColor(@ColorInt value: Int) = apply { this.overlayPaddingColor = value }

    /** color of the overlay padding using a color resource. */
    fun setOverlayPaddingColorResource(@ColorRes value: Int) = apply { this.overlayPaddingColor = context.contextColor(value) }

    /** sets a specific position of the overlay shape. */
    fun setOverlayPosition(value: Point) = apply { this.overlayPosition = value }

    /** sets a shape of the overlay over the anchor view. */
    fun setOverlayShape(value: BalloonOverlayShape) = apply { this.overlayShape = value }

    /** sets is status bar is visible or not in your screen. */
    fun setIsStatusBarVisible(value: Boolean) = apply {
      this.isStatusBarVisible = value
    }

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

    /** sets the balloon overlay showing animation using [BalloonAnimation]. */
    fun setBalloonOverlayAnimation(value: BalloonOverlayAnimation): Builder = apply {
      this.balloonOverlayAnimation = value
    }

    /** sets the balloon overlay showing animation using custom xml animation style. */
    fun setBalloonOverlayAnimationStyle(@StyleRes value: Int): Builder = apply {
      this.balloonOverlayAnimationStyle = value
    }

    /**
     * sets the duration of the circular animation.
     * this option only works with [BalloonAnimation.CIRCULAR] value in [setBalloonAnimation].
     */
    fun setCircularDuration(value: Long): Builder = apply {
      this.circularDuration = value
    }

    /** sets the balloon highlight animation using [BalloonHighlightAnimation]. */
    fun setBalloonHighlightAnimation(
      value: BalloonHighlightAnimation,
      startDelay: Long = 0L
    ): Builder = apply {
      this.balloonHighlightAnimation = value
      this.balloonHighlightAnimationStartDelay = startDelay
    }

    /** sets the balloon highlight animation using custom xml animation resource file. */
    fun setBalloonHighlightAnimationResource(
      @AnimRes value: Int,
      startDelay: Long = 0L
    ): Builder = apply {
      this.balloonHighlightAnimationStyle = value
      this.balloonHighlightAnimationStartDelay = startDelay
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

    /** sets a [View.OnTouchListener] to the popup. */
    fun setOnBalloonTouchListener(value: View.OnTouchListener): Builder = apply {
      this.onBalloonTouchListener = value
    }

    /** sets a [OnBalloonOverlayClickListener] to the overlay popup. */
    fun setOnBalloonOverlayClickListener(value: OnBalloonOverlayClickListener): Builder = apply {
      this.onBalloonOverlayClickListener = value
    }

    /** sets a [OnBalloonClickListener] to the popup using lambda. */
    @JvmSynthetic
    fun setOnBalloonClickListener(block: (View) -> Unit): Builder = apply {
      this.onBalloonClickListener = OnBalloonClickListener(block)
    }

    /** sets a [OnBalloonDismissListener] to the popup using lambda. */
    @JvmSynthetic
    fun setOnBalloonDismissListener(block: () -> Unit): Builder = apply {
      this.onBalloonDismissListener = OnBalloonDismissListener(block)
    }

    /** sets a [OnBalloonInitializedListener] to the popup using lambda. */
    @JvmSynthetic
    fun setOnBalloonInitializedListener(block: (View) -> Unit): Builder = apply {
      this.onBalloonInitializedListener = OnBalloonInitializedListener(block)
    }

    /** sets a [OnBalloonOutsideTouchListener] to the popup using lambda. */
    @JvmSynthetic
    fun setOnBalloonOutsideTouchListener(block: (View, MotionEvent) -> Unit): Builder = apply {
      this.onBalloonOutsideTouchListener = OnBalloonOutsideTouchListener(block)
      setDismissWhenTouchOutside(false)
    }

    /** sets a [OnBalloonOverlayClickListener] to the overlay popup using lambda. */
    fun setOnBalloonOverlayClickListener(block: () -> Unit): Builder = apply {
      this.onBalloonOverlayClickListener = OnBalloonOverlayClickListener(block)
    }

    /** dismisses when touch outside. */
    fun setDismissWhenTouchOutside(value: Boolean): Builder = apply {
      this.dismissWhenTouchOutside = value
      if (!value) {
        setFocusable(value)
      }
    }

    /** sets a [View.OnTouchListener] to the overlay popup. */
    fun setOnBalloonOverlayTouchListener(value: View.OnTouchListener): Builder = apply {
      this.onBalloonOverlayTouchListener = value
      setDismissWhenOverlayClicked(false)
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

    /** dismisses when the overlay popup is clicked. */
    fun setDismissWhenOverlayClicked(value: Boolean): Builder = apply {
      this.dismissWhenOverlayClicked = value
    }

    /** dismisses automatically some milliseconds later when the popup is shown. */
    fun setAutoDismissDuration(value: Long): Builder = apply { this.autoDismissDuration = value }

    /**
     * sets the preference name for persisting showing counts.
     * This method should be used with the [setShowCounts].
     *
     * @see (https://github.com/skydoves/balloon#persistence)
     */
    fun setPreferenceName(value: String): Builder = apply { this.preferenceName = value }

    /**
     * sets showing counts which how many times the Balloon popup will be shown up.
     * This method should be used with the [setPreferenceName].
     *
     * @see (https://github.com/skydoves/balloon#persistence)
     */
    fun setShowCounts(value: Int): Builder = apply { this.showTimes = value }

    /**
     * sets a lambda for invoking after the preference showing counts is reached the goal.
     * This method should be used ith the [setPreferenceName] and [setShowCounts].
     *
     * @see (https://github.com/skydoves/balloon#persistence)
     *
     * @param block A lambda for invoking after the preference showing counts is reached the goal.
     */
    fun runIfReachedShowCounts(block: () -> Unit): Builder = apply {
      runIfReachedShowCounts = block
    }

    /**
     * sets a [Runnable] for invoking after the preference showing counts is reached the goal.
     * This method should be used ith the [setPreferenceName] and [setShowCounts].
     *
     * @see (https://github.com/skydoves/balloon#persistence)
     *
     * @param runnable A [Runnable] for invoking after the preference showing counts is reached the goal.
     */
    fun runIfReachedShowCounts(runnable: Runnable): Builder = apply {
      runIfReachedShowCounts { runnable.run() }
    }

    /**
     * sets isFocusable option to the body window.
     * if true when the balloon is showing, can not touch other views and
     * onBackPressed will be fired to the balloon.
     * */
    fun setFocusable(value: Boolean): Builder = apply { this.isFocusable = value }

    /**
     * Create a new instance of the [Balloon] which includes customized attributes.
     *
     * @return A new created instance of the [Balloon].
     */
    fun build(): Balloon = Balloon(
      context = context,
      builder = this@Builder
    )
  }

  /**
   * An abstract factory class for creating [Balloon] instance.
   * A factory implementation class must have a default (non-argument) constructor.
   * This class is used to initialize an instance of the [Balloon] lazily in Activities and Fragments.
   *
   * @see [Lazy Initialization](https://github.com/skydoves/Balloon#lazy-initialization)
   */
  abstract class Factory {

    /**
     * Creates a new instance of [Balloon].
     *
     * @return A new created instance of the [Balloon].
     */
    abstract fun create(context: Context, lifecycle: LifecycleOwner?): Balloon
  }
}
