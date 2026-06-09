package com.moneymate.app.data.repository;

import com.moneymate.app.data.local.dao.AreaDao;
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
public final class AreaRepository_Factory implements Factory<AreaRepository> {
  private final Provider<AreaDao> areaDaoProvider;

  public AreaRepository_Factory(Provider<AreaDao> areaDaoProvider) {
    this.areaDaoProvider = areaDaoProvider;
  }

  @Override
  public AreaRepository get() {
    return newInstance(areaDaoProvider.get());
  }

  public static AreaRepository_Factory create(javax.inject.Provider<AreaDao> areaDaoProvider) {
    return new AreaRepository_Factory(Providers.asDaggerProvider(areaDaoProvider));
  }

  public static AreaRepository_Factory create(Provider<AreaDao> areaDaoProvider) {
    return new AreaRepository_Factory(areaDaoProvider);
  }

  public static AreaRepository newInstance(AreaDao areaDao) {
    return new AreaRepository(areaDao);
  }
}
