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
public final class BadLoanAlertWorker_AssistedFactory_Impl implements BadLoanAlertWorker_AssistedFactory {
  private final BadLoanAlertWorker_Factory delegateFactory;

  BadLoanAlertWorker_AssistedFactory_Impl(BadLoanAlertWorker_Factory delegateFactory) {
    this.delegateFactory = delegateFactory;
  }

  @Override
  public BadLoanAlertWorker create(Context p0, WorkerParameters p1) {
    return delegateFactory.get(p0, p1);
  }

  public static Provider<BadLoanAlertWorker_AssistedFactory> create(
      BadLoanAlertWorker_Factory delegateFactory) {
    return InstanceFactory.create(new BadLoanAlertWorker_AssistedFactory_Impl(delegateFactory));
  }

  public static dagger.internal.Provider<BadLoanAlertWorker_AssistedFactory> createFactoryProvider(
      BadLoanAlertWorker_Factory delegateFactory) {
    return InstanceFactory.create(new BadLoanAlertWorker_AssistedFactory_Impl(delegateFactory));
  }
}
