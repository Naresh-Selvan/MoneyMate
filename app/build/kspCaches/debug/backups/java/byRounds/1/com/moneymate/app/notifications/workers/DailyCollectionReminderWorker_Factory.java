package com.moneymate.app.notifications.workers;

import android.content.Context;
import androidx.work.WorkerParameters;
import com.moneymate.app.data.local.dao.PaymentDao;
import com.moneymate.app.notifications.NotificationHelper;
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
public final class DailyCollectionReminderWorker_Factory {
  private final Provider<PaymentDao> paymentDaoProvider;

  private final Provider<NotificationHelper> notificationHelperProvider;

  public DailyCollectionReminderWorker_Factory(Provider<PaymentDao> paymentDaoProvider,
      Provider<NotificationHelper> notificationHelperProvider) {
    this.paymentDaoProvider = paymentDaoProvider;
    this.notificationHelperProvider = notificationHelperProvider;
  }

  public DailyCollectionReminderWorker get(Context context, WorkerParameters params) {
    return newInstance(context, params, paymentDaoProvider.get(), notificationHelperProvider.get());
  }

  public static DailyCollectionReminderWorker_Factory create(
      javax.inject.Provider<PaymentDao> paymentDaoProvider,
      javax.inject.Provider<NotificationHelper> notificationHelperProvider) {
    return new DailyCollectionReminderWorker_Factory(Providers.asDaggerProvider(paymentDaoProvider), Providers.asDaggerProvider(notificationHelperProvider));
  }

  public static DailyCollectionReminderWorker_Factory create(
      Provider<PaymentDao> paymentDaoProvider,
      Provider<NotificationHelper> notificationHelperProvider) {
    return new DailyCollectionReminderWorker_Factory(paymentDaoProvider, notificationHelperProvider);
  }

  public static DailyCollectionReminderWorker newInstance(Context context, WorkerParameters params,
      PaymentDao paymentDao, NotificationHelper notificationHelper) {
    return new DailyCollectionReminderWorker(context, params, paymentDao, notificationHelper);
  }
}
