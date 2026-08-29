# Animation

Every animation here is a port of the corresponding `res/anim` resource from 1.x, with the same
duration, the same interpolator, and the same pivot. The three AOSP interpolators involved
(accelerate/decelerate, bounce, and overshoot) are reimplemented as Compose `Easing` functions,
so the motion is identical on Android, iOS, Desktop, and Web.

## Enter and exit

<p align="center">
<img src="https://user-images.githubusercontent.com/24237865/74601168-6115c580-50de-11ea-817b-a334f33b6f96.gif" align="center" width="21%"/>
<img src="https://user-images.githubusercontent.com/24237865/74601171-6410b600-50de-11ea-9ba0-5634e11f148a.gif" align="center" width="21%"/>
<img src="https://user-images.githubusercontent.com/24237865/74601170-63781f80-50de-11ea-8db4-93f1dd1291fc.gif" align="center" width="21%"/>
<img src="https://user-images.githubusercontent.com/24237865/74607359-b6bc9300-511b-11ea-978b-23bcc4399dce.gif" align="center" width="21%"/>
</p>

```kotlin
setBalloonAnimation(BalloonAnimation.ELASTIC)
```

| Value | Enter | Exit |
| --- | --- | --- |
| `NONE` | instant | instant |
| `FADE` | alpha 0 to 1 over 200ms, linear | reverse |
| `ELASTIC` | scale 0.5 to 1 over 250ms, bounce | scale to 0 over 250ms |
| `OVERSHOOT` | scale 0.5 to 1 over 250ms, overshoot | scale to 0 over 250ms |
| `CIRCULAR` | circular reveal, 500ms by default | scale to 0 over 200ms |

Every scale pivots on the balloon's center, which is what the original animation resources did
regardless of where the arrow sits.

```kotlin
setCircularDuration(700L)   // only affects CIRCULAR
```

!!! note "CIRCULAR clears focusability"

    `setBalloonAnimation(BalloonAnimation.CIRCULAR)` also sets `focusable` to `false`, which is
    what 1.x did so the reveal can play without the popup stealing input. Call
    `setFocusable(true)` afterwards if you want it back.

## Highlight animations

<p align="center">
<img src="https://user-images.githubusercontent.com/24237865/135755074-6a9c87fc-55b2-460e-b34e-0b6808684a97.gif" align="center" width="21%"/>
<img src="https://user-images.githubusercontent.com/24237865/135755077-02eeddbe-95fe-49ee-ad22-1f15879e84f1.gif" align="center" width="21%"/>
<img src="https://user-images.githubusercontent.com/24237865/135755079-29ed8cd8-92fe-4b2a-8671-b3522999c551.gif" align="center" width="21%"/>
<img src="https://user-images.githubusercontent.com/24237865/135755080-36dc7c8b-063a-442b-bcbd-bc000e92f9ac.gif" align="center" width="21%"/>
</p>

A looping animation that plays for as long as the balloon is showing, to draw the eye.

```kotlin
setBalloonHighlightAnimation(BalloonHighlightAnimation.HEARTBEAT)
setBalloonHighlightAnimation(BalloonHighlightAnimation.SHAKE, startDelayMillis = 300L)
```

| Value | Motion |
| --- | --- |
| `NONE` | nothing, the default |
| `HEARTBEAT` | pulses between 100% and 90% scale over 800ms |
| `SHAKE` | slides 13% of its own size toward the anchor and back over 650ms |
| `BREATH` | fades between 75% and 100% alpha over 800ms |
| `ROTATE` | spins in 3D, configured separately |

`HEARTBEAT` pivots on the edge opposite the arrow, so the balloon pulses away from its anchor
rather than through it. When the arrow is hidden it pivots on the center instead. `SHAKE` always
moves toward the arrow, which is toward the anchor.

Until `startDelayMillis` elapses the balloon is drawn untransformed, exactly as in 1.x.

### ROTATE

```kotlin
setBalloonHighlightAnimation(BalloonHighlightAnimation.ROTATE)
setBalloonRotationAnimation(
    BalloonRotateAnimation(
        direction = BalloonRotateDirection.RIGHT,
        turns = 1,
        loops = BalloonRotateAnimation.INFINITE,
        speedMillis = 2500,
        degreeX = 0,
        degreeZ = 0,
    ),
)
```

The balloon turns `360 * turns * direction` degrees around Y, plus optional fixed sweeps around
X and Z, over `speedMillis`, repeating `loops` times about its center. `INFINITE` repeats
forever. It is driven by `graphicsLayer`'s `rotationX`, `rotationY`, and `rotationZ`, whose
default camera distance matches `android.graphics.Camera`, so the projection is the same one
1.x produced.

## Overlay animation

```kotlin
setBalloonOverlayAnimation(BalloonOverlayAnimation.FADE)   // default
setBalloonOverlayAnimation(BalloonOverlayAnimation.NONE)
```

See [Overlay](overlay.md).

## Disabling animations in tests

Set `BalloonAnimation.NONE` so a screenshot or a semantics assertion runs against a settled
frame:

```kotlin
val style = BalloonStyle(animation = BalloonAnimation.NONE)
```

Highlight animations loop forever, which means the Compose test clock never goes idle. Drive it
by hand when you need one on screen:

```kotlin
mainClock.autoAdvance = false
runOnUiThread { balloonState.showAlignBottom() }
mainClock.advanceTimeBy(16L)
onNodeWithTag("body").assertIsDisplayed()
```
