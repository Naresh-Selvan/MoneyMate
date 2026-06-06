package com.moneymate.app.data.repository;

import com.moneymate.app.data.local.dao.PersonDao;
import com.moneymate.app.utils.FirestorePathProvider;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class PersonRepository_Factory implements Factory<PersonRepository> {
  private final Provider<PersonDao> personDaoProvider;

  private final Provider<FirestorePathProvider> pathsProvider;

  public PersonRepository_Factory(Provider<PersonDao> personDaoProvider,
      Provider<FirestorePathProvider> pathsProvider) {
    this.personDaoProvider = personDaoProvider;
    this.pathsProvider = pathsProvider;
  }

  @Override
  public PersonRepository get() {
    return newInstance(personDaoProvider.get(), pathsProvider.get());
  }

  public static PersonRepository_Factory create(Provider<PersonDao> personDaoProvider,
      Provider<FirestorePathProvider> pathsProvider) {
    return new PersonRepository_Factory(personDaoProvider, pathsProvider);
  }

  public static PersonRepository newInstance(PersonDao personDao, FirestorePathProvider paths) {
    return new PersonRepository(personDao, paths);
  }
}
