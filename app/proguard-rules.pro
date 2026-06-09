# ── Room Entities ──────────────────────────────────────────────────────────
# Room uses reflection to instantiate entities, construct Cursor-based
# constructors, and access fields.  ProGuard/R8 must not strip any of these.

-keep class com.moneymate.app.data.local.entity.** { *; }

# Room TypeConverters (if any exist at the package level)
-keep class com.moneymate.app.data.local.converter.** { *; }

# Keep Room DAO interfaces – used by the generated _Impl classes.
-keep interface com.moneymate.app.data.local.dao.** { *; }

# ── Hilt / Dagger ─────────────────────────────────────────────────────────
# Hilt generates components at compile time; reflection is used to discover
# members.  Keep everything in the Hilt-generated packages.

-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# Keep all @HiltViewModel classes and their @Inject constructors.
-keep class com.moneymate.app.ui.viewmodel.** { *; }

# Keep Hilt/Dagger generated component classes.
-keep class com.moneymate.app.**_HiltComponents { *; }
-keep class com.moneymate.app.**_Factory { *; }
-keep class com.moneymate.app.**_ProvideFactory { *; }
-keep class com.moneymate.app.Hilt_* { *; }

# ── Firebase ──────────────────────────────────────────────────────────────
# Firebase SDKs use reflection for serialization (Firestore), callbacks, and
# messaging.  Keep the SDK internals.

-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Keep Firestore model data classes used with toObject() / DocumentSnapshot.
-keep class com.moneymate.app.data.local.entity.** { *; }       # already above

# ── Kotlin Coroutines ─────────────────────────────────────────────────────
# Kotlin coroutines rely on internal continuation classes.

-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# ── General AndroidX ──────────────────────────────────────────────────────
# Keep Compose / Navigation / Lifecycle runtime classes.

-keep class androidx.compose.** { *; }
-keep class androidx.navigation.** { *; }
-keep class androidx.lifecycle.** { *; }

# ── Serialization / Reflection ────────────────────────────────────────────
# Keep Kotlin data classes used as JSON / Firestore payloads.
-keep class com.moneymate.app.data.** { *; }

# ── Apache POI / SLF4J (Excel export — Phase 5) ───────────────────────────
# Suppress missing-class warnings from R8 for optional POI dependencies that
# aren't needed on Android (e.g. SLF4J bindings, XMLBeans internal classes).
-dontwarn org.slf4j.**
-dontwarn org.apache.poi.**
-dontwarn org.apache.xmlbeans.**
-dontwarn org.openxmlformats.**
-dontwarn com.microsoft.schemas.**
-dontwarn org.etsi.**
-dontwarn org.w3.**
-dontwarn javax.xml.**
-dontwarn org.apache.commons.compress.**
