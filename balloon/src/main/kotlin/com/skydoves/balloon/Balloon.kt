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

@file:Suppress("unused", "RedundantVisibilityModifier")

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
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
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
import androidx.core.view.ViewCompat
import androidx.core.view.forEach
import androidx.core.widget.ImageViewCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import com.skydoves.balloon.BalloonCenterAlign.Companion.getRTLSupportAlign
import com.skydoves.balloon.animations.BalloonRotateAnimation
import com.skydoves.balloon.annotations.Dp
import com.skydoves.balloon.annotations.Sp
import com.skydoves.balloon.databinding.BalloonLayoutBodyBinding
import com.skydoves.balloon.databinding.BalloonLayoutOverlayBinding
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
import com.skydoves.balloon.extensions.runOnAfterSDK22
import com.skydoves.balloon.extensions.sumOfCompoundPadding
import com.skydoves.balloon.extensions.visible
import com.skydoves.balloon.overlay.BalloonOverlayAnimation
import com.skydoves.balloon.overlay.BalloonOverlayOval
import com.skydoves.balloon.overlay.BalloonOverlayShape
import kotlin.math.max
import kotlin.math.roundToInt

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
public inline fun createBalloon(
  context: Context,
  crossinline block: Balloon.Builder.() -> Unit
): Balloon =
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
public class Balloon private constructor(
  private val context: Context,
  private val builder: Builder
) : DefaultLifecycleObserver {

  /** A main content view of the popup. */
  private val binding: BalloonLayoutBodyBinding =
    BalloonLayoutBodyBinding.inflate(LayoutInflater.from(context), null, false)

  /** An overlay view of the background for highlighting the popup and an anchor. */
  private val overlayBinding: BalloonLayoutOverlayBinding =
    BalloonLayoutOverlayBinding.inflate(LayoutInflater.from(context), null, false)

  /** A main content window of the popup. */
  public val bodyWindow: PopupWindow = PopupWindow(
    binding.root,
    FrameLayout.LayoutParams.WRAP_CONTENT,
    FrameLayout.LayoutParams.WRAP_CONTENT
  )

  /** An overlay window of the background popup. */
  public val overlayWindow: PopupWindow = PopupWindow(
    overlayBinding.root,
    ViewGroup.LayoutParams.MATCH_PARENT,
    ViewGroup.LayoutParams.MATCH_PARENT
  )

  /** Denotes the popup is showing or not. */
  public var isShowing: Boolean = false
    private set

  /** Denotes the popup is already destroyed internally. */
  private var destroyed: Boolean = false

  /** Interface definition for a callback to be invoked when a balloon view is initialized. */
  @JvmField
  @set:JvmSynthetic
  public var onBalloonInitializedListener: OnBalloonInitializedListener? =
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
      context.lifecycle.addObserver(builder.lifecycleObserver ?: this@Balloon)
    } else {
      builder.lifecycleOwner?.lifecycle?.addObserver(builder.lifecycleObserver ?: this@Balloon)
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
    imageView: ImageView,
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
          oldBitmap.width.toFloat() / 2 - builder.arrowHalfSize, 0f,
          oldBitmap.width.toFloat(), 0f, startColor, endColor, Shader.TileMode.CLAMP
        )
      }
      ArrowOrientation.RIGHT, ArrowOrientation.TOP -> {
        LinearGradient(
          oldBitmap.width.toFloat() / 2 + builder.arrowHalfSize, 0f, 0f, 0f,
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
        startColor = bitmap.getPixel((x + builder.arrowHalfSize).toInt(), y.toInt())
        endColor = bitmap.getPixel((x - builder.arrowHalfSize).toInt(), y.toInt())
      }
      ArrowOrientation.LEFT, ArrowOrientation.RIGHT -> {
        startColor = bitmap.getPixel(x.toInt(), (y + builder.arrowHalfSize).toInt())
        endColor = bitmap.getPixel(x.toInt(), (y - builder.arrowHalfSize).toInt())
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
    return when (builder.arrowPositionRules) {
      ArrowPositionRules.ALIGN_BALLOON -> binding.balloonWrapper.width * builder.arrowPosition - builder.arrowHalfSize
      ArrowPositionRules.ALIGN_ANCHOR -> {
        when {
          anchorX + anchor.width < balloonX -> minPosition
          balloonX + getMeasuredWidth() < anchorX -> maxPosition
          else -> {
            val position =
              (anchor.width) * builder.arrowPosition + anchorX - balloonX - builder.arrowHalfSize
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
      setIsAttachedInDecor(builder.isAttachedInDecor)
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
    val parentView = layout.parent as? ViewGroup
    parentView?.removeView(layout)
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
        BalloonAnimation.ELASTIC -> bodyWindow.animationStyle = R.style.Balloon_Elastic_Anim
        BalloonAnimation.CIRCULAR -> {
          bodyWindow.contentView.circularRevealed(builder.circularDuration)
          bodyWindow.animationStyle = R.style.Balloon_Normal_Dispose_Anim
        }
        BalloonAnimation.FADE -> bodyWindow.animationStyle = R.style.Balloon_Fade_Anim
        BalloonAnimation.OVERSHOOT -> bodyWindow.animationStyle = R.style.Balloon_Overshoot_Anim
        BalloonAnimation.NONE -> bodyWindow.animationStyle = R.style.Balloon_Normal_Anim
      }
    } else {
      bodyWindow.animationStyle = builder.balloonAnimationStyle
    }
  }

  private fun applyBalloonOverlayAnimation() {
    if (builder.balloonOverlayAnimationStyle == NO_INT_VALUE) {
      when (builder.balloonOverlayAnimation) {
        BalloonOverlayAnimation.FADE -> overlayWindow.animationStyle = R.style.Balloon_Fade_Anim
        else -> overlayWindow.animationStyle = R.style.Balloon_Normal_Anim
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
              ArrowOrientation.TOP -> R.anim.balloon_heartbeat_bottom
              ArrowOrientation.BOTTOM -> R.anim.balloon_heartbeat_top
              ArrowOrientation.LEFT -> R.anim.balloon_heartbeat_right
              ArrowOrientation.RIGHT -> R.anim.balloon_heartbeat_left
            }
          } else {
            R.anim.balloon_heartbeat_center
          }
        }
        BalloonHighlightAnimation.SHAKE -> {
          when (builder.arrowOrientation) {
            ArrowOrientation.TOP -> R.anim.balloon_shake_bottom
            ArrowOrientation.BOTTOM -> R.anim.balloon_shake_top
            ArrowOrientation.LEFT -> R.anim.balloon_shake_right
            ArrowOrientation.RIGHT -> R.anim.balloon_shake_left
          }
        }
        BalloonHighlightAnimation.BREATH -> R.anim.balloon_fade
        BalloonHighlightAnimation.ROTATE -> return builder.balloonRotateAnimation
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
  public fun shouldShowUp(): Boolean {
    return this.builder.preferenceName?.let {
      this.balloonPersistence.shouldShowUp(it, builder.showTimes)
    } ?: true
  }

  /**
   * Shows [Balloon] tooltips on the [anchors] with some initializations related to arrow, content, and overlay.
   * The balloon will be shown with the [overlayWindow] if the anchorView's parent window is in a valid state.
   * The size of the content will be measured internally, and it will affect calculating the popup size.
   *
   * @param block A lambda block for showing the [bodyWindow].
   */
  @MainThread
  private inline fun show(vararg anchors: View, crossinline block: () -> Unit) {
    val mainAnchor = anchors[0]
    if (canShowBalloonWindow(mainAnchor)) {
      mainAnchor.post {
        canShowBalloonWindow(mainAnchor).takeIf { it } ?: return@post

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
        initializeArrow(mainAnchor)
        initializeBalloonContent()

        applyBalloonOverlayAnimation()
        showOverlayWindow(*anchors)
        passTouchEventToAnchor(mainAnchor)

        applyBalloonAnimation()
        startBalloonHighlightAnimation()
        block()
      }
    } else if (builder.dismissWhenShowAgain) {
      dismiss()
    }
  }

  private fun canShowBalloonWindow(anchor: View): Boolean {
    return !isShowing &&
      // If the balloon is already destroyed depending on the lifecycle,
      // We should not allow showing the popupWindow, it's related to `relay()` method. (#46)
      !destroyed &&
      // We should check the current Activity is running.
      // If the Activity is finishing, we can't attach the popupWindow to the Activity's window. (#92)
      !context.isFinishing &&
      // We should check the contentView is already attached to the decorView or backgroundView in the popupWindow.
      // Sometimes there is a concurrency issue between show and dismiss the popupWindow. (#149)
      bodyWindow.contentView.parent == null &&
      // we should check the anchor view is attached to the parent's window.
      ViewCompat.isAttachedToWindow(anchor)
  }

  private fun showOverlayWindow(vararg anchors: View) {
    if (builder.isVisibleOverlay) {
      val mainAnchor = anchors[0]
      if (anchors.size == 1) {
        overlayBinding.balloonOverlayView.anchorView = mainAnchor
      } else {
        overlayBinding.balloonOverlayView.anchorViewList = anchors.toList()
      }
      overlayWindow.showAtLocation(mainAnchor, Gravity.CENTER, 0, 0)
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
   * Shows the balloon over the anchor view (overlap) as the center aligns.
   * Even if you use with the [ArrowOrientationRules.ALIGN_ANCHOR], the alignment will not be guaranteed.
   * So if you use the function, use with [ArrowOrientationRules.ALIGN_FIXED] and fixed [ArrowOrientation].
   *
   * @param anchor A target view which popup will be shown with overlap.
   * @param xOff A horizontal offset from the anchor in pixels.
   * @param yOff A vertical offset from the anchor in pixels.
   * @param centerAlign A rule for deciding the alignment of the balloon.
   */
  @JvmOverloads
  public fun showAtCenter(
    anchor: View,
    xOff: Int = 0,
    yOff: Int = 0,
    centerAlign: BalloonCenterAlign = BalloonCenterAlign.TOP
  ) {
    val halfAnchorWidth = (anchor.measuredWidth * 0.5f).roundToInt()
    val halfAnchorHeight = (anchor.measuredHeight * 0.5f).roundToInt()
    val halfBalloonWidth = (getMeasuredWidth() * 0.5f).roundToInt()
    val halfBalloonHeight = (getMeasuredHeight() * 0.5f).roundToInt()
    val rtlAlign = centerAlign.getRTLSupportAlign(builder.isRtlLayout)
    show(anchor) {
      when (rtlAlign) {
        BalloonCenterAlign.TOP ->
          bodyWindow.showAsDropDown(
            anchor,
            builder.supportRtlLayoutFactor * (halfAnchorWidth - halfBalloonWidth + xOff),
            -(getMeasuredHeight() + halfAnchorHeight) + yOff
          )
        BalloonCenterAlign.BOTTOM ->
          bodyWindow.showAsDropDown(
            anchor,
            builder.supportRtlLayoutFactor * (halfAnchorWidth - halfBalloonWidth + xOff),
            -halfBalloonHeight + halfAnchorWidth + yOff
          )
        BalloonCenterAlign.START ->
          bodyWindow.showAsDropDown(
            anchor,
            builder.supportRtlLayoutFactor * (halfAnchorWidth - getMeasuredWidth() + xOff),
            (-getMeasuredHeight() + halfAnchorHeight) + yOff
          )
        BalloonCenterAlign.END ->
          bodyWindow.showAsDropDown(
            anchor,
            builder.supportRtlLayoutFactor * (halfAnchorWidth + getMeasuredWidth() + xOff),
            (-getMeasuredHeight() + halfAnchorHeight) + yOff
          )
      }
    }
  }

  /**
   * Shows the balloon on an anchor view as the center alignment with x-off and y-off and shows the next balloon sequentially.
   * This function returns the next balloon.
   *
   * @param balloon A next [Balloon] that will be shown sequentially after dismissing this popup.
   * @param anchor A target view which popup will be shown to.
   * @param xOff A horizontal offset from the anchor in pixels.
   * @param yOff A vertical offset from the anchor in pixels.
   * @param centerAlign A rule for deciding the align of the balloon.
   *
   * @return A next [balloon].
   *
   * @see [Show sequentially](https://github.com/skydoves/Balloon#show-sequentially)
   */
  @JvmOverloads
  public fun relayShowAtCenter(
    balloon: Balloon,
    anchor: View,
    xOff: Int = 0,
    yOff: Int = 0,
    centerAlign: BalloonCenterAlign = BalloonCenterAlign.TOP
  ): Balloon = relay(balloon) { it.showAtCenter(anchor, xOff, yOff, centerAlign) }

  /**
   * Shows the balloon on an anchor view as drop down with x-off and y-off.
   *
   * @param anchor A target view which popup will be shown to.
   * @param xOff A horizontal offset from the anchor in pixels.
   * @param yOff A vertical offset from the anchor in pixels.
   */
  @JvmOverloads
  public fun showAsDropDown(anchor: View, xOff: Int = 0, yOff: Int = 0) {
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
  @JvmOverloads
  public fun relayShowAsDropDown(
    balloon: Balloon,
    anchor: View,
    xOff: Int = 0,
    yOff: Int = 0
  ): Balloon =
    relay(balloon) { it.showAsDropDown(anchor, xOff, yOff) }

  /**
   * Shows the balloon on an anchor view as the top alignment with x-off and y-off.
   *
   * @param anchor A target view which popup will be shown to.
   * @param xOff A horizontal offset from the anchor in pixels.
   * @param yOff A vertical offset from the anchor in pixels.
   */
  @JvmOverloads
  public fun showAlignTop(anchor: View, xOff: Int = 0, yOff: Int = 0) {
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
  public fun relayShowAlignTop(
    balloon: Balloon,
    anchor: View,
    xOff: Int = 0,
    yOff: Int = 0
  ): Balloon =
    relay(balloon) { it.showAlignTop(anchor, xOff, yOff) }

  /**
   * Shows the balloon on an anchor view as the bottom alignment with x-off and y-off.
   *
   * @param anchor A target view which popup will be shown to.
   * @param xOff A horizontal offset from the anchor in pixels.
   * @param yOff A vertical offset from the anchor in pixels.
   */
  @JvmOverloads
  public fun showAlignBottom(anchor: View, xOff: Int = 0, yOff: Int = 0) {
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
  public fun relayShowAlignBottom(
    balloon: Balloon,
    anchor: View,
    xOff: Int = 0,
    yOff: Int = 0
  ): Balloon =
    relay(balloon) { it.showAlignBottom(anchor, xOff, yOff) }

  /**
   * Shows the balloon on an anchor view as the right alignment with x-off and y-off.
   *
   * @param anchor A target view which popup will be shown to.
   * @param xOff A horizontal offset from the anchor in pixels.
   * @param yOff A vertical offset from the anchor in pixels.
   */
  @JvmOverloads
  public fun showAlignRight(anchor: View, xOff: Int = 0, yOff: Int = 0) {
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
  public fun relayShowAlignRight(
    balloon: Balloon,
    anchor: View,
    xOff: Int = 0,
    yOff: Int = 0
  ): Balloon = relay(
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
  public fun showAlignLeft(anchor: View, xOff: Int = 0, yOff: Int = 0) {
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
  public fun relayShowAlignLeft(
    balloon: Balloon,
    anchor: View,
    xOff: Int = 0,
    yOff: Int = 0
  ): Balloon =
    relay(balloon) { it.showAlignLeft(anchor, xOff, yOff) }

  /**
   * Shows the balloon on an anchor view as the top alignment with x-off and y-off.
   *
   * @param mainAnchor A target view which popup will be displayed.
   * @param anchorList A list of anchors to display multiple overlay.
   * @param xOff A horizontal offset from the anchor in pixels.
   * @param yOff A vertical offset from the anchor in pixels.
   */
  @JvmOverloads
  public fun showAlign(mainAnchor: View, anchorList: List<View>, xOff: Int = 0, yOff: Int = 0) {
    val anchors = listOf(mainAnchor) + anchorList
    show(*anchors.toTypedArray()) {
      bodyWindow.showAsDropDown(
        mainAnchor,
        builder.supportRtlLayoutFactor * ((mainAnchor.measuredWidth / 2) - (getMeasuredWidth() / 2) + xOff),
        -getMeasuredHeight() - mainAnchor.measuredHeight + yOff
      )
    }
  }

  /**
   * updates popup and arrow position of the popup based on
   * a new target anchor view with additional x-off and y-off.
   *
   * @param anchor A target view which popup will be shown to.
   * @param xOff A horizontal offset from the anchor in pixels.
   * @param yOff A vertical offset from the anchor in pixels.
   */
  @JvmOverloads
  public fun update(anchor: View, xOff: Int = 0, yOff: Int = 0) {
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
  public fun dismiss() {
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
  public fun dismissWithDelay(delay: Long): Boolean =
    handler.postDelayed(autoDismissRunnable, delay)

  /** sets a [OnBalloonClickListener] to the popup. */
  public fun setOnBalloonClickListener(onBalloonClickListener: OnBalloonClickListener?) {
    this.binding.balloonWrapper.setOnClickListener {
      onBalloonClickListener?.onBalloonClick(it)
      if (builder.dismissWhenClicked) dismiss()
    }
  }

  /** clears all persisted preferences. */
  public fun clearAllPreferences() {
    this.balloonPersistence.clearAllPreferences()
  }

  /** sets a [OnBalloonClickListener] to the popup using lambda. */
  @JvmSynthetic
  public fun setOnBalloonClickListener(block: (View) -> Unit) {
    setOnBalloonClickListener(OnBalloonClickListener(block))
  }

  /**
   * sets a [OnBalloonInitializedListener] to the popup.
   * The [OnBalloonInitializedListener.onBalloonInitialized] will be invoked when inflating the
   * body content of the balloon is finished.
   */
  public fun setOnBalloonInitializedListener(onBalloonInitializedListener: OnBalloonInitializedListener?) {
    this.onBalloonInitializedListener = onBalloonInitializedListener
  }

  /**
   * sets a [OnBalloonInitializedListener] to the popup using a lambda.
   * The [OnBalloonInitializedListener.onBalloonInitialized] will be invoked when inflating the
   * body content of the balloon is finished.
   */
  @JvmSynthetic
  public fun setOnBalloonInitializedListener(block: (View) -> Unit) {
    setOnBalloonInitializedListener(OnBalloonInitializedListener(block))
  }

  /** sets a [OnBalloonDismissListener] to the popup. */
  public fun setOnBalloonDismissListener(onBalloonDismissListener: OnBalloonDismissListener?) {
    this.bodyWindow.setOnDismissListener {
      stopBalloonHighlightAnimation()
      this@Balloon.dismiss()
      onBalloonDismissListener?.onBalloonDismiss()
    }
  }

  /** sets a [OnBalloonDismissListener] to the popup using lambda. */
  @JvmSynthetic
  public fun setOnBalloonDismissListener(block: () -> Unit) {
    setOnBalloonDismissListener(OnBalloonDismissListener(block))
  }

  /** sets a [OnBalloonOutsideTouchListener] to the popup. */
  public fun setOnBalloonOutsideTouchListener(onBalloonOutsideTouchListener: OnBalloonOutsideTouchListener?) {
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
  public fun setOnBalloonOutsideTouchListener(block: (View, MotionEvent) -> Unit) {
    setOnBalloonOutsideTouchListener(
      OnBalloonOutsideTouchListener(block)
    )
  }

  /** sets a [View.OnTouchListener] to the popup. */
  public fun setOnBalloonTouchListener(onTouchListener: View.OnTouchListener?) {
    if (onTouchListener != null) {
      this.bodyWindow.setTouchInterceptor(onTouchListener)
    }
  }

  /** sets a [View.OnTouchListener] to the overlay popup */
  public fun setOnBalloonOverlayTouchListener(onTouchListener: View.OnTouchListener?) {
    if (onTouchListener != null) {
      this.overlayWindow.setTouchInterceptor(onTouchListener)
    }
  }

  /** sets a [View.OnTouchListener] to the overlay popup using lambda. */
  public fun setOnBalloonOverlayTouchListener(block: (View, MotionEvent) -> Boolean) {
    setOnBalloonOverlayTouchListener(
      View.OnTouchListener(block)
    )
  }

  private fun passTouchEventToAnchor(anchor: View) {
    if (!this.builder.passTouchEventToAnchor) return
    setOnBalloonOverlayTouchListener { view, event ->
      view.performClick()
      val rect = Rect()
      anchor.getGlobalVisibleRect(rect)
      if (rect.contains(event.rawX.toInt(), event.rawY.toInt())) {
        anchor.rootView.dispatchTouchEvent(event)
        true
      } else false
    }
  }

  /** sets a [OnBalloonOverlayClickListener] to the overlay popup. */
  public fun setOnBalloonOverlayClickListener(onBalloonOverlayClickListener: OnBalloonOverlayClickListener?) {
    this.overlayBinding.root.setOnClickListener {
      onBalloonOverlayClickListener?.onBalloonOverlayClick()
      if (builder.dismissWhenOverlayClicked) dismiss()
    }
  }

  /** sets a [OnBalloonOverlayClickListener] to the overlay popup using lambda. */
  @JvmSynthetic
  public fun setOnBalloonOverlayClickListener(block: () -> Unit) {
    setOnBalloonOverlayClickListener(
      OnBalloonOverlayClickListener(block)
    )
  }

  /**
   * sets whether the popup window will be attached in the decor frame of its parent window.
   * If you want to show up balloon on your DialogFragment, it's recommended to use with true. (#131)
   */
  public fun setIsAttachedInDecor(value: Boolean): Balloon = apply {
    runOnAfterSDK22 {
      this.bodyWindow.isAttachedInDecor = value
    }
  }

  /** gets measured width size of the balloon popup. */
  public fun getMeasuredWidth(): Int {
    val displayWidth = displaySize.x
    return when {
      builder.widthRatio != NO_Float_VALUE ->
        (displayWidth * builder.widthRatio).toInt()
      builder.minWidthRatio != NO_Float_VALUE || builder.maxWidthRatio != NO_Float_VALUE -> {
        val maxWidthRatio =
          if (builder.maxWidthRatio != NO_Float_VALUE) builder.maxWidthRatio else 1f
        binding.root.measuredWidth.coerceIn(
          (displayWidth * builder.minWidthRatio).toInt(),
          (displayWidth * maxWidthRatio).toInt()
        )
      }
      builder.width != BalloonSizeSpec.WRAP -> builder.width.coerceAtMost(displayWidth)
      else -> binding.root.measuredWidth.coerceIn(builder.minWidth, builder.maxWidth)
    }
  }

  /**
   * Measures the width of a [TextView] and set the measured with.
   * If the width of the parent XML layout is the `WRAP_CONTENT`, and the width of [TextView]
   * in the parent layout is `WRAP_CONTENT`, this method will measure the size of the width exactly.
   *
   * @param textView a target textView for measuring text width.
   */
  private fun measureTextWidth(textView: TextView, rootView: View) {
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
   * Traverse a [ViewGroup]'s view hierarchy and measure each [TextView] for measuring
   * the specific height of the [TextView] and calculating the proper height size of the balloon.
   *
   * @param parent a parent view for traversing and measuring.
   */
  private fun traverseAndMeasureTextWidth(parent: ViewGroup) {
    parent.forEach { child ->
      if (child is TextView) {
        measureTextWidth(child, parent)
      } else if (child is ViewGroup) {
        traverseAndMeasureTextWidth(child)
      }
    }
  }

  /** gets measured width size of the balloon popup text label. */
  private fun getMeasuredTextWidth(measuredWidth: Int, rootView: View): Int {
    val displayWidth = displaySize.x
    val spaces = rootView.paddingLeft + rootView.paddingRight + if (builder.iconDrawable != null) {
      builder.iconWidth + builder.iconSpace
    } else 0 + builder.marginRight + builder.marginLeft + (builder.arrowSize * 2)
    val maxTextWidth = builder.maxWidth - spaces

    return when {
      builder.widthRatio != NO_Float_VALUE ->
        (displayWidth * builder.widthRatio).toInt() - spaces
      builder.minWidthRatio != NO_Float_VALUE || builder.maxWidthRatio != NO_Float_VALUE -> {
        val maxWidthRatio =
          if (builder.maxWidthRatio != NO_Float_VALUE) builder.maxWidthRatio else 1f
        measuredWidth.coerceAtMost((displayWidth * maxWidthRatio).toInt() - spaces)
      }
      builder.width != BalloonSizeSpec.WRAP && builder.width <= displayWidth ->
        builder.width - spaces
      else -> measuredWidth.coerceAtMost(maxTextWidth)
    }
  }

  /** gets measured height size of the balloon popup. */
  public fun getMeasuredHeight(): Int {
    if (builder.height != BalloonSizeSpec.WRAP) {
      return builder.height
    }
    return this.binding.root.measuredHeight
  }

  /** gets a content view of the balloon popup window. */
  public fun getContentView(): ViewGroup {
    return binding.balloonCard
  }

  /** gets a arrow view of the balloon popup window. */
  public fun getBalloonArrowView(): View {
    return binding.balloonArrow
  }

  /** dismiss when the [LifecycleOwner] be on paused. */
  override fun onPause(owner: LifecycleOwner) {
    super.onPause(owner)
    if (builder.dismissWhenLifecycleOnPause) {
      dismiss()
    }
  }

  /** dismiss automatically when lifecycle owner is destroyed. */
  override fun onDestroy(owner: LifecycleOwner) {
    super.onDestroy(owner)
    this.destroyed = true
    this.overlayWindow.dismiss()
    this.bodyWindow.dismiss()
  }

  /** Builder class for creating [Balloon]. */
  @BalloonInlineDsl
  public class Builder(private val context: Context) {
    @Px
    @set:JvmSynthetic
    public var width: Int = BalloonSizeSpec.WRAP

    @Px
    @set:JvmSynthetic
    public var minWidth: Int = 0

    @Px
    @set:JvmSynthetic
    public var maxWidth: Int = displaySize.x

    @FloatRange(from = 0.0, to = 1.0)
    @set:JvmSynthetic
    public var widthRatio: Float = NO_Float_VALUE

    @FloatRange(from = 0.0, to = 1.0)
    @set:JvmSynthetic
    public var minWidthRatio: Float = NO_Float_VALUE

    @FloatRange(from = 0.0, to = 1.0)
    @set:JvmSynthetic
    public var maxWidthRatio: Float = NO_Float_VALUE

    @Px
    @set:JvmSynthetic
    public var height: Int = BalloonSizeSpec.WRAP

    @Px
    @set:JvmSynthetic
    public var paddingLeft: Int = 0

    @Px
    @set:JvmSynthetic
    public var paddingTop: Int = 0

    @Px
    @set:JvmSynthetic
    public var paddingRight: Int = 0

    @Px
    @set:JvmSynthetic
    public var paddingBottom: Int = 0

    @Px
    @set:JvmSynthetic
    public var marginRight: Int = 0

    @Px
    @set:JvmSynthetic
    public var marginLeft: Int = 0

    @Px
    @set:JvmSynthetic
    public var marginTop: Int = 0

    @Px
    @set:JvmSynthetic
    public var marginBottom: Int = 0

    @set:JvmSynthetic
    public var isVisibleArrow: Boolean = true

    @ColorInt
    @set:JvmSynthetic
    public var arrowColor: Int = NO_INT_VALUE

    @set:JvmSynthetic
    public var arrowColorMatchBalloon: Boolean = false

    @Px
    @set:JvmSynthetic
    public var arrowSize: Int = 12.dp

    public val arrowHalfSize: Float
      @JvmSynthetic @Px
      inline get() = arrowSize * 0.5f

    @FloatRange(from = 0.0, to = 1.0)
    @set:JvmSynthetic
    public var arrowPosition: Float = 0.5f

    @set:JvmSynthetic
    public var arrowPositionRules: ArrowPositionRules = ArrowPositionRules.ALIGN_BALLOON

    @set:JvmSynthetic
    public var arrowOrientationRules: ArrowOrientationRules =
      ArrowOrientationRules.ALIGN_ANCHOR

    @set:JvmSynthetic
    public var arrowOrientation: ArrowOrientation = ArrowOrientation.BOTTOM

    @set:JvmSynthetic
    public var arrowDrawable: Drawable? = null

    @set:JvmSynthetic
    public var arrowLeftPadding: Int = 0

    @set:JvmSynthetic
    public var arrowRightPadding: Int = 0

    @set:JvmSynthetic
    public var arrowTopPadding: Int = 0

    @set:JvmSynthetic
    public var arrowBottomPadding: Int = 0

    @set:JvmSynthetic
    public var arrowAlignAnchorPadding: Int = 0

    @set:JvmSynthetic
    public var arrowAlignAnchorPaddingRatio: Float = 2.5f

    @set:JvmSynthetic
    public var arrowElevation: Float = 0f

    @ColorInt
    @set:JvmSynthetic
    public var backgroundColor: Int = Color.BLACK

    @set:JvmSynthetic
    public var backgroundDrawable: Drawable? = null

    @Px
    @set:JvmSynthetic
    public var cornerRadius: Float = 5f.dp

    @set:JvmSynthetic
    public var text: CharSequence = ""

    @ColorInt
    @set:JvmSynthetic
    public var textColor: Int = Color.WHITE

    @set:JvmSynthetic
    public var textIsHtml: Boolean = false

    @set:JvmSynthetic
    public var movementMethod: MovementMethod? = null

    @Sp
    @set:JvmSynthetic
    public var textSize: Float = 12f

    @set:JvmSynthetic
    public var textTypeface: Int = Typeface.NORMAL

    @set:JvmSynthetic
    public var textTypefaceObject: Typeface? = null

    @set:JvmSynthetic
    public var textGravity: Int = Gravity.CENTER

    @set:JvmSynthetic
    public var textForm: TextForm? = null

    @set:JvmSynthetic
    public var iconDrawable: Drawable? = null

    @set:JvmSynthetic
    public var iconGravity: IconGravity = IconGravity.START

    @Px
    @set:JvmSynthetic
    public var iconWidth: Int = 28.dp

    @Px
    @set:JvmSynthetic
    public var iconHeight: Int = 28.dp

    @Px
    @set:JvmSynthetic
    public var iconSpace: Int = 8.dp

    @ColorInt
    @set:JvmSynthetic
    public var iconColor: Int = NO_INT_VALUE

    @set:JvmSynthetic
    public var iconForm: IconForm? = null

    @FloatRange(from = 0.0, to = 1.0)
    @set:JvmSynthetic
    public var alpha: Float = 1f

    @set:JvmSynthetic
    public var elevation: Float = 2f.dp

    @set:JvmSynthetic
    public var layout: View? = null

    @LayoutRes
    @set:JvmSynthetic
    public var layoutRes: Int? = null

    @set:JvmSynthetic
    public var isVisibleOverlay: Boolean = false

    @ColorInt
    @set:JvmSynthetic
    public var overlayColor: Int = Color.TRANSPARENT

    @Px
    @set:JvmSynthetic
    public var overlayPadding: Float = 0f

    @ColorInt
    @set:JvmSynthetic
    public var overlayPaddingColor: Int = Color.TRANSPARENT

    @set:JvmSynthetic
    public var overlayPosition: Point? = null

    @set:JvmSynthetic
    public var overlayShape: BalloonOverlayShape = BalloonOverlayOval

    @set:JvmSynthetic
    public var onBalloonClickListener: OnBalloonClickListener? = null

    @set:JvmSynthetic
    public var onBalloonDismissListener: OnBalloonDismissListener? = null

    @set:JvmSynthetic
    public var onBalloonInitializedListener: OnBalloonInitializedListener? = null

    @set:JvmSynthetic
    public var onBalloonOutsideTouchListener: OnBalloonOutsideTouchListener? = null

    @set:JvmSynthetic
    public var onBalloonTouchListener: View.OnTouchListener? = null

    @set:JvmSynthetic
    public var onBalloonOverlayTouchListener: View.OnTouchListener? = null

    @set:JvmSynthetic
    public var onBalloonOverlayClickListener: OnBalloonOverlayClickListener? = null

    @set:JvmSynthetic
    public var dismissWhenTouchOutside: Boolean = true

    @set:JvmSynthetic
    public var dismissWhenShowAgain: Boolean = false

    @set:JvmSynthetic
    public var dismissWhenClicked: Boolean = false

    @set:JvmSynthetic
    public var dismissWhenOverlayClicked: Boolean = true

    @set:JvmSynthetic
    public var dismissWhenLifecycleOnPause: Boolean = false

    @set:JvmSynthetic
    public var passTouchEventToAnchor: Boolean = false

    @set:JvmSynthetic
    public var autoDismissDuration: Long = NO_LONG_VALUE

    @set:JvmSynthetic
    public var lifecycleOwner: LifecycleOwner? = null

    @set:JvmSynthetic
    public var lifecycleObserver: LifecycleObserver? = null

    @StyleRes
    @set:JvmSynthetic
    public var balloonAnimationStyle: Int = NO_INT_VALUE

    @StyleRes
    @set:JvmSynthetic
    public var balloonOverlayAnimationStyle: Int = NO_INT_VALUE

    @set:JvmSynthetic
    public var balloonAnimation: BalloonAnimation = BalloonAnimation.FADE

    @set:JvmSynthetic
    public var balloonOverlayAnimation: BalloonOverlayAnimation = BalloonOverlayAnimation.FADE

    @set:JvmSynthetic
    public var circularDuration: Long = 500L

    @set:JvmSynthetic
    public var balloonHighlightAnimation: BalloonHighlightAnimation = BalloonHighlightAnimation.NONE

    @StyleRes
    @set:JvmSynthetic
    public var balloonHighlightAnimationStyle: Int = NO_INT_VALUE

    @set:JvmSynthetic
    public var balloonHighlightAnimationStartDelay: Long = 0L

    @set:JvmSynthetic
    public var balloonRotateAnimation: BalloonRotateAnimation? = null

    @set:JvmSynthetic
    public var preferenceName: String? = null

    @set:JvmSynthetic
    public var showTimes: Int = 1

    @set:JvmSynthetic
    public var runIfReachedShowCounts: (() -> Unit)? = null

    @set:JvmSynthetic
    public var isRtlLayout: Boolean =
      context.resources.configuration.layoutDirection == LayoutDirection.RTL

    @set:JvmSynthetic
    public var supportRtlLayoutFactor: Int = LTR.unaryMinus(isRtlLayout)

    @set:JvmSynthetic
    public var isFocusable: Boolean = true

    @set:JvmSynthetic
    public var isStatusBarVisible: Boolean = true

    @set:JvmSynthetic
    public var isAttachedInDecor: Boolean = true

    /** sets the width size. */
    public fun setWidth(@Dp value: Int): Builder = apply {
      require(
        value > 0 || value == BalloonSizeSpec.WRAP
      ) { "The width of the balloon must bigger than zero." }
      this.width = value.dp
    }

    /** sets the width size using a dimension resource. */
    public fun setWidthResource(@DimenRes value: Int): Builder = apply {
      this.width = context.dimenPixel(value)
    }

    /**
     * sets the minimum size of the width.
     * this functionality works only with the [BalloonSizeSpec.WRAP].
     */
    public fun setMinWidth(@Dp value: Int): Builder = apply {
      this.minWidth = value.dp
    }

    /**
     * sets the minimum size of the width using a dimension resource.
     * this functionality works only with the [BalloonSizeSpec.WRAP].
     */
    public fun setMinWidthResource(@DimenRes value: Int): Builder = apply {
      this.minWidth = context.dimenPixel(value)
    }

    /**
     * sets the maximum size of the width.
     * this functionality works only with the [BalloonSizeSpec.WRAP].
     */
    public fun setMaxWidth(@Dp value: Int): Builder = apply {
      this.maxWidth = value.dp
    }

    /**
     * sets the maximum size of the width using a dimension resource.
     * this functionality works only with the [BalloonSizeSpec.WRAP].
     */
    public fun setMaxWidthResource(@DimenRes value: Int): Builder = apply {
      this.maxWidth = context.dimenPixel(value)
    }

    /** sets the width size by the display screen size ratio. */
    public fun setWidthRatio(
      @FloatRange(from = 0.0, to = 1.0) value: Float
    ): Builder = apply { this.widthRatio = value }

    /** sets the minimum width size by the display screen size ratio. */
    public fun setMinWidthRatio(
      @FloatRange(from = 0.0, to = 1.0) value: Float
    ): Builder = apply { this.minWidthRatio = value }

    /** sets the maximum width size by the display screen size ratio. */
    public fun setMaxWidthRatio(
      @FloatRange(from = 0.0, to = 1.0) value: Float
    ): Builder = apply { this.maxWidthRatio = value }

    /** sets the height size. */
    public fun setHeight(@Dp value: Int): Builder = apply {
      require(
        value > 0 || value == BalloonSizeSpec.WRAP
      ) { "The height of the balloon must bigger than zero." }
      this.height = value.dp
    }

    /** sets the height size using a dimension resource. */
    public fun setHeightResource(@DimenRes value: Int): Builder = apply {
      this.height = context.dimenPixel(value)
    }

    /** sets the width and height sizes of the balloon. */
    public fun setSize(@Dp width: Int, @Dp height: Int): Builder = apply {
      setWidth(width)
      setHeight(height)
    }

    /** sets the width and height sizes of the balloon using a dimension resource. */
    public fun setSizeResource(@DimenRes width: Int, @DimenRes height: Int): Builder = apply {
      setWidthResource(width)
      setHeightResource(height)
    }

    /** sets the padding on the balloon content all directions. */
    public fun setPadding(@Dp value: Int): Builder = apply {
      setPaddingLeft(value)
      setPaddingTop(value)
      setPaddingRight(value)
      setPaddingBottom(value)
    }

    /** sets the padding on the balloon content all directions using dimension resource. */
    public fun setPaddingResource(@DimenRes value: Int): Builder = apply {
      val padding = context.dimenPixel(value)
      this.paddingLeft = padding
      this.paddingTop = padding
      this.paddingRight = padding
      this.paddingBottom = padding
    }

    /** sets the horizontal (right and left) padding on the balloon content. */
    public fun setPaddingHorizontal(@Dp value: Int): Builder = apply {
      setPaddingLeft(value)
      setPaddingRight(value)
    }

    /** sets the horizontal (right and left) padding on the balloon content using dimension resource. */
    public fun setPaddingHorizontalResource(@DimenRes value: Int): Builder = apply {
      setPaddingLeftResource(value)
      setPaddingRightResource(value)
    }

    /** sets the vertical (top and bottom) padding on the balloon content. */
    public fun setPaddingVertical(@Dp value: Int): Builder = apply {
      setPaddingTop(value)
      setPaddingBottom(value)
    }

    /** sets the vertical (top and bottom) padding on the balloon content using dimension resource. */
    public fun setPaddingVerticalResource(@DimenRes value: Int): Builder = apply {
      setPaddingTopResource(value)
      setPaddingBottomResource(value)
    }

    /** sets the left padding on the balloon content. */
    public fun setPaddingLeft(@Dp value: Int): Builder = apply { this.paddingLeft = value.dp }

    /** sets the left padding on the balloon content using dimension resource. */
    public fun setPaddingLeftResource(@DimenRes value: Int): Builder = apply {
      this.paddingLeft = context.dimenPixel(value)
    }

    /** sets the top padding on the balloon content. */
    public fun setPaddingTop(@Dp value: Int): Builder = apply { this.paddingTop = value.dp }

    /** sets the top padding on the balloon content using dimension resource. */
    public fun setPaddingTopResource(@DimenRes value: Int): Builder = apply {
      this.paddingTop = context.dimenPixel(value)
    }

    /** sets the right padding on the balloon content. */
    public fun setPaddingRight(@Dp value: Int): Builder = apply {
      this.paddingRight = value.dp
    }

    /** sets the right padding on the balloon content using dimension resource. */
    public fun setPaddingRightResource(@DimenRes value: Int): Builder = apply {
      this.paddingRight = context.dimenPixel(value)
    }

    /** sets the bottom padding on the balloon content. */
    public fun setPaddingBottom(@Dp value: Int): Builder = apply {
      this.paddingBottom = value.dp
    }

    /** sets the bottom padding on the balloon content using dimension resource. */
    public fun setPaddingBottomResource(@DimenRes value: Int): Builder = apply {
      this.paddingBottom = context.dimenPixel(value)
    }

    /** sets the margin on the balloon all directions. */
    public fun setMargin(@Dp value: Int): Builder = apply {
      setMarginLeft(value)
      setMarginTop(value)
      setMarginRight(value)
      setMarginBottom(value)
    }

    /** sets the margin on the balloon all directions using a dimension resource. */
    public fun setMarginResource(@DimenRes value: Int): Builder = apply {
      val margin = context.dimenPixel(value)
      this.marginLeft = margin
      this.marginTop = margin
      this.marginRight = margin
      this.marginBottom = margin
    }

    /** sets the horizontal (left and right) margins on the balloon. */
    public fun setMarginHorizontal(@Dp value: Int): Builder = apply {
      setMarginLeft(value)
      setMarginRight(value)
    }

    /** sets the horizontal (left and right) margins on the balloon using a dimension resource. */
    public fun setMarginHorizontalResource(@DimenRes value: Int): Builder = apply {
      setMarginLeftResource(value)
      setMarginRightResource(value)
    }

    /** sets the vertical (top and bottom) margins on the balloon. */
    public fun setMarginVertical(@Dp value: Int): Builder = apply {
      setMarginTop(value)
      setMarginBottom(value)
    }

    /** sets the vertical (top and bottom) margins on the balloon using a dimension resource. */
    public fun setMarginVerticalResource(@DimenRes value: Int): Builder = apply {
      setMarginTopResource(value)
      setMarginBottomResource(value)
    }

    /** sets the left margin on the balloon. */
    public fun setMarginLeft(@Dp value: Int): Builder = apply {
      this.marginLeft = value.dp
    }

    /** sets the left margin on the balloon using dimension resource. */
    public fun setMarginLeftResource(@DimenRes value: Int): Builder = apply {
      this.marginLeft = context.dimenPixel(value)
    }

    /** sets the top margin on the balloon. */
    public fun setMarginTop(@Dp value: Int): Builder = apply {
      this.marginTop = value.dp
    }

    /** sets the top margin on the balloon using dimension resource. */
    public fun setMarginTopResource(@DimenRes value: Int): Builder = apply {
      this.marginTop = context.dimenPixel(value)
    }

    /** sets the right margin on the balloon. */
    public fun setMarginRight(@Dp value: Int): Builder = apply {
      this.marginRight = value.dp
    }

    /** sets the right margin on the balloon using dimension resource. */
    public fun setMarginRightResource(@DimenRes value: Int): Builder = apply {
      this.marginRight = context.dimenPixel(value)
    }

    /** sets the bottom margin on the balloon. */
    public fun setMarginBottom(@Dp value: Int): Builder = apply {
      this.marginBottom = value.dp
    }

    /** sets the bottom margin on the balloon using dimension resource. */
    public fun setMarginBottomResource(@DimenRes value: Int): Builder = apply {
      this.marginBottom = context.dimenPixel(value)
    }

    /** sets the visibility of the arrow. */
    public fun setIsVisibleArrow(value: Boolean): Builder = apply { this.isVisibleArrow = value }

    /** sets a color of the arrow. */
    public fun setArrowColor(@ColorInt value: Int): Builder = apply { this.arrowColor = value }

    /**
     * sets if arrow color should match the color of the balloon card.
     * Overrides [arrowColor]. Does not work with custom arrows.
     */
    public fun setArrowColorMatchBalloon(value: Boolean): Builder = apply {
      this.arrowColorMatchBalloon = value
    }

    /** sets a color of the arrow using a resource. */
    public fun setArrowColorResource(@ColorRes value: Int): Builder = apply {
      this.arrowColor = context.contextColor(value)
    }

    /** sets the size of the arrow. */
    public fun setArrowSize(@Dp value: Int): Builder = apply {
      this.arrowSize =
        if (value == BalloonSizeSpec.WRAP) {
          BalloonSizeSpec.WRAP
        } else {
          value.dp
        }
    }

    /** sets the size of the arrow using dimension resource. */
    public fun setArrowSizeResource(@DimenRes value: Int): Builder = apply {
      this.arrowSize = context.dimenPixel(value)
    }

    /** sets the arrow position by popup size ration. The popup size depends on [arrowOrientation]. */
    public fun setArrowPosition(
      @FloatRange(from = 0.0, to = 1.0) value: Float
    ): Builder = apply { this.arrowPosition = value }

    /**
     * ArrowPositionRules determines the position of the arrow depending on the aligning rules.
     *
     * [ArrowPositionRules.ALIGN_BALLOON]: Align the arrow position depending on the balloon popup body.
     * [ArrowPositionRules.ALIGN_ANCHOR]: Align the arrow position depending on an anchor.
     */
    public fun setArrowPositionRules(value: ArrowPositionRules): Builder =
      apply { this.arrowPositionRules = value }

    /** sets the arrow orientation using [ArrowOrientation]. */
    public fun setArrowOrientation(value: ArrowOrientation): Builder = apply {
      this.arrowOrientation = value
    }

    /**
     * ArrowOrientationRules determines the orientation of the arrow depending on the aligning rules.
     *
     * [ArrowOrientationRules.ALIGN_ANCHOR]: Align depending on the position of an anchor.
     * [ArrowOrientationRules.ALIGN_FIXED]: Align to fixed [ArrowOrientation].
     */
    public fun setArrowOrientationRules(value: ArrowOrientationRules): Builder = apply {
      this.arrowOrientationRules = value
    }

    /** sets a custom drawable of the arrow. */
    public fun setArrowDrawable(value: Drawable?): Builder = apply {
      this.arrowDrawable = value?.mutate()
      if (value != null && arrowSize == BalloonSizeSpec.WRAP) {
        arrowSize = max(value.intrinsicWidth, value.intrinsicHeight)
      }
    }

    /** sets a custom drawable of the arrow using the resource. */
    public fun setArrowDrawableResource(@DrawableRes value: Int): Builder = apply {
      setArrowDrawable(context.contextDrawable(value))
    }

    /** sets the left padding of the arrow. */
    public fun setArrowLeftPadding(@Dp value: Int): Builder = apply {
      this.arrowLeftPadding = value.dp
    }

    /** sets the left padding of the arrow using the resource. */
    public fun setArrowLeftPaddingResource(@DimenRes value: Int): Builder = apply {
      this.arrowLeftPadding = context.dimenPixel(value)
    }

    /** sets the right padding of the arrow. */
    public fun setArrowRightPadding(@Dp value: Int): Builder = apply {
      this.arrowRightPadding = value.dp
    }

    /** sets the right padding of the arrow using the resource. */
    public fun setArrowRightPaddingResource(@DimenRes value: Int): Builder = apply {
      this.arrowRightPadding = context.dimenPixel(value)
    }

    /** sets the top padding of the arrow. */
    public fun setArrowTopPadding(@Dp value: Int): Builder = apply {
      this.arrowTopPadding = value.dp
    }

    /** sets the top padding of the arrow using the resource. */
    public fun setArrowTopPaddingResource(@DimenRes value: Int): Builder = apply {
      this.arrowTopPadding = context.dimenPixel(value)
    }

    /** sets the bottom padding of the arrow. */
    public fun setArrowBottomPadding(@Dp value: Int): Builder = apply {
      this.arrowBottomPadding = value.dp
    }

    /** sets the bottom padding of the arrow using the resource. */
    public fun setArrowBottomPaddingResource(@DimenRes value: Int): Builder = apply {
      this.arrowBottomPadding = context.dimenPixel(value)
    }

    /** sets the padding of the arrow when aligning anchor using with [ArrowPositionRules.ALIGN_ANCHOR]. */
    public fun setArrowAlignAnchorPadding(@Dp value: Int): Builder = apply {
      this.arrowAlignAnchorPadding = value.dp
    }

    /** sets the padding of the arrow the resource when aligning anchor using with [ArrowPositionRules.ALIGN_ANCHOR]. */
    public fun setArrowAlignAnchorPaddingResource(@DimenRes value: Int): Builder = apply {
      this.arrowAlignAnchorPadding = context.dimenPixel(value)
    }

    /** sets the padding ratio of the arrow when aligning anchor using with [ArrowPositionRules.ALIGN_ANCHOR]. */
    public fun setArrowAlignAnchorPaddingRatio(value: Float): Builder = apply {
      this.arrowAlignAnchorPaddingRatio = value
    }

    /** sets the elevation of the arrow. */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public fun setArrowElevation(@Dp value: Int): Builder = apply {
      this.arrowElevation = value.dp.toFloat()
    }

    /** sets the elevation of the arrow using dimension resource. */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public fun setArrowElevationResource(@DimenRes value: Int): Builder = apply {
      this.arrowElevation = context.dimen(value)
    }

    /** sets the background color of the arrow and popup. */
    public fun setBackgroundColor(@ColorInt value: Int): Builder =
      apply { this.backgroundColor = value }

    /** sets the background color of the arrow and popup using the resource color. */
    public fun setBackgroundColorResource(@ColorRes value: Int): Builder = apply {
      this.backgroundColor = context.contextColor(value)
    }

    /** sets the background drawable of the popup. */
    public fun setBackgroundDrawable(value: Drawable?): Builder = apply {
      this.backgroundDrawable = value?.mutate()
    }

    /** sets the background drawable of the popup by the resource. */
    public fun setBackgroundDrawableResource(@DrawableRes value: Int): Builder = apply {
      this.backgroundDrawable = context.contextDrawable(value)?.mutate()
    }

    /** sets the corner radius of the popup. */
    public fun setCornerRadius(@Dp value: Float): Builder = apply {
      this.cornerRadius = value.dp
    }

    /** sets the corner radius of the popup using dimension resource. */
    public fun setCornerRadiusResource(@DimenRes value: Int): Builder = apply {
      this.cornerRadius = context.dimen(value)
    }

    /** sets the main text content of the popup. */
    public fun setText(value: CharSequence): Builder = apply { this.text = value }

    /** sets the main text content of the popup using resource. */
    public fun setTextResource(@StringRes value: Int): Builder = apply {
      this.text = context.getString(value)
    }

    /** sets the color of the main text content. */
    public fun setTextColor(@ColorInt value: Int): Builder = apply { this.textColor = value }

    /** sets the color of the main text content using the resource color. */
    public fun setTextColorResource(@ColorRes value: Int): Builder = apply {
      this.textColor = context.contextColor(value)
    }

    /** sets whether the text will be parsed as HTML (using Html.fromHtml(..)) */
    public fun setTextIsHtml(value: Boolean): Builder = apply { this.textIsHtml = value }

    /** sets the movement method for TextView. */
    public fun setMovementMethod(value: MovementMethod): Builder =
      apply { this.movementMethod = value }

    /** sets the size of the main text content. */
    public fun setTextSize(@Sp value: Float): Builder = apply { this.textSize = value }

    /** sets the size of the main text content using dimension resource. */
    public fun setTextSizeResource(@DimenRes value: Int): Builder = apply {
      this.textSize = context.px2Sp(context.dimen(value))
    }

    /** sets the typeface of the main text content. */
    public fun setTextTypeface(value: Int): Builder = apply { this.textTypeface = value }

    /** sets the typeface of the main text content. */
    public fun setTextTypeface(value: Typeface): Builder = apply { this.textTypefaceObject = value }

    /**
     * sets gravity of the text.
     * this only works when the width or setWidthRatio set explicitly.
     */
    public fun setTextGravity(value: Int): Builder = apply {
      this.textGravity = value
    }

    /** applies [TextForm] attributes to the main text content. */
    public fun setTextForm(value: TextForm): Builder = apply { this.textForm = value }

    /** sets the icon drawable of the popup. */
    public fun setIconDrawable(value: Drawable?): Builder =
      apply { this.iconDrawable = value?.mutate() }

    /** sets the icon drawable of the popup using the resource. */
    public fun setIconDrawableResource(@DrawableRes value: Int): Builder = apply {
      this.iconDrawable = context.contextDrawable(value)?.mutate()
    }

    /** sets the icon gravity of the popup using the resource. */
    public fun setIconGravity(value: IconGravity): Builder = apply {
      this.iconGravity = value
    }

    /** sets the width size of the icon drawable. */
    public fun setIconWidth(@Dp value: Int): Builder = apply {
      this.iconWidth = value.dp
    }

    /** sets the width size of the icon drawable using the dimension resource. */
    public fun setIconWidthResource(@DimenRes value: Int): Builder = apply {
      this.iconWidth = context.dimenPixel(value)
    }

    /** sets the height size of the icon drawable. */
    public fun setIconHeight(@Dp value: Int): Builder = apply {
      this.iconHeight = value.dp
    }

    /** sets the height size of the icon drawable using the dimension resource. */
    public fun setIconHeightResource(@DimenRes value: Int): Builder = apply {
      this.iconHeight = context.dimenPixel(value)
    }

    /** sets the size of the icon drawable. */
    public fun setIconSize(@Dp value: Int): Builder = apply {
      setIconWidth(value)
      setIconHeight(value)
    }

    /** sets the size of the icon drawable using the dimension resource. */
    public fun setIconSizeResource(@DimenRes value: Int): Builder = apply {
      setIconWidthResource(value)
      setIconHeightResource(value)
    }

    /** sets the color of the icon drawable. */
    public fun setIconColor(@ColorInt value: Int): Builder = apply { this.iconColor = value }

    /** sets the color of the icon drawable using the resource color. */
    public fun setIconColorResource(@ColorRes value: Int): Builder = apply {
      this.iconColor = context.contextColor(value)
    }

    /** sets the space between the icon and the main text content. */
    public fun setIconSpace(@Dp value: Int): Builder = apply { this.iconSpace = value.dp }

    /** sets the space between the icon and the main text content using dimension resource. */
    public fun setIconSpaceResource(@DimenRes value: Int): Builder = apply {
      this.iconSpace = context.dimenPixel(value)
    }

    /** applies [IconForm] attributes to the icon. */
    public fun setIconForm(value: IconForm): Builder = apply { this.iconForm = value }

    /** sets the alpha value to the popup. */
    public fun setAlpha(@FloatRange(from = 0.0, to = 1.0) value: Float): Builder = apply {
      this.alpha = value
    }

    /** sets the elevation to the popup. */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public fun setElevation(@Dp value: Int): Builder = apply {
      this.elevation = value.dp.toFloat()
    }

    /** sets the elevation to the popup using dimension resource. */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public fun setElevationResource(@DimenRes value: Int): Builder = apply {
      this.elevation = context.dimen(value)
    }

    /** sets a custom layout resource to the popup content. */
    public fun setLayout(@LayoutRes layoutRes: Int): Builder = apply { this.layoutRes = layoutRes }

    /** sets a custom layout view to the popup content. */
    public fun setLayout(layout: View): Builder = apply { this.layout = layout }

    /** sets a [ViewBinding] to the popup content. */
    public fun <T : ViewBinding> setLayout(binding: T): Builder = apply {
      this.layout = binding.root
    }

    /** sets the visibility of the overlay for highlighting an anchor. */
    public fun setIsVisibleOverlay(value: Boolean): Builder =
      apply { this.isVisibleOverlay = value }

    /** background color of the overlay. */
    public fun setOverlayColor(@ColorInt value: Int): Builder = apply { this.overlayColor = value }

    /** background color of the overlay using a color resource. */
    public fun setOverlayColorResource(@ColorRes value: Int): Builder = apply {
      this.overlayColor = context.contextColor(value)
    }

    /** sets a padding value of the overlay shape internally. */
    public fun setOverlayPadding(@Dp value: Float): Builder =
      apply { this.overlayPadding = value.dp }

    /** sets a padding value of the overlay shape internally using dimension resource.. */
    public fun setOverlayPaddingResource(@DimenRes value: Int): Builder = apply {
      this.overlayPadding = context.dimen(value)
    }

    /** sets color of the overlay padding. */
    public fun setOverlayPaddingColor(@ColorInt value: Int): Builder =
      apply { this.overlayPaddingColor = value }

    /** sets color of the overlay padding using a color resource. */
    public fun setOverlayPaddingColorResource(@ColorRes value: Int): Builder = apply {
      this.overlayPaddingColor = context.contextColor(value)
    }

    /** sets a specific position of the overlay shape. */
    public fun setOverlayPosition(value: Point): Builder = apply { this.overlayPosition = value }

    /** sets a shape of the overlay over the anchor view. */
    public fun setOverlayShape(value: BalloonOverlayShape): Builder =
      apply { this.overlayShape = value }

    /** sets is status bar is visible or not in your screen. */
    public fun setIsStatusBarVisible(value: Boolean): Builder = apply {
      this.isStatusBarVisible = value
    }

    /**
     * sets whether the popup window will be attached in the decor frame of its parent window.
     * If you want to show up balloon on your DialogFragment, it's recommended to use with true. (#131)
     */
    public fun setIsAttachedInDecor(value: Boolean): Builder = apply {
      this.isAttachedInDecor = value
    }

    /**
     * sets the [LifecycleOwner] for dismissing automatically when the [LifecycleOwner] is destroyed.
     * It will prevents memory leak : [Avoid Memory Leak](https://github.com/skydoves/balloon#avoid-memory-leak)
     */
    public fun setLifecycleOwner(value: LifecycleOwner?): Builder =
      apply { this.lifecycleOwner = value }

    /**
     * sets the [LifecycleObserver] for observing the the [lifecycleOwner]'s lifecycle states.
     */
    public fun setLifecycleObserver(value: LifecycleObserver): Builder =
      apply { this.lifecycleObserver = value }

    /** sets the balloon showing animation using [BalloonAnimation]. */
    public fun setBalloonAnimation(value: BalloonAnimation): Builder = apply {
      this.balloonAnimation = value
      if (value == BalloonAnimation.CIRCULAR) {
        setFocusable(false)
      }
    }

    /** sets the balloon showing animation using custom xml animation style. */
    public fun setBalloonAnimationStyle(@StyleRes value: Int): Builder = apply {
      this.balloonAnimationStyle = value
    }

    /** sets the balloon overlay showing animation using [BalloonAnimation]. */
    public fun setBalloonOverlayAnimation(value: BalloonOverlayAnimation): Builder = apply {
      this.balloonOverlayAnimation = value
    }

    /** sets the balloon overlay showing animation using custom xml animation style. */
    public fun setBalloonOverlayAnimationStyle(@StyleRes value: Int): Builder = apply {
      this.balloonOverlayAnimationStyle = value
    }

    /**
     * sets the duration of the circular animation.
     * this option only works with [BalloonAnimation.CIRCULAR] value in [setBalloonAnimation].
     */
    public fun setCircularDuration(value: Long): Builder = apply {
      this.circularDuration = value
    }

    /** sets the balloon highlight animation using [BalloonHighlightAnimation]. */
    @JvmOverloads
    public fun setBalloonHighlightAnimation(
      value: BalloonHighlightAnimation,
      startDelay: Long = 0L
    ): Builder = apply {
      this.balloonHighlightAnimation = value
      this.balloonHighlightAnimationStartDelay = startDelay
    }

    /** sets the balloon highlight animation using custom xml animation resource file. */
    @JvmOverloads
    public fun setBalloonHighlightAnimationResource(
      @AnimRes value: Int,
      startDelay: Long = 0L
    ): Builder = apply {
      this.balloonHighlightAnimationStyle = value
      this.balloonHighlightAnimationStartDelay = startDelay
    }

    /** sets a [BalloonRotateAnimation] to give highlight animation. */
    public fun setBalloonRotationAnimation(
      balloonRotateAnimation: BalloonRotateAnimation
    ): Builder = apply {
      this.balloonRotateAnimation = balloonRotateAnimation
    }

    /** sets a [OnBalloonClickListener] to the popup. */
    public fun setOnBalloonClickListener(value: OnBalloonClickListener): Builder = apply {
      this.onBalloonClickListener = value
    }

    /** sets a [OnBalloonDismissListener] to the popup. */
    public fun setOnBalloonDismissListener(value: OnBalloonDismissListener): Builder = apply {
      this.onBalloonDismissListener = value
    }

    /** sets a [OnBalloonInitializedListener] to the popup. */
    public fun setOnBalloonInitializedListener(value: OnBalloonInitializedListener): Builder =
      apply {
        this.onBalloonInitializedListener = value
      }

    /** sets a [OnBalloonOutsideTouchListener] to the popup. */
    public fun setOnBalloonOutsideTouchListener(value: OnBalloonOutsideTouchListener): Builder =
      apply {
        this.onBalloonOutsideTouchListener = value
      }

    /** sets a [View.OnTouchListener] to the popup. */
    public fun setOnBalloonTouchListener(value: View.OnTouchListener): Builder = apply {
      this.onBalloonTouchListener = value
    }

    /** sets a [OnBalloonOverlayClickListener] to the overlay popup. */
    public fun setOnBalloonOverlayClickListener(value: OnBalloonOverlayClickListener): Builder =
      apply {
        this.onBalloonOverlayClickListener = value
      }

    /** sets a [OnBalloonClickListener] to the popup using lambda. */
    @JvmSynthetic
    public fun setOnBalloonClickListener(block: (View) -> Unit): Builder = apply {
      this.onBalloonClickListener = OnBalloonClickListener(block)
    }

    /** sets a [OnBalloonDismissListener] to the popup using lambda. */
    @JvmSynthetic
    public fun setOnBalloonDismissListener(block: () -> Unit): Builder = apply {
      this.onBalloonDismissListener = OnBalloonDismissListener(block)
    }

    /** sets a [OnBalloonInitializedListener] to the popup using lambda. */
    @JvmSynthetic
    public fun setOnBalloonInitializedListener(block: (View) -> Unit): Builder = apply {
      this.onBalloonInitializedListener = OnBalloonInitializedListener(block)
    }

    /** sets a [OnBalloonOutsideTouchListener] to the popup using lambda. */
    @JvmSynthetic
    public fun setOnBalloonOutsideTouchListener(block: (View, MotionEvent) -> Unit): Builder =
      apply {
        this.onBalloonOutsideTouchListener = OnBalloonOutsideTouchListener(block)
        setDismissWhenTouchOutside(false)
      }

    /** sets a [OnBalloonOverlayClickListener] to the overlay popup using lambda. */
    public fun setOnBalloonOverlayClickListener(block: () -> Unit): Builder = apply {
      this.onBalloonOverlayClickListener = OnBalloonOverlayClickListener(block)
    }

    /** dismisses when touch outside. */
    public fun setDismissWhenTouchOutside(value: Boolean): Builder = apply {
      this.dismissWhenTouchOutside = value
      if (!value) {
        setFocusable(value)
      }
    }

    /** sets a [View.OnTouchListener] to the overlay popup. */
    public fun setOnBalloonOverlayTouchListener(value: View.OnTouchListener): Builder = apply {
      this.onBalloonOverlayTouchListener = value
      setDismissWhenOverlayClicked(false)
    }

    /** dismisses when invoked show function again. */
    public fun setDismissWhenShowAgain(value: Boolean): Builder = apply {
      this.dismissWhenShowAgain = value
    }

    /** dismisses when the popup clicked. */
    public fun setDismissWhenClicked(value: Boolean): Builder =
      apply { this.dismissWhenClicked = value }

    /** dismisses when the [LifecycleOwner] be on paused. */
    public fun setDismissWhenLifecycleOnPause(value: Boolean): Builder = apply {
      this.dismissWhenLifecycleOnPause = value
    }

    /** dismisses when the overlay popup is clicked. */
    public fun setDismissWhenOverlayClicked(value: Boolean): Builder = apply {
      this.dismissWhenOverlayClicked = value
    }

    /** pass touch events through the overlay to the anchor. */
    public fun setShouldPassTouchEventToAnchor(value: Boolean): Builder = apply {
      this.passTouchEventToAnchor = value
    }

    /** dismisses automatically some milliseconds later when the popup is shown. */
    public fun setAutoDismissDuration(value: Long): Builder =
      apply { this.autoDismissDuration = value }

    /**
     * sets the preference name for persisting showing counts.
     * This method should be used with the [setShowCounts].
     *
     * @see (https://github.com/skydoves/balloon#persistence)
     */
    public fun setPreferenceName(value: String): Builder = apply { this.preferenceName = value }

    /**
     * sets showing counts which how many times the Balloon popup will be shown up.
     * This method should be used with the [setPreferenceName].
     *
     * @see (https://github.com/skydoves/balloon#persistence)
     */
    public fun setShowCounts(value: Int): Builder = apply { this.showTimes = value }

    /**
     * sets a lambda for invoking after the preference showing counts is reached the goal.
     * This method should be used ith the [setPreferenceName] and [setShowCounts].
     *
     * @see (https://github.com/skydoves/balloon#persistence)
     *
     * @param block A lambda for invoking after the preference showing counts is reached the goal.
     */
    public fun runIfReachedShowCounts(block: () -> Unit): Builder = apply {
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
    public fun runIfReachedShowCounts(runnable: Runnable): Builder = apply {
      runIfReachedShowCounts { runnable.run() }
    }

    /**
     * sets the balloon should support the RTL layout.
     * The RTL layout is enabled by default, but you can disable this by passing false to the [isRtlSupport].
     *
     * @param isRtlSupport Decides the balloon should support RTL layout.
     */
    public fun setRtlSupports(isRtlSupport: Boolean): Builder = apply {
      this.isRtlLayout = isRtlSupport
    }

    /**
     * sets isFocusable option to the body window.
     * if true when the balloon is showing, can not touch other views and
     * onBackPressed will be fired to the balloon.
     * */
    public fun setFocusable(value: Boolean): Builder = apply { this.isFocusable = value }

    /**
     * Create a new instance of the [Balloon] which includes customized attributes.
     *
     * @return A new created instance of the [Balloon].
     */
    public fun build(): Balloon = Balloon(
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
  public abstract class Factory {

    /**
     * Creates a new instance of [Balloon].
     *
     * @return A new created instance of the [Balloon].
     */
    public abstract fun create(context: Context, lifecycle: LifecycleOwner?): Balloon
  }
}
