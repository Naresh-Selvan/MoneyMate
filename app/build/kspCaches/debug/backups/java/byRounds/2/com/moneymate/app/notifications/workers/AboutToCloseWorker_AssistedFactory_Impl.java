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
public final class AboutToCloseWorker_AssistedFactory_Impl implements AboutToCloseWorker_AssistedFactory {
  private final AboutToCloseWorker_Factory delegateFactory;

  AboutToCloseWorker_AssistedFactory_Impl(AboutToCloseWorker_Factory delegateFactory) {
    this.delegateFactory = delegateFactory;
  }

  @Override
  public AboutToCloseWorker create(Context p0, WorkerParameters p1) {
    return delegateFactory.get(p0, p1);
  }

  public static Provider<AboutToCloseWorker_AssistedFactory> create(
      AboutToCloseWorker_Factory delegateFactory) {
    return InstanceFactory.create(new AboutToCloseWorker_AssistedFactory_Impl(delegateFactory));
  }

  public static dagger.internal.Provider<AboutToCloseWorker_AssistedFactory> createFactoryProvider(
      AboutToCloseWorker_Factory delegateFactory) {
    return InstanceFactory.create(new AboutToCloseWorker_AssistedFactory_Impl(delegateFactory));
  }
}
