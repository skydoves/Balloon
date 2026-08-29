# Listeners

Listeners live on `BalloonState`, not on the builder. `BalloonStyle` is value equal data so that
two identical styles compare equal and a restyle can be detected cheaply, and storing lambdas
inside it would break that.

```kotlin
val balloonState = rememberBalloonState(style)

balloonState.onBalloonClick = { /* the body was tapped */ }
balloonState.onOverlayClick = { /* the scrim was tapped */ }
balloonState.onDismiss = { /* the balloon closed */ }
```

## onBalloonClick

Fires when the balloon body is tapped, before any dismissal caused by `setDismissWhenClicked`.

```kotlin
val style = rememberBalloonBuilder {
    setDismissWhenClicked(true)
}
balloonState.onBalloonClick = {
    analytics.log("tooltip_tapped")
}
```

A tap that a child of the balloon body consumes does not reach this listener, so a clickable row
inside the balloon behaves the way you would expect.

## onOverlayClick

Fires when the overlay scrim is tapped, before any dismissal caused by
`setDismissWhenOverlayClicked`.

```kotlin
balloonState.onOverlayClick = {
    analytics.log("tour_scrim_tapped")
}
```

## onDismiss

Fires exactly once per visible to hidden transition. Calling `dismiss()` on an already hidden
balloon is a no-op and does not re-fire it.

```kotlin
balloonState.onDismiss = {
    preferences.markTooltipSeen()
}
```

It fires for every reason a balloon can close, including the auto dismiss timer, an outside tap,
the back key, and the anchor leaving the composition or scrolling out of the window.

## Observing state instead

Often you do not need a callback at all. `isVisible` is snapshot state:

```kotlin
val rotation by animateFloatAsState(if (balloonState.isVisible) 180f else 0f)

Icon(
    modifier = Modifier.rotate(rotation),
    imageVector = Icons.Default.ExpandMore,
    contentDescription = null,
)
```

And `await()` turns "run this after it closes" into straight line code:

```kotlin
LaunchedEffect(Unit) {
    balloonState.awaitAlignBottom()
    preferences.markTooltipSeen()
}
```

## What is not here

`OnBalloonInitializedListener`, `OnBalloonOutsideTouchListener`, `setOnBalloonTouchListener`, and
`setOnBalloonOverlayTouchListener` are gone. They existed to expose `View` and `MotionEvent`
plumbing that has no multiplatform equivalent. Use Compose instead:

| 1.x | 2.0.0 |
| --- | --- |
| `setOnBalloonInitializedListener` | `Modifier.onGloballyPositioned` inside the balloon content |
| `setOnBalloonOutsideTouchListener` | `setDismissWhenTouchOutside(false)` plus your own handling |
| `setOnBalloonTouchListener` | `Modifier.pointerInput` inside the balloon content |
| `setOnBalloonOverlayTouchListener` | `onOverlayClick` |
