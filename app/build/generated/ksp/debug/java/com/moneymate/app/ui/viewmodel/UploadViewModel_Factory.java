package com.moneymate.app.ui.viewmodel;

import com.moneymate.app.data.repository.DefaultPersonRepository;
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
public final class UploadViewModel_Factory implements Factory<UploadViewModel> {
  private final Provider<LoanFileRepository> loanFileRepositoryProvider;

  private final Provider<PersonRepository> personRepositoryProvider;

  private final Provider<PaymentRepository> paymentRepositoryProvider;

  private final Provider<DefaultPersonRepository> defaultPersonRepositoryProvider;

  public UploadViewModel_Factory(Provider<LoanFileRepository> loanFileRepositoryProvider,
      Provider<PersonRepository> personRepositoryProvider,
      Provider<PaymentRepository> paymentRepositoryProvider,
      Provider<DefaultPersonRepository> defaultPersonRepositoryProvider) {
    this.loanFileRepositoryProvider = loanFileRepositoryProvider;
    this.personRepositoryProvider = personRepositoryProvider;
    this.paymentRepositoryProvider = paymentRepositoryProvider;
    this.defaultPersonRepositoryProvider = defaultPersonRepositoryProvider;
  }

  @Override
  public UploadViewModel get() {
    return newInstance(loanFileRepositoryProvider.get(), personRepositoryProvider.get(), paymentRepositoryProvider.get(), defaultPersonRepositoryProvider.get());
  }

  public static UploadViewModel_Factory create(
      Provider<LoanFileRepository> loanFileRepositoryProvider,
      Provider<PersonRepository> personRepositoryProvider,
      Provider<PaymentRepository> paymentRepositoryProvider,
      Provider<DefaultPersonRepository> defaultPersonRepositoryProvider) {
    return new UploadViewModel_Factory(loanFileRepositoryProvider, personRepositoryProvider, paymentRepositoryProvider, defaultPersonRepositoryProvider);
  }

  public static UploadViewModel newInstance(LoanFileRepository loanFileRepository,
      PersonRepository personRepository, PaymentRepository paymentRepository,
      DefaultPersonRepository defaultPersonRepository) {
    return new UploadViewModel(loanFileRepository, personRepository, paymentRepository, defaultPersonRepository);
  }
}
