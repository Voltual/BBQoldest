-dontobfuscate
-keepnames class ** { *; }
-keep class cc.bbq.xq.RetrofitClient { *; }
-keep class cc.bbq.xq.RetrofitClient$* { *; } # 仅直接内部类
 -assumenosideeffects class android.util.Log { *; }
-assumenosideeffects class kotlinx.coroutines.DebugStrings {
    public static *** toString(...);
}
-assumenosideeffects class **$$Lambda$* { *; }
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses