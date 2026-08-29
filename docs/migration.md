# Migration from 1.x to 2.0.0

Balloon 2.0.0 replaces the View based implementation with a Compose Multiplatform one. The
artifact name stays `com.github.skydoves:balloon`, but everything inside it is new.

If you are not ready to move, **stay on `1.7.6`**. It is the last release of the View based
library and its documentation lives under [Balloon 1.x (View)](legacy-view/getting-started.md).

```kotlin
// keep the View based library
implementation("com.github.skydoves:balloon:1.7.6")
implementation("com.github.skydoves:balloon-compose:1.7.6")
```

## What changed

| | 1.x | 2.0.0 |
| --- | --- | --- |
| Artifacts | `balloon`, `balloon-compose` | `balloon` |
| Platforms | Android | Android, iOS, Desktop (JVM), Web (Wasm) |
| Rendering | `PopupWindow` plus Android views | Compose `Popup` and a Compose `Shape` |
| Package | `com.skydoves.balloon`, `com.skydoves.balloon.compose` | `com.skydoves.balloon` |
| Content | `setText`, `TextForm`, `IconForm`, `setLayout` | a `@Composable` slot |
| Anchoring | you pass a `View` to every show call | `BalloonState` knows its own anchor |
| Lifecycle | `setLifecycleOwner`, manual disposal | composition disposal |

There is no `Context`, no `View`, no `Drawable`, no `Typeface`, and no XML anywhere in the API.

## Dependency

=== "Compose Multiplatform"

    ```kotlin
    kotlin {
        sourceSets {
            commonMain.dependencies {
                implementation("com.github.skydoves:balloon:2.0.0")
            }
        }
    }
    ```

=== "Android only"

    ```kotlin
    dependencies {
        implementation("com.github.skydoves:balloon:2.0.0")
    }
    ```

Remove `balloon-compose`. It is folded into `balloon`.

## Coming from the View API

=== "1.x"

    ```kotlin
    val balloon = Balloon.Builder(context)
        .setWidthRatio(0.7f)
        .setHeight(BalloonSizeSpec.WRAP)
        .setText("Now you can edit your profile!")
        .setTextColorResource(R.color.white)
        .setTextSize(15f)
        .setIconDrawableResource(R.drawable.ic_edit)
        .setArrowSize(10)
        .setArrowPosition(0.5f)
        .setPadding(12)
        .setCornerRadius(8f)
        .setBackgroundColorResource(R.color.purple)
        .setBalloonAnimation(BalloonAnimation.ELASTIC)
        .setLifecycleOwner(lifecycleOwner)
        .build()

    balloon.showAlignTop(anchorView)
    ```

=== "2.0.0"

    ```kotlin
    val style = rememberBalloonBuilder {
        setWidthRatio(0.7f)
        setArrowSize(10.dp)
        setArrowPosition(0.5f)
        setPadding(12.dp)
        setCornerRadius(8.dp)
        setBackgroundColor(Color(0xFF785EF0))
        setBalloonAnimation(BalloonAnimation.ELASTIC)
    }
    val balloonState = rememberBalloonState(style)

    Balloon(
        state = balloonState,
        balloonContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Now you can edit your profile!", color = Color.White, fontSize = 15.sp)
            }
        },
    ) {
        Button(onClick = { balloonState.showAlignTop() }) { Text(text = "Edit profile") }
    }
    ```

The biggest shift: **content is a Compose slot, not a builder property.** Text color, size,
typeface, icons, and custom layouts all move into the slot.

## Coming from balloon-compose

This one is nearly a drop-in. The entry points already have the same names.

=== "1.x"

    ```kotlin
    import com.skydoves.balloon.compose.rememberBalloonBuilder
    import com.skydoves.balloon.compose.rememberBalloonState
    import com.skydoves.balloon.compose.balloon

    val builder = rememberBalloonBuilder {
        setArrowSize(10)
        setPadding(12)
        setCornerRadius(8f)
        setBackgroundColorResource(R.color.purple)
    }
    val balloonState = rememberBalloonState(builder)

    Button(
        modifier = Modifier.balloon(balloonState) { Text("Tooltip") },
        onClick = { balloonState.showAlignTop() },
    ) { Text("Anchor") }
    ```

