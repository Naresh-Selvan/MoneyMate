package com.moneymate.app.notifications;

import android.content.Context;
import com.moneymate.app.utils.AppPreferences;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class WorkerScheduler_Factory implements Factory<WorkerScheduler> {
  private final Provider<Context> contextProvider;

  private final Provider<AppPreferences> appPreferencesProvider;

  public WorkerScheduler_Factory(Provider<Context> contextProvider,
      Provider<AppPreferences> appPreferencesProvider) {
    this.contextProvider = contextProvider;
    this.appPreferencesProvider = appPreferencesProvider;
  }

  @Override
  public WorkerScheduler get() {
    return newInstance(contextProvider.get(), appPreferencesProvider.get());
  }

  public static WorkerScheduler_Factory create(javax.inject.Provider<Context> contextProvider,
      javax.inject.Provider<AppPreferences> appPreferencesProvider) {
    return new WorkerScheduler_Factory(Providers.asDaggerProvider(contextProvider), Providers.asDaggerProvider(appPreferencesProvider));
  }

  public static WorkerScheduler_Factory create(Provider<Context> contextProvider,
      Provider<AppPreferences> appPreferencesProvider) {
    return new WorkerScheduler_Factory(contextProvider, appPreferencesProvider);
  }

  public static WorkerScheduler newInstance(Context context, AppPreferences appPreferences) {
    return new WorkerScheduler(context, appPreferences);
  }
}
