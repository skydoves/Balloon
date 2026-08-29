# Overview

<p align="center">
  <img src="https://user-images.githubusercontent.com/24237865/61194943-f9d70380-a6ff-11e9-807f-ba1ca8126f8a.gif" width="280"/>
  <img src="https://user-images.githubusercontent.com/24237865/61225579-d346b600-a75b-11e9-84f8-3c06047b5003.gif" width="280"/>
</p>

Balloon is a modernized and sophisticated tooltip library for **Compose Multiplatform**, fully
customizable with an arrow and animations. A single artifact runs on Android, iOS, Desktop (JVM),
and Web (Wasm).

!!! info "Balloon 2.0.0 is a rewrite"

    Version 2.0.0 replaces the View based implementation with a Compose Multiplatform one.
    There is no `Context`, no `View`, and no XML in the API. If you are upgrading, start with
    the [Migration guide](migration.md). The View based library is still available at `1.7.6`
    and is documented under [Balloon 1.x (View)](legacy-view/getting-started.md).

## Key features

- **Multiplatform**: one artifact for Android, iOS, Desktop, and Web
- **Compose native**: the balloon body is a composable slot, so you build it like any other UI
- **Two APIs**: wrap an anchor with `Balloon(...)`, or decorate one in place with `Modifier.balloon`
- **Smart placement**: the arrow points at the anchor automatically, and the balloon flips sides when there is no room
- **Rich animations**: fade, overshoot, elastic, and circular reveal, plus looping highlight animations
- **Overlay**: dim the window and cut the anchor out of it to build a spotlight tour
- **Coroutine friendly**: every `show` has a `suspend` twin that returns when the balloon closes

## Who's using Balloon?

Balloon hits **+800,000 downloads every month** around the globe.

![globe](https://user-images.githubusercontent.com/24237865/196018576-a9c87534-81a2-4618-8519-0024b67964bf.png)

!!! note "Featured on Google Dev Library"

    Balloon is featured on the [Google Dev Library](https://devlibrary.withgoogle.com/products/android/repos/skydoves-Balloon), recognized for its quality and usefulness in the Android development community.

## Quick start

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

Then build a style, remember a state, and attach it to an anchor:

```kotlin
val style = rememberBalloonBuilder {
    setArrowSize(10.dp)
    setPadding(12.dp)
    setCornerRadius(8.dp)
    setBackgroundColor(Color(0xFF785EF0))
    setBalloonAnimation(BalloonAnimation.ELASTIC)
}
val balloonState = rememberBalloonState(style)

Balloon(
    state = balloonState,
    balloonContent = { Text(text = "Now you can edit your profile!", color = Color.White) },
) {
    Button(onClick = { balloonState.showAlignTop() }) {
        Text(text = "Edit profile")
    }
}
```

## Supported targets

| Target | Artifact suffix |
| --- | --- |
| Android | `balloon-android` |
| Desktop (JVM) | `balloon-desktop` |
| iOS (arm64) | `balloon-iosarm64` |
| iOS (simulator, arm64) | `balloon-iossimulatorarm64` |
| iOS (x64) | `balloon-iosx64` |
| Web (Wasm) | `balloon-wasm-js` |

Gradle picks the right one for you. Depend on `com.github.skydoves:balloon` and nothing else.

## Where to go next

- [Getting Started](getting-started.md) covers the two ways to attach a balloon
- [Showing a Balloon](showing.md) covers placement, offsets, and coroutine sequences
- [Migration from 1.x](migration.md) maps every old option to its 2.0.0 counterpart
