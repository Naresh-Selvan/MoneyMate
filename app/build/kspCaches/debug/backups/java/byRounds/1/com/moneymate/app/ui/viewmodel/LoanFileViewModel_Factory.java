package com.moneymate.app.ui.viewmodel;

import com.moneymate.app.data.repository.LoanFileRepository;
import com.moneymate.app.data.repository.MaintenanceRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
    "cast"
})
public final class LoanFileViewModel_Factory implements Factory<LoanFileViewModel> {
  private final Provider<LoanFileRepository> repositoryProvider;

  private final Provider<MaintenanceRepository> maintenanceRepositoryProvider;

  public LoanFileViewModel_Factory(Provider<LoanFileRepository> repositoryProvider,
      Provider<MaintenanceRepository> maintenanceRepositoryProvider) {
    this.repositoryProvider = repositoryProvider;
    this.maintenanceRepositoryProvider = maintenanceRepositoryProvider;
  }

  @Override
  public LoanFileViewModel get() {
    return newInstance(repositoryProvider.get(), maintenanceRepositoryProvider.get());
  }

  public static LoanFileViewModel_Factory create(Provider<LoanFileRepository> repositoryProvider,
      Provider<MaintenanceRepository> maintenanceRepositoryProvider) {
    return new LoanFileViewModel_Factory(repositoryProvider, maintenanceRepositoryProvider);
  }

  public static LoanFileViewModel newInstance(LoanFileRepository repository,
      MaintenanceRepository maintenanceRepository) {
    return new LoanFileViewModel(repository, maintenanceRepository);
  }
}