=== "2.0.0"

    ```kotlin
    import com.skydoves.balloon.BalloonHost
    import com.skydoves.balloon.rememberBalloonBuilder
    import com.skydoves.balloon.rememberBalloonState
    import com.skydoves.balloon.balloon

    val style = rememberBalloonBuilder {
        setArrowSize(10.dp)
        setPadding(12.dp)
        setCornerRadius(8.dp)
        setBackgroundColor(Color(0xFF785EF0))
    }
    val balloonState = rememberBalloonState(style)

    BalloonHost {
        Button(
            modifier = Modifier.balloon(balloonState) { Text("Tooltip") },
            onClick = { balloonState.showAlignTop() },
        ) { Text("Anchor") }
    }
    ```

Three things to change:

1. Update the imports from `com.skydoves.balloon.compose` to `com.skydoves.balloon`
2. Wrap the screen in `BalloonHost`
3. Swap `Int` and resource id arguments for `Dp` and `Color`

## Setter mapping

Everything below is verified against the View implementation. Both demos rendered the same 89
configurations with fixed size, sentinel colored anchors and bodies, and the two screenshots
were diffed pixel by pixel. Rows marked **differs** are the deliberate departures, all listed
again under [Known differences](#known-differences).

### Shape and arrow

| 1.x | 2.0.0 | Notes |
| --- | --- | --- |
| `setCornerRadius(Float)`, `*Resource` | `setCornerRadius(Dp)` | |
| `setArrowSize(Int)`, `*Resource` | `setArrowSize(Dp)` | |
| not available | `setArrowSize(width: Dp, height: Dp)` | new overload |
| `setArrowWidth(Int)`, `setArrowHeight(Int)` | `setArrowWidth(Dp)`, `setArrowHeight(Dp)` | width is the base, height the protrusion, on every orientation. 1.x swaps them for START and END, which misplaces a non square arrow there |
| `setIsVisibleArrow(Boolean)` | `setIsVisibleArrow(Boolean)` | **differs**: hiding the arrow also releases its space, so the body sits flush against the anchor |
| `setArrowOrientation(...)` | `setArrowOrientation(...)` | **differs**: leaving it unset derives the edge from the alignment, so the arrow points at the anchor without being named |
| `setArrowOrientationRules(...)` | `setArrowOrientationRules(...)` | `ALIGN_FIXED` keeps an explicit orientation through a flip |
| `setArrowPosition(Float)` | `setArrowPosition(Float)` | clamped to `0f..1f`, measured against the popup box as before |
| `setArrowPositionRules(...)` | `setArrowPositionRules(...)` | `ALIGN_ANCHOR` reproduces `getArrowConstraintPositionX` including its clamp band |
| `setArrowAlignAnchorPadding(Int)`, `*Resource` | `setArrowAlignAnchorPadding(Dp)` | |
| `setArrowAlignAnchorPaddingRatio(Float)` | same | still `2.5f` by default |
| `setArrowColor(Int)`, `*Resource` | `setArrowColor(Color)` | `Color.Unspecified` inherits the background |
| `setArrowColorMatchBalloon(true)` | `setArrowColor(Color.Unspecified)` | the unspecified sentinel is "match balloon" |
| `setArrowDrawable(...)`, `*Resource` | not supported | no `Drawable` in common code, the arrow is part of the shape |
| `setArrowLeftPadding`, `Top`, `Right`, `BottomPadding` | not supported | those inset the arrow image view, use `setArrowSize` and `setArrowPosition` |
| `setArrowElevation(...)` | not supported | Compose has no per arrow elevation |
| `setIsClipArrowEnabled(...)` | not supported | 2.0.0 always draws one merged body and arrow path, with the default path's geometry, so both 1.x modes land on the same pixels |
| `setBackgroundColor(Int)`, `*Resource` | `setBackgroundColor(Color)` | |
| `setBackgroundDrawable(...)`, `*Resource` | not supported | layer your own painter inside the slot |
| `setBalloonStroke(Int, Float)` | `setBalloonStroke(Color, Dp)`, `setBorder(Color, Dp)` | **differs**: drawn at the thickness you asked for, around the arrow too. 1.x force enabled clip arrow mode, then drew the stroke and clipped half of it away |
| `setAlpha(Float)` | `setAlpha(Float)` | applied once to the whole balloon |
| `setElevation(Int)`, `*Resource` | `setElevation(Dp)` | the inset the popup reserves, which is what the width specs measure against. The shadow itself is not drawn, see below |

### Size and spacing

| 1.x | 2.0.0 | Notes |
| --- | --- | --- |
| `setPadding(Int)`, `*Resource` | `setPadding(Dp)` | |
| `setPadding{Left,Top,Right,Bottom}(Int)` | `setPadding{Start,Top,End,Bottom}(Dp)` | start and end, so it mirrors under RTL |
| `setPaddingHorizontal`, `setPaddingVertical` | same, with `Dp` | each preserves the other axis |
| `setMargin*` (every variant) | `setMargin*` (every variant, `Dp`) | |
| `setWidth(Int)`, `*Resource` | `setWidth(Dp)` | sizes the popup box, card plus margins plus the elevation inset, exactly as `bodyWindow.width` did |
| `setWidthRatio`, `setMinWidthRatio`, `setMaxWidthRatio` | same | fractions of the window, same precedence as 1.x |
| `setMinWidth`, `setMaxWidth`, `*Resource` | `setMinWidth(Dp)`, `setMaxWidth(Dp)` | also popup box bounds |
| `setHeight(Int)`, `*Resource` | `setHeight(Dp)` | popup box height |
| `setSize(w, h)`, `*Resource` | `setSize(Dp, Dp)` | |
| `BalloonSizeSpec.WRAP` | `Dp.Unspecified` | the default |
| `setMeasuredWidth(Int)` | not supported | dead code in 1.x, written but never read |

### Text and icon

The whole `TextForm` and `IconForm` surface is replaced by the `balloonContent` slot. That
covers `setText*`, `setTextColor*`, `setTextSize*`, `setTextIsHtml`, `setMovementMethod`,
`setEnableAutoSized`, `setMin/MaxAutoSizeTextSize`, `setTextTypeface`, `setTextLineSpacing`,
`setTextLetterSpacing`, `setIncludeFontPadding`, `setTextGravity`, `setTextForm`, every
`setIcon*`, and `setLayout(View | @LayoutRes | ViewBinding)`.

```kotlin
balloonContent = {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = Color.White)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "Edit your profile", color = Color.White, fontSize = 15.sp)
    }
}
```

### Overlay

| 1.x | 2.0.0 | Notes |
| --- | --- | --- |
| `setIsVisibleOverlay(Boolean)` | same | requires a `BalloonHost` ancestor |
| `setOverlayColor(Int)`, `*Resource` | `setOverlayColor(Color)` | |
| `setOverlayPadding(Float)`, `*Resource` | `setOverlayPadding(Dp)` | |
| `setOverlayPadding(l, t, r, b)` | `setOverlayPadding(start, top, end, bottom)` | RTL aware |
| `setOverlayShape(...)` | `setOverlayShape(BalloonOverlayShape.…)` | `Empty`, `Rect`, `Oval`, `Circle(radius)`, `RoundRect(radiusX, radiusY)`, `RoundRectPerCorner(topStart, topEnd, bottomEnd, bottomStart)`. **Radii are `Dp` here and raw pixels in 1.x** |
| `setBalloonOverlayAnimation(...)` | same | `NONE` or `FADE` |
| `setDismissWhenOverlayClicked(...)` | same | |
| `setOverlayPaddingColor(Int)`, `*Resource` | `setOverlayPaddingColor(Color)` | fills the band the padding opens up |
| `setOverlayPaddingShader` | not supported | `Shader` is Android only |
| `setOverlayPosition(Point)`, `setOverlayGravity(Int)` | not supported | the cut-out always follows the anchor |

### Animation

| 1.x | 2.0.0 | Notes |
| --- | --- | --- |
| `setBalloonAnimation(...)` | same | same durations, interpolators, and center pivots. `CIRCULAR` also clears focusability, as before |
| `setCircularDuration(Long)` | same | |
| `setBalloonHighlightAnimation(..., startDelay)` | same | `HEARTBEAT`, `SHAKE`, `BREATH`, `ROTATE`, with the original magnitudes and pivots |
| `setBalloonRotationAnimation(...)` | `setBalloonRotationAnimation(BalloonRotateAnimation(...))` | a data class instead of a builder, same parameters |
| `setBalloonAnimationStyle`, `setBalloonOverlayAnimationStyle`, `setBalloonHighlightAnimationResource` | not supported | XML animation styles are Android only |

### Behavior, listeners, lifecycle

| 1.x | 2.0.0 | Notes |
| --- | --- | --- |
| `setDismissWhenTouchOutside(Boolean)` | same | turning it off also clears focusability, as before |
| `setDismissWhenBackPressed(Boolean)` | same | back on Android, Escape elsewhere |
| `setDismissWhenClicked`, `setDismissWhenShowAgain`, `setFocusable`, `setAutoDismissDuration` | same | |
| `setOnBalloonClickListener(...)` | `balloonState.onBalloonClick = { }` | a property on the state, because `BalloonStyle` is value equal data |
| `setOnBalloonDismissListener(...)` | `balloonState.onDismiss = { }` | |
| `setOnBalloonOverlayClickListener(...)` | `balloonState.onOverlayClick = { }` | |
| `setOnBalloonInitializedListener(...)` | `Modifier.onGloballyPositioned` in the slot | |
| `setOnBalloonTouchListener` | not supported | `View.OnTouchListener` and `MotionEvent` are Android only. Use `Modifier.pointerInput` inside the balloon content |
| `setOnBalloonOutsideTouchListener` | not supported | A popup cannot report touches outside itself, so there is nothing to hook. Observe `onDismiss`, or handle the gesture in the layout that owns the anchor |
| `setOnBalloonOverlayTouchListener` | `balloonState.onOverlayClick` | taps only, not raw motion events |
| `setDismissWhenTouchMargin(Boolean)` | same | still defaults to `true`, and still only acts when `setDismissWhenTouchOutside` is on |
| `setShouldPassTouchEventToAnchor(...)` | `setFocusable(false)` | a non focusable popup already lets touches through |
| `setDismissWhenLifecycleOnPause`, `setLifecycleOwner`, `setLifecycleObserver` | not needed | composition disposal dismisses the balloon |
| `setIsStatusBarVisible`, `setIsAttachedInDecor`, `setIsClippingEnabled`, `setIsComposableContent` | not supported | `PopupWindow` specific |
| `setRtlSupports(...)` | not needed | `LocalLayoutDirection` handles RTL throughout |
| `setPreferenceName`, `setShowCounts`, `runIfReachedShowCounts` | not supported | no multiplatform key value store is assumed, gate `show()` with `kotlinx-multiplatform-settings` or your own storage |

### Show and dismiss API

| 1.x | 2.0.0 | Notes |
| --- | --- | --- |
| `showAlignTop/Bottom/Start/End(anchor, x, y)` | `showAlignTop/Bottom/Start/End(x: Dp, y: Dp)` | the state knows its anchor |
| `showAsDropDown(anchor, x, y)` | `showAsDropDown(x: Dp, y: Dp)` | |
| `showAtCenter(anchor, x, y, centerAlign)` | `showAtCenter(centerAlign, x: Dp, y: Dp)` | |
| `showAlign(align, anchor, ...)` | `show(align, x: Dp, y: Dp)` | |
| `awaitAlign*`, `awaitAtCenter`, `awaitAsDropDown` | same names, `suspend` | |
| `update(anchor, x, y)`, `updateAlign*` | `update(align, x: Dp, y: Dp)` | moves without replaying the enter animation |
| `dismiss()` | `dismiss()` | |
| `dismissWithDelay(delay)` | `dismissWithDelay(scope, delayMillis)` | runs on the scope you hand it |
| `isShowing` | `isShowing` or `isVisible` | snapshot state, readable in composition |
| `relayShow*`, `awaitBalloonWindows { }` | not supported | chain `awaitAlign*` calls in a coroutine instead |
| `getMeasuredWidth/Height()`, `getContentView()`, `getBalloonArrowView()` | not applicable | there are no views |

## Known differences

These are the places where 2.0.0 deliberately does not reproduce 1.x. Each one was found by the
pixel diff described above.

1. **A hidden arrow takes no space.** `setIsVisibleArrow(false)` puts the body flush against the
   anchor. 1.x left an `arrowHeight - 1px` gap.
2. **The arrow orientation defaults to pointing at the anchor.** In 1.x `arrowOrientation`
   defaulted to `BOTTOM` and the auto rule only ever swapped an orientation for its opposite on
   the same axis, so `showAlignStart()` on a default builder left the arrow pointing down.
3. **Balloons flip instead of clamping.** When the requested side has no room but the opposite
   side does, the balloon moves and the arrow follows. 1.x only flipped vertically, through
   `PopupWindow`, and horizontally it slid the balloon along the window edge until it overlapped
   its own anchor.
4. **`setBalloonStroke` draws the thickness you asked for**, around the arrow too, and does not
   switch the arrow to a different geometry.
5. **No drop shadow.** `elevation` reserves its space and drives the width math, but the shadow
   is not drawn: Compose can only cast a shadow from a convex outline, and a balloon with an
   arrow notch is not convex. Add `Modifier.shadow(...)` inside the slot if you need one.
6. **`BalloonOverlayShape.Circle` and `RoundRect` take `Dp`**, where 1.x took raw pixels.
   Passing the same number gives a different result, so convert deliberately.
7. **The arrow never enters a rounded corner.** Its base is clamped to the straight part of the
   edge, `cornerRadius + arrowWidth / 2` in from each end. 1.x applied no such clamp, so an
   `arrowPosition` of `0f` or `1f` floated the triangle past the corner.

Everything else matches to the pixel.

## Behavior changes worth knowing

Beyond the deliberate differences above, four things behave differently for the same input.

1. **`setAutoDismissDuration(0L)` now means "never".** 1.x used `-1L` as the disabled sentinel
   and treated `0L` as "dismiss immediately". Here `0L` disables auto dismiss and negatives are
   clamped to it. Pass a positive value, or leave it unset.
2. **`BalloonHighlightAnimation.ROTATE` animates out of the box.** In 1.x
   `balloonRotateAnimation` defaulted to `null`, so `ROTATE` without an explicit
   `setBalloonRotationAnimation` did nothing at all. The default here is a real
   `BalloonRotateAnimation()`, with the same values 1.x's own builder defaulted to.
3. **`BalloonOverlayAnimation.NONE` is now instant.** In 1.x it fell through to
   `Balloon_Normal_Anim`, which scaled the scrim from the centre over 200ms, so "none" was not
   none. Use `FADE` if you want a transition.
4. **`ROTATE`'s perspective is density relative.** `android.graphics.Camera` sat at a fixed
   576px whatever the screen; `graphicsLayer` scales the camera distance by density, so the
   same rotation reads slightly flatter on a dense screen and consistent across devices.

## A note on text color

`backgroundColor` still defaults to `Color.Black`, but the content is your composable now, so a
bare `Text(...)` picks up the ambient `LocalContentColor`. Under a light `MaterialTheme` that is
near black, which is invisible on a black balloon. 1.x set `textColor` to white for you. Set the
color yourself, or give the balloon a background that suits your theme:

```kotlin
balloonContent = { Text(text = "Tooltip", color = Color.White) }
```


## Staying on 1.x

The View based library is not going anywhere, it just stops receiving releases. `1.7.6` remains
on Maven Central and its documentation stays published under
[Balloon 1.x (View)](legacy-view/getting-started.md).

```kotlin
implementation("com.github.skydoves:balloon:1.7.6")
implementation("com.github.skydoves:balloon-compose:1.7.6")
```

Do not mix `1.7.6` and `2.0.0` of `com.github.skydoves:balloon` in one build. They are the same
coordinate, so Gradle resolves to one of them, and the two have no API in common.
