# Getting Started

This guide covers installing Balloon 2.0.0 and attaching your first tooltip.

## Installation

[![Maven Central](https://img.shields.io/maven-central/v/com.github.skydoves/balloon.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.github.skydoves%22%20AND%20a:%22balloon%22)

=== "Compose Multiplatform"

    ```kotlin
    kotlin {
        sourceSets {
            commonMain.dependencies {
                implementation("com.github.skydoves:balloon:2.0.0")
            }
        }
    }
    ```

=== "Android only"

    ```kotlin
    dependencies {
        implementation("com.github.skydoves:balloon:2.0.0")
    }
    ```

Balloon depends on Compose Multiplatform 1.10.x and nothing else. The minimum Android SDK is 23.

## The two pieces

Every balloon is made of a **style** and a **state**.

`BalloonStyle` is immutable value data that describes how the balloon looks. Build it with the
fluent `Balloon.Builder`, usually through `rememberBalloonBuilder`:

```kotlin
val style = rememberBalloonBuilder {
    setArrowSize(10.dp)
    setArrowPosition(0.5f)
    setPadding(12.dp)
    setCornerRadius(8.dp)
    setBackgroundColor(Color(0xFF785EF0))
    setBalloonAnimation(BalloonAnimation.ELASTIC)
}
```

The builder also works outside composition, which is handy when the style is a constant:

```kotlin
val style = Balloon.Builder().apply {
    setArrowSize(10.dp)
    setPadding(12.dp)
    setCornerRadius(8.dp)
    setBackgroundColor(Color(0xFF785EF0))
    setBalloonAnimation(BalloonAnimation.ELASTIC)
}.build()
```

`BalloonStyle` has no public constructor on purpose. It carries 43 properties, and a
constructor that names all of them would freeze every one of them into the published binary
interface, so adding a 44th option later would break code compiled against 2.0.0. The builder
is the one entry point and its signature never has to change.

`BalloonState` decides when the balloon shows and where:

```kotlin
val balloonState = rememberBalloonState(style)
```

The state survives recomposition. Passing an updated `style` restyles a visible balloon in place
without hiding it, so you can animate colors or sizes freely.

## Attaching a balloon

### Option 1: the Balloon composable

Wrap the anchor. The body goes in `balloonContent` and the anchor goes in the trailing lambda.

```kotlin
Balloon(
    state = balloonState,
    balloonContent = {
        Text(text = "Now you can edit your profile!", color = Color.White)
    },
) {
    Button(onClick = { balloonState.showAlignTop() }) {
        Text(text = "Edit profile")
    }
}
```

Layout modifiers that belong to the anchor go on the `Balloon` composable itself, because it
wraps the anchor in a `Box`:

```kotlin
Balloon(
    modifier = Modifier.weight(1f),
    state = balloonState,
    balloonContent = { Text(text = "Tooltip") },
) {
    Text(text = "Anchor")
}
```

### Option 2: Modifier.balloon

When you would rather decorate an existing composable than wrap it, use `Modifier.balloon`. A
Compose modifier cannot emit content, so the balloon is rendered by a `BalloonHost` that sits
above the anchor in the tree.

```kotlin
BalloonHost {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Button(
            modifier = Modifier.balloon(balloonState) {
                Text(text = "Now you can edit your profile!", color = Color.White)
            },
            onClick = { balloonState.showAlignTop() },
        ) {
            Text(text = "Edit profile")
        }
    }
}
```

Wrap a screen in `BalloonHost` once and every `Modifier.balloon` below it works. If you forget,
the modifier throws an `IllegalStateException` naming `BalloonHost` rather than silently
rendering nothing.

The balloon body is composed inside the host, but it reads the CompositionLocals that were in
scope at the `Modifier.balloon` call site, so your `MaterialTheme`, `LocalContentColor`, and
`LocalLayoutDirection` are the anchor's, not the host's.

!!! tip "When do I need BalloonHost?"

    Always for `Modifier.balloon`, and for any balloon that turns on an overlay. The overlay
    scrim is drawn by the host so it can cover the entire window including the system bars,
    which a popup window cannot do.

## Showing and dismissing

```kotlin
balloonState.showAlignTop()
balloonState.dismiss()
balloonState.toggle()
```

See [Showing a Balloon](showing.md) for every placement option, offsets, auto dismiss, and
coroutine sequences.

## Custom content

There is no `TextForm`, no `IconForm`, and no custom layout resource. The body is a Compose slot:

```kotlin
Balloon(
    state = balloonState,
    balloonContent = {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Edit your profile", color = Color.White)
        }
    },
) {
    ProfileImage(onClick = { balloonState.showAlignBottom() })
}
```
