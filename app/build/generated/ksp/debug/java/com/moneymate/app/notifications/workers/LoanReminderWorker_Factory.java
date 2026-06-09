package com.moneymate.app.notifications.workers;

import android.content.Context;
import androidx.work.WorkerParameters;
import com.moneymate.app.notifications.NotificationHelper;
import com.moneymate.app.utils.AppPreferences;
import dagger.internal.DaggerGenerated;
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
public final class LoanReminderWorker_Factory {
  private final Provider<NotificationHelper> notificationHelperProvider;

  private final Provider<AppPreferences> appPreferencesProvider;

  public LoanReminderWorker_Factory(Provider<NotificationHelper> notificationHelperProvider,
      Provider<AppPreferences> appPreferencesProvider) {
    this.notificationHelperProvider = notificationHelperProvider;
    this.appPreferencesProvider = appPreferencesProvider;
  }

  public LoanReminderWorker get(Context context, WorkerParameters params) {
    return newInstance(context, params, notificationHelperProvider.get(), appPreferencesProvider.get());
  }

  public static LoanReminderWorker_Factory create(
      javax.inject.Provider<NotificationHelper> notificationHelperProvider,
      javax.inject.Provider<AppPreferences> appPreferencesProvider) {
    return new LoanReminderWorker_Factory(Providers.asDaggerProvider(notificationHelperProvider), Providers.asDaggerProvider(appPreferencesProvider));
  }

  public static LoanReminderWorker_Factory create(
      Provider<NotificationHelper> notificationHelperProvider,
      Provider<AppPreferences> appPreferencesProvider) {
    return new LoanReminderWorker_Factory(notificationHelperProvider, appPreferencesProvider);
  }

  public static LoanReminderWorker newInstance(Context context, WorkerParameters params,
      NotificationHelper notificationHelper, AppPreferences appPreferences) {
    return new LoanReminderWorker(context, params, notificationHelper, appPreferences);
  }
}
