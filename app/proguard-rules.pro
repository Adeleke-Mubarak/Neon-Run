# Neon Run: Fracture — ProGuard / R8 Configuration

# Preserve OpenGL ES 3.0 JNI and Native Bindings
-keepclasseswithmembernames class * {
    native <methods>;
}

# Preserve Game Data Models and Enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class com.example.longrunner.game.player.CharacterData { *; }
-keep class com.example.longrunner.game.powerups.PowerUpType { *; }
-keep class com.example.longrunner.game.collectibles.CollectibleType { *; }
-keep class com.example.longrunner.game.obstacles.ObstacleType { *; }
-keep class com.example.longrunner.game.missions.MissionType { *; }
-keep class com.example.longrunner.game.achievements.AchievementId { *; }
-keep class com.example.longrunner.game.settings.SettingsManager { *; }

# Preserve Android View & Activity entrypoints
-keep public class com.example.longrunner.MainActivity { *; }
-keep class com.example.longrunner.ui.GameSurfaceView { *; }
-keep class com.example.longrunner.game.graphics.Renderer3D { *; }
-keep class com.example.longrunner.game.graphics.Shader { *; }

# General optimizations
-dontwarn androidx.**
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
