# Size and Spacing

## The box model

A balloon is not just the colored card you see. The popup box that gets positioned is

```
popup = margin + reserve + card(padding + your content)
```

where the reserve is the arrow protrusion on the arrow axis and `elevation` on the other axis.
Every width and height spec below sizes the **popup box**, exactly as it did in 1.x, so a ported
call site lands on the same pixels.

If you want the visible card to be exactly the size you asked for, set `setElevation(0.dp)` and
leave the margins at zero.

## Width

```kotlin
setWidth(200.dp)  // fixed
setWidthRatio(0.6f)  // fraction of the window width
setMinWidth(120.dp)
setMaxWidth(320.dp)
setMinWidthRatio(0.3f)
setMaxWidthRatio(0.9f)
```

They are resolved in this order, which is the order 1.x used:

1. `widthRatio`, if greater than `0f`, wins and gives an exact width
2. otherwise `minWidthRatio` or `maxWidthRatio`, if either is set, bound a wrapping balloon
3. otherwise `width`, if specified, gives an exact width capped at the window
4. otherwise the balloon wraps its content, bounded by `minWidth` and `maxWidth`

Leave `width` at `Dp.Unspecified` (the default) to wrap.

## Height

```kotlin
setHeight(120.dp)
setSize(width = 200.dp, height = 120.dp)
```

`Dp.Unspecified` wraps. There is no height ratio, matching 1.x.

## Padding

Padding is the space between the card's edge and your content.

```kotlin
setPadding(12.dp)
setPadding(start = 8.dp, top = 4.dp, end = 8.dp, bottom = 4.dp)
setPaddingHorizontal(16.dp)
setPaddingVertical(8.dp)
setPaddingStart(8.dp)
setPaddingTop(4.dp)
setPaddingEnd(8.dp)
setPaddingBottom(4.dp)
```

`setPaddingHorizontal` and `setPaddingVertical` each leave the other axis untouched.

Everything is start and end rather than left and right, so it mirrors under a right to left
layout direction.

## Margin

Margin keeps the balloon off the window edges and reduces the width every spec measures against.

```kotlin
setMargin(12.dp)
setMargin(start = 8.dp, top = 0.dp, end = 8.dp, bottom = 0.dp)
setMarginHorizontal(16.dp)
setMarginVertical(8.dp)
setMarginStart(8.dp)
setMarginTop(4.dp)
setMarginEnd(8.dp)
setMarginBottom(4.dp)
```

## Elevation

```kotlin
setElevation(2.dp)  // default
```

In 1.x this was both the shadow and an inset around the card. Here it is the inset only, which
is what makes a wrapping balloon stop `2 * elevation` short of the window edges and what the
width specs are measured against. The drop shadow itself is not drawn, because Compose can only
cast a shadow from a convex outline and a balloon with an arrow notch is not convex. Add a
`Modifier.shadow(...)` inside the balloon content if you want one.

## Corner radius

```kotlin
setCornerRadius(12.dp)
```

The radius is clamped to half of the card's shorter side, so an oversized value rounds the card
into a stadium shape instead of inverting the path.
