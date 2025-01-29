# R8 full mode strips signatures from non-kept items.
# This is required for preventing obfuscation the Balloon.Builder's constructors.
-keep class com.skydoves.balloon.** { *; }
-keep class ** extends com.skydoves.balloon.**