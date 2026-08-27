# Migrating to `balloon-compose-multiplatform`

This guide is for users of the existing `balloon` (Android-only) and
`balloon-compose` (Android-only Compose wrapper) libraries who want to share
their tooltip code across Android, iOS, Desktop and Wasm/JS targets.

> **The existing `balloon` and `balloon-compose` modules are unchanged.** This
> module is purely additive — your existing Android-only code keeps working
> while you migrate one screen at a time.

## TL;DR

The KMP module ships a **migration-friendly facade** with the same names you
already know:

- `Balloon.Builder` with most of the original setters
- `rememberBalloonBuilder { ... }` Compose DSL
- `BalloonAnimation`, `ArrowOrientation`, `ArrowPositionRules` enums

What's gone (and why):

- All `*Resource(...)` setters — KMP has no `R.dimen` / `R.color` / `R.string`.
  Pass values directly: `12.dp`, `Color(0xFFAA0000)`, `"text literal"`.
- All `Drawable` / `View` / `Typeface` / `MovementMethod` setters — those types
  are Android-only.
- `Lifecycle` integration — composition lifecycle handles dismissal in Compose.
- Overlay / highlight / preference / show-counts / circular-reveal features —
  these layered features will land in follow-up releases as the KMP renderer
  matures.

## Old vs. New, side-by-side

### Old (Android-only)

```kotlin
// balloon-compose
val builder = rememberBalloonBuilder {
    setArrowSize(10)
    setArrowPosition(0.5f)
    setArrowPositionRules(ArrowPositionRules.ALIGN_BALLOON)
    setWidthRatio(1.0f)
    setHeight(BalloonSizeSpec.WRAP)
    setPaddingResource(R.dimen.tooltip_padding)
    setCornerRadiusResource(R.dimen.tooltip_corner)
    setTextResource(R.string.tooltip_text)
    setTextColorResource(R.color.white)
    setTextIsHtml(true)
    setBackgroundColorResource(R.color.tooltip_bg)
    setBalloonAnimation(BalloonAnimation.ELASTIC)
    setLifecycleOwner(lifecycleOwner)
}
```

### New (KMP, commonMain)

```kotlin
// balloon-compose-multiplatform
val style = rememberBalloonBuilder {
    setArrowSize(10.dp)
    setArrowPosition(0.5f)
    setArrowPositionRules(ArrowPositionRules.ALIGN_BALLOON)
    setMaxWidth(280.dp)                         // widthRatio -> explicit max
    setPadding(12.dp)                           // dimens are inline now
    setCornerRadius(8.dp)
    setBackgroundColor(Color(0xFF272727))
    setBalloonAnimation(BalloonAnimation.ELASTIC)
    setAnimationDurationMillis(250)
}

// Text content moves out of the builder and into the balloonContent slot:
val state = rememberBalloonState(style)
Balloon(
    state = state,
    balloonContent = {
        Text(
            text = "Tooltip text",
            color = Color.White,
        )
    },
) {
    // anchor — the composable the balloon points at
    Button(onClick = { state.showAlignTop() }) { Text("Show") }
}
```

The biggest mental shift: **content is a Compose slot now, not a builder
property.** You no longer call `setText(...)` / `setTextColor(...)` — just put a
`Text(...)` (or any composable) inside the `balloonContent { ... }` slot, and
trigger it with `state.show*()` from the anchor.

## Setter mapping

Everything below is measured against the View implementation: `:app` and `:androidApp` each
ship a parity harness (`ParityActivity` / `KmpParityActivity`) that renders the same 89
configurations with fixed-size, sentinel-coloured anchors and bodies, and a driver diffs the
two screenshots pixel for pixel. Rows marked **differs** are the cases where the two
implementations deliberately part ways; every other supported row is pixel-identical.

### Shape and arrow

