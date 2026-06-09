package com.moneymate.app.di;

import com.moneymate.app.data.local.AppDatabase;
import com.moneymate.app.data.local.dao.ExpenseCategoryDao;
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
public final class AppModule_ProvideExpenseCategoryDaoFactory implements Factory<ExpenseCategoryDao> {
  private final Provider<AppDatabase> dbProvider;

  public AppModule_ProvideExpenseCategoryDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public ExpenseCategoryDao get() {
    return provideExpenseCategoryDao(dbProvider.get());
  }

  public static AppModule_ProvideExpenseCategoryDaoFactory create(
      javax.inject.Provider<AppDatabase> dbProvider) {
    return new AppModule_ProvideExpenseCategoryDaoFactory(Providers.asDaggerProvider(dbProvider));
  }

  public static AppModule_ProvideExpenseCategoryDaoFactory create(
      Provider<AppDatabase> dbProvider) {
    return new AppModule_ProvideExpenseCategoryDaoFactory(dbProvider);
  }

  public static ExpenseCategoryDao provideExpenseCategoryDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideExpenseCategoryDao(db));
  }
}
