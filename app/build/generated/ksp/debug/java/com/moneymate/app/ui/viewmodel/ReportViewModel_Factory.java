package com.moneymate.app.ui.viewmodel;

import com.moneymate.app.data.repository.ExpenseRepository;
import com.moneymate.app.data.repository.InvestmentRepository;
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
public final class ReportViewModel_Factory implements Factory<ReportViewModel> {
  private final Provider<PaymentRepository> paymentRepositoryProvider;

  private final Provider<PersonRepository> personRepositoryProvider;

  private final Provider<ExpenseRepository> expenseRepositoryProvider;

  private final Provider<InvestmentRepository> investmentRepositoryProvider;

  public ReportViewModel_Factory(Provider<PaymentRepository> paymentRepositoryProvider,
      Provider<PersonRepository> personRepositoryProvider,
      Provider<ExpenseRepository> expenseRepositoryProvider,
      Provider<InvestmentRepository> investmentRepositoryProvider) {
    this.paymentRepositoryProvider = paymentRepositoryProvider;
    this.personRepositoryProvider = personRepositoryProvider;
    this.expenseRepositoryProvider = expenseRepositoryProvider;
    this.investmentRepositoryProvider = investmentRepositoryProvider;
  }

  @Override
  public ReportViewModel get() {
    return newInstance(paymentRepositoryProvider.get(), personRepositoryProvider.get(), expenseRepositoryProvider.get(), investmentRepositoryProvider.get());
  }

  public static ReportViewModel_Factory create(
      javax.inject.Provider<PaymentRepository> paymentRepositoryProvider,
      javax.inject.Provider<PersonRepository> personRepositoryProvider,
      javax.inject.Provider<ExpenseRepository> expenseRepositoryProvider,
      javax.inject.Provider<InvestmentRepository> investmentRepositoryProvider) {
    return new ReportViewModel_Factory(Providers.asDaggerProvider(paymentRepositoryProvider), Providers.asDaggerProvider(personRepositoryProvider), Providers.asDaggerProvider(expenseRepositoryProvider), Providers.asDaggerProvider(investmentRepositoryProvider));
  }

  public static ReportViewModel_Factory create(
      Provider<PaymentRepository> paymentRepositoryProvider,
      Provider<PersonRepository> personRepositoryProvider,
      Provider<ExpenseRepository> expenseRepositoryProvider,
      Provider<InvestmentRepository> investmentRepositoryProvider) {
    return new ReportViewModel_Factory(paymentRepositoryProvider, personRepositoryProvider, expenseRepositoryProvider, investmentRepositoryProvider);
  }

  public static ReportViewModel newInstance(PaymentRepository paymentRepository,
      PersonRepository personRepository, ExpenseRepository expenseRepository,
      InvestmentRepository investmentRepository) {
    return new ReportViewModel(paymentRepository, personRepository, expenseRepository, investmentRepository);
  }
}
