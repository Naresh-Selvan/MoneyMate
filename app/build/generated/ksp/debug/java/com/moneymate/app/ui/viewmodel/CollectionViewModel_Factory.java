package com.moneymate.app.ui.viewmodel;

import com.moneymate.app.data.repository.PaymentRepository;
import com.moneymate.app.data.repository.PersonRepository;
import com.moneymate.app.utils.FirestorePathProvider;
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
public final class CollectionViewModel_Factory implements Factory<CollectionViewModel> {
  private final Provider<PersonRepository> personRepositoryProvider;

  private final Provider<PaymentRepository> paymentRepositoryProvider;

  private final Provider<FirestorePathProvider> pathsProvider;

  public CollectionViewModel_Factory(Provider<PersonRepository> personRepositoryProvider,
      Provider<PaymentRepository> paymentRepositoryProvider,
      Provider<FirestorePathProvider> pathsProvider) {
    this.personRepositoryProvider = personRepositoryProvider;
    this.paymentRepositoryProvider = paymentRepositoryProvider;
    this.pathsProvider = pathsProvider;
  }

  @Override
  public CollectionViewModel get() {
    return newInstance(personRepositoryProvider.get(), paymentRepositoryProvider.get(), pathsProvider.get());
  }

  public static CollectionViewModel_Factory create(
      javax.inject.Provider<PersonRepository> personRepositoryProvider,
      javax.inject.Provider<PaymentRepository> paymentRepositoryProvider,
      javax.inject.Provider<FirestorePathProvider> pathsProvider) {
    return new CollectionViewModel_Factory(Providers.asDaggerProvider(personRepositoryProvider), Providers.asDaggerProvider(paymentRepositoryProvider), Providers.asDaggerProvider(pathsProvider));
  }

  public static CollectionViewModel_Factory create(
      Provider<PersonRepository> personRepositoryProvider,
      Provider<PaymentRepository> paymentRepositoryProvider,
      Provider<FirestorePathProvider> pathsProvider) {
    return new CollectionViewModel_Factory(personRepositoryProvider, paymentRepositoryProvider, pathsProvider);
  }

  public static CollectionViewModel newInstance(PersonRepository personRepository,
      PaymentRepository paymentRepository, FirestorePathProvider paths) {
    return new CollectionViewModel(personRepository, paymentRepository, paths);
  }
}
