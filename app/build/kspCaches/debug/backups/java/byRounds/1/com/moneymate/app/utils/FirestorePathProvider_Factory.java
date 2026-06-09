package com.moneymate.app.utils;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class FirestorePathProvider_Factory implements Factory<FirestorePathProvider> {
  private final Provider<AppPreferences> prefsProvider;

  public FirestorePathProvider_Factory(Provider<AppPreferences> prefsProvider) {
    this.prefsProvider = prefsProvider;
  }

  @Override
  public FirestorePathProvider get() {
    return newInstance(prefsProvider.get());
  }

  public static FirestorePathProvider_Factory create(
      javax.inject.Provider<AppPreferences> prefsProvider) {
    return new FirestorePathProvider_Factory(Providers.asDaggerProvider(prefsProvider));
  }

  public static FirestorePathProvider_Factory create(Provider<AppPreferences> prefsProvider) {
    return new FirestorePathProvider_Factory(prefsProvider);
  }

  public static FirestorePathProvider newInstance(AppPreferences prefs) {
    return new FirestorePathProvider(prefs);
  }
}
