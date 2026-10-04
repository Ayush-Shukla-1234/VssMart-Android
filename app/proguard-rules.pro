# ProGuard / R8 Rules for VssMart

# Keep data models to ensure Firestore and JSON serialization work smoothly
-keep class io.mastercoding.vssmart.data.model.** { *; }
-keepclassmembers class io.mastercoding.vssmart.data.model.** {
    <fields>;
    <init>(...);
    public <methods>;
}

# Keep Firebase classes
-keepattributes *Annotation*
-dontwarn com.google.firebase.**
-keep class com.google.firebase.** { *; }
-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName <fields>;
    @com.google.firebase.firestore.Exclude <fields>;
    @com.google.firebase.firestore.Exclude <methods>;
}

# Keep Glide rules
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
    <init>(...);
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}
-dontwarn com.bumptech.glide.load.resource.bitmap.VideoDecoder

# AndroidX & Material Components
-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**
