package com.moneymate.app.data.repository;

import com.moneymate.app.data.local.dao.DefaultPersonDao;
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
public final class DefaultPersonRepository_Factory implements Factory<DefaultPersonRepository> {
  private final Provider<DefaultPersonDao> daoProvider;

  public DefaultPersonRepository_Factory(Provider<DefaultPersonDao> daoProvider) {
    this.daoProvider = daoProvider;
  }

  @Override
  public DefaultPersonRepository get() {
    return newInstance(daoProvider.get());
  }

  public static DefaultPersonRepository_Factory create(Provider<DefaultPersonDao> daoProvider) {
    return new DefaultPersonRepository_Factory(daoProvider);
  }

  public static DefaultPersonRepository newInstance(DefaultPersonDao dao) {
    return new DefaultPersonRepository(dao);
  }
}
