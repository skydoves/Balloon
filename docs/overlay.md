# Overlay

<p align="center">
<img alt="Balloon example" src="https://user-images.githubusercontent.com/24237865/96139366-c7870800-0f39-11eb-9542-e98eac7ef193.gif" align="center" width="21%"/>
<img alt="Balloon example" src="https://user-images.githubusercontent.com/24237865/96138448-c0abc580-0f38-11eb-92e6-daf2f8266a3e.gif" align="center" width="21%"/>
<img alt="Balloon example" src="https://user-images.githubusercontent.com/24237865/96139358-c524ae00-0f39-11eb-82ff-90a4a734e076.gif" align="center" width="21%"/>
<img alt="Balloon example" src="https://user-images.githubusercontent.com/24237865/96138463-c3a6b600-0f38-11eb-8a2d-57cf96c4190c.gif" align="center" width="21%"/>
</p>

An overlay dims the whole window and cuts the anchor out of it. That is how you build a
spotlight style onboarding tour.

## Basic usage

```kotlin
val style = rememberBalloonBuilder {
    setIsVisibleOverlay(true)
    setOverlayColor(Color(0x99000000))
    setOverlayPadding(6.dp)
    setOverlayShape(BalloonOverlayShape.Oval)
}
```

!!! warning "An overlay needs a BalloonHost"

    The scrim is drawn by `BalloonHost`, not by a popup. A Compose `Popup` cannot do the job
    on Android: its layout params are wrap content and the composition root clamps the
    reported size to a measure spec that excludes the navigation bar, so a popup scrim would
    stop short of the bottom of the screen.

    ```kotlin
    BalloonHost(modifier = Modifier.fillMaxSize()) {
        // Balloon(...) and Modifier.balloon(...) anchors go here
    }
    ```

    A balloon with `setIsVisibleOverlay(true)` outside a host throws an
    `IllegalStateException` that says exactly this.

!!! note "The scrim covers the host, not the window"

    The scrim fills the host's `Box` and is composited offscreen so the cut-out can erase
    from it, which means it cannot paint outside those bounds. Balloon 1.x always dimmed the
    whole display, because its scrim had a `MATCH_PARENT` window of its own.

    To get the same result, put `BalloonHost` at the root of an edge-to-edge window with
    `Modifier.fillMaxSize()`. Wrapping only a subtree dims only that subtree, which is
    sometimes what you want and is worth knowing either way.

## Shapes

```kotlin
setOverlayShape(BalloonOverlayShape.Empty)  // no cut-out, dim everything
setOverlayShape(BalloonOverlayShape.Rect)  // the anchor's bounds
setOverlayShape(BalloonOverlayShape.Oval)  // inscribed in the bounds, default
setOverlayShape(BalloonOverlayShape.Circle(radius = 40.dp))
setOverlayShape(BalloonOverlayShape.RoundRect(radiusX = 12.dp, radiusY = 12.dp))
setOverlayShape(
    BalloonOverlayShape.RoundRectPerCorner(
        topStart = 16.dp,
        topEnd = 4.dp,
        bottomEnd = 16.dp,
        bottomStart = 4.dp,
    ),
)
```

`Circle` with no radius uses half of the anchor's longer side. Balloon 1.x had no default
here at all: its radius was mandatory, and a circle built without one drew nothing.

`RoundRectPerCorner` is the counterpart of 1.x's four-argument `BalloonOverlayRoundRect`. Its
corners are start relative, so they mirror under a right to left layout.

!!! note "Radii are Dp, not pixels"

    `Circle` and `RoundRect` took raw pixels in 1.x and take `Dp` here. Passing the same
    number gives a different result, so convert deliberately when porting.

## Padding

Extra space added around the anchor before the shape is cut:

```kotlin
setOverlayPadding(6.dp)
setOverlayPadding(start = 4.dp, top = 10.dp, end = 20.dp, bottom = 30.dp)
```

That band is transparent by default, like the rest of the cut-out. Fill it to draw a highlight
ring around the anchor:

```kotlin
setOverlayPadding(6.dp)
setOverlayPaddingColor(Color(0xFFFFC107))
```

## Animation

```kotlin
setBalloonOverlayAnimation(BalloonOverlayAnimation.FADE)  // default, 200ms linear
setBalloonOverlayAnimation(BalloonOverlayAnimation.NONE)
```

## Interaction

The scrim is modal. A tap on it never falls through to the content underneath.

```kotlin
setDismissWhenOverlayClicked(true)  // default
balloonState.onOverlayClick = { analytics.log("tour_scrim_tapped") }
```

`onOverlayClick` fires before any dismissal, so it runs whether or not
`dismissWhenOverlayClicked` is on.

## Building a tour

Combine an overlay with the `suspend` show calls and a tour is a handful of lines:

```kotlin
BalloonHost {
    val tourStyle = rememberBalloonBuilder {
        setIsVisibleOverlay(true)
        setOverlayColor(Color(0xCC000000))
        setOverlayPadding(8.dp)
        setOverlayShape(BalloonOverlayShape.RoundRect(12.dp))
        setBackgroundColor(Color.White)
        setPadding(12.dp)
        setCornerRadius(8.dp)
    }
    val step1 = rememberBalloonState(tourStyle)
    val step2 = rememberBalloonState(tourStyle)

    LaunchedEffect(Unit) {
        step1.awaitAlignBottom()
        step2.awaitAlignTop()
    }

    Column {
        Button(modifier = Modifier.balloon(step1) { Text("Start here") }, onClick = {}) {
            Text("Home")
        }
        Button(modifier = Modifier.balloon(step2) { Text("Then here") }, onClick = {}) {
            Text("Profile")
        }
    }
}
```
