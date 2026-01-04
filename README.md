<h1 align="center">Balloon</h1></br>

<p align="center">
:balloon: Modernized and sophisticated tooltips, fully customizable with an arrow and animations on Android.
</p>
</br>
<p align="center">
  <a href="https://devlibrary.withgoogle.com/products/android/repos/skydoves-Balloon"><img alt="Google" src="https://skydoves.github.io/badges/google-devlib.svg"/></a>
  <a href="https://twitter.com/googledevs/status/1476223093773418502"><img alt="Twitter" src="https://skydoves.github.io/badges/twitter-developers.svg"/></a>
  <a href="https://www.linkedin.com/feed/update/urn:li:activity:6881990083344519168/"><img alt="LinkedIn" src="https://skydoves.github.io/badges/linkedin-developers.svg"/></a>
  <a href="https://github.com/doveletter"><img alt="Profile" src="https://skydoves.github.io/badges/dove-letter.svg"/></a><br>
  <a href="https://opensource.org/licenses/Apache-2.0"><img alt="License" src="https://img.shields.io/badge/License-Apache%202.0-blue.svg"/></a>
  <a href="https://android-arsenal.com/api?level=21"><img alt="API" src="https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat"/></a>
  <a href="https://github.com/skydoves/Balloon/actions"><img alt="Build Status" src="https://github.com/skydoves/Balloon/workflows/Android%20CI/badge.svg"/></a>
  <a href="https://medium.com/swlh/a-lightweight-tooltip-popup-for-android-ef9484a992d7"><img alt="Medium" src="https://skydoves.github.io/badges/Story-Medium.svg"/></a>
  <a href="https://github.com/skydoves"><img alt="Profile" src="https://skydoves.github.io/badges/skydoves.svg"/></a>
  <a href="https://skydoves.github.io/libraries/balloon/html/balloon/com.skydoves.balloon/index.html"><img alt="Dokka" src="https://skydoves.github.io/badges/dokka-balloon.svg"/></a>
</p> <br>

<p align="center">
<img src="https://user-images.githubusercontent.com/24237865/61194943-f9d70380-a6ff-11e9-807f-ba1ca8126f8a.gif" width="280"/>
<img src="https://user-images.githubusercontent.com/24237865/61225579-d346b600-a75b-11e9-84f8-3c06047b5003.gif" width="280"/>
<img src="https://user-images.githubusercontent.com/24237865/148673977-dba2e44c-c2fb-4fb4-a648-e26e8541e865.png" width="252"/>
</p>

