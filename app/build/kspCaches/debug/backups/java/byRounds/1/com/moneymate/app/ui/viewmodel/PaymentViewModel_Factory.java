package com.moneymate.app.ui.viewmodel;

import com.moneymate.app.data.repository.PaymentRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class PaymentViewModel_Factory implements Factory<PaymentViewModel> {
  private final Provider<PaymentRepository> repositoryProvider;

  public PaymentViewModel_Factory(Provider<PaymentRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public PaymentViewModel get() {
    return newInstance(repositoryProvider.get());
  }

  public static PaymentViewModel_Factory create(Provider<PaymentRepository> repositoryProvider) {
    return new PaymentViewModel_Factory(repositoryProvider);
  }

  public static PaymentViewModel newInstance(PaymentRepository repository) {
    return new PaymentViewModel(repository);
  }
}
