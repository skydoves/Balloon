plugins {
  id("com.android.test")
  id("org.jetbrains.kotlin.android")
}

android {
  compileSdk = 32

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }

  kotlinOptions {
    jvmTarget = "1.8"
  }

  defaultConfig {
    minSdk = 23
    targetSdk = 32
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
  implementation(com.skydoves.balloon.Dependencies.ANDROIDX_TEST_RUNNER)
  implementation(com.skydoves.balloon.Dependencies.MACRO_BENCHMARK)
  implementation(com.skydoves.balloon.Dependencies.BASE_PROFILE)
  implementation(com.skydoves.balloon.Dependencies.ANDROIDX_UI_AUTOMATOR)
}

androidComponents {
  beforeVariants(selector().all()) {
    it.enable = it.buildType == "benchmark"
  }
}