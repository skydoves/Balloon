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

// Text content moves out of the builder and into the slot:
val state = rememberBalloonState(style)
Box(modifier = Modifier.balloon(state) {
    Text(
        text = "Tooltip text",
        color = Color.White,
    )
}) { /* anchor */ }
```

The biggest mental shift: **content is a Compose slot now, not a builder
property.** You no longer call `setText(...)` / `setTextColor(...)` — just put a
`Text(...)` (or any composable) inside the `Modifier.balloon { ... }` lambda.

## Setter mapping

| Old `Balloon.Builder` setter | New `Balloon.Builder` setter | Notes |
| --- | --- | --- |
| `setCornerRadius(Float)` | `setCornerRadius(Dp)` | Pass `Dp` directly. |
| `setCornerRadiusResource(@DimenRes)` | `setCornerRadius(value.dp)` | Inline the value. |
| `setArrowSize(Int)` | `setArrowSize(Dp)` | |
| `setArrowSize(width, height)` (n/a) | `setArrowSize(width: Dp, height: Dp)` | New convenience overload. |
| `setArrowSizeResource(@DimenRes)` | `setArrowSize(value.dp)` | |
| `setArrowWidth(Int)` | `setArrowWidth(Dp)` | |
| `setArrowWidthResource(@DimenRes)` | `setArrowWidth(value.dp)` | |
| `setArrowHeight(Int)` | `setArrowHeight(Dp)` | |
| `setArrowHeightResource(@DimenRes)` | `setArrowHeight(value.dp)` | |
| `setArrowOrientation(ArrowOrientation)` | `setArrowOrientation(ArrowOrientation)` | Same enum (re-declared in this package). |
| `setArrowOrientationRules(...)` | _not supported in KMP_ | Auto-derived from `BalloonAlign`; pass an explicit `setArrowOrientation` to fix it. |
| `setArrowPosition(Float)` | `setArrowPosition(Float)` | Now clamped to `0f..1f`. |
| `setArrowPositionRules(...)` | `setArrowPositionRules(...)` | Same enum. |
| `setArrowDrawable(...)` / `setArrowDrawableResource(...)` | _not supported in KMP_ | No `Drawable` in commonMain; arrow is rendered via `BalloonShape`. |
| `setArrowLeftPadding` / `Top` / `Right` / `BottomPadding` (+ `*Resource`) | _not supported in KMP_ | Use `setArrowPosition` to move the arrow along its edge. |
| `setArrowAlignAnchorPadding(...)` (+ `*Resource`) | _not supported in KMP_ | Will be reintroduced when the popup positioner is finalized. |
| `setArrowAlignAnchorPaddingRatio(...)` | _not supported in KMP_ | As above. |
| `setArrowElevation(...)` (+ `*Resource`) | _not supported in KMP_ | Compose has no per-arrow elevation primitive. |
| `setArrowColor(@ColorInt)` | `setArrowColor(Color)` | `Color.Unspecified` inherits the background color. |
| `setArrowColorMatchBalloon(true)` | `setArrowColor(Color.Unspecified)` | Same effect; the unspecified sentinel _is_ "match balloon". |
| `setArrowColorResource(@ColorRes)` | `setArrowColor(Color(0x...))` | |
| `setIsVisibleArrow(false)` | `setArrowSize(0.dp)` | Hiding the arrow == zero-sized arrow. |
| `setBackgroundColor(@ColorInt)` | `setBackgroundColor(Color)` | |
| `setBackgroundColorResource(@ColorRes)` | `setBackgroundColor(Color(0x...))` | |
| `setBackgroundDrawable(...)` (+ `*Resource`) | _not supported in KMP_ | Compose-native rendering uses `BalloonShape`; layer your own painter inside the slot if you need a gradient. |
| `setBalloonStroke(@ColorInt, @Dp Float)` | `setBalloonStroke(Color, Dp)` | Alias for `setBorder`. |
| `setIsClipArrowEnabled(...)` | _not supported in KMP_ | The KMP renderer always clips the arrow into the body shape. |
| `setPadding(Int)` | `setPadding(Dp)` | |
| `setPaddingResource(@DimenRes)` | `setPadding(value.dp)` | |
| `setPadding{Left,Top,Right,Bottom}(Int)` | `setPadding(start, top, end, bottom)` | Use the directional overload. |
| `setPaddingHorizontal(Int)` / `setPaddingVertical(Int)` | `setPaddingHorizontal(Dp)` / `setPaddingVertical(Dp)` | |
| `setPadding{Left,Top,Right,Bottom}Resource(@DimenRes)` | `setPadding(start.dp, top.dp, end.dp, bottom.dp)` | |
| `setMargin*` (all variants) | _not supported in KMP_ | Apply a Compose `Modifier.padding(...)` to the anchor or use `Popup` offsets via `BalloonState.show(xOffset, yOffset)`. |
| `setWidth(Int)` / `setWidthResource` | _not supported in KMP_ | Compose composables size to their content; constrain via the slot's `Modifier`. |
| `setMinWidth*` / `setMaxWidthRatio` / `setWidthRatio` / `setMinWidthRatio` | _not supported in KMP_ | Use `setMaxWidth(Dp)` for an upper bound; otherwise constrain via the slot. |
| `setMaxWidth(Int)` | `setMaxWidth(Dp)` | |
| `setMeasuredWidth(Int)` | _not supported in KMP_ | Anti-pattern in Compose. |
| `setHeight*` / `setSize*` / `setSizeResource` | _not supported in KMP_ | As above. |
| `setText(CharSequence)` (+ `*Resource`) | _not a builder concern_ | Place a `Text(...)` inside the `Modifier.balloon { ... }` slot. |
| `setTextColor(@ColorInt)` (+ `*Resource`) | _not a builder concern_ | Set on the `Text(...)` itself. |
| `setTextSize(Float)` (+ `*Resource`) | _not a builder concern_ | Use `MaterialTheme.typography` or `Text(fontSize = ...)`. |
| `setTextIsHtml(...)` | _not supported in KMP_ | Use `AnnotatedString` / a Markdown library inside the slot. |
| `setMovementMethod(...)` | _not supported in KMP_ | View-only API. |
| `setEnableAutoSized(...)` / `setMin/MaxAutoSizeTextSize(...)` | _not supported in KMP_ | Compose has no equivalent; pre-compute or use `BasicText` with `AutoSize` (Compose 1.7+). |
| `setTextTypeface(Int|Typeface)` | _not supported in KMP_ | `Typeface` is Android-only; pass a `FontFamily` to the `Text(...)` directly. |
| `setTextLineSpacing(...)` / `setTextLetterSpacing(...)` (+ `*Resource`) | _not supported in KMP_ | Set on the `Text(...)` directly. |
| `setIncludeFontPadding(...)` | _not supported in KMP_ | Android-`TextView`-specific. |
| `setTextGravity(Int)` | _not supported in KMP_ | Use `Text(textAlign = ...)`. |
| `setTextForm(TextForm)` | _not supported in KMP_ | View-only abstraction. |
| `setIcon*` (all variants) | _not a builder concern_ | Compose icons go inside the slot — `Row { Icon(...); Text(...) }`. |
| `setAlpha(Float)` | _not supported in KMP_ | Wrap the slot in `Modifier.alpha(...)` if needed. |
| `setElevation(Int)` (+ `*Resource`) | _not supported in KMP_ | Wrap the slot in `Modifier.shadow(...)`. |
| `setLayout(View|@LayoutRes Int|ViewBinding)` | _not a builder concern_ | The whole point of the Compose slot. |
| `setIsVisibleOverlay(...)` and the entire `Overlay*` family | _not yet supported_ | Overlay highlighting will return in a follow-up release. |
| `setOnBalloonClickListener(...)` | _not a builder concern_ | Use `Modifier.clickable { ... }` inside the slot. |
| `setOnBalloonDismissListener(...)` | Observe `BalloonState.isVisible` | Or compose a `LaunchedEffect` keyed on the state. |
| `setOnBalloonInitializedListener(...)` | _not needed in KMP_ | Compose callbacks (`onGloballyPositioned`) fire naturally. |
| `setOnBalloonOutsideTouchListener(...)` / `setOnBalloonOverlayTouchListener(...)` / `setOnBalloonOverlayClickListener(...)` | _not yet supported_ | See overlay note above. |
| `setOnBalloonTouchListener(View.OnTouchListener)` | _not supported in KMP_ | `View.OnTouchListener` is Android-only. |
| `setDismissWhenTouchOutside(...)` | `setDismissWhenTouchOutside(Boolean)` | |
| `setDismissWhenBackPressed(...)` | `setDismissWhenBackPressed(Boolean)` | KMP-equivalent dismissal on Escape / back. |
| `setDismissWhenTouchMargin(...)` | _not yet supported_ | Margin model not finalized for KMP. |
| `setDismissWhenShowAgain(...)` | _not needed in KMP_ | `BalloonState.show()` is idempotent. |
| `setDismissWhenClicked(...)` | _not a builder concern_ | Call `state.dismiss()` from the slot's `Modifier.clickable { ... }`. |
| `setDismissWhenOverlayClicked(...)` | _not yet supported_ | See overlay note above. |
| `setDismissWhenLifecycleOnPause(...)` | _not needed in KMP_ | Composition disposal handles this. |
| `setShouldPassTouchEventToAnchor(...)` | _not yet supported_ | Pending popup-host design in commonMain. |
| `setAutoDismissDuration(Long)` | _not yet supported_ | Use `LaunchedEffect(state) { delay(ms); state.dismiss() }`. |
| `setLifecycleOwner(...)` / `setLifecycleObserver(...)` | _not needed in KMP_ | See above. |
| `setBalloonAnimation(BalloonAnimation)` | `setBalloonAnimation(BalloonAnimation)` | Same enum; new module renames `CIRCULAR` → approximation, see `BalloonTransitions.kt`. |
| `setBalloonAnimationStyle(@StyleRes)` | _not supported in KMP_ | XML animation styles are Android-only. |
| `setBalloonOverlayAnimation*` | _not yet supported_ | Tied to overlay support. |
| `setCircularDuration(Long)` | `setAnimationDurationMillis(Int)` | Generalized to all animations. |
| `setBalloonHighlightAnimation*` / `setBalloonRotationAnimation` | _not yet supported_ | Highlight animations will return as Compose `Modifier`s. |
| `setPreferenceName(...)` / `setShowCounts(...)` / `runIfReachedShowCounts(...)` | _not supported in KMP_ | Use `kotlinx-multiplatform-settings` (or your DI) to gate `state.show()` yourself. |
| `setIsRtlLayout(...)` / `setSupportRtlLayoutFactor(...)` | _not needed in KMP_ | Compose's `LocalLayoutDirection` handles RTL. |
| `setFocusable(...)` | _not yet supported_ | Will be revisited with the popup host. |
| `setIsStatusBarVisible(...)` / `setIsAttachedInDecor(...)` / `setIsClippingEnabled(...)` | _not supported in KMP_ | Android `PopupWindow`-specific. |
| `setIsComposableContent(...)` | _not needed in KMP_ | The slot is _always_ composable in this module. |

## Notes for migrators

- **Resources go inline.** `R.dimen.x` becomes `12.dp`, `R.color.y` becomes
  `Color(0xFFAA0000)`, `R.string.z` becomes a `"literal"` (or fetched via
  `stringResource(...)` inside the slot composable, _not_ on the builder).
- **Padding tracking.** Internally the new builder tracks four directional
  `Dp`s, then assembles a single `PaddingValues` at `build()` time. This keeps
  `setPaddingHorizontal` / `setPaddingVertical` orthogonal without needing a
  `LayoutDirection` to read an existing `PaddingValues` back.
- **`setIsVisibleArrow(false)`** has no direct equivalent — pass
  `setArrowSize(0.dp)` instead. `BalloonShape` already treats zero-sized arrows
  as "no arrow."
- **`setArrowColorMatchBalloon(true)`** is the default behavior: leave
  `arrowColor` at `Color.Unspecified` and the arrow inherits the background.
- This is **purely additive** — the existing `balloon` and `balloon-compose`
  modules are not deprecated. Migrate at your own pace, screen by screen.
