package com.moneymate.app.ui.viewmodel;

import com.moneymate.app.data.repository.LoanFileRepository;
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

  public LoanFileViewModel_Factory(Provider<LoanFileRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public LoanFileViewModel get() {
    return newInstance(repositoryProvider.get());
  }

  public static LoanFileViewModel_Factory create(Provider<LoanFileRepository> repositoryProvider) {
    return new LoanFileViewModel_Factory(repositoryProvider);
  }

  public static LoanFileViewModel newInstance(LoanFileRepository repository) {
    return new LoanFileViewModel(repository);
  }
}
