package com.moneymate.app.di;

import com.moneymate.app.data.local.AppDatabase;
import com.moneymate.app.data.local.dao.PaymentDao;
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
public final class AppModule_ProvidePaymentDaoFactory implements Factory<PaymentDao> {
  private final Provider<AppDatabase> dbProvider;

  public AppModule_ProvidePaymentDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public PaymentDao get() {
    return providePaymentDao(dbProvider.get());
  }

  public static AppModule_ProvidePaymentDaoFactory create(
      javax.inject.Provider<AppDatabase> dbProvider) {
    return new AppModule_ProvidePaymentDaoFactory(Providers.asDaggerProvider(dbProvider));
  }

  public static AppModule_ProvidePaymentDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new AppModule_ProvidePaymentDaoFactory(dbProvider);
  }

  public static PaymentDao providePaymentDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.providePaymentDao(db));
  }
}
