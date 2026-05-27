package com.moneymate.app.di;

import com.moneymate.app.data.local.AppDatabase;
import com.moneymate.app.data.local.dao.DefaultPersonDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class AppModule_ProvideDefaultPersonDaoFactory implements Factory<DefaultPersonDao> {
  private final Provider<AppDatabase> dbProvider;

  public AppModule_ProvideDefaultPersonDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public DefaultPersonDao get() {
    return provideDefaultPersonDao(dbProvider.get());
  }

  public static AppModule_ProvideDefaultPersonDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new AppModule_ProvideDefaultPersonDaoFactory(dbProvider);
  }

  public static DefaultPersonDao provideDefaultPersonDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideDefaultPersonDao(db));
  }
}
