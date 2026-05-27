package com.moneymate.app.ui.viewmodel;

import com.moneymate.app.data.repository.DefaultPersonRepository;
import com.moneymate.app.data.repository.LoanFileRepository;
import com.moneymate.app.data.repository.PersonRepository;
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

  private final Provider<PersonRepository> personRepositoryProvider;

  private final Provider<DefaultPersonRepository> defaultPersonRepositoryProvider;

  public LoanFileViewModel_Factory(Provider<LoanFileRepository> repositoryProvider,
      Provider<PersonRepository> personRepositoryProvider,
      Provider<DefaultPersonRepository> defaultPersonRepositoryProvider) {
    this.repositoryProvider = repositoryProvider;
    this.personRepositoryProvider = personRepositoryProvider;
    this.defaultPersonRepositoryProvider = defaultPersonRepositoryProvider;
  }

  @Override
  public LoanFileViewModel get() {
    return newInstance(repositoryProvider.get(), personRepositoryProvider.get(), defaultPersonRepositoryProvider.get());
  }

  public static LoanFileViewModel_Factory create(Provider<LoanFileRepository> repositoryProvider,
      Provider<PersonRepository> personRepositoryProvider,
      Provider<DefaultPersonRepository> defaultPersonRepositoryProvider) {
    return new LoanFileViewModel_Factory(repositoryProvider, personRepositoryProvider, defaultPersonRepositoryProvider);
  }

  public static LoanFileViewModel newInstance(LoanFileRepository repository,
      PersonRepository personRepository, DefaultPersonRepository defaultPersonRepository) {
    return new LoanFileViewModel(repository, personRepository, defaultPersonRepository);
  }
}
