# Showing Balloon

This guide covers the various ways to display and dismiss Balloon tooltips.

## Display Methods

You can show the Balloon using different alignment methods. Each method positions the Balloon relative to an anchor view.

### Align Top

Shows the Balloon above the anchor view:

```kotlin
balloon.showAlignTop(anchor) // shows above the anchor
balloon.showAlignTop(anchor, xOff, yOff) // with offset
```

### Align Bottom

Shows the Balloon below the anchor view:

```kotlin
balloon.showAlignBottom(anchor) // shows below the anchor
balloon.showAlignBottom(anchor, xOff, yOff) // with offset
```

### Align Start

Shows the Balloon at the start (left in LTR) of the anchor view:

```kotlin
balloon.showAlignStart(anchor) // shows at start
balloon.showAlignStart(anchor, xOff, yOff) // with offset
```

### Align End

Shows the Balloon at the end (right in LTR) of the anchor view:

```kotlin
balloon.showAlignEnd(anchor) // shows at end
balloon.showAlignEnd(anchor, xOff, yOff) // with offset
```

### As Dropdown

Shows the Balloon as a dropdown below the anchor:

```kotlin
balloon.showAsDropDown(anchor) // shows as dropdown
balloon.showAsDropDown(anchor, xOff, yOff) // with offset
```

### At Center

Shows the Balloon overlapping the anchor at its center:

```kotlin
balloon.showAtCenter(anchor) // shows at center
balloon.showAtCenter(anchor, xOff, yOff, BalloonCenterAlign.TOP)
```

## Kotlin Extensions

You can also use Kotlin extension functions on views:

```kotlin
myButton.showAlignTop(balloon)
myButton.showAlignBottom(balloon)
myButton.showAlignStart(balloon)
myButton.showAlignEnd(balloon)
```

## Dismissing Balloon

### Immediate Dismiss

```kotlin
balloon.dismiss()
```

### Dismiss with Delay

```kotlin
balloon.dismissWithDelay(1000L) // dismisses after 1 second
```

### Auto Dismiss

Set the Balloon to automatically dismiss after a duration:

```kotlin
Balloon.Builder(context)
    .setAutoDismissDuration(1000L) // auto dismiss after 1 second
```

## Sequential Display

You can show multiple Balloons sequentially using relay methods or coroutines.

### Using Relay Methods

```kotlin
customListBalloon
    .relayShowAlignBottom(customProfileBalloon, circleImageView)
    .relayShowAlignTop(customTagBalloon, bottomNavigationView, 130, 0)

// Start the sequence
customListBalloon.showAlignBottom(anchorView)
```

### Using Coroutines

```kotlin
coroutineScope.launch {
    customListBalloon.awaitAlignBottom(anchorView)
    customProfileBalloon.awaitAlignBottom(circleImageView)
    customTagBalloon.awaitAlignTop(bottomNavigationView, 130, 0)
}
```

!!! note

    The `relayShow__` and `await__` methods overwrite the `setOnDismissListener` internally, so you cannot use `setOnDismissListener` at the same time.

## Update Position

You can update the Balloon's position while it's displayed:

```kotlin
balloon.updateAlignTop(xOff, yOff)
balloon.updateAlignBottom(xOff, yOff)
balloon.updateAlignStart(xOff, yOff)
balloon.updateAlignEnd(xOff, yOff)
balloon.update(xOff, yOff)
```

## Display in DialogFragment

When showing a Balloon inside a DialogFragment, you may need to attach it to the decor frame:

```kotlin
balloon.setIsAttachedInDecor(true)
```
