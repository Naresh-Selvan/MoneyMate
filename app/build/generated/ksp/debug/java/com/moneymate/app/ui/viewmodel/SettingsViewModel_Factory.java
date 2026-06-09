package com.moneymate.app.ui.viewmodel;

import com.moneymate.app.notifications.WorkerScheduler;
import com.moneymate.app.utils.AppPreferences;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
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
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<AppPreferences> prefsProvider;

  private final Provider<WorkerScheduler> workerSchedulerProvider;

  public SettingsViewModel_Factory(Provider<AppPreferences> prefsProvider,
      Provider<WorkerScheduler> workerSchedulerProvider) {
    this.prefsProvider = prefsProvider;
    this.workerSchedulerProvider = workerSchedulerProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(prefsProvider.get(), workerSchedulerProvider.get());
  }

  public static SettingsViewModel_Factory create(
      javax.inject.Provider<AppPreferences> prefsProvider,
      javax.inject.Provider<WorkerScheduler> workerSchedulerProvider) {
    return new SettingsViewModel_Factory(Providers.asDaggerProvider(prefsProvider), Providers.asDaggerProvider(workerSchedulerProvider));
  }

  public static SettingsViewModel_Factory create(Provider<AppPreferences> prefsProvider,
      Provider<WorkerScheduler> workerSchedulerProvider) {
    return new SettingsViewModel_Factory(prefsProvider, workerSchedulerProvider);
  }

  public static SettingsViewModel newInstance(AppPreferences prefs,
      WorkerScheduler workerScheduler) {
    return new SettingsViewModel(prefs, workerScheduler);
  }
}
