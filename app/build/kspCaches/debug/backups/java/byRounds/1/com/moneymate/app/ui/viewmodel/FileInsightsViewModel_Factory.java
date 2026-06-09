package com.moneymate.app.ui.viewmodel;

import com.moneymate.app.data.repository.PaymentRepository;
import com.moneymate.app.data.repository.PersonRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
      javax.inject.Provider<PersonRepository> personRepositoryProvider,
      javax.inject.Provider<PaymentRepository> paymentRepositoryProvider) {
    return new FileInsightsViewModel_Factory(Providers.asDaggerProvider(personRepositoryProvider), Providers.asDaggerProvider(paymentRepositoryProvider));
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
