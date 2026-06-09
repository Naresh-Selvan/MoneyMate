package com.moneymate.app.di;

import com.moneymate.app.utils.AppPreferences;
import com.moneymate.app.utils.FirestorePathProvider;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class AppModule_ProvideFirestorePathProviderFactory implements Factory<FirestorePathProvider> {
  private final Provider<AppPreferences> prefsProvider;

  public AppModule_ProvideFirestorePathProviderFactory(Provider<AppPreferences> prefsProvider) {
    this.prefsProvider = prefsProvider;
  }

  @Override
  public FirestorePathProvider get() {
    return provideFirestorePathProvider(prefsProvider.get());
  }

  public static AppModule_ProvideFirestorePathProviderFactory create(
      javax.inject.Provider<AppPreferences> prefsProvider) {
    return new AppModule_ProvideFirestorePathProviderFactory(Providers.asDaggerProvider(prefsProvider));
  }

  public static AppModule_ProvideFirestorePathProviderFactory create(
      Provider<AppPreferences> prefsProvider) {
    return new AppModule_ProvideFirestorePathProviderFactory(prefsProvider);
  }

  public static FirestorePathProvider provideFirestorePathProvider(AppPreferences prefs) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideFirestorePathProvider(prefs));
  }
}
