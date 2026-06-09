package com.moneymate.app.di;

import com.moneymate.app.data.local.AppDatabase;
import com.moneymate.app.data.local.dao.AppUserDao;
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
public final class AppModule_ProvideAppUserDaoFactory implements Factory<AppUserDao> {
  private final Provider<AppDatabase> dbProvider;

  public AppModule_ProvideAppUserDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public AppUserDao get() {
    return provideAppUserDao(dbProvider.get());
  }

  public static AppModule_ProvideAppUserDaoFactory create(
      javax.inject.Provider<AppDatabase> dbProvider) {
    return new AppModule_ProvideAppUserDaoFactory(Providers.asDaggerProvider(dbProvider));
  }

  public static AppModule_ProvideAppUserDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new AppModule_ProvideAppUserDaoFactory(dbProvider);
  }

  public static AppUserDao provideAppUserDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideAppUserDao(db));
  }
}
