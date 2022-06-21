package com.skydoves.balloon

object Versions {
  internal const val ANDROID_GRADLE_PLUGIN = "7.2.0"
  internal const val ANDROID_GRADLE_SPOTLESS = "6.3.0"
  internal const val GRADLE_NEXUS_PUBLISH_PLUGIN = "1.1.0"
  internal const val KOTLIN = "1.6.21"
  internal const val KOTLIN_GRADLE_DOKKA = "1.6.10"
  internal const val KOTLIN_BINARY_VALIDATOR = "0.8.0"

  internal const val APPCOMPAT = "1.4.0"
  internal const val MATERIAL = "1.5.0"
  internal const val FRAGMENT_KTX = "1.4.0"
  internal const val LIFECYCLE = "2.4.1"
  internal const val ANNOTATION = "1.3.0"
}

object Dependencies {
  const val androidGradlePlugin =
    "com.android.tools.build:gradle:${Versions.ANDROID_GRADLE_PLUGIN}"
  const val gradleNexusPublishPlugin =
    "io.github.gradle-nexus:publish-plugin:${Versions.GRADLE_NEXUS_PUBLISH_PLUGIN}"
  const val kotlinGradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.KOTLIN}"
  const val spotlessGradlePlugin =
    "com.diffplug.spotless:spotless-plugin-gradle:${Versions.ANDROID_GRADLE_SPOTLESS}"
  const val dokka = "org.jetbrains.dokka:dokka-gradle-plugin:${Versions.KOTLIN_GRADLE_DOKKA}"
  const val kotlinBinaryValidator =
    "org.jetbrains.kotlinx:binary-compatibility-validator:${Versions.KOTLIN_BINARY_VALIDATOR}"

  const val appcompat = "androidx.appcompat:appcompat:${Versions.APPCOMPAT}"
  const val material = "com.google.android.material:material:${Versions.MATERIAL}"
  const val fragmentKtx = "androidx.fragment:fragment-ktx:${Versions.FRAGMENT_KTX}"
  const val lifecycle = "androidx.lifecycle:lifecycle-common-java8:${Versions.LIFECYCLE}"
  const val annotation = "androidx.annotation:annotation:${Versions.ANNOTATION}"
}
