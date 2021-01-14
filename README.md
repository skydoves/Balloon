<h1 align="center">Balloon</h1></br>

<p align="center">
:balloon: A lightweight popup like tooltips, fully customizable with arrow and animations.
</p>
</br>
<p align="center">
  <a href="https://opensource.org/licenses/Apache-2.0"><img alt="License" src="https://img.shields.io/badge/License-Apache%202.0-blue.svg"/></a>
  <a href="https://android-arsenal.com/api?level=16"><img alt="API" src="https://img.shields.io/badge/API-16%2B-brightgreen.svg?style=flat"/></a>
  <a href="https://github.com/skydoves/Balloon/actions"><img alt="Build Status" src="https://github.com/skydoves/Balloon/workflows/Android%20CI/badge.svg"/></a> 
  <a href="https://medium.com/swlh/a-lightweight-tooltip-popup-for-android-ef9484a992d7"><img alt="Medium" src="https://skydoves.github.io/badges/Story-Medium.svg"/></a>
  <a href="https://github.com/skydoves"><img alt="Profile" src="https://skydoves.github.io/badges/skydoves.svg"/></a>
  <a href="https://skydoves.github.io/libraries/balloon/html/balloon/com.skydoves.balloon/index.html"><img alt="Javadoc" src="https://skydoves.github.io/badges/javadoc-balloon.svg"/></a>
</p> <br>

<p align="center">
<img src="https://user-images.githubusercontent.com/24237865/61194943-f9d70380-a6ff-11e9-807f-ba1ca8126f8a.gif" width="32%"/>
<img src="https://user-images.githubusercontent.com/24237865/61225579-d346b600-a75b-11e9-84f8-3c06047b5003.gif" width="32%"/>
</p>

