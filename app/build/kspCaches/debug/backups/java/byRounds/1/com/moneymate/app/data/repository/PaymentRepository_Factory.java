package com.moneymate.app.data.repository;

import com.moneymate.app.data.local.dao.PaymentDao;
import com.moneymate.app.data.local.dao.PersonDao;
import com.moneymate.app.utils.FirestorePathProvider;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
    "cast"
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

  public static PaymentRepository_Factory create(Provider<PaymentDao> paymentDaoProvider,
      Provider<PersonDao> personDaoProvider, Provider<FirestorePathProvider> pathsProvider) {
    return new PaymentRepository_Factory(paymentDaoProvider, personDaoProvider, pathsProvider);
  }

  public static PaymentRepository newInstance(PaymentDao paymentDao, PersonDao personDao,
      FirestorePathProvider paths) {
    return new PaymentRepository(paymentDao, personDao, paths);
  }
}