| Old `Balloon.Builder` setter | New `Balloon.Builder` setter | Notes |
| --- | --- | --- |
| `setCornerRadius(Float)` (+ `*Resource`) | `setCornerRadius(Dp)` | Pass `Dp` directly. |
| `setArrowSize(Int)` (+ `*Resource`) | `setArrowSize(Dp)` | |
| — | `setArrowSize(width: Dp, height: Dp)` | New convenience overload. |
| `setArrowWidth(Int)` / `setArrowHeight(Int)` (+ `*Resource`) | `setArrowWidth(Dp)` / `setArrowHeight(Dp)` | Width is the base along the edge, height the protrusion — on **every** orientation. The View swaps them for START/END, which misplaces a non-square arrow there. |
| `setIsVisibleArrow(Boolean)` | `setIsVisibleArrow(Boolean)` | **differs:** hiding the arrow here also releases the space it occupied, so the body sits flush against the anchor. The View keeps reserving `arrowHeight - 1px` for an arrow nobody can see. |
| `setArrowOrientation(...)` | `setArrowOrientation(...)` | **differs (better default):** leaving it unset derives the edge from the `BalloonAlign` you show with, so the arrow always points at the anchor. The View defaults to `BOTTOM` and its auto-rule only swaps within one axis, so `showAlignStart()` on a default builder leaves the arrow pointing *down*. |
| `setArrowOrientationRules(...)` | `setArrowOrientationRules(...)` | `ALIGN_FIXED` keeps an explicit orientation even when a lack of room flips the balloon. |
| `setArrowPosition(Float)` | `setArrowPosition(Float)` | Clamped to `0f..1f`. Measured against the popup box (margins excluded) exactly like the original — so `0.5f` is dead centre. **differs** only at `0f` / `1f`, where the View floats the triangle clear of the body and this clamps it inside the corner curve. |
| `setArrowPositionRules(...)` | `setArrowPositionRules(...)` | Same enum. `ALIGN_ANCHOR` reproduces `getArrowConstraintPositionX`, including its `minPosition` band. |
| `setArrowAlignAnchorPadding(...)` (+ `*Resource`) | `setArrowAlignAnchorPadding(Dp)` | |
| `setArrowAlignAnchorPaddingRatio(Float)` | `setArrowAlignAnchorPaddingRatio(Float)` | Defaults to `2.5f`, as before. |
| `setArrowColor(@ColorInt)` (+ `*Resource`) | `setArrowColor(Color)` | `Color.Unspecified` inherits the background. |
| `setArrowColorMatchBalloon(true)` | `setArrowColor(Color.Unspecified)` | The unspecified sentinel *is* "match balloon". |
| `setArrowDrawable(...)` (+ `*Resource`) | _not supported_ | No `Drawable` in commonMain; the arrow is part of `BalloonShape`. |
| `setArrowLeftPadding` / `Top` / `Right` / `BottomPadding` | _not supported_ | These inset the arrow `ImageView`'s own drawable; use `setArrowSize` / `setArrowPosition`. |
| `setArrowElevation(...)` | _not supported_ | Compose has no per-arrow elevation primitive. |
| `setIsClipArrowEnabled(...)` | _not supported_ | This renderer always draws one merged body+arrow path (what `true` gives) but with the default path's geometry (`arrowHeight - 1px` protrusion), so both original modes land on the same pixels. |
| `setBackgroundColor(@ColorInt)` (+ `*Resource`) | `setBackgroundColor(Color)` | |
| `setBackgroundDrawable(...)` (+ `*Resource`) | _not supported_ | Layer your own painter inside the slot. |
| `setBalloonStroke(@ColorInt, Float)` | `setBalloonStroke(Color, Dp)` / `setBorder(Color, Dp)` | **differs:** the stroke is drawn at the thickness you asked for, around the real silhouette (arrow included). `setBalloonStroke` in the View force-enables clip-arrow mode, then draws the stroke and clips half of it away, so the arrow stops protruding and the line comes out thinner than requested. |
| `setAlpha(Float)` | `setAlpha(Float)` | Applied once to the whole balloon; the View alpha-composites the card and arrow separately. |
| `setElevation(Int)` (+ `*Resource`) | `setElevation(Dp)` | The inset the popup reserves on the axis orthogonal to the arrow — which is what every width spec is measured against. **The drop shadow itself is not drawn** (see *Known differences*). |

