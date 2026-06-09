package com.moneymate.app.notifications.workers;

import android.content.Context;
import androidx.work.WorkerParameters;
import com.moneymate.app.data.local.dao.PaymentDao;
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
public final class BadLoanAlertWorker_Factory {
  private final Provider<PaymentDao> paymentDaoProvider;

  private final Provider<NotificationHelper> notificationHelperProvider;

  private final Provider<AppPreferences> preferencesProvider;

  public BadLoanAlertWorker_Factory(Provider<PaymentDao> paymentDaoProvider,
      Provider<NotificationHelper> notificationHelperProvider,
      Provider<AppPreferences> preferencesProvider) {
    this.paymentDaoProvider = paymentDaoProvider;
    this.notificationHelperProvider = notificationHelperProvider;
    this.preferencesProvider = preferencesProvider;
  }

  public BadLoanAlertWorker get(Context context, WorkerParameters params) {
    return newInstance(context, params, paymentDaoProvider.get(), notificationHelperProvider.get(), preferencesProvider.get());
  }

  public static BadLoanAlertWorker_Factory create(
      javax.inject.Provider<PaymentDao> paymentDaoProvider,
      javax.inject.Provider<NotificationHelper> notificationHelperProvider,
      javax.inject.Provider<AppPreferences> preferencesProvider) {
    return new BadLoanAlertWorker_Factory(Providers.asDaggerProvider(paymentDaoProvider), Providers.asDaggerProvider(notificationHelperProvider), Providers.asDaggerProvider(preferencesProvider));
  }

  public static BadLoanAlertWorker_Factory create(Provider<PaymentDao> paymentDaoProvider,
      Provider<NotificationHelper> notificationHelperProvider,
      Provider<AppPreferences> preferencesProvider) {
    return new BadLoanAlertWorker_Factory(paymentDaoProvider, notificationHelperProvider, preferencesProvider);
  }

  public static BadLoanAlertWorker newInstance(Context context, WorkerParameters params,
      PaymentDao paymentDao, NotificationHelper notificationHelper, AppPreferences preferences) {
    return new BadLoanAlertWorker(context, params, paymentDao, notificationHelper, preferences);
  }
}
