# Balloon 1.x (View)

!!! warning "This section documents the previous major version"

    Balloon `1.7.6` is the **last release of the View based library**. It still works and stays
    published on Maven Central, but it receives no further releases.

    New projects should use [Balloon 2.0.0](../index.md), which is built on Compose
    Multiplatform. If you are upgrading, read the [Migration guide](../migration.md).

## What is here

Balloon 1.x is an Android only tooltip library built on `PopupWindow`. It ships two artifacts:

| Artifact | What it is |
| --- | --- |
| `com.github.skydoves:balloon:1.7.6` | the View and XML implementation |
| `com.github.skydoves:balloon-compose:1.7.6` | a Jetpack Compose wrapper around it |

```kotlin
dependencies {
    implementation("com.github.skydoves:balloon:1.7.6")
    implementation("com.github.skydoves:balloon-compose:1.7.6")
}
```

## Pages

- [Getting Started](getting-started.md)
- [Showing Balloon](showing.md)
- [Arrow](arrow.md)
- [Text and Icon](text-icon.md)
- [Customization](customization.md)
- [Overlay](overlay.md)
- [Animation](animation.md)
- [Listeners](listeners.md)
- [Persistence](persistence.md)
- Jetpack Compose
    - [Overview](compose/overview.md)
    - [BalloonState](compose/balloon-state.md)
    - [Compose Extensions](compose/extensions.md)

## Should I upgrade?

| You are using | Recommendation |
| --- | --- |
| `balloon-compose` in a Compose app | Upgrade. The entry points already share their names, so the change is mostly imports, `Dp`, and `Color` |
| `balloon` from Compose via `Modifier.balloon` | Upgrade, same as above |
| `balloon` from XML layouts, Fragments, or RecyclerView adapters | Stay on `1.7.6`. There is no View API in 2.0.0 |
| Starting a new project | Use 2.0.0 |

See the [Migration guide](../migration.md) for a setter by setter mapping.