### Size and spacing

| Old | New | Notes |
| --- | --- | --- |
| `setPadding(Int)` (+ `*Resource`) | `setPadding(Dp)` | |
| `setPadding{Left,Top,Right,Bottom}(Int)` | `setPadding{Start,Top,End,Bottom}(Dp)` | RTL-aware: `Start`/`End`, not `Left`/`Right`. |
| `setPaddingHorizontal` / `setPaddingVertical` | same, with `Dp` | Each preserves the other axis. |
| `setMargin*` (all variants) | `setMargin*` (all variants, `Dp`) | Keeps the balloon off the window edges and shrinks the width every spec measures. |
| `setWidth(Int)` (+ `*Resource`) | `setWidth(Dp)` | Sizes the **popup box** (card + margins + elevation inset), exactly as `bodyWindow.width` does in the original. Set `setElevation(0.dp)` and no margins for a card of exactly this width. |
| `setWidthRatio` / `setMinWidthRatio` / `setMaxWidthRatio` | same | Fractions of the window width, with the original's precedence: `widthRatio` > min/max ratio > `width` > wrap. |
| `setMinWidth` / `setMaxWidth` (+ `*Resource`) | `setMinWidth(Dp)` / `setMaxWidth(Dp)` | Also popup-box bounds. |
| `setHeight(Int)` (+ `*Resource`) | `setHeight(Dp)` | Popup-box height. |
| `setSize(w, h)` (+ `*Resource`) | `setSize(Dp, Dp)` | |
| `setMeasuredWidth(Int)` | _not supported_ | Dead code in the original — written but never read. |

### Text and icon

The whole `TextForm` / `IconForm` surface is replaced by the `balloonContent { ... }` slot:
put a `Text(...)`, `Icon(...)`, `Row`, or anything else in it and style it the Compose way.
That covers `setText*`, `setTextColor*`, `setTextSize*`, `setTextIsHtml`, `setMovementMethod`,
`setEnableAutoSized`, `setMin/MaxAutoSizeTextSize`, `setTextTypeface`, `setTextLineSpacing`,
`setTextLetterSpacing`, `setIncludeFontPadding`, `setTextGravity`, `setTextForm`, every
`setIcon*`, and `setLayout(View | @LayoutRes | ViewBinding)`.

### Overlay

| Old | New | Notes |
| --- | --- | --- |
| `setIsVisibleOverlay(Boolean)` | `setIsVisibleOverlay(Boolean)` | Requires a `BalloonHost { ... }` ancestor — the scrim is drawn by the host so it can cover the whole window, which a `Popup` cannot. |
| `setOverlayColor(@ColorInt)` (+ `*Resource`) | `setOverlayColor(Color)` | |
| `setOverlayPadding(Float)` (+ `*Resource`) | `setOverlayPadding(Dp)` | |
| `setOverlayPadding(l, t, r, b)` | `setOverlayPadding(start, top, end, bottom)` | RTL-aware. |
| `setOverlayShape(...)` | `setOverlayShape(BalloonOverlayShape.…)` | `Empty` / `Rect` / `Oval` / `Circle(radius: Dp)` / `RoundRect(rx: Dp, ry: Dp)`. **Note the unit:** the original takes raw pixels here, this takes `Dp`. |
| `setBalloonOverlayAnimation(...)` | `setBalloonOverlayAnimation(...)` | `NONE` / `FADE`. |
| `setDismissWhenOverlayClicked(...)` | `setDismissWhenOverlayClicked(Boolean)` | |
| `setOverlayPaddingColor(...)` / `setOverlayPaddingShader(...)` | _not supported_ | The padding ring's own fill; `Shader` is Android-only. |
| `setOverlayPosition(Point)` / `setOverlayGravity(Int)` | _not supported_ | The cut-out always follows the anchor. |

