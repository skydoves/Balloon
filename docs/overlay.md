# Overlay

Balloon supports displaying an overlay over the entire screen except for the anchor view, creating a spotlight effect that highlights the anchor.

## Basic Overlay

```kotlin
Balloon.Builder(context)
    .setIsVisibleOverlay(true)
    .setOverlayColorResource(R.color.overlay)
    .setOverlayPadding(6f)
    .setOverlayPaddingColorResource(R.color.colorPrimary)
    .setBalloonOverlayAnimation(BalloonOverlayAnimation.FADE)
    .setDismissWhenOverlayClicked(false)
```

## Overlay Shapes

You can customize the shape of the highlighted area around the anchor.

### Oval (Default)

```kotlin
Balloon.Builder(context)
    .setOverlayShape(BalloonOverlayOval)
```

### Circle

```kotlin
Balloon.Builder(context)
    .setOverlayShape(BalloonOverlayCircle(radius = 36f))
```

### Rectangle

```kotlin
Balloon.Builder(context)
    .setOverlayShape(BalloonOverlayRect)
```

### Rounded Rectangle

```kotlin
Balloon.Builder(context)
    .setOverlayShape(BalloonOverlayRoundRect(12f, 12f))
```

You can also set individual corner radii:

```kotlin
Balloon.Builder(context)
    .setOverlayShape(
        BalloonOverlayRoundRect(
            topLeftRadius = 8f,
            topRightRadius = 8f,
            bottomLeftRadius = 16f,
            bottomRightRadius = 16f
        )
    )
```

### Preview

| OVAL | CIRCLE | RECT | ROUNDRECT |
|:----:|:------:|:----:|:---------:|
| <img src="https://user-images.githubusercontent.com/24237865/96139366-c7870800-0f39-11eb-9542-e98eac7ef193.gif" width="100%"/> | <img src="https://user-images.githubusercontent.com/24237865/96138448-c0abc580-0f38-11eb-92e6-daf2f8266a3e.gif" width="100%"/> | <img src="https://user-images.githubusercontent.com/24237865/96139358-c524ae00-0f39-11eb-82ff-90a4a734e076.gif" width="100%"/> | <img src="https://user-images.githubusercontent.com/24237865/96138463-c3a6b600-0f38-11eb-8a2d-57cf96c4190c.gif" width="100%"/> |

## Overlay Position

Set a specific position for the overlay shape:

```kotlin
Balloon.Builder(context)
    .setOverlayPosition(Point(x, y))
```

## Overlay Animation

```kotlin
BalloonOverlayAnimation.NONE
BalloonOverlayAnimation.FADE // default
```

## Overlay Padding

Add padding around the highlighted area:

```kotlin
Balloon.Builder(context)
    .setOverlayPadding(8f) // padding in dp
    .setOverlayPaddingColorResource(R.color.paddingColor) // padding color
```

## Overlay Interaction

### Dismiss When Overlay Clicked

```kotlin
Balloon.Builder(context)
    .setDismissWhenOverlayClicked(true) // dismiss balloon when overlay is clicked
```

### Overlay Click Listener

```kotlin
Balloon.Builder(context)
    .setOnBalloonOverlayClickListener {
        // handle overlay click
    }
```

### Overlay Touch Listener

```kotlin
Balloon.Builder(context)
    .setOnBalloonOverlayTouchListener { view, event ->
        // handle overlay touch
        true // return true if handled
    }
```

## Multiple Anchors

You can display the overlay with multiple anchors highlighted:

```kotlin
balloon.showAlign(
    align = BalloonAlign.TOP,
    mainAnchor = mainView,
    subAnchorList = listOf(secondaryView1, secondaryView2),
    xOff = 0,
    yOff = 0
)
```
