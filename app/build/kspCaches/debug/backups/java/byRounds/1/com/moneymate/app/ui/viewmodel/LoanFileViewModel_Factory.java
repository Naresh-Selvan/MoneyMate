package com.moneymate.app.ui.viewmodel;

import com.moneymate.app.auth.AuditLogger;
import com.moneymate.app.data.repository.LoanFileRepository;
import com.moneymate.app.data.repository.MaintenanceRepository;
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
public final class LoanFileViewModel_Factory implements Factory<LoanFileViewModel> {
  private final Provider<LoanFileRepository> repositoryProvider;

  private final Provider<MaintenanceRepository> maintenanceRepositoryProvider;

  private final Provider<AuditLogger> auditLoggerProvider;

  public LoanFileViewModel_Factory(Provider<LoanFileRepository> repositoryProvider,
      Provider<MaintenanceRepository> maintenanceRepositoryProvider,
      Provider<AuditLogger> auditLoggerProvider) {
    this.repositoryProvider = repositoryProvider;
    this.maintenanceRepositoryProvider = maintenanceRepositoryProvider;
    this.auditLoggerProvider = auditLoggerProvider;
  }

  @Override
  public LoanFileViewModel get() {
    return newInstance(repositoryProvider.get(), maintenanceRepositoryProvider.get(), auditLoggerProvider.get());
  }

  public static LoanFileViewModel_Factory create(
      javax.inject.Provider<LoanFileRepository> repositoryProvider,
      javax.inject.Provider<MaintenanceRepository> maintenanceRepositoryProvider,
      javax.inject.Provider<AuditLogger> auditLoggerProvider) {
    return new LoanFileViewModel_Factory(Providers.asDaggerProvider(repositoryProvider), Providers.asDaggerProvider(maintenanceRepositoryProvider), Providers.asDaggerProvider(auditLoggerProvider));
  }

  public static LoanFileViewModel_Factory create(Provider<LoanFileRepository> repositoryProvider,
      Provider<MaintenanceRepository> maintenanceRepositoryProvider,
      Provider<AuditLogger> auditLoggerProvider) {
    return new LoanFileViewModel_Factory(repositoryProvider, maintenanceRepositoryProvider, auditLoggerProvider);
  }

  public static LoanFileViewModel newInstance(LoanFileRepository repository,
      MaintenanceRepository maintenanceRepository, AuditLogger auditLogger) {
    return new LoanFileViewModel(repository, maintenanceRepository, auditLogger);
  }
}
