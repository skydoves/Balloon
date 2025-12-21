# Jetpack Compose Overview

Balloon provides first-class support for Jetpack Compose with a simple and intuitive API using the `Modifier.balloon()` extension.

## Installation

Add the Compose dependency to your `build.gradle`:

```gradle
dependencies {
    implementation("com.github.skydoves:balloon-compose:$version")
}
```

## Basic Usage

The recommended way to use Balloon in Compose is with `Modifier.balloon()`:

```kotlin
@Composable
fun MyScreen() {
    val builder = rememberBalloonBuilder {
        setText("Edit your profile here!")
        setArrowSize(10)
        setArrowPosition(0.5f)
        setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
        setBackgroundColorResource(R.color.skyBlue)
        setBalloonAnimation(BalloonAnimation.ELASTIC)
    }
    val balloonState = rememberBalloonState(builder)

    Button(
        onClick = { balloonState.showAlignTop() },
        modifier = Modifier.balloon(balloonState) {
            Text("Tooltip content")
        }
    ) {
        Text("Show Balloon")
    }
}
```

## Components

The Compose integration consists of:

1. **`rememberBalloonBuilder`** - Creates a memoized `Balloon.Builder`
2. **`rememberBalloonState`** - Creates a `BalloonState` for controlling the balloon
3. **`Modifier.balloon()`** - Attaches a balloon to any composable

## Balloon Content

The balloon content is defined as a composable lambda:

```kotlin
Modifier.balloon(balloonState) {
    // Any composable content
    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Welcome to the app!",
            color = Color.White
        )
    }
}
```

## Showing the Balloon

Use `BalloonState` methods to show the balloon:

```kotlin
// Show aligned to different positions
balloonState.showAlignTop()
balloonState.showAlignBottom()
balloonState.showAlignStart()
balloonState.showAlignEnd()

// Show at center
balloonState.showAtCenter()

// Show as dropdown
balloonState.showAsDropDown()
```

## Dismissing the Balloon

```kotlin
// Dismiss immediately
balloonState.dismiss()

// Dismiss with delay
balloonState.dismissWithDelay(1000L)
```

## Compose Color Extensions

Use Compose colors directly with the builder:

```kotlin
val builder = rememberBalloonBuilder {
    setBackgroundColor(Color(0xFF4A90E2))
    setTextColor(Color.White)
    setArrowColor(Color(0xFF4A90E2))
    setOverlayColor(Color.Black.copy(alpha = 0.5f))
}
```

## Example

Here's a complete example:

```kotlin
@Composable
fun ProfileScreen() {
    val builder = rememberBalloonBuilder {
        setArrowSize(10)
        setArrowPosition(0.5f)
        setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
        setWidth(BalloonSizeSpec.WRAP)
        setHeight(BalloonSizeSpec.WRAP)
        setPadding(12)
        setMarginHorizontal(12)
        setCornerRadius(8f)
        setBackgroundColor(Color(0xFF333333))
        setBalloonAnimation(BalloonAnimation.FADE)
    }
    val balloonState = rememberBalloonState(builder)

    IconButton(
        onClick = { balloonState.showAlignBottom() },
        modifier = Modifier.balloon(balloonState) {
            Text(
                text = "Click to edit your profile",
                color = Color.White,
                modifier = Modifier.padding(8.dp)
            )
        }
    ) {
        Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "Edit"
        )
    }
}
```
