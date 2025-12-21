# Customization

This guide covers various customization options for Balloon tooltips.

## Background

### Background Color

```kotlin
Balloon.Builder(context)
    .setBackgroundColor(Color.BLACK)
    .setBackgroundColorResource(R.color.skyBlue)
```

### Corner Radius

```kotlin
Balloon.Builder(context)
    .setCornerRadius(8f)
```

### Alpha (Transparency)

```kotlin
Balloon.Builder(context)
    .setAlpha(0.9f) // 0.0 to 1.0
```

## Stroke (Border)

Add a stroke (border) to the Balloon container and arrow:

```kotlin
Balloon.Builder(context)
    .setBalloonStroke(
        color = Color.WHITE,
        thickness = 4f
    )
```

<img src="https://github.com/user-attachments/assets/c188b987-7fb1-4877-ae8e-2ba486e9cea1" width="310px"/>

## Elevation and Shadow

```kotlin
Balloon.Builder(context)
    .setElevation(6f)
```

## Focus and Dismiss Behavior

### Focusable

```kotlin
Balloon.Builder(context)
    .setFocusable(true) // gains focus when shown
```

### Dismiss When Clicked

```kotlin
Balloon.Builder(context)
    .setDismissWhenClicked(true) // dismiss when Balloon is clicked
```

### Dismiss When Touched Outside

```kotlin
Balloon.Builder(context)
    .setDismissWhenTouchOutside(true) // dismiss when touching outside
```

### Dismiss When Shown Again

```kotlin
Balloon.Builder(context)
    .setDismissWhenShowAgain(true) // dismiss current if show is called again
```

### Dismiss When Lifecycle Destroyed

```kotlin
Balloon.Builder(context)
    .setDismissWhenLifecycleOnPause(true) // dismiss on onPause
```

## Pass Touch Events to Anchor

Allow touch events on the Balloon to pass through to the anchor view:

```kotlin
Balloon.Builder(context)
    .setPassTouchEventToAnchor(true)
```

## RTL Support

Enable right-to-left layout support:

```kotlin
Balloon.Builder(context)
    .setIsRtlLayout(true)
```

## Status Bar Visibility

Control whether the Balloon appears above or below the status bar:

```kotlin
Balloon.Builder(context)
    .setIsStatusBarVisible(true)
```

## Preferences Name

Set a unique name for persistence features:

```kotlin
Balloon.Builder(context)
    .setPreferenceName("my_balloon")
```

## Measured Size

Get the measured dimensions of the Balloon:

```kotlin
val width = balloon.getMeasuredWidth()
val height = balloon.getMeasuredHeight()
```

## Content View Access

Access the internal content view for advanced customization:

```kotlin
val contentView: ViewGroup = balloon.getContentView()
val arrowView: View = balloon.getBalloonArrowView()
```
