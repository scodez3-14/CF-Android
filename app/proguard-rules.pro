# Retrofit, OkHttp, Room, Hilt, Jsoup and Coil all ship their own consumer
# rules, so this file only covers the reflection-based code that does not.

# ── Gson ───────────────────────────────────────────────────────────────────────
# Gson reads fields reflectively; without these rules minification breaks both
# API parsing and the Room List<String> converters.

-keepattributes Signature
-keepattributes *Annotation*

# All API DTOs are (de)serialized by Gson.
-keep class com.codeforces.app.data.api.** { *; }

# The Room type converters use Gson via TypeToken reflection.
-keep class com.codeforces.app.data.db.Converters { *; }

# The JSON wrapper classes are generic; keep generic signatures so TypeToken
# deserialization still resolves element types.
-keep class com.codeforces.app.data.api.CfResponse { *; }

# ── Jsoup ──────────────────────────────────────────────────────────────────────
# Guards against future Jsoup versions dropping their consumer rules.
-keep class org.jsoup.** { *; }

# Jsoup references jspecify (nullable) annotations that aren't bundled.
-dontwarn org.jspecify.annotations.**
