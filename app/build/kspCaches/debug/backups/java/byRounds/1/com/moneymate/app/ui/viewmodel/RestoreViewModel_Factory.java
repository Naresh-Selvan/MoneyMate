package com.moneymate.app.ui.viewmodel;

import com.moneymate.app.data.repository.LoanFileRepository;
import com.moneymate.app.data.repository.PaymentRepository;
import com.moneymate.app.data.repository.PersonRepository;
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
public final class RestoreViewModel_Factory implements Factory<RestoreViewModel> {
  private final Provider<LoanFileRepository> loanFileRepositoryProvider;

  private final Provider<PersonRepository> personRepositoryProvider;

  private final Provider<PaymentRepository> paymentRepositoryProvider;

  public RestoreViewModel_Factory(Provider<LoanFileRepository> loanFileRepositoryProvider,
      Provider<PersonRepository> personRepositoryProvider,
      Provider<PaymentRepository> paymentRepositoryProvider) {
    this.loanFileRepositoryProvider = loanFileRepositoryProvider;
    this.personRepositoryProvider = personRepositoryProvider;
    this.paymentRepositoryProvider = paymentRepositoryProvider;
  }

  @Override
  public RestoreViewModel get() {
    return newInstance(loanFileRepositoryProvider.get(), personRepositoryProvider.get(), paymentRepositoryProvider.get());
  }

  public static RestoreViewModel_Factory create(
      Provider<LoanFileRepository> loanFileRepositoryProvider,
      Provider<PersonRepository> personRepositoryProvider,
      Provider<PaymentRepository> paymentRepositoryProvider) {
    return new RestoreViewModel_Factory(loanFileRepositoryProvider, personRepositoryProvider, paymentRepositoryProvider);
  }

  public static RestoreViewModel newInstance(LoanFileRepository loanFileRepository,
      PersonRepository personRepository, PaymentRepository paymentRepository) {
    return new RestoreViewModel(loanFileRepository, personRepository, paymentRepository);
  }
}
