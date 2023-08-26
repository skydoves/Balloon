import com.skydoves.balloon.Configuration

plugins {
  id("com.android.test")
  id("org.jetbrains.kotlin.android")
}

android {
  compileSdk = Configuration.compileSdk
  namespace = "com.skydoves.balloon.benchmark"

  defaultConfig {
    minSdk = Configuration.minSdkBenchmark
    targetSdk = Configuration.targetSdk
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }

  testOptions {
    managedDevices {
      devices {
        maybeCreate<com.android.build.api.dsl.ManagedVirtualDevice>("pixel2api31").apply {
          // Use device profiles you typically see in Android Studio.
          device = "Pixel 2"
          // Use only API levels 27 and higher.
          apiLevel = 31
          // To include Google services, use "google".
          systemImageSource = "aosp"
        }
      }
    }
  }

  kotlinOptions {
    jvmTarget = libs.versions.jvmTarget.get()
  }

  buildTypes {
    // This benchmark buildType is used for benchmarking, and should function like your
    // release build (for example, with minification on). It"s signed with a debug key
    // for easy local/CI testing.
    create("benchmark") {
      isDebuggable = true
      signingConfig = getByName("debug").signingConfig
      matchingFallbacks += listOf("release")
    }
  }

  targetProjectPath = ":benchmark-app"
  experimentalProperties["android.experimental.self-instrumenting"] = true
}

dependencies {
  implementation(libs.androidx.test.runner)
  implementation(libs.androidx.test.uiautomator)
  implementation(libs.androidx.benchmark.macro)
  implementation(libs.androidx.profileinstaller)
}

androidComponents {
  beforeVariants(selector().all()) {
    it.enable = it.buildType == "benchmark"
  }
}