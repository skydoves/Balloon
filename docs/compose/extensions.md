# Compose Extensions

Balloon provides extension functions for `Balloon.Builder` that allow you to use Compose `Color` values directly.

## Color Extensions

### Arrow Color

Set the arrow color using a Compose `Color`:

```kotlin
val builder = rememberBalloonBuilder {
    setArrowColor(Color(0xFF4A90E2))
}
```

### Background Color

Set the balloon background color:

```kotlin
val builder = rememberBalloonBuilder {
    setBackgroundColor(Color.DarkGray)
}
```

### Text Color

Set the text color:

```kotlin
val builder = rememberBalloonBuilder {
    setTextColor(Color.White)
}
```

### Icon Color

Set the icon drawable color:

```kotlin
val builder = rememberBalloonBuilder {
    setIconColor(Color.Yellow)
}
```

### Overlay Color

Set the overlay background color:

```kotlin
val builder = rememberBalloonBuilder {
    setOverlayColor(Color.Black.copy(alpha = 0.6f))
}
```

### Overlay Padding Color

Set the overlay padding color:

```kotlin
val builder = rememberBalloonBuilder {
    setOverlayPaddingColor(Color.Blue)
}
```

## Complete Example

```kotlin
@Composable
fun ThemedBalloon() {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    val surfaceColor = MaterialTheme.colorScheme.surface

    val builder = rememberBalloonBuilder {
        setArrowSize(10)
        setArrowPosition(0.5f)
        setWidth(BalloonSizeSpec.WRAP)
        setHeight(BalloonSizeSpec.WRAP)
        setPadding(12)
        setCornerRadius(8f)

        // Use Compose colors directly
        setBackgroundColor(primaryColor)
        setTextColor(onPrimaryColor)
        setArrowColor(primaryColor)
        setOverlayColor(Color.Black.copy(alpha = 0.5f))
    }

    val balloonState = rememberBalloonState(builder)

    Button(
        onClick = { balloonState.showAlignTop() },
        modifier = Modifier.balloon(balloonState) {
            Text(
                text = "Themed tooltip",
                color = onPrimaryColor,
                modifier = Modifier.padding(8.dp)
            )
        }
    ) {
        Text("Show Balloon")
    }
}
```

## Using with Material Theme

The extensions work seamlessly with Material Theme colors:

```kotlin
val builder = rememberBalloonBuilder {
    val colors = MaterialTheme.colorScheme

    setBackgroundColor(colors.primaryContainer)
    setTextColor(colors.onPrimaryContainer)
    setArrowColor(colors.primaryContainer)
    setOverlayColor(colors.scrim.copy(alpha = 0.5f))
}
```

## Alpha Support

Compose colors with alpha channels are fully supported:

```kotlin
val builder = rememberBalloonBuilder {
    setBackgroundColor(Color.Black.copy(alpha = 0.8f))
    setOverlayColor(Color.Black.copy(alpha = 0.4f))
}
```
