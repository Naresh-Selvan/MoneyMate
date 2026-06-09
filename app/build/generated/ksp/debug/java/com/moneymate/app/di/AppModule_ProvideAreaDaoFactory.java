package com.moneymate.app.di;

import com.moneymate.app.data.local.AppDatabase;
import com.moneymate.app.data.local.dao.AreaDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class AppModule_ProvideAreaDaoFactory implements Factory<AreaDao> {
  private final Provider<AppDatabase> dbProvider;

  public AppModule_ProvideAreaDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public AreaDao get() {
    return provideAreaDao(dbProvider.get());
  }

  public static AppModule_ProvideAreaDaoFactory create(
      javax.inject.Provider<AppDatabase> dbProvider) {
    return new AppModule_ProvideAreaDaoFactory(Providers.asDaggerProvider(dbProvider));
  }

  public static AppModule_ProvideAreaDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new AppModule_ProvideAreaDaoFactory(dbProvider);
  }

  public static AreaDao provideAreaDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideAreaDao(db));
  }
}
