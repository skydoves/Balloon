# Custom views inflated from XML layouts via LayoutInflater (uses reflection).
# balloon_layout_body.xml references RadiusLayout and VectorTextView.
# balloon_layout_overlay.xml references BalloonAnchorOverlayView.
-keep class com.skydoves.balloon.radius.RadiusLayout { <init>(...); }
-keep class com.skydoves.balloon.vectortext.VectorTextView { <init>(...); }
-keep class com.skydoves.balloon.overlay.BalloonAnchorOverlayView { <init>(...); }

# Balloon.Factory subclasses are instantiated via reflection (getDeclaredConstructor().newInstance())
# in ActivityBalloonLazy, FragmentBalloonLazy, and ViewBalloonLazy.
-keep class * extends com.skydoves.balloon.Balloon$Factory { <init>(); }
