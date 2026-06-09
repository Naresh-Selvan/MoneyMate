package com.moneymate.app.data.repository;

import com.moneymate.app.data.local.dao.PersonDao;
import com.moneymate.app.utils.FirestorePathProvider;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
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

  public static PersonRepository_Factory create(javax.inject.Provider<PersonDao> personDaoProvider,
      javax.inject.Provider<FirestorePathProvider> pathsProvider) {
    return new PersonRepository_Factory(Providers.asDaggerProvider(personDaoProvider), Providers.asDaggerProvider(pathsProvider));
  }

  public static PersonRepository_Factory create(Provider<PersonDao> personDaoProvider,
      Provider<FirestorePathProvider> pathsProvider) {
    return new PersonRepository_Factory(personDaoProvider, pathsProvider);
  }

  public static PersonRepository newInstance(PersonDao personDao, FirestorePathProvider paths) {
    return new PersonRepository(personDao, paths);
  }
}
