package com.moneymate.app.ui.viewmodel;

import com.moneymate.app.auth.AuditLogger;
import com.moneymate.app.auth.SessionManager;
import com.moneymate.app.data.repository.PersonRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class PersonViewModel_Factory implements Factory<PersonViewModel> {
  private final Provider<PersonRepository> repositoryProvider;

  private final Provider<AuditLogger> auditLoggerProvider;

  private final Provider<SessionManager> sessionManagerProvider;

  public PersonViewModel_Factory(Provider<PersonRepository> repositoryProvider,
      Provider<AuditLogger> auditLoggerProvider, Provider<SessionManager> sessionManagerProvider) {
    this.repositoryProvider = repositoryProvider;
    this.auditLoggerProvider = auditLoggerProvider;
    this.sessionManagerProvider = sessionManagerProvider;
  }

  @Override
  public PersonViewModel get() {
    return newInstance(repositoryProvider.get(), auditLoggerProvider.get(), sessionManagerProvider.get());
  }

  public static PersonViewModel_Factory create(
      javax.inject.Provider<PersonRepository> repositoryProvider,
      javax.inject.Provider<AuditLogger> auditLoggerProvider,
      javax.inject.Provider<SessionManager> sessionManagerProvider) {
    return new PersonViewModel_Factory(Providers.asDaggerProvider(repositoryProvider), Providers.asDaggerProvider(auditLoggerProvider), Providers.asDaggerProvider(sessionManagerProvider));
  }

  public static PersonViewModel_Factory create(Provider<PersonRepository> repositoryProvider,
      Provider<AuditLogger> auditLoggerProvider, Provider<SessionManager> sessionManagerProvider) {
    return new PersonViewModel_Factory(repositoryProvider, auditLoggerProvider, sessionManagerProvider);
  }

  public static PersonViewModel newInstance(PersonRepository repository, AuditLogger auditLogger,
      SessionManager sessionManager) {
    return new PersonViewModel(repository, auditLogger, sessionManager);
  }
}