## Including in your project
[![Download](https://api.bintray.com/packages/devmagician/maven/balloon/images/download.svg)](https://bintray.com/devmagician/maven/balloon/_latestVersion)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.skydoves/balloon.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.github.skydoves%22%20AND%20a:%22balloon%22)
[![Balloon](https://jitpack.io/v/skydoves/Balloon.svg)](https://jitpack.io/#skydoves/Balloon)
### Gradle 
Add below codes to your **root** `build.gradle` file (not your module build.gradle file).
```gradle
allprojects {
    repositories {
        jcenter()
    }
}
```
And add a dependency code to your **module**'s `build.gradle` file.
```gradle
dependencies {
    implementation "com.github.skydoves:balloon:1.3.0"
}
```

## Usage

### Basic Example for Java
Here is a basic example of implementing balloon popup with icon and text using `Balloon.Builder` class.<br>

```java
Balloon balloon = new Balloon.Builder(context)
    .setArrowSize(10)
    .setArrowOrientation(ArrowOrientation.TOP)
    .setArrowConstraints(ArrowConstraints.ALIGN_ANCHOR)
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

### Create using kotlin dsl
This is how to create `Balloon` instance using kotlin dsl.

```kotlin
val balloon = createBalloon(context) {
  setArrowSize(10)
  setWidth(BalloonSizeSpec.WRAP)
  setHeight(65)
  setArrowPosition(0.7f)
  setCornerRadius(4f)
  setAlpha(0.9f)
  setText("You can access your profile from now on.")
  setTextColorResource(R.color.white_93)
  setTextIsHtml(true)
  setIconDrawable(ContextCompat.getDrawable(context, R.drawable.ic_profile))
  setBackgroundColorResource(R.color.colorPrimary)
  setOnBalloonClickListener(onBalloonClickListener)
  setBalloonAnimation(BalloonAnimation.FADE)
  setLifecycleOwner(lifecycleOwner)
}
```
### Width and height
We can handle sizes of the popup width and height using the below ways. <br>
If we would not set any specific sizes of the width/height of the balloon, the size of the popup will be decided by the content. 
#### Padding
Balloon wraps a content. We can control the content size of the balloon using padding of the content.<br>
```kotlin
balloon.setPadding(6) // sets 6dp padding to all directions (left-top-right-bottom)
balloon.setPaddingLeft(8) // sets 8dp padding to content's left.
balloon.setPaddingTop(12) // sets 12dp padding to content's top.
```
#### Specific size
We can set the specific size of the balloon regardless size of the contents.
```kotlin
balloon.setWidth(220) // sets 220dp width size.
balloon.setHeight(160) // sets 160dp height size.
balloon.setWidth(BalloonSizeSpec.WRAP) // sets width size depending on the content's size.
balloon.setHeight(BalloonSizeSpec.WRAP) // sets height size depending on the content's size.
```
#### According to screen ratio
Also, we can set the width according to the ratio of the horizontal screen's size.
```kotlin
balloon.setWidthRatio(0.5f) // sets width as 50% of the horizontal screen's size.
```

### Show and dismiss
This is how to show balloon popup and dismiss. <br>
We can set the balloon popup's position using `x-Offset` and `y-Offset` attributes. <br>
And show based on top/bottom/right/left alignment if we use `showAlign__` method.

```kotlin
balloon.show(anchor: View) // shows the balloon on the center of an anchor view.
balloon.show(anchor: View, xOff: Int, yOff: Int) // shows the balloon on an anchor view with x-off and y-off.
balloon.showAlignTop(anchor: View) // shows the balloon on an anchor view as the top alignment.
balloon.showAlignTop(anchor: View, xOff: Int, yOff: Int) // shows top alignment with x-off and y-off.
balloon.showAlignBottom(anchor: View) // shows the balloon on an anchor view as the bottom alignment.
balloon.showAlignBottom(anchor: View, xOff: Int, yOff: Int) // shows bottom alignment with x-off and y-off.
balloon.showAlignRight(anchor: View) // shows the balloon on an anchor view as the right alignment.
balloon.showAlignRight(anchor: View, xOff: Int, yOff: Int) // shows right alignment with x-off and y-off.
balloon.showAlignLeft(anchor: View) // shows the balloon on an anchor view as the left alignment.
balloon.showAlignLeft(anchor: View, xOff: Int, yOff: Int) // shows left alignment with x-off and y-off.
```

Or we can show balloon popup using kotlin extension.

```java
myButton.showAlignTop(balloon)
```
We can dismiss popup simply using `Balloon.dismiss()` method.
```java
balloon.dismiss()
balloon.dismissWithDelay(1000L) // dismisses 1000 milliseconds later when the popup is shown
```
We can dismiss  automatically some milliseconds later when the popup is shown using <br> 
`setAutoDismissDuration` method on `Balloon.Builder`.
```java
Balloon.Builder(context)
   // dismisses automatically 1000 milliseconds later when the popup is shown.
   .setAutoDismissDuration(1000L)
   ...
```

### Show sequentially
We can show balloon popup sequentially using `relayShow` method. <br>
The `relayShow` method makes that `setOnDismissListener` of the first balloon is reset to show the <br>
next balloon and returns an instance of the next balloon.

```kotlin
customListBalloon
  .relayShowAlignBottom(customProfileBalloon, circleImageView) // relay to customListBalloon
  .relayShowAlignTop(customTagBalloon, bottomNavigationView, 130, 0) // relay to customProfileBalloon

// show sequentially customListBalloon-customProfileBalloon-customTagBalloon
customListBalloon.showAlignBottom(toolbar_list)
```

### Margin
If the location of the balloon according to the anchor is located at the boundaries on the screen,<br>
the balloon will be stick to the end of the screen. In that case, we can resolve by giving margins to the balloon.
```kotlin
.setMargin(12) // sets the margin on the balloon all directions.
.setMarginLeft(14) // sets the left margin on the balloon.
.setMarginRight(14) // sets the right margin on the balloon.
```

### Arrow Composition
We can customize the arrow on the balloon popup. <br>

```java
.setIsVisibleArrow(true) // sets the visibility of the arrow.
.setArrowSize(10) // sets the arrow size.
.setArrowSize(BalloonSizeSpec.WRAP) // sets arrow size depending on the original resources' size.
.setArrowPosition(0.8f) // sets the arrow position using the popup size's ratio (0 ~ 1.0)
.setArrowOrientation(ArrowOrientation.TOP) // sets the arrow orientation. top, bottom, left, right
.setArrowDrawable(ContextCompat.getDrawable(context, R.drawable.arrow)) // sets the arrow drawable.
```

#### ArrowConstraints
We can determines the constraints of the arrow positioning using the `ArrowConstraints`.<br>
This constraint affects the `setArrowPosition`.
```kotlin
// Aligning arrow based on the anchor view. 
// if an arrowPosition is 0.5, the arrow will be positioned center of the anchor view.
.setArrowConstraints(ArrowConstraints.ALIGN_ANCHOR)
// Aligning arrow based on the balloon popup.
// if an arrowPosition is 0.5, the arrow will be positioned center of the balloon popup.
.setArrowConstraints(ArrowConstraints.ALIGN_BALLOON) // default
```

Below previews are implemented using `setArrowOrientation` and `setArrowPosition` methods. <br>
`setArrowPosition` measures the balloon popup size and sets the arrow's position using the ratio value.

Orientation: BOTTOM<br> Position: 0.62<br> showAlignTop | Orientation: TOP<br> Position : 0.5<br> showAlignBottom | Orientation: LEFT<br> Position: 0.5<br> showAlignRight  | Orientation: RIGHT<br> Position: 0.5<br> showAlignLeft |
| :---------------: | :---------------: | :---------------: | :---------------: |
| <img src="https://user-images.githubusercontent.com/24237865/61320410-55120e80-a844-11e9-9af6-cae49b8897e7.gif" align="center" width="100%"/> | <img src="https://user-images.githubusercontent.com/24237865/61320412-55120e80-a844-11e9-9ca9-81375707886e.gif" align="center" width="100%"/> | <img src="https://user-images.githubusercontent.com/24237865/61320415-55aaa500-a844-11e9-874f-ca44be02aace.gif" align="center" width="100%"/> | <img src="https://user-images.githubusercontent.com/24237865/61320416-55aaa500-a844-11e9-9aa1-53e409ca63fb.gif" align="center" width="100%"/> |

### Text Composition
We can customize the text on the balloon popup.

```java
.setText("You can edit your profile now!")
.setTextSize(15f)
.setTextTypeface(Typeface.BOLD)
.setTextColor(ContextCompat.getColor(context, R.color.white_87))
```

If your text has HTML in it, you can enable HTML rendering by adding this:
```java
.setTextIsHtml(true)
```

This will parse the text using `Html.fromHtml(text)`.

### TextForm
TextFrom is an attribute class that has some attributes about TextView for customizing popup text.

```java
TextForm textForm = new TextForm.Builder(context)
  .setText("This is a TextForm")
  .setTextColorResource(R.color.colorPrimary)
  .setTextSize(14f)
  .setTextTypeface(Typeface.BOLD)
  .build();

builder.setTextForm(textForm);
```

This is how to create `TextForm` using kotlin dsl.

```kotlin
val form = textForm(context) {
  text = "This is a TextForm"
  textColor = ContextCompat.getColor(context, com.skydoves.balloondemo.R.color.white_87)
  textSize = 14f
  textTypeface = Typeface.BOLD
}
```

### Icon Composition
We can customize the icon on the balloon popup.

```java
.setIconSpace(10) // sets right margin of the icon.
.setIconSize(20) // sets size of the icon.
.setIconDrawable(ContextCompat.getDrawable(context, R.drawable.ic_edit)) // sets a drawable resource.
```

### IconForm
IconForm is an attribute class that has some attributes about ImageView for customizing popup icon.
 
```java
IconForm iconForm = new IconForm.Builder(context)
  .setDrawable(ContextCompat.getDrawable(context, R.drawable.arrow))
  .setIconColor(ContextCompat.getColor(context, R.color.colorPrimary))
  .setIconSize(20)
  .setIconSpace(12)
  .build();
  
builder.setIconForm(iconForm);
```

This is how to create `IconForm` using kotlin dsl.

```kotlin
val form = iconForm(context) {
  drawable = ContextCompat.getDrawable(context, R.drawable.arrow)
  iconColor = ContextCompat.getColor(context, R.color.skyblue)
  iconSize = 20
  iconSpace = 12
}
```

### OnBalloonClickListener, OnBalloonDismissListener, OnBalloonOutsideTouchListener
We can listen to the balloon popup is clicked, dismissed or touched outside using listeners.

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

We can simplify it using kotlin.
```kotlin
.setOnBalloonClickListener { Toast.makeText(context, "clicked", Toast.LENGTH_SHORT).show() }
.setOnBalloonDismissListener { Toast.makeText(context, "dismissed", Toast.LENGTH_SHORT).show() }
.setOnBalloonOutsideTouchListener { Toast.makeText(context, "touched outside", Toast.LENGTH_SHORT).show() }
```

### Customized layout
We can fully customize the balloon layout using below method.
```java
.setLayout(R.layout.my_balloon_layout)
```

This is an example of implementing custom balloon popup.

<img src="https://user-images.githubusercontent.com/24237865/61226019-aba41d80-a75c-11e9-9362-52e4742244b5.gif" align="right" width="32%"/>

Firstly create an xml layout file like `layout_custom_profile` on your taste.
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
And next we can get the inflated custom layout using `getContentView` method.
```java
val button: Button = 
  balloon.getContentView().findViewById(R.id.button_edit)
button.setOnClickListener {
  Toast.makeText(context, "Edit", Toast.LENGTH_SHORT).show()
  balloon.dismiss()
}
```

### Persistence
If you want to show-up the balloon popup only once or a specific number of times, here is how to implement it simply.<br>
```java
.setPreferenceName("MyBalloon") // sets preference name of the Balloon.
.setShowCounts(3) // show-up three of times the popup. the default value is 1.
.runIfReachedShowCounts {
  // do something after the preference showing counts is reached the goal.
}
```

But you can implement it more variously using [Only](https://github.com/skydoves/Only).

```kotlin
only("introPopup", times = 3) {
  onDo { balloon.showAlignTop(anchor) }
}
```

### Avoid Memory leak
Dialog, PopupWindow and etc.. have memory leak issue if not dismissed before activity or fragment are destroyed.<br>
But Lifecycles are now integrated with the Support Library since Architecture Components 1.0 Stable released.<br>
So we can solve the memory leak issue so easily.<br>

Just use `setLifecycleOwner` method. Then `dismiss` method will be called automatically before activity or fragment would be destroyed.
```java
.setLifecycleOwner(lifecycleOwner)
```

### Lazy initialization
We can initialize the balloon property lazily using `balloon` keyword and `Balloon.Factory` abstract class.<br>
The `balloon` extension keyword can be used on `Activity`, `Fragment`, and `View`.

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
We can show an overlay window over the whole screen except an anchor view.
```kotlin
.setIsVisibleOverlay(true) // sets the visibility of the overlay for highlighting an anchor.
.setOverlayColorResource(R.color.overlay) // background color of the overlay using a color resource.
.setOverlayPadding(6f) // sets a padding value of the overlay shape internally.
.setBalloonOverlayAnimation(BalloonOverlayAnimation.FADE) // default is fade.
```
We can change the shape of the highlighting using `.setOverlayShape`.
```kotlin
.setOverlayShape(BalloonOverlayOval) // default shape
.setOverlayShape(BalloonOverlayRect)
.setOverlayShape(BalloonOverlayCircle(radius = 36f))
.setOverlayShape(BalloonOverlayRoundRect(12f, 12f))
```
OVAL | CIRCLE | RECT | ROUNDRECT |
| :---------------: | :---------------: | :---------------: | :---------------: |
| <img src="https://user-images.githubusercontent.com/24237865/96139366-c7870800-0f39-11eb-9542-e98eac7ef193.gif" align="center" width="100%"/> | <img src="https://user-images.githubusercontent.com/24237865/96138448-c0abc580-0f38-11eb-92e6-daf2f8266a3e.gif" align="center" width="100%"/> | <img src="https://user-images.githubusercontent.com/24237865/96139358-c524ae00-0f39-11eb-82ff-90a4a734e076.gif" align="center" width="100%"/> | <img src="https://user-images.githubusercontent.com/24237865/96138463-c3a6b600-0f38-11eb-8a2d-57cf96c4190c.gif" align="center" width="100%"/> |

And we can set the specific position of the overlay shape using the below method.
```kotlin
.setOverlayPosition(Point(x, y)) // sets a specific position of the overlay shape.
```

### BalloonAnimation
We can implement popup animations when showing and dismissing.

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

## Balloon builder methods
We can reference all kinds and descriptions of functions details here.<br>
- [Balloon documentations](https://skydoves.github.io/libraries/balloon/html/balloon/com.skydoves.balloon/-balloon/index.html)
- [Builder documentations](https://skydoves.github.io/libraries/balloon/html/balloon/com.skydoves.balloon/-balloon/-builder/index.html)
```java
.setWidth(value: Int)
.setWidthRatio(@FloatRange(from = 0.0, to = 1.0) value: Float)
.setHeight(value: Int)
.setSize(value: Int, value: Int)
.setSpace(value: Int)
.setPadding(value: Int)
.setPaddingLeft(value: Int)
.setPaddingTop(value: Int)
.setPaddingRight(value: Int)
.setPaddingBottom(value: Int)
.setMargin(value: Int)
.setMarginLeft(value: Int)
.setMarginTop(value: Int)
.setMarginRight(value: Int)
.setMarginBottom(value: Int)
.setElevation(value: Int)
.setIsVisibleArrow(value: Boolean)
.setArrowSize(value: Int)
.setArrowPosition(@FloatRange(from = 0.0, to = 1.0) value: Float)
.setArrowOrientation(value: ArrowOrientation)
.setArrowConstraints(ArrowConstraints.ALIGN_ANCHOR)
.setArrowColor(value: Int)
.setArrowColorResource(@ColorRes value: Int)
.setArrowDrawable(value: Drawable?)
.setArrowDrawableResource(@DrawableRes value: Int)
.setArrowAlignAnchorPadding(value: Int)
.setArrowAlignAnchorPaddingRatio(value: Float)
.setBackgroundColor(value: Int)
.setBackgroundColorResource(@ColorRes value: Int)
.setBackgroundDrawable(value: Drawable?)
.setBackgroundDrawableResource(@DrawableRes value: Int)
.setCornerRadius(value: Float)
.setText(value: String)
.setTextResource(value: Int)
.setTextColor(value: Int)
.setTextColorResource(value: Int)
.setTextSize(value: Float)
.setTextTypeface(value: Int)
.setTextGravity(value: Int)
.setTextForm(value: TextForm)
.setIconDrawable(value: Drawable?)
.setIconDrawableResource(@DrawableRes value: Int)
.setIconSize(value: Int)
.setIconWidth(value: Int)
.setIconHeight(value: Int)
.setIconColor(value: Int)
.setIconColorResource(@ColorRes value: Int)
.setIconSpace(value: Int)
.setIconForm(value: IconForm)
.setIconGravity(value: IconGravity)
.setAlpha(@FloatRange(from = 0.0, to = 1.0) value: Float)
.setLayout(@LayoutRes layout: Int)
.setIsVisibleOverlay(value: Boolean)
.setOverlayColor(@ColorInt value: Int)
.setOverlayColorResource(@ColorRes value: Int)
.setOverlayPadding(@Dp value: Float)
.setOverlayPosition(value: Point)
.setOverlayShape(value: BalloonOverlayShape)
.setPreferenceName(value: String)
.setShowCount(value: Int)
.isRtlSupport(value: Boolean)
.setFocusable(value: Boolean)
.setLifecycleOwner(value: LifecycleOwner)
.setDismissWhenClicked(value: Boolean)
.setDismissWhenLifecycleOnPause(value: Boolean)
.setDismissWhenTouchOutside(value: Boolean)
.setDismissWhenShowAgain(value: Boolean)
.setDismissWhenOverlayClicked(value: Boolean)
.setBalloonAnimation(value: BalloonAnimation)
.setOnBalloonClickListener(value: OnBalloonClickListener)
.setOnBalloonDismissListener(value: OnBalloonDismissListener)
.setOnBalloonInitializedListener(value: OnBalloonInitializedListener)
.setOnBalloonOutsideTouchListener(value: OnBalloonOutsideTouchListener)
.setOnBalloonOverlayClickListener(value: OnBalloonOverlayClickListener)
.setDismissWhenTouchOutside(value: Boolean)
```

## Find this library useful? :heart:
Support it by joining __[stargazers](https://github.com/skydoves/balloon/stargazers)__ for this repository. :star:

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
