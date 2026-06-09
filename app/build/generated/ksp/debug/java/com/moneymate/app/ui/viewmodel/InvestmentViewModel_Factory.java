package com.moneymate.app.ui.viewmodel;

import com.moneymate.app.data.repository.InvestmentRepository;
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
public final class InvestmentViewModel_Factory implements Factory<InvestmentViewModel> {
  private final Provider<InvestmentRepository> repositoryProvider;

  public InvestmentViewModel_Factory(Provider<InvestmentRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public InvestmentViewModel get() {
    return newInstance(repositoryProvider.get());
  }

  public static InvestmentViewModel_Factory create(
      javax.inject.Provider<InvestmentRepository> repositoryProvider) {
    return new InvestmentViewModel_Factory(Providers.asDaggerProvider(repositoryProvider));
  }

  public static InvestmentViewModel_Factory create(
      Provider<InvestmentRepository> repositoryProvider) {
    return new InvestmentViewModel_Factory(repositoryProvider);
  }

  public static InvestmentViewModel newInstance(InvestmentRepository repository) {
    return new InvestmentViewModel(repository);
  }
}
