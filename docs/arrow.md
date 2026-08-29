# Arrow

The arrow is part of the balloon's silhouette, not a separate image. It is carved into the same
path as the body, so a border traces around it and a corner radius never clips it.

## Size

```kotlin
setIsVisibleArrow(true)
setArrowSize(10.dp)                             // square
setArrowSize(width = 16.dp, height = 8.dp)      // base along the edge, protrusion outward
setArrowWidth(16.dp)
setArrowHeight(8.dp)
```

`width` is always the base along the balloon's edge and `height` is always how far the tip
sticks out, on every orientation. The visible protrusion is `arrowHeight - 1px`, which is the
one pixel the arrow overlaps the body by so no seam shows between them.

!!! note "Hiding the arrow frees its space"

    `setIsVisibleArrow(false)` also releases the space the arrow occupied, so the body sits
    flush against the anchor. Balloon 1.x kept reserving it, which left a gap the size of an
    arrow nobody could see.

## Orientation

By default the arrow edge is derived from the alignment you show with, so it always points back
at the anchor:

| Shown with | Arrow edge |
| --- | --- |
| `showAlignTop()` | `BOTTOM` |
| `showAlignBottom()` | `TOP` |
| `showAlignStart()` | `END` |
| `showAlignEnd()` | `START` |
| `showAsDropDown()` | `TOP` |

To pin it instead:

```kotlin
setArrowOrientation(ArrowOrientation.TOP)
setArrowOrientationRules(ArrowOrientationRules.ALIGN_FIXED)
```

`ArrowOrientationRules` decides what happens when a lack of room flips the balloon to the
opposite side:

- `ALIGN_ANCHOR` (default): the arrow follows the balloon so it keeps pointing at the anchor
- `ALIGN_FIXED`: the arrow stays on the edge you named, wherever the balloon ends up

A flip never moves the arrow to a different axis. A horizontally pinned arrow stays horizontal
even when the balloon flips vertically, because the arrow axis decides how the balloon reserves
space and changing it mid placement would feed back into the decision that caused it.

## Position along the edge

```kotlin
setArrowPosition(0.62f)                                  // 0f..1f
setArrowPositionRules(ArrowPositionRules.ALIGN_BALLOON)  // default
```

`ALIGN_BALLOON` reads the value as a fraction of the balloon, so `0.5f` centers the arrow.

`ALIGN_ANCHOR` reads it as a fraction of the **anchor** instead, so the arrow keeps pointing at
the same spot on the anchor no matter where the balloon lands or how the window clamps it. This
is what you want for a wide balloon over a narrow anchor.

```kotlin
setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
setArrowPosition(0.5f)   // always points at the middle of the anchor
```

Under `ALIGN_ANCHOR` the arrow is kept clear of the balloon's ends by

```
arrowSize * arrowAlignAnchorPaddingRatio + arrowAlignAnchorPadding
```

which you can tune:

```kotlin
setArrowAlignAnchorPaddingRatio(2.5f)   // default
setArrowAlignAnchorPadding(8.dp)        // default 0.dp
```

The arrow base is also clamped to the straight part of the edge, `cornerRadius + arrowWidth / 2`
in from each end, so it never rides up onto a rounded corner.

## Color

```kotlin
setArrowColor(Color.White)
```

Leave it at `Color.Unspecified`, the default, and the arrow inherits `backgroundColor`. When you
set a different color the arrow is painted in its own pass on top of the body fill but behind
the balloon's content, so it can never cover what you put inside.