### Animation

| Old | New | Notes |
| --- | --- | --- |
| `setBalloonAnimation(...)` | `setBalloonAnimation(...)` | Same enum, same durations, interpolators and centre pivots as the `res/anim` resources — the AOSP interpolators are ported in `BalloonEasings.kt`. `CIRCULAR` also turns focusability off, as in the original. |
| `setCircularDuration(Long)` | `setCircularDuration(Long)` | |
| `setBalloonHighlightAnimation(..., startDelay)` | same | `HEARTBEAT` / `SHAKE` / `BREATH` / `ROTATE`, with the original magnitudes and pivots. |
| `setBalloonRotationAnimation(...)` | `setBalloonRotationAnimation(BalloonRotateAnimation(...))` | A data class instead of a builder; same `direction` / `turns` / `loops` / `speedMillis` / `degreeX` / `degreeZ`. |
| `setBalloonAnimationStyle(@StyleRes)` / `setBalloonOverlayAnimationStyle` / `setBalloonHighlightAnimationResource` | _not supported_ | XML animation styles are Android-only. |

### Behaviour, listeners and lifecycle

| Old | New | Notes |
| --- | --- | --- |
| `setDismissWhenTouchOutside(Boolean)` | same | Turning it off also clears focusability, as in the original. |
| `setDismissWhenBackPressed(Boolean)` | same | Back key on Android, Escape elsewhere. |
| `setDismissWhenClicked(Boolean)` | same | |
| `setDismissWhenShowAgain(Boolean)` | same | |
| `setFocusable(Boolean)` | same | |
| `setAutoDismissDuration(Long)` | same | `0L` disables. |
| `setOnBalloonClickListener(...)` | `balloonState.onBalloonClick = { … }` | A property on the state: `BalloonStyle` is value-equal data and lambdas would break that. |
| `setOnBalloonDismissListener(...)` | `balloonState.onDismiss = { … }` | |
| `setOnBalloonOverlayClickListener(...)` | `balloonState.onOverlayClick = { … }` | |
| `setOnBalloonInitializedListener(...)` | _not needed_ | Use `Modifier.onGloballyPositioned` inside the slot. |
| `setOnBalloonTouchListener` / `setOnBalloonOutsideTouchListener` / `setOnBalloonOverlayTouchListener` | _not supported_ | `View.OnTouchListener` / `MotionEvent` are Android-only; use `Modifier.pointerInput` in the slot. |
| `setDismissWhenTouchMargin(...)` | _not supported_ | The margin is layout-only here and never takes touches. |
| `setShouldPassTouchEventToAnchor(...)` | `setFocusable(false)` | A non-focusable popup already lets touches through. |
| `setDismissWhenLifecycleOnPause(...)` / `setLifecycleOwner(...)` / `setLifecycleObserver(...)` | _not needed_ | Composition disposal dismisses the balloon. |
| `setIsStatusBarVisible` / `setIsAttachedInDecor` / `setIsClippingEnabled` / `setIsComposableContent` | _not supported_ | `PopupWindow`-specific. |
| `setRtlSupports(...)` | _not needed_ | `LocalLayoutDirection` handles RTL throughout. |
| `setPreferenceName` / `setShowCounts` / `runIfReachedShowCounts` | _not supported_ | No multiplatform key-value store is assumed; gate `state.show()` with `kotlinx-multiplatform-settings` or your own DI. |

### Show / dismiss API

`Balloon.showAlignTop(anchor, …)` and friends become methods on `BalloonState`, which knows
its own anchor:

