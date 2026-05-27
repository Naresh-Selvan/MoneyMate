package com.moneymate.app.data.repository;

import com.moneymate.app.data.local.dao.PaymentDao;
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

  public PaymentRepository_Factory(Provider<PaymentDao> paymentDaoProvider) {
    this.paymentDaoProvider = paymentDaoProvider;
  }

  @Override
  public PaymentRepository get() {
    return newInstance(paymentDaoProvider.get());
  }

  public static PaymentRepository_Factory create(Provider<PaymentDao> paymentDaoProvider) {
    return new PaymentRepository_Factory(paymentDaoProvider);
  }

  public static PaymentRepository newInstance(PaymentDao paymentDao) {
    return new PaymentRepository(paymentDao);
  }
}
