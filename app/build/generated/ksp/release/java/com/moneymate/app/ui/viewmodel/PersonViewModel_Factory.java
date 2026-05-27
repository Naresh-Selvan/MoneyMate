package com.moneymate.app.ui.viewmodel;

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
public final class PersonViewModel_Factory implements Factory<PersonViewModel> {
  private final Provider<PersonRepository> repositoryProvider;

  public PersonViewModel_Factory(Provider<PersonRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public PersonViewModel get() {
    return newInstance(repositoryProvider.get());
  }

  public static PersonViewModel_Factory create(Provider<PersonRepository> repositoryProvider) {
    return new PersonViewModel_Factory(repositoryProvider);
  }

  public static PersonViewModel newInstance(PersonRepository repository) {
    return new PersonViewModel(repository);
  }
}
