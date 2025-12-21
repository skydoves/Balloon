# Listeners

Balloon provides various listeners to respond to user interactions and lifecycle events.

## Click Listeners

### Balloon Click Listener

Triggered when the user clicks on the Balloon content:

```kotlin
Balloon.Builder(context)
    .setOnBalloonClickListener { view ->
        // handle balloon click
    }
```

### Balloon Overlay Click Listener

Triggered when the user clicks on the overlay area:

```kotlin
Balloon.Builder(context)
    .setOnBalloonOverlayClickListener {
        // handle overlay click
    }
```

## Dismiss Listener

Triggered when the Balloon is dismissed:

```kotlin
Balloon.Builder(context)
    .setOnBalloonDismissListener {
        // handle balloon dismiss
    }
```

## Initialization Listener

Triggered after the Balloon is fully initialized and laid out:

```kotlin
Balloon.Builder(context)
    .setOnBalloonInitializedListener { view ->
        // balloon is ready
    }
```

## Touch Listeners

### Balloon Touch Listener

Handle touch events on the Balloon content:

```kotlin
Balloon.Builder(context)
    .setOnBalloonTouchListener { view, motionEvent ->
        // handle touch event
        true // return true if consumed
    }
```

### Outside Touch Listener

Triggered when the user touches outside the Balloon:

```kotlin
Balloon.Builder(context)
    .setOnBalloonOutsideTouchListener { view, motionEvent ->
        // handle outside touch
    }
```

### Overlay Touch Listener

Handle touch events on the overlay:

```kotlin
Balloon.Builder(context)
    .setOnBalloonOverlayTouchListener { view, motionEvent ->
        // handle overlay touch
        true // return true if consumed
    }
```

## Setting Listeners After Building

You can also set listeners after building the Balloon:

```kotlin
val balloon = Balloon.Builder(context)
    .setText("Hello World!")
    .build()

balloon.setOnBalloonClickListener { view ->
    // handle click
}

balloon.setOnBalloonDismissListener {
    // handle dismiss
}
```

## Listeners with Interfaces

For Java or when you need a more structured approach, use the listener interfaces:

```kotlin
balloon.setOnBalloonClickListener(object : OnBalloonClickListener {
    override fun onBalloonClick(view: View) {
        // handle click
    }
})

balloon.setOnBalloonDismissListener(object : OnBalloonDismissListener {
    override fun onBalloonDismiss() {
        // handle dismiss
    }
})
```

## Chaining Show with Dismiss Listener

A common pattern is to show another Balloon when one is dismissed:

```kotlin
val firstBalloon = Balloon.Builder(context)
    .setText("First tooltip")
    .setOnBalloonDismissListener {
        secondBalloon.showAlignBottom(anchor)
    }
    .build()

val secondBalloon = Balloon.Builder(context)
    .setText("Second tooltip")
    .build()

firstBalloon.showAlignBottom(anchor)
```
