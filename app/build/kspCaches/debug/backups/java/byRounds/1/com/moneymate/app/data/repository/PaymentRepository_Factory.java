package com.moneymate.app.data.repository;

import com.moneymate.app.data.local.dao.PaymentDao;
import com.moneymate.app.data.local.dao.PersonDao;
import com.moneymate.app.utils.FirestorePathProvider;
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
public final class PaymentRepository_Factory implements Factory<PaymentRepository> {
  private final Provider<PaymentDao> paymentDaoProvider;

  private final Provider<PersonDao> personDaoProvider;

  private final Provider<FirestorePathProvider> pathsProvider;

  public PaymentRepository_Factory(Provider<PaymentDao> paymentDaoProvider,
      Provider<PersonDao> personDaoProvider, Provider<FirestorePathProvider> pathsProvider) {
    this.paymentDaoProvider = paymentDaoProvider;
    this.personDaoProvider = personDaoProvider;
    this.pathsProvider = pathsProvider;
  }

  @Override
  public PaymentRepository get() {
    return newInstance(paymentDaoProvider.get(), personDaoProvider.get(), pathsProvider.get());
  }

  public static PaymentRepository_Factory create(
      javax.inject.Provider<PaymentDao> paymentDaoProvider,
      javax.inject.Provider<PersonDao> personDaoProvider,
      javax.inject.Provider<FirestorePathProvider> pathsProvider) {
    return new PaymentRepository_Factory(Providers.asDaggerProvider(paymentDaoProvider), Providers.asDaggerProvider(personDaoProvider), Providers.asDaggerProvider(pathsProvider));
  }

  public static PaymentRepository_Factory create(Provider<PaymentDao> paymentDaoProvider,
      Provider<PersonDao> personDaoProvider, Provider<FirestorePathProvider> pathsProvider) {
    return new PaymentRepository_Factory(paymentDaoProvider, personDaoProvider, pathsProvider);
  }

  public static PaymentRepository newInstance(PaymentDao paymentDao, PersonDao personDao,
      FirestorePathProvider paths) {
    return new PaymentRepository(paymentDao, personDao, paths);
  }
}
