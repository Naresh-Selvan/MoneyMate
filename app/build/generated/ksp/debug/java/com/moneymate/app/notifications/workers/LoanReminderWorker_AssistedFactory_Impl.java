package com.moneymate.app.notifications.workers;

import android.content.Context;
import androidx.work.WorkerParameters;
import dagger.internal.DaggerGenerated;
import dagger.internal.InstanceFactory;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class LoanReminderWorker_AssistedFactory_Impl implements LoanReminderWorker_AssistedFactory {
  private final LoanReminderWorker_Factory delegateFactory;

  LoanReminderWorker_AssistedFactory_Impl(LoanReminderWorker_Factory delegateFactory) {
    this.delegateFactory = delegateFactory;
  }

  @Override
  public LoanReminderWorker create(Context p0, WorkerParameters p1) {
    return delegateFactory.get(p0, p1);
  }

  public static Provider<LoanReminderWorker_AssistedFactory> create(
      LoanReminderWorker_Factory delegateFactory) {
    return InstanceFactory.create(new LoanReminderWorker_AssistedFactory_Impl(delegateFactory));
  }

  public static dagger.internal.Provider<LoanReminderWorker_AssistedFactory> createFactoryProvider(
      LoanReminderWorker_Factory delegateFactory) {
    return InstanceFactory.create(new LoanReminderWorker_AssistedFactory_Impl(delegateFactory));
  }
}
