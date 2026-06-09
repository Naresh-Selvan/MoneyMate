package com.moneymate.app.ui.viewmodel;

import com.moneymate.app.data.repository.ExpenseRepository;
import com.moneymate.app.data.repository.InvestmentRepository;
import com.moneymate.app.data.repository.LoanFileRepository;
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
public final class UploadViewModel_Factory implements Factory<UploadViewModel> {
  private final Provider<LoanFileRepository> loanFileRepositoryProvider;

  private final Provider<PersonRepository> personRepositoryProvider;

  private final Provider<PaymentRepository> paymentRepositoryProvider;

  private final Provider<ExpenseRepository> expenseRepositoryProvider;

  private final Provider<InvestmentRepository> investmentRepositoryProvider;

  private final Provider<FirestorePathProvider> pathsProvider;

  public UploadViewModel_Factory(Provider<LoanFileRepository> loanFileRepositoryProvider,
      Provider<PersonRepository> personRepositoryProvider,
      Provider<PaymentRepository> paymentRepositoryProvider,
      Provider<ExpenseRepository> expenseRepositoryProvider,
      Provider<InvestmentRepository> investmentRepositoryProvider,
      Provider<FirestorePathProvider> pathsProvider) {
    this.loanFileRepositoryProvider = loanFileRepositoryProvider;
    this.personRepositoryProvider = personRepositoryProvider;
    this.paymentRepositoryProvider = paymentRepositoryProvider;
    this.expenseRepositoryProvider = expenseRepositoryProvider;
    this.investmentRepositoryProvider = investmentRepositoryProvider;
    this.pathsProvider = pathsProvider;
  }

  @Override
  public UploadViewModel get() {
    return newInstance(loanFileRepositoryProvider.get(), personRepositoryProvider.get(), paymentRepositoryProvider.get(), expenseRepositoryProvider.get(), investmentRepositoryProvider.get(), pathsProvider.get());
  }

  public static UploadViewModel_Factory create(
      javax.inject.Provider<LoanFileRepository> loanFileRepositoryProvider,
      javax.inject.Provider<PersonRepository> personRepositoryProvider,
      javax.inject.Provider<PaymentRepository> paymentRepositoryProvider,
      javax.inject.Provider<ExpenseRepository> expenseRepositoryProvider,
      javax.inject.Provider<InvestmentRepository> investmentRepositoryProvider,
      javax.inject.Provider<FirestorePathProvider> pathsProvider) {
    return new UploadViewModel_Factory(Providers.asDaggerProvider(loanFileRepositoryProvider), Providers.asDaggerProvider(personRepositoryProvider), Providers.asDaggerProvider(paymentRepositoryProvider), Providers.asDaggerProvider(expenseRepositoryProvider), Providers.asDaggerProvider(investmentRepositoryProvider), Providers.asDaggerProvider(pathsProvider));
  }

  public static UploadViewModel_Factory create(
      Provider<LoanFileRepository> loanFileRepositoryProvider,
      Provider<PersonRepository> personRepositoryProvider,
      Provider<PaymentRepository> paymentRepositoryProvider,
      Provider<ExpenseRepository> expenseRepositoryProvider,
      Provider<InvestmentRepository> investmentRepositoryProvider,
      Provider<FirestorePathProvider> pathsProvider) {
    return new UploadViewModel_Factory(loanFileRepositoryProvider, personRepositoryProvider, paymentRepositoryProvider, expenseRepositoryProvider, investmentRepositoryProvider, pathsProvider);
  }

  public static UploadViewModel newInstance(LoanFileRepository loanFileRepository,
      PersonRepository personRepository, PaymentRepository paymentRepository,
      ExpenseRepository expenseRepository, InvestmentRepository investmentRepository,
      FirestorePathProvider paths) {
    return new UploadViewModel(loanFileRepository, personRepository, paymentRepository, expenseRepository, investmentRepository, paths);
  }
}
