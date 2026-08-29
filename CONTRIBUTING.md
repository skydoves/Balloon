## How to contribute
We'd love to accept your patches and contributions to this project. There are just a few small guidelines you need to follow.

## Preparing a pull request for review
Ensure your change is properly formatted by running:

```gradle
./gradlew spotlessApply
```

Then dump binary API of this library that is public in sense of Kotlin visibilities and ensures that the public binary API wasn't changed in a way that make this change binary incompatible. 

```gradle
./gradlew apiDump
```

Please correct any failures before requesting a review.

## Golden screenshot tests

`:balloon:desktopTest` renders 211 balloon configurations and compares each one against a
stored PNG under `balloon/src/desktopTest/resources/golden`. Anything that changes what the
library draws will fail there, which is the point: the goldens are the record of how a balloon
is supposed to look.

A failing case writes `<name>-expected.png`, `<name>-actual.png` and a magenta `<name>-diff.png`
into `balloon/build/reports/golden`, so look at those before deciding what to do.

If the change was intentional, re-record and commit the new PNGs alongside it:

```gradle
./gradlew :balloon:desktopTest -Pballoon.updateGolden
```

Re-recording is a normal part of a rendering change. Re-recording to make a failure go away
without looking at the diff is not, since it silently rewrites the thing the suite exists to
protect.

New cases go in `GoldenCases.kt`. Two things to watch for:

- Give the case a geometry where the setting is actually observable. Several knobs, the
  `ALIGN_ANCHOR` padding band in particular, do nothing at the default arrow position, so a
  case that varies only the setter renders identically to its neighbours and can never fail.
- Leave animations off. `BalloonAnimation.NONE`, no highlight animation, no auto-dismiss. A
  frame captured part way through an animation is a flaky golden; behaviour that moves belongs
  in the suites under `commonTest` and `skiaTest`.

## Code reviews
All submissions, including submissions by project members, require review. We use GitHub pull requests for this purpose. Consult [GitHub Help](https://docs.github.com/en/github/collaborating-with-pull-requests/proposing-changes-to-your-work-with-pull-requests/about-pull-requests) for more information on using pull requests.