## Who's using Balloon?
**👉 [Check out who's using Balloon](/usecases.md)**

Balloon hits **+800,000 downloads every month** around the globe! :balloon:

![globe](https://user-images.githubusercontent.com/24237865/196018576-a9c87534-81a2-4618-8519-0024b67964bf.png)

<img align="right" width="130px" src="https://user-images.githubusercontent.com/24237865/210227682-cbc03479-8625-4213-b907-4f15217f91ba.png"/>

## Balloon in Jetpack Compose

If you want to use Balloon in your Jetpack Compose project, check out the **[Balloon in Jetpack Compose](https://github.com/skydoves/Balloon#balloon-in-jetpack-compose-1)** section. You can also check out the blog post **[Tooltips for Jetpack Compose: Improve User Experience to the Next Level](https://medium.com/@skydoves/tooltips-for-jetpack-compose-improve-user-experience-to-the-next-level-68791ab8e07f)** for more details.

## 💝 Sponsors

<a href="https://coderabbit.link/Jaewoong" target="_blank"> <img width="300" alt="coderabbit" src="https://www.coderabbit.ai/_next/image?url=https%3A%2F%2Fvictorious-bubble-f69a016683.media.strapiapp.com%2FCr_logo_dark_f656abe8e3.png&w=384&q=75" /></a>

<a href="https://getstream.io/chat/sdk/android/?utm_source=github&utm_medium=referral&utm_content=&utm_campaign=Jaewoong_github_2025" target="_blank"> <img width="260" alt="stream" src="https://github.com/user-attachments/assets/87a69228-4fef-4f48-ad98-1e2c606c5b7e" /></a>

## Including in your project
[![Maven Central](https://img.shields.io/maven-central/v/com.github.skydoves/balloon.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.github.skydoves%22%20AND%20a:%22balloon%22)

### Gradle
Add the dependency below to your **module**'s `build.gradle` file:

```kotlin
dependencies {
    implementation("com.github.skydoves:balloon:1.7.2")
}
```

## How to Use
Balloon supports both Kotlin and Java projects, so you can reference it by your language.

### Create Balloon with Kotlin
We can create an instance of the Balloon with the `Balloon.Builder` class.

```kotlin
val balloon = Balloon.Builder(context)
  .setWidthRatio(1.0f)
  .setHeight(BalloonSizeSpec.WRAP)
  .setText("Edit your profile here!")
  .setTextColorResource(R.color.white_87)
  .setTextSize(15f)
  .setIconDrawableResource(R.drawable.ic_edit)
  .setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
  .setArrowSize(10)
  .setArrowPosition(0.5f)
  .setPadding(12)
  .setCornerRadius(8f)
  .setBackgroundColorResource(R.color.skyBlue)
  .setBalloonAnimation(BalloonAnimation.ELASTIC)
  .setLifecycleOwner(lifecycle)
  .build()
```

### Create Balloon with Kotlin DSL
We can also create an instance of the Balloon with the Kotlin DSL.

<details>
 <summary>Keep reading for more details</summary>

You can create an instance of the Balloon with `createBalloon` as the example below:
```kotlin
val balloon = createBalloon(context) {
  setWidthRatio(1.0f)
  setHeight(BalloonSizeSpec.WRAP)
  setText("Edit your profile here!")
  setTextColorResource(R.color.white_87)
  setTextSize(15f)
  setIconDrawableResource(R.drawable.ic_edit)
  setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
  setArrowSize(10)
  setArrowPosition(0.5f)
  setPadding(12)
  setCornerRadius(8f)
  setBackgroundColorResource(R.color.skyBlue)
  setBalloonAnimation(BalloonAnimation.ELASTIC)
  setLifecycleOwner(lifecycle)
  build()
}
```
</details>

### Create Balloon with Java
You can create an instance of the Balloon with Java by using the `Balloon.Builder` class.

<details>
 <summary>Keep reading for more details</summary>

You can create an instance of the Balloon as the following example below:
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
    .setTextColor(ContextCompat.getColor(context, R.color.white_93))
    .setTextIsHtml(true)
    .setIconDrawable(ContextCompat.getDrawable(context, R.drawable.ic_profile))
    .setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
    .setOnBalloonClickListener(onBalloonClickListener)
    .setBalloonAnimation(BalloonAnimation.FADE)
    .setLifecycleOwner(lifecycleOwner)
    .build();
```
</details>

### Show up Balloon
We can show up the Balloon using the functions below. If we use `showAlign__` method, we can show up the Balloon based on alignments (**top, bottom, right, left**). Also, we can adjust specific positions of the Balloon by using `x-Offset` and `y-Offset` parameters. <br>

```kotlin
balloon.showAlignTop(anchor: View) // shows the balloon on an anchor view as the top alignment.
balloon.showAlignTop(anchor: View, xOff: Int, yOff: Int) // shows top alignment with x-off and y-off.
balloon.showAlignBottom(anchor: View) // shows the balloon on an anchor view as the bottom alignment.
balloon.showAlignBottom(anchor: View, xOff: Int, yOff: Int) // shows bottom alignment with x-off and y-off.
balloon.showAlignEnd(anchor: View) // shows the balloon on an anchor view as the end alignment.
balloon.showAlignEnd(anchor: View, xOff: Int, yOff: Int) // shows end alignment with x-off and y-off.
balloon.showAlignStart(anchor: View) // shows the balloon on an anchor view as the start alignment.
balloon.showAlignStart(anchor: View, xOff: Int, yOff: Int) // shows start alignment with x-off and y-off.
balloon.showAsDropDown(anchor: View) // shows the balloon as a dropdown without any alignments.
balloon.showAsDropDown(anchor: View, xOff: Int, yOff: Int) // shows no alignments with x-off and y-off.
balloon.showAtCenter(anchor: View, xOff: Int, yOff: Int, centerAlign: BalloonCenterAlign.TOP)
// shows the balloon over the anchor view (overlap) as the center aligns.
```

Also, We can show up the Balloon with Kotlin extensions.

```kotlin
myButton.showAlignTop(balloon)
```

### Dismiss Balloon
We can dismiss the Balloon by using the `Balloon.dismiss()` method.

```kotlin
balloon.dismiss()
balloon.dismissWithDelay(1000L) // dismisses 1000 milliseconds later when the popup is shown
```

We can dismiss automatically with a delay after the Balloon is shown using the `setAutoDismissDuration` method.

```kotlin
Balloon.Builder(context)
   // dismisses automatically 1000 milliseconds later when the popup is shown.
   .setAutoDismissDuration(1000L)
   ...
```

### Show up Balloon Sequentially
We can show up a couple of Balloons sequentially with the `relayShow__` and `await__` methods.

```kotlin
customListBalloon
  .relayShowAlignBottom(customProfileBalloon, circleImageView) // relay to customListBalloon
  .relayShowAlignTop(customTagBalloon, bottomNavigationView, 130, 0) // relay to customProfileBalloon

// show sequentially customListBalloon-customProfileBalloon-customTagBalloon
customListBalloon.showAlignBottom(anchorView)
```
```kotlin
coroutineScope.launch {
  customListBalloon.awaitAlignBottom(anchorView)
  customProfileBalloon.awaitAlignBottom(circleImageView, 0, 0)
  customTagBalloon.awaitAlignTop(bottomNavigationView, 130, 0)
}
```

> Note: The `relayShow__` and `await__` methods overwrite the `setOnDismissListener` internally, so you can't use the `setOnDismissListener` at the same time.

#### Parallel Displaying
We can show multiple balloons at the same time with sequential behavior.
```kotlin
lifecycleScope.launch {
  // shows balloons at the same time
  awaitBalloons {
    // dismissing of any balloon dismisses all of them. Default behaviour
    dismissSequentially = false

    textView.alignTop(balloonAlignTop)
    textView.alignStart(balloonAlignStart)
    textView.alignEnd(balloonAlignEnd)
    textView.alignBottom(balloonAlignBottom)
  }

  // shows another group after dismissing the previous group.
  awaitBalloons {
    dismissSequentially = true // balloons dismissed individually

    imageView.alignTop(balloonAlignTop)
    imageView.alignStart(balloonAlignStart)
    imageView.alignEnd(balloonAlignEnd)
    imageView.alignBottom(balloonAlignBottom)
  }
}
```
> Note: The methods inside `awaitBalloons` are `atCenter`, `asDropdown`, `alignTop` and etc. Don't confuse with `show__` and `await__` methods.

### Width and height
We can adjust specific width and height sizes of Balloon with the below builder methods. If we don't set any specific sizes of the width and height of the Balloon, the size of the Balloon will be decided by the content.

#### Specific size
We can set specific sizes of the Balloon regardless size of the contents.

```kotlin
balloon.setWidth(220) // sets 220dp width size.
balloon.setHeight(160) // sets 160dp height size.
```

#### Wrap Content Sizes
We can set dynamic sizes of Balloon, which depends on sizes of the internal content.

```kotlin
balloon.setWidth(BalloonSizeSpec.WRAP) // sets width size depending on the content's size.
balloon.setHeight(BalloonSizeSpec.WRAP) // sets height size depending on the content's size.
```

#### Depending on Screen Size
Also, we can set the width size depending on the ratio of the screen's size (horizontal).

```kotlin
balloon.setWidthRatio(0.5f) // sets width as 50% of the horizontal screen's size.
```

### Padding
Balloon wraps contents. We can adjust the content size of the Balloon by adding paddings on the content like.<br>

```kotlin
balloon.setPadding(6) // sets 6dp padding to all directions (left-top-right-bottom)
balloon.setPaddingLeft(8) // sets 8dp padding to content's left.
balloon.setPaddingTop(12) // sets 12dp padding to content's top.
```

### Margin
If the location of the balloon according to the anchor would be located at the boundaries on the screen,<br>
the balloon will be stick to the end of the screen. In this case, we can give horizontal margins to the balloon.

```kotlin
.setMargin(12) // sets the margin on the balloon all directions.
.setMarginLeft(14) // sets the left margin on the balloon.
.setMarginRight(14) // sets the right margin on the balloon.
```

### Auto-sized text

You can set auto-sized text based on the balloon's window size, specifying minimum and maximum text sizes, as shown in the example below:

```kotlin
.setTextSize(15f)
.setMinAutoSizeTextSize(14f)
.setMaxAutoSizeTextSize(18f)
.setEnableAutoSized(true)
```

> Note: Ensure that the maximum auto text size is set to a value higher than the minimum auto text size.

### Arrow Composition
We can customize the arrow on the Balloon with various methods. For more details, check out the [Balloon.Builder](https://skydoves.github.io/libraries/balloon/html/balloon/com.skydoves.balloon/-balloon/-builder/index.html).

```kotlin
.setIsVisibleArrow(true) // sets the visibility of the arrow.
.setArrowSize(10) // sets the arrow size.
.setArrowSize(BalloonSizeSpec.WRAP) // sets arrow size depending on the original resources' size.
.setArrowPosition(0.8f) // sets the arrow position using the popup size's ratio (0 ~ 1.0)
.setArrowOrientation(ArrowOrientation.TOP) // sets the arrow orientation. top, bottom, left, right
.setArrowDrawable(ContextCompat.getDrawable(context, R.drawable.arrow)) // sets the arrow drawable.
```

#### ArrowPositionRules
We can decide the position of the arrow depending on the aligning rules with the `ArrowPositionRules`.<br>
```kotlin
// Align the arrow position depending on an anchor.
// if `arrowPosition` is 0.5, the arrow will be located in the middle of an anchor.
.setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)

// Align the arrow position depending on the balloon popup body.
// if `arrowPosition` is 0.5, the arrow will be located in the middle of the tooltip.
.setArrowPositionRules(ArrowPositionRules.ALIGN_BALLOON) // default
```

#### ArrowOrientationRules
We can decide the orientation of the arrow depending on the aligning rules with the `ArrowOrientationRules`.<br>
```kotlin
// Align depending on the position of an anchor.
// For example, `arrowOrientation` is ArrowOrientation.TOP and
// we want to show up the balloon under an anchor using the `Balloon.showAlignBottom`.
// However, if there is not enough free space to place the tooltip at the bottom of the anchor,
// tooltips will be placed top of the anchor and the orientation of the arrow will be `ArrowOrientation.BOTTOM`.
.setArrowOrientationRules(ArrowOrientationRules.ALIGN_ANCHOR) // default

// Align to fixed ArrowOrientation value.
.setArrowOrientationRules(ArrowOrientationRules.ALIGN_FIXED)
```

Below previews are shows examples of `setArrowOrientation` and `setArrowPosition` methods. <br>
The `setArrowPosition` measures the Balloon's size and sets the arrow's position with the ratio value.

Orientation: BOTTOM<br> Position: 0.62<br> showAlignTop | Orientation: TOP<br> Position : 0.5<br> showAlignBottom | Orientation: START<br> Position: 0.5<br> showAlignStart | Orientation: END<br> Position: 0.5<br> showAlignEnd |
| :---------------: | :---------------: | :---------------: | :---------------: |
| <img src="https://user-images.githubusercontent.com/24237865/61320410-55120e80-a844-11e9-9af6-cae49b8897e7.gif" align="center" width="100%"/> | <img src="https://user-images.githubusercontent.com/24237865/61320412-55120e80-a844-11e9-9ca9-81375707886e.gif" align="center" width="100%"/> | <img src="https://user-images.githubusercontent.com/24237865/61320415-55aaa500-a844-11e9-874f-ca44be02aace.gif" align="center" width="100%"/> | <img src="https://user-images.githubusercontent.com/24237865/61320416-55aaa500-a844-11e9-9aa1-53e409ca63fb.gif" align="center" width="100%"/> |

### Text Composition
We can customize the text on the Balloon.

```kotlin
.setText("You can edit your profile now!")
.setTextSize(15f)
.setTextTypeface(Typeface.BOLD)
.setTextColorResource(R.color.colorAccent))
.setTextGravity(Gravity.START)
```

If your text includes HTML tags, you can render the text by enabling HTML option with `setTextIsHtml` method.

```java
.setTextIsHtml(true)
```

This method will parse the text with the `Html.fromHtml(text)` internally.

### TextForm
`TextForm` has some attributes for `TextView` to customize the text of the Balloon. You can create the `TextForm` instance and reuse it on multiple Balloons.

```kotlin
val textForm = TextForm.Builder(context)
  .setText("Edit your profile here!")
  .setTextColorResource(R.color.white_87)
  .setTextSize(14f)
  .setTextTypeface(Typeface.BOLD)
  .build()

val balloon = Balloon.Builder(context)
  .setTextForm(textForm)
  ...
```

<details>
 <summary>Create TextForm with Kotlin DSL</summary>

You can create an instance of the `TextForm` with Kotlin DSL as the example below:

```kotlin
val textForm = textForm(context) {
  setText("Edit your profile here!")
  setTextColorResource(R.color.white_87)
  setTextSize(14f)
  setTextTypeface(Typeface.BOLD)
}

val balloon = Balloon.Builder(context)
  .setTextForm(textForm)
  ...
```
</details>

<details>
 <summary>Create TextForm with Java</summary>

You can create an instance of the `TextForm` with Java as the example below:

```java
TextForm textForm = new TextForm.Builder(context)
  .setText("Edit your profile here!")
  .setTextColorResource(R.color.white_87)
  .setTextSize(14f)
  .setTextTypeface(Typeface.BOLD)
  .build();

Balloon balloon = new Balloon.Builder(context)
  .setTextForm(textForm)
  ...
```
</details>

### Icon Composition
We can customize the icon on the balloon.

```java
.setIconSpace(10) // sets right margin of the icon.
.setIconSize(20) // sets size of the icon.
.setIconDrawable(ContextCompat.getDrawable(context, R.drawable.ic_edit)) // sets a drawable resource.
```

### IconForm
`IconForm` has some attributes for `ImageView` to customize the icon of the Balloon. You can create the `IconForm` instance and reuse it on multiple Balloons.

```kotlin
val iconForm = IconForm.Builder(context)
  .setDrawable(ContextCompat.getDrawable(context, R.drawable.arrow))
  .setIconColor(ContextCompat.getColor(context, R.color.skyblue))
  .setIconSize(20)
  .setIconSize(12)
  .build()

val balloon = Balloon.Builder(context)
  .setIconForm(iconForm)
  ...
```

<details>
 <summary>Create IconForm with Kotlin DSL</summary>

You can create an instance of the `IconForm` with Kotlin DSL as the example below:

```kotlin
val iconForm = iconForm(context) {
  setDrawable(ContextCompat.getDrawable(context, R.drawable.arrow))
  setIconColor(ContextCompat.getColor(context, R.color.skyblue))
  setIconSize(20)
  setIconSize(12)
}

val balloon = Balloon.Builder(context)
  .setIconForm(iconForm)
  ...
```
</details>

<details>
 <summary>Create IconForm with Java</summary>

You can create an instance of the `IconForm` with Java as the example below:

```java
IconForm iconForm = new IconForm.Builder(context)
  .setDrawable(ContextCompat.getDrawable(context, R.drawable.arrow))
  .setIconColor(ContextCompat.getColor(context, R.color.skyblue))
  .setIconSize(20)
  .setIconSize(12)
  .build();

Balloon balloon = new Balloon.Builder(context)
  .setIconForm(iconForm)
  ...
```
</details>


### OnBalloonClickListener, OnBalloonDismissListener, OnBalloonOutsideTouchListener
We can listen to if the Balloon is clicked, dismissed, and touched outside with the below listeners.

```kotlin
.setOnBalloonClickListener { Toast.makeText(context, "clicked", Toast.LENGTH_SHORT).show() }
.setOnBalloonDismissListener { Toast.makeText(context, "dismissed", Toast.LENGTH_SHORT).show() }
.setOnBalloonOutsideTouchListener { Toast.makeText(context, "touched outside", Toast.LENGTH_SHORT).show() }
```

<details>
 <summary>Set Listeners with Java</summary>

You can set listeners with Java as the example below:

```java
balloon.setOnBalloonClickListener(new OnBalloonClickListener() {
  @Override
  public void onBalloonClick() {
    // doSomething;
  }
});

balloon.setOnBalloonDismissListener(new OnBalloonDismissListener() {
  @Override
  public void onBalloonDismiss() {
    // doSomething;
  }
});

balloon.setOnBalloonOutsideTouchListener(new OnBalloonOutsideTouchListener() {
  @Override
  public void onBalloonOutsideTouch() {
    // doSomething;
  }
});
```

</details>

### Custom Balloon Layout
You can fully customize the layout of the Balloon with the method below:

```kotlin
.setLayout(R.layout.my_balloon_layout)
```

You can build the Balloon with your own layout as the following example:

<img src="https://user-images.githubusercontent.com/24237865/61226019-aba41d80-a75c-11e9-9362-52e4742244b5.gif" align="right" width="310px"/>

First, create your XML layout file like `layout_custom_profile` to your taste and set it with the `setLayout` method.

```kotlin
val balloon = Balloon.Builder(context)
  .setLayout(R.layout.layout_custom_profile)
  .setArrowSize(10)
  .setArrowOrientation(ArrowOrientation.TOP)
  .setArrowPosition(0.5f)
  .setWidthRatio(0.55f)
  .setHeight(250)
  .setCornerRadius(4f)
  .setBackgroundColor(ContextCompat.getColor(this, R.color.black))
  .setBalloonAnimation(BalloonAnimation.CIRCULAR)
  .setLifecycleOwner(lifecycleOwner)
  .build()
```

That's all. If you need to get Views or need some interactions, you can get your custom layout with the `getContentView()` method from your instance of the Balloon.

```kotlin
val button: Button =
  balloon.getContentView().findViewById(R.id.button_edit)
button.setOnClickListener {
  Toast.makeText(context, "Edit", Toast.LENGTH_SHORT).show()
  balloon.dismiss()
}
```

<img alt="Image" src="https://github.com/user-attachments/assets/c188b987-7fb1-4877-ae8e-2ba486e9cea1" align="right" width="310px" />

### Stroke

You can apply a custom stroke to the balloon container and its arrow using the code below:

```kotlin
val balloon = Balloon.Builder(context)
  ..
  .setBalloonStroke(
        color = Color.White.toArgb(),
        thickness = 4f,
      )
  .build()
```

### Persistence
If you want to show up the Balloon only once or a specific number of times, you can implement it as the following example:<br>

```kotlin
balloon.setPreferenceName("MyBalloon") // sets preference name of the Balloon.
balloon.setShowCounts(3) // show-up three of times the popup. the default value is 1.
balloon.runIfReachedShowCounts {
  // do something after the preference showing counts is reached the goal.
  }
```

Also, you can clear all persisted preferences with the method below:
```kotlin
balloon.clearAllPreferences()
```

### Avoid Memory leak
Dialog, PopupWindow etc, can have memory leak issues if not dismissed before the activity or fragment is destroyed.<br>
But Lifecycles are now integrated with the Support Library since Architecture Components 1.0 Stable was released.<br>
So we can solve the memory leak issue very easily like the below.<br>

Just use `setLifecycleOwner` method. Then the `dismiss()` method will be called automatically before your activity or fragment would be destroyed.
```kotlin
.setLifecycleOwner(lifecycleOwner)
```

### Lazy initialization
You can initialize a property of the Balloon lazily with the `balloon()` extension and `Balloon.Factory` abstract class.<br>
The `balloon()` extension keyword can be used on your `Activity`, `Fragment`, and `View`.

__Before__<br>
`CustomActivity.kt`
```kotlin
class CustomActivity : AppCompatActivity() {
  private val profileBalloon by lazy { BalloonUtils.getProfileBalloon(context = this, lifecycleOwner = this) }

  // ...
}
```

__After__<br>
`CustomActivity.kt`
```kotlin
class CustomActivity : AppCompatActivity() {
  private val profileBalloon by balloon<ProfileBalloonFactory>()

  // ...
}
```

We should create a class which extends `Balloon.Factory`.<br>
An implementation class of the factory must have a default(non-argument) constructor. <br><br>
`ProfileBalloonFactory.kt`
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
      setBackgroundColor(ContextCompat.getColor(context, R.color.background900))
      setBalloonAnimation(BalloonAnimation.CIRCULAR)
      setLifecycleOwner(lifecycle)
    }
  }
}
```

### BalloonOverlay
We can show up an overlay over the whole screen except an anchor view.

```kotlin
balloon.setIsVisibleOverlay(true) // sets the visibility of the overlay for highlighting an anchor.
balloon.setOverlayColorResource(R.color.overlay) // background color of the overlay using a color resource.
balloon.setOverlayPadding(6f) // sets a padding value of the overlay shape internally.
balloon.setOverlayPaddingColorResource(R.color.colorPrimary) // sets color of the overlay padding using a color resource.
balloon.setBalloonOverlayAnimation(BalloonOverlayAnimation.FADE) // default is fade.
balloon.setDismissWhenOverlayClicked(false) // disable dismissing the balloon when the overlay is clicked.
```

We can change the shape of the highlighting using the `.setOverlayShape` method.

```kotlin
balloon.setOverlayShape(BalloonOverlayOval) // default shape
balloon.setOverlayShape(BalloonOverlayRect)
balloon.setOverlayShape(BalloonOverlayCircle(radius = 36f))
balloon.setOverlayShape(BalloonOverlayRoundRect(12f, 12f))
```
OVAL | CIRCLE | RECT | ROUNDRECT |
| :---------------: | :---------------: | :---------------: | :---------------: |
| <img src="https://user-images.githubusercontent.com/24237865/96139366-c7870800-0f39-11eb-9542-e98eac7ef193.gif" align="center" width="280px"/> | <img src="https://user-images.githubusercontent.com/24237865/96138448-c0abc580-0f38-11eb-92e6-daf2f8266a3e.gif" align="center" width="280px"/> | <img src="https://user-images.githubusercontent.com/24237865/96139358-c524ae00-0f39-11eb-82ff-90a4a734e076.gif" align="center" width="280px"/> | <img src="https://user-images.githubusercontent.com/24237865/96138463-c3a6b600-0f38-11eb-8a2d-57cf96c4190c.gif" align="center" width="280px"/> |

Also, we can set the specific position of the overlay shape with the method below:
```kotlin
balloon.setOverlayPosition(Point(x, y)) // sets a specific position of the overlay shape.
```

### BalloonAnimation
We can implement popup animations while showing and dismissing.

```kotlin
BalloonAnimation.NONE
BalloonAnimation.FADE
BalloonAnimation.OVERSHOOT
BalloonAnimation.ELASTIC
BalloonAnimation.CIRCULAR
```

FADE | OVERSHOOT | ELASTIC | CIRCULAR |
| :---------------: | :---------------: | :---------------: | :---------------: |
| <img src="https://user-images.githubusercontent.com/24237865/74601168-6115c580-50de-11ea-817b-a334f33b6f96.gif" align="center" width="100%"/> | <img src="https://user-images.githubusercontent.com/24237865/74601171-6410b600-50de-11ea-9ba0-5634e11f148a.gif" align="center" width="100%"/> | <img src="https://user-images.githubusercontent.com/24237865/74601170-63781f80-50de-11ea-8db4-93f1dd1291fc.gif" align="center" width="100%"/> | <img src="https://user-images.githubusercontent.com/24237865/74607359-b6bc9300-511b-11ea-978b-23bcc4399dce.gif" align="center" width="100%"/> |

### BalloonHighlightAnimation
We can give a repeated dynamic animations to the Balloon while it's showing up. The animation would work differently by the position of the arrow.

HEARTBEAT | SHAKE | BREATH | ROTATE |
| :---------------: | :---------------: | :---------------: | :---------------: |
| <img src="https://user-images.githubusercontent.com/24237865/135755074-6a9c87fc-55b2-460e-b34e-0b6808684a97.gif" align="center" width="100%"/> | <img src="https://user-images.githubusercontent.com/24237865/135755077-02eeddbe-95fe-49ee-ad22-1f15879e84f1.gif" align="center" width="100%"/> | <img src="https://user-images.githubusercontent.com/24237865/135755079-29ed8cd8-92fe-4b2a-8671-b3522999c551.gif" align="center" width="100%"/> | <img src="https://user-images.githubusercontent.com/24237865/135755080-36dc7c8b-063a-442b-bcbd-bc000e92f9ac.gif" align="center" width="100%"/> |

```kotlin
BalloonHighlightAnimation.NONE
BalloonHighlightAnimation.HEARTBEAT
BalloonHighlightAnimation.SHAKE
BalloonHighlightAnimation.BREATH
BalloonHighlightAnimation.ROTATE

.setBalloonHighlightAnimation(BalloonHighlightAnimation.SHAKE)
```

We can implement the rotate animation like the example below:

```kotlin
.setBalloonHighlightAnimation(BalloonHighlightAnimation.ROTATE)
.setBalloonRotationAnimation(
        BalloonRotateAnimation.Builder().setLoops(2).setSpeeds(2500).setTurns(INFINITE).build())
```

## Balloon builder methods
For more details, you can check out the documentations below:
- [Balloon documentations](https://skydoves.github.io/Balloon/balloon/com.skydoves.balloon/-balloon/index.html)
- [Builder documentations](https://skydoves.github.io/Balloon/balloon/com.skydoves.balloon/-balloon/-builder/index.html)

<img align="right" width="130px" src="https://user-images.githubusercontent.com/24237865/210227682-cbc03479-8625-4213-b907-4f15217f91ba.png"/>

## Balloon in Jetpack Compose

Balloon allows you to display tooltips in Jetpack Compose easily using the `Modifier.balloon()` API.

[![Maven Central](https://img.shields.io/maven-central/v/com.github.skydoves/balloon.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.github.skydoves%22%20AND%20a:%22balloon%22)

Add the dependency below to your **module**'s `build.gradle` file:

```gradle
dependencies {
    implementation("com.github.skydoves:balloon-compose:$version")
}
```

### Modifier.balloon

You can create and display tooltips using the `Modifier.balloon()` extension along with `rememberBalloonState()`, as demonstrated in the following example:

```kotlin
// Create and remember a builder for the Balloon.
val builder = rememberBalloonBuilder {
  setArrowSize(10)
  setArrowPosition(0.5f)
  setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
  setWidth(BalloonSizeSpec.WRAP)
  setHeight(BalloonSizeSpec.WRAP)
  setPadding(12)
  setMarginHorizontal(12)
  setCornerRadius(8f)
  setBackgroundColorResource(R.color.skyBlue)
  setBalloonAnimation(BalloonAnimation.ELASTIC)
}

// Create and remember the balloon state.
val balloonState = rememberBalloonState(builder)

Button(
  modifier = Modifier
    .align(Alignment.Center)
    .balloon(balloonState) {
      Text(text = "Now you can edit your profile!")
    },
  onClick = { balloonState.showAlignTop() },
) {
  Text(text = "Show Balloon")
}
```

### BalloonState

`BalloonState` is a state holder for managing balloon display and interactions. It provides control over the balloon lifecycle, including showing, dismissing, updating, and setting up listeners. You can create an instance using `rememberBalloonState()`:

```kotlin
val balloonState = rememberBalloonState(builder)

// Show the balloon
balloonState.showAlignTop()
balloonState.showAlignBottom()
balloonState.showAlignStart()
balloonState.showAlignEnd()
balloonState.showAtCenter()

// Dismiss the balloon
balloonState.dismiss()

// Check if the balloon is showing
if (balloonState.isShowing) {
  // ...
}
```

### Various Balloon Positions

You can display the balloon in different positions relative to the anchor composable:

```kotlin
val balloonState = rememberBalloonState(builder)

Column {
  Button(
    modifier = Modifier.balloon(balloonState) { Text("Tooltip content") },
    onClick = { balloonState.showAlignTop() }, // Shows above the button
  ) {
    Text(text = "Show Top")
  }

  Button(
    modifier = Modifier.balloon(balloonState) { Text("Tooltip content") },
    onClick = { balloonState.showAlignBottom() }, // Shows below the button
  ) {
    Text(text = "Show Bottom")
  }

  Button(
    modifier = Modifier.balloon(balloonState) { Text("Tooltip content") },
    onClick = { balloonState.showAlignStart() }, // Shows at the start of the button
  ) {
    Text(text = "Show Start")
  }

  Button(
    modifier = Modifier.balloon(balloonState) { Text("Tooltip content") },
    onClick = { balloonState.showAlignEnd() }, // Shows at the end of the button
  ) {
    Text(text = "Show End")
  }
}
```

### Auto-Display Balloon on Layout Ready

To automatically show a Balloon when your layout is drawn, you can use `LaunchedEffect`:

```kotlin
val balloonState = rememberBalloonState(builder)

LaunchedEffect(Unit) {
  balloonState.showAlignTop()
}

Button(
  modifier = Modifier.balloon(balloonState) {
    Text(text = "Now you can edit your profile!")
  },
  onClick = { balloonState.showAlignTop() },
) {
  Text(text = "Show Balloon")
}
```

### Compose Extensions

The `balloon-compose` package provides useful Compose extensions, such as setting a color with `androidx.compose.ui.graphics.Color`:

```kotlin
val builder = rememberBalloonBuilder {
  setText("Now you can edit your profile!")
  setArrowSize(10)
  setWidthRatio(1.0f)
  setHeight(BalloonSizeSpec.WRAP)
  setArrowOrientation(ArrowOrientation.BOTTOM)
  setArrowPosition(0.5f)
  setPadding(12)
  setMarginHorizontal(12)
  setTextSize(15f)
  setCornerRadius(8f)
  setTextColor(Color.White) // Set text color with Compose color.
  setBackgroundColor(Color.White) // Set background color with Compose color.
  setIconDrawableResource(R.drawable.ic_edit)
}
```

### Listening to Balloon Events

You can set listeners on the `BalloonState` to respond to balloon events:

```kotlin
val balloonState = rememberBalloonState(builder)

// Set click listener
balloonState.setOnBalloonClickListener {
  // Handle balloon click
}

// Set dismiss listener
balloonState.setOnBalloonDismissListener {
  // Handle balloon dismiss
}

// Set outside touch listener
balloonState.setOnBalloonOutsideTouchListener { view, event ->
  // Handle outside touch
}
```

## Find this library useful? :heart:
Support it by joining __[stargazers](https://github.com/skydoves/balloon/stargazers)__ for this repository. :star: <br>
Also, __[follow me](https://github.com/skydoves)__ on GitHub for my next creations! 🤩

# License
```xml
Copyright 2019 skydoves (Jaewoong Eum)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
