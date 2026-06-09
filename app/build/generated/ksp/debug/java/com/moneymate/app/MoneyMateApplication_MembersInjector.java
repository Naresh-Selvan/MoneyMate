package com.moneymate.app;

import androidx.hilt.work.HiltWorkerFactory;
import com.moneymate.app.data.export.ExportManager;
import com.moneymate.app.notifications.NotificationChannelManager;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;

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
public final class MoneyMateApplication_MembersInjector implements MembersInjector<MoneyMateApplication> {
  private final Provider<ExportManager> exportManagerProvider;

  private final Provider<HiltWorkerFactory> workerFactoryProvider;

  private final Provider<NotificationChannelManager> channelManagerProvider;

  public MoneyMateApplication_MembersInjector(Provider<ExportManager> exportManagerProvider,
      Provider<HiltWorkerFactory> workerFactoryProvider,
      Provider<NotificationChannelManager> channelManagerProvider) {
    this.exportManagerProvider = exportManagerProvider;
    this.workerFactoryProvider = workerFactoryProvider;
    this.channelManagerProvider = channelManagerProvider;
  }

  public static MembersInjector<MoneyMateApplication> create(
      Provider<ExportManager> exportManagerProvider,
      Provider<HiltWorkerFactory> workerFactoryProvider,
      Provider<NotificationChannelManager> channelManagerProvider) {
    return new MoneyMateApplication_MembersInjector(exportManagerProvider, workerFactoryProvider, channelManagerProvider);
  }

  public static MembersInjector<MoneyMateApplication> create(
      javax.inject.Provider<ExportManager> exportManagerProvider,
      javax.inject.Provider<HiltWorkerFactory> workerFactoryProvider,
      javax.inject.Provider<NotificationChannelManager> channelManagerProvider) {
    return new MoneyMateApplication_MembersInjector(Providers.asDaggerProvider(exportManagerProvider), Providers.asDaggerProvider(workerFactoryProvider), Providers.asDaggerProvider(channelManagerProvider));
  }

  @Override
  public void injectMembers(MoneyMateApplication instance) {
    injectExportManager(instance, exportManagerProvider.get());
    injectWorkerFactory(instance, workerFactoryProvider.get());
    injectChannelManager(instance, channelManagerProvider.get());
  }

  @InjectedFieldSignature("com.moneymate.app.MoneyMateApplication.exportManager")
  public static void injectExportManager(MoneyMateApplication instance,
      ExportManager exportManager) {
    instance.exportManager = exportManager;
  }

  @InjectedFieldSignature("com.moneymate.app.MoneyMateApplication.workerFactory")
  public static void injectWorkerFactory(MoneyMateApplication instance,
      HiltWorkerFactory workerFactory) {
    instance.workerFactory = workerFactory;
  }

  @InjectedFieldSignature("com.moneymate.app.MoneyMateApplication.channelManager")
  public static void injectChannelManager(MoneyMateApplication instance,
      NotificationChannelManager channelManager) {
    instance.channelManager = channelManager;
  }
}
