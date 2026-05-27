package com.moneymate.app.di;

import com.moneymate.app.data.local.AppDatabase;
import com.moneymate.app.data.local.dao.EditRequestDao;
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
public final class AppModule_ProvideEditRequestDaoFactory implements Factory<EditRequestDao> {
  private final Provider<AppDatabase> dbProvider;

  public AppModule_ProvideEditRequestDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public EditRequestDao get() {
    return provideEditRequestDao(dbProvider.get());
  }

  public static AppModule_ProvideEditRequestDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new AppModule_ProvideEditRequestDaoFactory(dbProvider);
  }

  public static EditRequestDao provideEditRequestDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideEditRequestDao(db));
  }
}
