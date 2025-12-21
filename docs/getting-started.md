# Getting Started

This guide covers the basics of creating and displaying Balloon tooltips in your Android application.

## Installation

Add the dependency below to your module's `build.gradle` file:

[![Maven Central](https://img.shields.io/maven-central/v/com.github.skydoves/balloon.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.github.skydoves%22%20AND%20a:%22balloon%22)

=== "Groovy"

    ```groovy
    dependencies {
        implementation("com.github.skydoves:balloon:$version")
    }
    ```

=== "Kotlin"

    ```kotlin
    dependencies {
        implementation("com.github.skydoves:balloon:$version")
    }
    ```

## Creating a Balloon

### Using Balloon.Builder

You can create a Balloon instance using the `Balloon.Builder` class:

```kotlin
val balloon = Balloon.Builder(context)
    .setWidthRatio(1.0f)
    .setHeight(BalloonSizeSpec.WRAP)
    .setText("Edit your profile here!")
    .setTextColorResource(R.color.white)
    .setTextSize(15f)
    .setIconDrawableResource(R.drawable.ic_edit)
    .setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
    .setArrowSize(10)
    .setArrowPosition(0.5f)
    .setPadding(12)
    .setCornerRadius(8f)
    .setBackgroundColorResource(R.color.skyBlue)
    .setBalloonAnimation(BalloonAnimation.ELASTIC)
    .setLifecycleOwner(lifecycleOwner)
    .build()
```

### Using Kotlin DSL

You can also create a Balloon using the Kotlin DSL with `createBalloon`:

```kotlin
val balloon = createBalloon(context) {
    setWidthRatio(1.0f)
    setHeight(BalloonSizeSpec.WRAP)
    setText("Edit your profile here!")
    setTextColorResource(R.color.white)
    setTextSize(15f)
    setIconDrawableResource(R.drawable.ic_edit)
    setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
    setArrowSize(10)
    setArrowPosition(0.5f)
    setPadding(12)
    setCornerRadius(8f)
    setBackgroundColorResource(R.color.skyBlue)
    setBalloonAnimation(BalloonAnimation.ELASTIC)
    setLifecycleOwner(lifecycleOwner)
}
```

### Using Java

```java
Balloon balloon = new Balloon.Builder(context)
    .setArrowSize(10)
    .setArrowOrientation(ArrowOrientation.TOP)
    .setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
    .setArrowPosition(0.5f)
    .setWidth(BalloonSizeSpec.WRAP)
    .setHeight(65)
    .setTextSize(15f)
    .setCornerRadius(4f)
    .setAlpha(0.9f)
    .setText("You can access your profile from now on.")
    .setTextColor(ContextCompat.getColor(context, R.color.white))
    .setIconDrawable(ContextCompat.getDrawable(context, R.drawable.ic_profile))
    .setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
    .setOnBalloonClickListener(onBalloonClickListener)
    .setBalloonAnimation(BalloonAnimation.FADE)
    .setLifecycleOwner(lifecycleOwner)
    .build();
```

## Width and Height

You can adjust the width and height of the Balloon using various approaches.

### Specific Size

Set specific sizes regardless of the content:

```kotlin
Balloon.Builder(context)
    .setWidth(220) // sets 220dp width
    .setHeight(160) // sets 160dp height
```

### Wrap Content

Set dynamic sizes depending on the content:

```kotlin
Balloon.Builder(context)
    .setWidth(BalloonSizeSpec.WRAP) // width depends on content
    .setHeight(BalloonSizeSpec.WRAP) // height depends on content
```

### Screen Ratio

Set width based on the screen size ratio:

```kotlin
Balloon.Builder(context)
    .setWidthRatio(0.5f) // 50% of the horizontal screen size
```

## Padding and Margin

### Padding

Adjust the content padding inside the Balloon:

```kotlin
Balloon.Builder(context)
    .setPadding(6) // 6dp padding on all sides
    .setPaddingLeft(8) // 8dp left padding
    .setPaddingTop(12) // 12dp top padding
    .setPaddingRight(8)
    .setPaddingBottom(12)
```

### Margin

Add margins to the Balloon container:

```kotlin
Balloon.Builder(context)
    .setMargin(12) // margin on all sides
    .setMarginLeft(14)
    .setMarginRight(14)
    .setMarginHorizontal(14) // left and right margins
```

## Lifecycle Management

To avoid memory leaks, always set the lifecycle owner. The Balloon will automatically dismiss when the activity or fragment is destroyed:

```kotlin
Balloon.Builder(context)
    .setLifecycleOwner(lifecycleOwner)
```

!!! warning "Important"

    Always set the lifecycle owner to prevent memory leaks. Dialog and PopupWindow can leak memory if not properly dismissed before the activity is destroyed.

## Lazy Initialization

You can initialize a Balloon lazily using the `balloon()` extension and a `Balloon.Factory`:

```kotlin
class CustomActivity : AppCompatActivity() {
    private val profileBalloon by balloon<ProfileBalloonFactory>()
}
```

Create a factory class that extends `Balloon.Factory`:

```kotlin
class ProfileBalloonFactory : Balloon.Factory() {

    override fun create(context: Context, lifecycle: LifecycleOwner): Balloon {
        return createBalloon(context) {
            setLayout(R.layout.layout_custom_profile)
            setArrowSize(10)
            setArrowOrientation(ArrowOrientation.TOP)
            setArrowPosition(0.5f)
            setWidthRatio(0.55f)
            setHeight(250)
            setCornerRadius(4f)
            setBackgroundColorResource(R.color.background)
            setBalloonAnimation(BalloonAnimation.CIRCULAR)
            setLifecycleOwner(lifecycle)
        }
    }
}
```

!!! note

    The factory class must have a default (no-argument) constructor.
