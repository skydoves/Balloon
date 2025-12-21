# Animation

Balloon supports various animations for showing, dismissing, and highlighting tooltips.

## Balloon Animation

These animations play when the Balloon is shown and dismissed.

```kotlin
Balloon.Builder(context)
    .setBalloonAnimation(BalloonAnimation.ELASTIC)
```

### Available Animations

```kotlin
BalloonAnimation.NONE // no animation
BalloonAnimation.FADE // fade in/out
BalloonAnimation.OVERSHOOT // overshoot effect
BalloonAnimation.ELASTIC // elastic bounce
BalloonAnimation.CIRCULAR // circular reveal
```

### Preview

| FADE | OVERSHOOT | ELASTIC | CIRCULAR |
|:----:|:---------:|:-------:|:--------:|
| <img src="https://user-images.githubusercontent.com/24237865/74601168-6115c580-50de-11ea-817b-a334f33b6f96.gif" width="100%"/> | <img src="https://user-images.githubusercontent.com/24237865/74601171-6410b600-50de-11ea-9ba0-5634e11f148a.gif" width="100%"/> | <img src="https://user-images.githubusercontent.com/24237865/74601170-63781f80-50de-11ea-8db4-93f1dd1291fc.gif" width="100%"/> | <img src="https://user-images.githubusercontent.com/24237865/74607359-b6bc9300-511b-11ea-978b-23bcc4399dce.gif" width="100%"/> |

## Highlight Animation

These are repeating animations that play while the Balloon is displayed, drawing attention to the tooltip.

```kotlin
Balloon.Builder(context)
    .setBalloonHighlightAnimation(BalloonHighlightAnimation.SHAKE)
```

### Available Highlight Animations

```kotlin
BalloonHighlightAnimation.NONE // no highlight animation
BalloonHighlightAnimation.HEARTBEAT // pulsing effect
BalloonHighlightAnimation.SHAKE // shaking effect
BalloonHighlightAnimation.BREATH // breathing effect
BalloonHighlightAnimation.ROTATE // rotation effect
```

### Preview

| HEARTBEAT | SHAKE | BREATH | ROTATE |
|:---------:|:-----:|:------:|:------:|
| <img src="https://user-images.githubusercontent.com/24237865/135755074-6a9c87fc-55b2-460e-b34e-0b6808684a97.gif" width="100%"/> | <img src="https://user-images.githubusercontent.com/24237865/135755077-02eeddbe-95fe-49ee-ad22-1f15879e84f1.gif" width="100%"/> | <img src="https://user-images.githubusercontent.com/24237865/135755079-29ed8cd8-92fe-4b2a-8671-b3522999c551.gif" width="100%"/> | <img src="https://user-images.githubusercontent.com/24237865/135755080-36dc7c8b-063a-442b-bcbd-bc000e92f9ac.gif" width="100%"/> |

## Rotate Animation Configuration

Customize the rotate animation with additional options:

```kotlin
Balloon.Builder(context)
    .setBalloonHighlightAnimation(BalloonHighlightAnimation.ROTATE)
    .setBalloonRotationAnimation(
        BalloonRotateAnimation.Builder()
            .setLoops(2)
            .setSpeeds(2500)
            .setTurns(INFINITE)
            .build()
    )
```

## Overlay Animation

Control the animation of the overlay:

```kotlin
Balloon.Builder(context)
    .setBalloonOverlayAnimation(BalloonOverlayAnimation.FADE)
```

### Available Overlay Animations

```kotlin
BalloonOverlayAnimation.NONE // no overlay animation
BalloonOverlayAnimation.FADE // fade in/out (default)
```

## Animation Duration

Customize animation durations for circular reveal animation:

```kotlin
Balloon.Builder(context)
    .setCircularDuration(500L) // duration in milliseconds
```

## Custom Animations

You can use custom animation resources:

```kotlin
Balloon.Builder(context)
    .setBalloonAnimationStyle(R.style.CustomBalloonAnimation)
```

Define your custom animation style in `styles.xml`:

```xml
<style name="CustomBalloonAnimation" parent="android:Animation">
    <item name="android:windowEnterAnimation">@anim/custom_enter</item>
    <item name="android:windowExitAnimation">@anim/custom_exit</item>
</style>
```
