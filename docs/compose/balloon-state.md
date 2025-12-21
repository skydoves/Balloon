# BalloonState

`BalloonState` is the primary way to control balloon display in Jetpack Compose. It provides methods for showing, dismissing, and interacting with balloons.

## Creating BalloonState

Use `rememberBalloonState` to create a memoized state instance:

```kotlin
@Composable
fun MyScreen() {
    val builder = rememberBalloonBuilder {
        setText("Hello World!")
    }
    val balloonState = rememberBalloonState(builder)
}
```

You can also pass an optional key to trigger recomposition:

```kotlin
val balloonState = rememberBalloonState(
    builder = builder,
    key = someKey // recomposes when key changes
)
```

## Properties

### isShowing

Check if the balloon is currently visible:

```kotlin
if (balloonState.isShowing) {
    // balloon is visible
}
```

### isAttached

Check if the balloon state is attached to a composable:

```kotlin
if (balloonState.isAttached) {
    // safe to call show methods
}
```

### balloon

Access the underlying `Balloon` instance:

```kotlin
val balloon = balloonState.balloon
```

### anchorView

Access the anchor view:

```kotlin
val anchor = balloonState.anchorView
```

## Show Methods

### Show Aligned

Show the balloon aligned to different positions relative to the anchor:

```kotlin
// Show above the anchor
balloonState.showAlignTop()
balloonState.showAlignTop(xOff = 10, yOff = -5)

// Show below the anchor
balloonState.showAlignBottom()
balloonState.showAlignBottom(xOff = 0, yOff = 10)

// Show to the start (left in LTR)
balloonState.showAlignStart()
balloonState.showAlignStart(xOff = -10, yOff = 0)

// Show to the end (right in LTR)
balloonState.showAlignEnd()
balloonState.showAlignEnd(xOff = 10, yOff = 0)
```

### Show at Center

Show the balloon at the center of the anchor:

```kotlin
balloonState.showAtCenter()
balloonState.showAtCenter(
    xOff = 0,
    yOff = 0,
    centerAlign = BalloonCenterAlign.TOP
)
```

### Show as Dropdown

Show the balloon as a dropdown below the anchor:

```kotlin
balloonState.showAsDropDown()
balloonState.showAsDropDown(xOff = 0, yOff = 10)
```

## Suspend Functions

For coroutine-based control, use the await variants:

```kotlin
LaunchedEffect(Unit) {
    balloonState.awaitAlignTop()
    // continues after balloon is dismissed

    balloonState.awaitAlignBottom()
    // continues after balloon is dismissed
}
```

## Dismiss Methods

```kotlin
// Dismiss immediately
balloonState.dismiss()

// Dismiss with delay (returns true if scheduled)
val scheduled = balloonState.dismissWithDelay(1000L)
```

## Update Position

Update the balloon position while it's visible:

```kotlin
balloonState.updateAlignTop(xOff = 0, yOff = -10)
balloonState.updateAlignBottom(xOff = 0, yOff = 10)
balloonState.updateAlignStart(xOff = -10, yOff = 0)
balloonState.updateAlignEnd(xOff = 10, yOff = 0)
balloonState.update(xOff = 0, yOff = 0)
```

## Listeners

Set listeners on the balloon state:

```kotlin
balloonState.setOnBalloonClickListener { view ->
    // handle click
}

balloonState.setOnBalloonDismissListener {
    // handle dismiss
}

balloonState.setOnBalloonOutsideTouchListener { view, event ->
    // handle outside touch
}

balloonState.setOnBalloonOverlayClickListener {
    // handle overlay click
}
```

## Persistence

Check if the balloon should show based on persistence settings:

```kotlin
if (balloonState.shouldShowUp()) {
    balloonState.showAlignBottom()
}

// Clear persisted preferences
balloonState.clearAllPreferences()
```

## Measurements

Get the measured dimensions:

```kotlin
val width = balloonState.getMeasuredWidth()
val height = balloonState.getMeasuredHeight()
```

## Content View Access

Access the internal views:

```kotlin
val contentView = balloonState.getContentView()
val arrowView = balloonState.getBalloonArrowView()
```

## Example with State

```kotlin
@Composable
fun TooltipButton() {
    val builder = rememberBalloonBuilder {
        setArrowSize(10)
        setArrowPosition(0.5f)
        setBackgroundColor(Color.Gray)
    }
    val balloonState = rememberBalloonState(builder)

    // Set up listeners
    LaunchedEffect(balloonState) {
        balloonState.setOnBalloonDismissListener {
            println("Balloon dismissed")
        }
    }

    Button(
        onClick = {
            if (balloonState.isShowing) {
                balloonState.dismiss()
            } else {
                balloonState.showAlignTop()
            }
        },
        modifier = Modifier.balloon(balloonState) {
            Text("Tooltip text", color = Color.White)
        }
    ) {
        Text(if (balloonState.isShowing) "Hide" else "Show")
    }
}
```
