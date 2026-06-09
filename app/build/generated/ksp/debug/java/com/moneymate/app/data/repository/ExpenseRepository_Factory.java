package com.moneymate.app.data.repository;

import com.moneymate.app.data.local.dao.ExpenseCategoryDao;
import com.moneymate.app.data.local.dao.ExpenseDao;
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
public final class ExpenseRepository_Factory implements Factory<ExpenseRepository> {
  private final Provider<ExpenseDao> expenseDaoProvider;

  private final Provider<ExpenseCategoryDao> categoryDaoProvider;

  public ExpenseRepository_Factory(Provider<ExpenseDao> expenseDaoProvider,
      Provider<ExpenseCategoryDao> categoryDaoProvider) {
    this.expenseDaoProvider = expenseDaoProvider;
    this.categoryDaoProvider = categoryDaoProvider;
  }

  @Override
  public ExpenseRepository get() {
    return newInstance(expenseDaoProvider.get(), categoryDaoProvider.get());
  }

  public static ExpenseRepository_Factory create(
      javax.inject.Provider<ExpenseDao> expenseDaoProvider,
      javax.inject.Provider<ExpenseCategoryDao> categoryDaoProvider) {
    return new ExpenseRepository_Factory(Providers.asDaggerProvider(expenseDaoProvider), Providers.asDaggerProvider(categoryDaoProvider));
  }

  public static ExpenseRepository_Factory create(Provider<ExpenseDao> expenseDaoProvider,
      Provider<ExpenseCategoryDao> categoryDaoProvider) {
    return new ExpenseRepository_Factory(expenseDaoProvider, categoryDaoProvider);
  }

  public static ExpenseRepository newInstance(ExpenseDao expenseDao,
      ExpenseCategoryDao categoryDao) {
    return new ExpenseRepository(expenseDao, categoryDao);
  }
}
