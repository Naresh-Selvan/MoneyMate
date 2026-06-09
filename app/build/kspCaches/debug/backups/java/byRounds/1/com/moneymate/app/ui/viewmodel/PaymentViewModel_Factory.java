package com.moneymate.app.ui.viewmodel;

import android.content.Context;
import com.moneymate.app.auth.AuditLogger;
import com.moneymate.app.data.repository.PaymentRepository;
import com.moneymate.app.data.repository.PersonRepository;
import com.moneymate.app.notifications.NotificationHelper;
import com.moneymate.app.utils.AppPreferences;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class PaymentViewModel_Factory implements Factory<PaymentViewModel> {
  private final Provider<PaymentRepository> repositoryProvider;

  private final Provider<PersonRepository> personRepositoryProvider;

  private final Provider<NotificationHelper> notificationHelperProvider;

  private final Provider<AppPreferences> preferencesProvider;

  private final Provider<AuditLogger> auditLoggerProvider;

  private final Provider<Context> contextProvider;

  public PaymentViewModel_Factory(Provider<PaymentRepository> repositoryProvider,
      Provider<PersonRepository> personRepositoryProvider,
      Provider<NotificationHelper> notificationHelperProvider,
      Provider<AppPreferences> preferencesProvider, Provider<AuditLogger> auditLoggerProvider,
      Provider<Context> contextProvider) {
    this.repositoryProvider = repositoryProvider;
    this.personRepositoryProvider = personRepositoryProvider;
    this.notificationHelperProvider = notificationHelperProvider;
    this.preferencesProvider = preferencesProvider;
    this.auditLoggerProvider = auditLoggerProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public PaymentViewModel get() {
    return newInstance(repositoryProvider.get(), personRepositoryProvider.get(), notificationHelperProvider.get(), preferencesProvider.get(), auditLoggerProvider.get(), contextProvider.get());
  }

  public static PaymentViewModel_Factory create(
      javax.inject.Provider<PaymentRepository> repositoryProvider,
      javax.inject.Provider<PersonRepository> personRepositoryProvider,
      javax.inject.Provider<NotificationHelper> notificationHelperProvider,
      javax.inject.Provider<AppPreferences> preferencesProvider,
      javax.inject.Provider<AuditLogger> auditLoggerProvider,
      javax.inject.Provider<Context> contextProvider) {
    return new PaymentViewModel_Factory(Providers.asDaggerProvider(repositoryProvider), Providers.asDaggerProvider(personRepositoryProvider), Providers.asDaggerProvider(notificationHelperProvider), Providers.asDaggerProvider(preferencesProvider), Providers.asDaggerProvider(auditLoggerProvider), Providers.asDaggerProvider(contextProvider));
  }

  public static PaymentViewModel_Factory create(Provider<PaymentRepository> repositoryProvider,
      Provider<PersonRepository> personRepositoryProvider,
      Provider<NotificationHelper> notificationHelperProvider,
      Provider<AppPreferences> preferencesProvider, Provider<AuditLogger> auditLoggerProvider,
      Provider<Context> contextProvider) {
    return new PaymentViewModel_Factory(repositoryProvider, personRepositoryProvider, notificationHelperProvider, preferencesProvider, auditLoggerProvider, contextProvider);
  }

  public static PaymentViewModel newInstance(PaymentRepository repository,
      PersonRepository personRepository, NotificationHelper notificationHelper,
      AppPreferences preferences, AuditLogger auditLogger, Context context) {
    return new PaymentViewModel(repository, personRepository, notificationHelper, preferences, auditLogger, context);
  }
}
