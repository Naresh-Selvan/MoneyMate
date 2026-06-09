package com.moneymate.app.di;

import com.moneymate.app.data.local.AppDatabase;
import com.moneymate.app.data.local.dao.InvestmentTypeDao;
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
public final class AppModule_ProvideInvestmentTypeDaoFactory implements Factory<InvestmentTypeDao> {
  private final Provider<AppDatabase> dbProvider;

  public AppModule_ProvideInvestmentTypeDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public InvestmentTypeDao get() {
    return provideInvestmentTypeDao(dbProvider.get());
  }

  public static AppModule_ProvideInvestmentTypeDaoFactory create(
      javax.inject.Provider<AppDatabase> dbProvider) {
    return new AppModule_ProvideInvestmentTypeDaoFactory(Providers.asDaggerProvider(dbProvider));
  }

  public static AppModule_ProvideInvestmentTypeDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new AppModule_ProvideInvestmentTypeDaoFactory(dbProvider);
  }

  public static InvestmentTypeDao provideInvestmentTypeDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideInvestmentTypeDao(db));
  }
}
