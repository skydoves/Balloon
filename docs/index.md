# Overview

<p align="center">
  <img src="https://user-images.githubusercontent.com/24237865/61194943-f9d70380-a6ff-11e9-807f-ba1ca8126f8a.gif" width="280"/>
  <img src="https://user-images.githubusercontent.com/24237865/61225579-d346b600-a75b-11e9-84f8-3c06047b5003.gif" width="280"/>
</p>

Balloon is a modernized and sophisticated tooltips library for Android, fully customizable with an arrow and animations. It supports both traditional Android Views and Jetpack Compose, making it versatile for any Android project.

## Key Features

- **Fully Customizable**: Configure arrow position, size, orientation, colors, text, icons, and more
- **Rich Animations**: Support for fade, overshoot, elastic, and circular reveal animations
- **Highlight Animations**: Heartbeat, shake, breath, and rotate animations while displayed
- **Overlay Support**: Highlight anchor views with customizable overlay shapes
- **Jetpack Compose Support**: Native Compose integration with `Modifier.balloon()` API
- **Lifecycle Aware**: Automatically dismisses when activity/fragment is destroyed
- **Persistence**: Show tooltips only once or a specific number of times
- **Sequential Display**: Chain multiple balloons to show sequentially

## Who's using Balloon?

Balloon hits **+800,000 downloads every month** around the globe!

![globe](https://user-images.githubusercontent.com/24237865/196018576-a9c87534-81a2-4618-8519-0024b67964bf.png)

!!! note "Featured on Google Dev Library"

    Balloon is featured on the [Google Dev Library](https://devlibrary.withgoogle.com/products/android/repos/skydoves-Balloon), recognized for its quality and usefulness in the Android development community.

## Quick Start

Add the dependency below to your module's `build.gradle` file:

[![Maven Central](https://img.shields.io/maven-central/v/com.github.skydoves/balloon.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.github.skydoves%22%20AND%20a:%22balloon%22)

=== "Groovy"

    ```groovy
    dependencies {
        implementation "com.github.skydoves:balloon:$version"
    }
    ```

=== "Kotlin"

    ```kotlin
    dependencies {
        implementation("com.github.skydoves:balloon:$version")
    }
    ```

For Jetpack Compose, add the compose dependency:

=== "Groovy"

    ```groovy
    dependencies {
        implementation "com.github.skydoves:balloon-compose:$version"
    }
    ```

=== "Kotlin"

    ```kotlin
    dependencies {
        implementation("com.github.skydoves:balloon-compose:$version")
    }
    ```

## Basic Example

### Android Views

```kotlin
val balloon = Balloon.Builder(context)
    .setWidthRatio(1.0f)
    .setHeight(BalloonSizeSpec.WRAP)
    .setText("Edit your profile here!")
    .setTextColorResource(R.color.white)
    .setTextSize(15f)
    .setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
    .setArrowSize(10)
    .setArrowPosition(0.5f)
    .setPadding(12)
    .setCornerRadius(8f)
    .setBackgroundColorResource(R.color.skyBlue)
    .setBalloonAnimation(BalloonAnimation.ELASTIC)
    .setLifecycleOwner(lifecycleOwner)
    .build()

// Show the balloon
balloon.showAlignTop(anchorView)
```

### Jetpack Compose

```kotlin
val builder = rememberBalloonBuilder {
    setArrowSize(10)
    setArrowPosition(0.5f)
    setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
    setWidth(BalloonSizeSpec.WRAP)
    setHeight(BalloonSizeSpec.WRAP)
    setPadding(12)
    setCornerRadius(8f)
    setBackgroundColorResource(R.color.skyBlue)
    setBalloonAnimation(BalloonAnimation.ELASTIC)
}

val balloonState = rememberBalloonState(builder)

Button(
    modifier = Modifier.balloon(balloonState) {
        Text(text = "Edit your profile here!")
    },
    onClick = { balloonState.showAlignTop() },
) {
    Text(text = "Show Balloon")
}
```

## Preview Gallery

| Orientation: BOTTOM | Orientation: TOP | Orientation: START | Orientation: END |
|:-------------------:|:----------------:|:------------------:|:----------------:|
| <img src="https://user-images.githubusercontent.com/24237865/61320410-55120e80-a844-11e9-9af6-cae49b8897e7.gif" width="100%"/> | <img src="https://user-images.githubusercontent.com/24237865/61320412-55120e80-a844-11e9-9ca9-81375707886e.gif" width="100%"/> | <img src="https://user-images.githubusercontent.com/24237865/61320415-55aaa500-a844-11e9-874f-ca44be02aace.gif" width="100%"/> | <img src="https://user-images.githubusercontent.com/24237865/61320416-55aaa500-a844-11e9-9aa1-53e409ca63fb.gif" width="100%"/> |

| FADE | OVERSHOOT | ELASTIC | CIRCULAR |
|:----:|:---------:|:-------:|:--------:|
| <img src="https://user-images.githubusercontent.com/24237865/74601168-6115c580-50de-11ea-817b-a334f33b6f96.gif" width="100%"/> | <img src="https://user-images.githubusercontent.com/24237865/74601171-6410b600-50de-11ea-9ba0-5634e11f148a.gif" width="100%"/> | <img src="https://user-images.githubusercontent.com/24237865/74601170-63781f80-50de-11ea-8db4-93f1dd1291fc.gif" width="100%"/> | <img src="https://user-images.githubusercontent.com/24237865/74607359-b6bc9300-511b-11ea-978b-23bcc4399dce.gif" width="100%"/> |
