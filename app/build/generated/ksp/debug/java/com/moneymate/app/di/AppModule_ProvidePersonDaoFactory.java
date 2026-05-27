package com.moneymate.app.di;

import com.moneymate.app.data.local.AppDatabase;
import com.moneymate.app.data.local.dao.PersonDao;
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
public final class AppModule_ProvidePersonDaoFactory implements Factory<PersonDao> {
  private final Provider<AppDatabase> dbProvider;

  public AppModule_ProvidePersonDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public PersonDao get() {
    return providePersonDao(dbProvider.get());
  }

  public static AppModule_ProvidePersonDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new AppModule_ProvidePersonDaoFactory(dbProvider);
  }

  public static PersonDao providePersonDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.providePersonDao(db));
  }
}
