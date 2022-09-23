### 🎯 Goal
Describe the big picture of your changes here to communicate to the maintainers why we should accept this pull request. If it fixes a bug or resolves a feature request, be sure to link to that issue.

### 🛠 Implementation details
Describe the implementation details for this Pull Request.

### ✍️ Explain examples
Explain examples with code for this updates.

### Preparing a pull request for review
Ensure your change is properly formatted by running:

```bash
$ ./gradlew spotlessApply
```

Then dump binary API of this library that is public in sense of Kotlin visibilities and ensures that the public binary API wasn't changed in a way that make this change binary incompatible.

```bash
./gradlew apiDump
```

Please correct any failures before requesting a review.

## Code reviews
All submissions, including submissions by project members, require review. We use GitHub pull requests for this purpose. Consult [GitHub Help](https://docs.github.com/en/github/collaborating-with-pull-requests/proposing-changes-to-your-work-with-pull-requests/about-pull-requests) for more information on using pull requests.
