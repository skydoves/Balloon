# Arrow

This guide covers how to customize the arrow on your Balloon tooltips.

## Basic Arrow Configuration

```kotlin
Balloon.Builder(context)
    .setIsVisibleArrow(true) // show or hide the arrow
    .setArrowSize(10) // arrow size in dp
    .setArrowPosition(0.5f) // position ratio (0.0 ~ 1.0)
    .setArrowOrientation(ArrowOrientation.BOTTOM) // arrow direction
    .setArrowDrawable(ContextCompat.getDrawable(context, R.drawable.arrow)) // custom drawable
```

## Arrow Orientation

The arrow orientation determines which side of the Balloon the arrow appears on:

```kotlin
ArrowOrientation.TOP // arrow points upward
ArrowOrientation.BOTTOM // arrow points downward
ArrowOrientation.START // arrow points to the start (left in LTR)
ArrowOrientation.END // arrow points to the end (right in LTR)
```

| TOP | BOTTOM | START | END |
|:---:|:------:|:-----:|:---:|
| <img src="https://user-images.githubusercontent.com/24237865/61320412-55120e80-a844-11e9-9ca9-81375707886e.gif" width="100%"/> | <img src="https://user-images.githubusercontent.com/24237865/61320410-55120e80-a844-11e9-9af6-cae49b8897e7.gif" width="100%"/> | <img src="https://user-images.githubusercontent.com/24237865/61320415-55aaa500-a844-11e9-874f-ca44be02aace.gif" width="100%"/> | <img src="https://user-images.githubusercontent.com/24237865/61320416-55aaa500-a844-11e9-9aa1-53e409ca63fb.gif" width="100%"/> |

## Arrow Position

The `setArrowPosition` method uses a ratio value from 0.0 to 1.0 to determine where the arrow is placed along the Balloon edge.

```kotlin
Balloon.Builder(context)
    .setArrowPosition(0.5f) // center
    .setArrowPosition(0.2f) // 20% from the start
    .setArrowPosition(0.8f) // 80% from the start
```

## Arrow Position Rules

You can choose how the arrow position is calculated:

### ALIGN_ANCHOR

The arrow position is calculated relative to the anchor view:

```kotlin
Balloon.Builder(context)
    .setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
    .setArrowPosition(0.5f) // arrow will be at the center of the anchor
```

### ALIGN_BALLOON

The arrow position is calculated relative to the Balloon body:

```kotlin
Balloon.Builder(context)
    .setArrowPositionRules(ArrowPositionRules.ALIGN_BALLOON) // default
    .setArrowPosition(0.5f) // arrow will be at the center of the Balloon
```

## Arrow Orientation Rules

You can choose how the arrow orientation is determined:

### ALIGN_ANCHOR (Recommended)

The arrow orientation adapts based on available screen space:

```kotlin
Balloon.Builder(context)
    .setArrowOrientationRules(ArrowOrientationRules.ALIGN_ANCHOR) // default
```

For example, if you set `ArrowOrientation.TOP` and call `showAlignBottom`, but there's not enough space below the anchor, the Balloon will be shown above the anchor and the arrow orientation will automatically change to `BOTTOM`.

### ALIGN_FIXED

The arrow orientation is fixed and does not change:

```kotlin
Balloon.Builder(context)
    .setArrowOrientationRules(ArrowOrientationRules.ALIGN_FIXED)
```

## Arrow Size

### Fixed Size

```kotlin
Balloon.Builder(context)
    .setArrowSize(10) // 10dp
```

### Wrap Content

The arrow size matches the original drawable resource size:

```kotlin
Balloon.Builder(context)
    .setArrowSize(BalloonSizeSpec.WRAP)
```

## Arrow Color

The arrow color is automatically set to match the Balloon background color. You can also set a custom arrow drawable:

```kotlin
Balloon.Builder(context)
    .setArrowDrawableResource(R.drawable.custom_arrow)
    .setArrowColorResource(R.color.custom_arrow_color)
```

## Arrow Constraints

You can add left and right padding constraints to the arrow:

```kotlin
Balloon.Builder(context)
    .setArrowLeftPadding(10) // minimum distance from left edge
    .setArrowRightPadding(10) // minimum distance from right edge
```

## Arrow Elevation

Add elevation to the arrow for shadow effect:

```kotlin
Balloon.Builder(context)
    .setArrowElevation(4f)
```
