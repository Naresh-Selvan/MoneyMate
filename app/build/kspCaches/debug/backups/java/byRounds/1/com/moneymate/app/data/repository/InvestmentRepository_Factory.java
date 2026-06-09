package com.moneymate.app.data.repository;

import com.moneymate.app.data.local.dao.InvestmentDao;
import com.moneymate.app.data.local.dao.InvestmentTypeDao;
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
public final class InvestmentRepository_Factory implements Factory<InvestmentRepository> {
  private final Provider<InvestmentDao> investmentDaoProvider;

  private final Provider<InvestmentTypeDao> typeDaoProvider;

  public InvestmentRepository_Factory(Provider<InvestmentDao> investmentDaoProvider,
      Provider<InvestmentTypeDao> typeDaoProvider) {
    this.investmentDaoProvider = investmentDaoProvider;
    this.typeDaoProvider = typeDaoProvider;
  }

  @Override
  public InvestmentRepository get() {
    return newInstance(investmentDaoProvider.get(), typeDaoProvider.get());
  }

  public static InvestmentRepository_Factory create(
      javax.inject.Provider<InvestmentDao> investmentDaoProvider,
      javax.inject.Provider<InvestmentTypeDao> typeDaoProvider) {
    return new InvestmentRepository_Factory(Providers.asDaggerProvider(investmentDaoProvider), Providers.asDaggerProvider(typeDaoProvider));
  }

  public static InvestmentRepository_Factory create(Provider<InvestmentDao> investmentDaoProvider,
      Provider<InvestmentTypeDao> typeDaoProvider) {
    return new InvestmentRepository_Factory(investmentDaoProvider, typeDaoProvider);
  }

  public static InvestmentRepository newInstance(InvestmentDao investmentDao,
      InvestmentTypeDao typeDao) {
    return new InvestmentRepository(investmentDao, typeDao);
  }
}
