# Persistence

Balloon supports persisting show counts to control how many times a tooltip is displayed. This is useful for onboarding flows where you only want to show tips a limited number of times.

## Show Count Limit

Set a maximum number of times the Balloon should be shown:

```kotlin
Balloon.Builder(context)
    .setPreferenceName("my_tooltip")
    .setShowCounts(3) // show only 3 times
```

With this configuration, the Balloon will only be displayed the first 3 times `showAlign*` is called. After that, the Balloon will not show.

## Check if Should Show

Before showing, you can check if the Balloon should be displayed based on the show count:

```kotlin
if (balloon.shouldShowUp()) {
    balloon.showAlignBottom(anchor)
}
```

The `shouldShowUp()` method returns `true` if the show count hasn't been reached yet.

## Clear Preferences

Reset the show count for a specific Balloon:

```kotlin
balloon.clearAllPreferences()
```

This clears all persisted data for the Balloon, allowing it to be shown again.

## Preference Name

Each Balloon that uses persistence should have a unique preference name:

```kotlin
Balloon.Builder(context)
    .setPreferenceName("unique_tooltip_name")
    .setShowCounts(1) // show only once
```

The preference name is used as a key to store the show count in SharedPreferences.

## BalloonPersistence

For advanced use cases, you can access the `BalloonPersistence` singleton directly:

```kotlin
val persistence = BalloonPersistence.getInstance(context)

// Check if should show
val shouldShow = persistence.shouldShowUp("tooltip_name", 3)

// Manually increment count
persistence.putIncrementedCounts("tooltip_name")

// Clear all preferences
persistence.clearAllPreferences()
```

## One-Time Tooltips

A common use case is showing a tooltip only once:

```kotlin
val onboardingBalloon = Balloon.Builder(context)
    .setText("This is a new feature!")
    .setPreferenceName("feature_onboarding")
    .setShowCounts(1)
    .build()

// This will only show the first time
onboardingBalloon.showAlignBottom(anchor)
```
