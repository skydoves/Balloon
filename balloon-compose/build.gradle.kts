import com.skydoves.balloon.Configuration
import org.jetbrains.dokka.gradle.DokkaTask
import java.net.URL


plugins {
  id(libs.plugins.android.library.get().pluginId)
  id(libs.plugins.kotlin.android.get().pluginId)
  id(libs.plugins.compose.compiler.get().pluginId)
  id(libs.plugins.nexus.plugin.get().pluginId)
  id(libs.plugins.baseline.profile.get().pluginId)
  id(libs.plugins.dokka.get().pluginId)
}

apply(from = "${rootDir}/scripts/publish-module.gradle.kts")

mavenPublishing {
  val artifactId = "balloon-compose"
  coordinates(
    Configuration.artifactGroup,
    artifactId,
    rootProject.extra.get("libVersion").toString()
  )

  pom {
    name.set(artifactId)
    description.set("Modernized and sophisticated tooltips, fully customizable with an arrow and animations for Android.")
  }
}

android {
  compileSdk = Configuration.compileSdk
  namespace = "com.skydoves.balloon.compose"

  defaultConfig {
    minSdk = Configuration.minSdk
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildFeatures {
    compose = true
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }

  packaging {
    resources {
      excludes.add("/META-INF/{AL2.0,LGPL2.1}")
    }
  }

  lint {
    abortOnError = false
  }
}

tasks.withType<DokkaTask>().configureEach {
  dokkaSourceSets {
    named("main") {
      moduleName.set("balloon-compose")
      includes.from("README.md")
      sourceLink {
        remoteUrl.set(URL("https://github.com/skydoves/balloon"))
        remoteLineSuffix.set("#L")
      }
    }
  }
}

baselineProfile {
  baselineProfileOutputDir = "."
  filter {
    include("com.skydoves.balloon.compose.**")
  }
}

kotlin {
  compilerOptions {
    jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    freeCompilerArgs.addAll(
      "-Xexplicit-api=strict",
      "-opt-in=com.skydoves.balloon.annotations.InternalBalloonApi",
    )
  }

  target {
    compilations.configureEach {
      if (name.contains("Test")) {
        compileTaskProvider.configure {
          compilerOptions {
            freeCompilerArgs.set(listOf("-opt-in=com.skydoves.balloon.annotations.InternalBalloonApi"))
          }
        }
      }
    }
  }
}

tasks.withType(JavaCompile::class.java).configureEach {
  this.targetCompatibility = libs.versions.jvmTarget.get()
  this.sourceCompatibility = libs.versions.jvmTarget.get()
}

dependencies {
  api(project(":balloon"))

  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.material)
  implementation(libs.androidx.compose.runtime)
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.lifecycle)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.savedstate.compose)

  testImplementation(libs.junit)
  testImplementation(libs.truth)
  testImplementation(libs.robolectric)
  testImplementation(libs.mockk)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.androidx.test.core)
  testImplementation(platform(libs.androidx.compose.bom))
  testImplementation(libs.androidx.compose.ui.test)

  androidTestImplementation(libs.junit)
  androidTestImplementation(libs.truth)
  androidTestImplementation(libs.androidx.test.core)
  androidTestImplementation(libs.androidx.test.runner)
  androidTestImplementation(libs.androidx.test.rules)
  androidTestImplementation(libs.androidx.test.ext.junit)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test)
  debugImplementation(libs.androidx.compose.ui.test.manifest)

  baselineProfile(project(":benchmark"))
  dokkaPlugin(libs.android.documentation.plugin)
}
