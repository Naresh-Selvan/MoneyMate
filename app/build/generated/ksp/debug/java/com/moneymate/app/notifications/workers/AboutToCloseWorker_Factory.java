package com.moneymate.app.notifications.workers;

import android.content.Context;
import androidx.work.WorkerParameters;
import com.moneymate.app.data.local.dao.PersonDao;
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
public final class AboutToCloseWorker_Factory {
  private final Provider<PersonDao> personDaoProvider;

  private final Provider<NotificationHelper> notificationHelperProvider;

  private final Provider<AppPreferences> preferencesProvider;

  public AboutToCloseWorker_Factory(Provider<PersonDao> personDaoProvider,
      Provider<NotificationHelper> notificationHelperProvider,
      Provider<AppPreferences> preferencesProvider) {
    this.personDaoProvider = personDaoProvider;
    this.notificationHelperProvider = notificationHelperProvider;
    this.preferencesProvider = preferencesProvider;
  }

  public AboutToCloseWorker get(Context context, WorkerParameters params) {
    return newInstance(context, params, personDaoProvider.get(), notificationHelperProvider.get(), preferencesProvider.get());
  }

  public static AboutToCloseWorker_Factory create(
      javax.inject.Provider<PersonDao> personDaoProvider,
      javax.inject.Provider<NotificationHelper> notificationHelperProvider,
      javax.inject.Provider<AppPreferences> preferencesProvider) {
    return new AboutToCloseWorker_Factory(Providers.asDaggerProvider(personDaoProvider), Providers.asDaggerProvider(notificationHelperProvider), Providers.asDaggerProvider(preferencesProvider));
  }

  public static AboutToCloseWorker_Factory create(Provider<PersonDao> personDaoProvider,
      Provider<NotificationHelper> notificationHelperProvider,
      Provider<AppPreferences> preferencesProvider) {
    return new AboutToCloseWorker_Factory(personDaoProvider, notificationHelperProvider, preferencesProvider);
  }

  public static AboutToCloseWorker newInstance(Context context, WorkerParameters params,
      PersonDao personDao, NotificationHelper notificationHelper, AppPreferences preferences) {
    return new AboutToCloseWorker(context, params, personDao, notificationHelper, preferences);
  }
}
