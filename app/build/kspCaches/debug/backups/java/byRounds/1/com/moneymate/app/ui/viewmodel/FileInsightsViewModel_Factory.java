package com.moneymate.app.ui.viewmodel;

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
public final class FileInsightsViewModel_Factory implements Factory<FileInsightsViewModel> {
  private final Provider<PersonRepository> personRepositoryProvider;

  private final Provider<PaymentRepository> paymentRepositoryProvider;

  public FileInsightsViewModel_Factory(Provider<PersonRepository> personRepositoryProvider,
      Provider<PaymentRepository> paymentRepositoryProvider) {
    this.personRepositoryProvider = personRepositoryProvider;
    this.paymentRepositoryProvider = paymentRepositoryProvider;
  }

  @Override
  public FileInsightsViewModel get() {
    return newInstance(personRepositoryProvider.get(), paymentRepositoryProvider.get());
  }

  public static FileInsightsViewModel_Factory create(
      Provider<PersonRepository> personRepositoryProvider,
      Provider<PaymentRepository> paymentRepositoryProvider) {
    return new FileInsightsViewModel_Factory(personRepositoryProvider, paymentRepositoryProvider);
  }

  public static FileInsightsViewModel newInstance(PersonRepository personRepository,
      PaymentRepository paymentRepository) {
    return new FileInsightsViewModel(personRepository, paymentRepository);
  }
}
