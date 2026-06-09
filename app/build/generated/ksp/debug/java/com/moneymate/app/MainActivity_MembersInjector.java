package com.moneymate.app;

import com.moneymate.app.auth.AuditLogger;
import com.moneymate.app.notifications.WorkerScheduler;
import com.moneymate.app.utils.AppPreferences;
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
public final class MainActivity_MembersInjector implements MembersInjector<MainActivity> {
  private final Provider<AppPreferences> appPreferencesProvider;

  private final Provider<WorkerScheduler> workerSchedulerProvider;

  private final Provider<AuditLogger> auditLoggerProvider;

  public MainActivity_MembersInjector(Provider<AppPreferences> appPreferencesProvider,
      Provider<WorkerScheduler> workerSchedulerProvider,
      Provider<AuditLogger> auditLoggerProvider) {
    this.appPreferencesProvider = appPreferencesProvider;
    this.workerSchedulerProvider = workerSchedulerProvider;
    this.auditLoggerProvider = auditLoggerProvider;
  }

  public static MembersInjector<MainActivity> create(
      Provider<AppPreferences> appPreferencesProvider,
      Provider<WorkerScheduler> workerSchedulerProvider,
      Provider<AuditLogger> auditLoggerProvider) {
    return new MainActivity_MembersInjector(appPreferencesProvider, workerSchedulerProvider, auditLoggerProvider);
  }

  public static MembersInjector<MainActivity> create(
      javax.inject.Provider<AppPreferences> appPreferencesProvider,
      javax.inject.Provider<WorkerScheduler> workerSchedulerProvider,
      javax.inject.Provider<AuditLogger> auditLoggerProvider) {
    return new MainActivity_MembersInjector(Providers.asDaggerProvider(appPreferencesProvider), Providers.asDaggerProvider(workerSchedulerProvider), Providers.asDaggerProvider(auditLoggerProvider));
  }

  @Override
  public void injectMembers(MainActivity instance) {
    injectAppPreferences(instance, appPreferencesProvider.get());
    injectWorkerScheduler(instance, workerSchedulerProvider.get());
    injectAuditLogger(instance, auditLoggerProvider.get());
  }

  @InjectedFieldSignature("com.moneymate.app.MainActivity.appPreferences")
  public static void injectAppPreferences(MainActivity instance, AppPreferences appPreferences) {
    instance.appPreferences = appPreferences;
  }

  @InjectedFieldSignature("com.moneymate.app.MainActivity.workerScheduler")
  public static void injectWorkerScheduler(MainActivity instance, WorkerScheduler workerScheduler) {
    instance.workerScheduler = workerScheduler;
  }

  @InjectedFieldSignature("com.moneymate.app.MainActivity.auditLogger")
  public static void injectAuditLogger(MainActivity instance, AuditLogger auditLogger) {
    instance.auditLogger = auditLogger;
  }
}
