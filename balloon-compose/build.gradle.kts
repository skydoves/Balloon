import com.skydoves.balloon.Configuration
import org.jetbrains.dokka.DokkaConfiguration.Visibility
import org.jetbrains.dokka.gradle.DokkaTaskPartial

@Suppress("DSL_SCOPE_VIOLATION")
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
  }

  buildFeatures {
    compose = true
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }

  kotlinOptions {
    jvmTarget = libs.versions.jvmTarget.get()
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

baselineProfile {
  baselineProfileOutputDir = "."
  filter {
    include("com.skydoves.balloon.compose.**")
  }
}

kotlin {
  compilerOptions {
    freeCompilerArgs.addAll(
      "-Xexplicit-api=strict",
      "-opt-in=com.skydoves.balloon.annotations.InternalBalloonApi",
    )
  }
}

tasks.withType<DokkaTaskPartial>().configureEach {
  dokkaSourceSets.configureEach {
    documentedVisibilities.set(
      setOf(Visibility.PUBLIC)
    )
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

  baselineProfile(project(":benchmark"))
  dokkaPlugin(libs.android.documentation.plugin)
}
