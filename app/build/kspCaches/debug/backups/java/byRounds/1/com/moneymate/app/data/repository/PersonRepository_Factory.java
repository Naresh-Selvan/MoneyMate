package com.moneymate.app.data.repository;

import com.moneymate.app.data.local.dao.PersonDao;
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

  public PersonRepository_Factory(Provider<PersonDao> personDaoProvider) {
    this.personDaoProvider = personDaoProvider;
  }

  @Override
  public PersonRepository get() {
    return newInstance(personDaoProvider.get());
  }

  public static PersonRepository_Factory create(Provider<PersonDao> personDaoProvider) {
    return new PersonRepository_Factory(personDaoProvider);
  }

  public static PersonRepository newInstance(PersonDao personDao) {
    return new PersonRepository(personDao);
  }
}
