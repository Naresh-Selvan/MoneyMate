package com.moneymate.app.di;

import com.moneymate.app.data.local.AppDatabase;
import com.moneymate.app.data.local.dao.InvestmentDao;
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
public final class AppModule_ProvideInvestmentDaoFactory implements Factory<InvestmentDao> {
  private final Provider<AppDatabase> dbProvider;

  public AppModule_ProvideInvestmentDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public InvestmentDao get() {
    return provideInvestmentDao(dbProvider.get());
  }

  public static AppModule_ProvideInvestmentDaoFactory create(
      javax.inject.Provider<AppDatabase> dbProvider) {
    return new AppModule_ProvideInvestmentDaoFactory(Providers.asDaggerProvider(dbProvider));
  }

  public static AppModule_ProvideInvestmentDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new AppModule_ProvideInvestmentDaoFactory(dbProvider);
  }

  public static InvestmentDao provideInvestmentDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideInvestmentDao(db));
  }
}