| Old (`Balloon` / `BalloonWindow`) | New (`BalloonState`) | Notes |
| --- | --- | --- |
| `showAlignTop/Bottom/Start/End(anchor, x, y)` | `showAlignTop/Bottom/Start/End(x: Dp, y: Dp)` | |
| `showAsDropDown(anchor, x, y)` | `showAsDropDown(x: Dp, y: Dp)` | |
| `showAtCenter(anchor, x, y, centerAlign)` | `showAtCenter(centerAlign, x: Dp, y: Dp)` | |
| `showAlign(align, anchor, …)` | `show(align, x: Dp, y: Dp)` | |
| `awaitAlign*` / `awaitAtCenter` / `awaitAsDropDown` | same names, `suspend` | |
| `update(anchor, x, y)` / `updateAlign*` | `update(align, x: Dp, y: Dp)` | Moves without re-running the enter animation. |
| `dismiss()` | `dismiss()` | |
| `dismissWithDelay(delay)` | `dismissWithDelay(scope, delayMillis)` | Runs on the caller's scope. |
| `isShowing` | `isShowing` / `isVisible` | |
| `relayShow*` / `awaitBalloonWindows { … }` | _not supported_ | Chain them yourself with `awaitAlign*` in a coroutine. |
| `getMeasuredWidth/Height()` / `getContentView()` / `getBalloonArrowView()` | _not applicable_ | No `View`s involved. |

## Known differences

These are the places where this implementation deliberately does **not** reproduce the View
one. Each was found by the pixel-diff harness described above.

1. **A hidden arrow takes no space.** `setIsVisibleArrow(false)` puts the body flush against
   the anchor; the View leaves an `arrowHeight - 1px` gap.
2. **The arrow orientation defaults to "point at the anchor".** See the table above.
3. **Balloons flip instead of clamping.** When the requested side has no room but the
   opposite side does, the balloon moves to the other side and the arrow follows it. The
   View only ever flips vertically (via `PopupWindow`), and horizontally it slides the
   balloon along the window edge until it overlaps its own anchor.
4. **`setBalloonStroke` draws the thickness you asked for**, around the arrow too, and does
   not switch the arrow to a different geometry.
5. **No drop shadow.** `elevation` reserves its space and drives the width math, but the
   shadow itself is not drawn: Compose can only cast a shadow from a convex outline, and a
   balloon with an arrow notch is not convex. Add `Modifier.shadow(...)` inside the slot if
   you need one.
6. **`BalloonOverlayShape.Circle` / `RoundRect` take `Dp`,** where the original takes raw
   pixels. Passing the same number gives a different result — convert deliberately.
7. **The arrow never enters a rounded corner.** Its base is clamped to the straight part of
   the edge, `cornerRadius + arrowWidth / 2` in from each end. The View applies no such
   clamp, so an `arrowPosition` of `0f` / `1f` — or an `ALIGN_ANCHOR` anchor point that
   falls outside the balloon — floats the triangle over (or past) the corner curve. The
   difference is at most `cornerRadius` and only appears at those extremes.

## Notes for migrators

- **Resources go inline.** `R.dimen.x` becomes `12.dp`, `R.color.y` becomes
  `Color(0xFFAA0000)`, `R.string.z` becomes a `"literal"` (or `stringResource(...)` inside
  the slot composable, *not* on the builder).
- **Padding tracking.** The builder keeps four directional `Dp`s and assembles a single
  `PaddingValues` at `build()` time, so `setPaddingHorizontal` / `setPaddingVertical` stay
  orthogonal without needing a `LayoutDirection` to read an existing `PaddingValues` back.
- **Overlays need a host.** Wrap the screen in `BalloonHost { … }` when any balloon under it
  sets `setIsVisibleOverlay(true)`, or uses `Modifier.balloon`. Forgetting it throws with a
  message that says so rather than silently drawing nothing.
- This is **purely additive** — the existing `balloon` and `balloon-compose` modules are not
  deprecated. Migrate at your own pace, screen by screen.
