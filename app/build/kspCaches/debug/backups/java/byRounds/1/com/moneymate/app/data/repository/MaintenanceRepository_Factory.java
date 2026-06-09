package com.moneymate.app.data.repository;

import com.moneymate.app.data.local.dao.FileDao;
import com.moneymate.app.data.local.dao.PaymentDao;
import com.moneymate.app.data.local.dao.PersonDao;
import com.moneymate.app.utils.FirestorePathProvider;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class MaintenanceRepository_Factory implements Factory<MaintenanceRepository> {
  private final Provider<FileDao> fileDaoProvider;

  private final Provider<PersonDao> personDaoProvider;

  private final Provider<PaymentDao> paymentDaoProvider;

  private final Provider<FirestorePathProvider> pathsProvider;

  public MaintenanceRepository_Factory(Provider<FileDao> fileDaoProvider,
      Provider<PersonDao> personDaoProvider, Provider<PaymentDao> paymentDaoProvider,
      Provider<FirestorePathProvider> pathsProvider) {
    this.fileDaoProvider = fileDaoProvider;
    this.personDaoProvider = personDaoProvider;
    this.paymentDaoProvider = paymentDaoProvider;
    this.pathsProvider = pathsProvider;
  }

  @Override
  public MaintenanceRepository get() {
    return newInstance(fileDaoProvider.get(), personDaoProvider.get(), paymentDaoProvider.get(), pathsProvider.get());
  }

  public static MaintenanceRepository_Factory create(javax.inject.Provider<FileDao> fileDaoProvider,
      javax.inject.Provider<PersonDao> personDaoProvider,
      javax.inject.Provider<PaymentDao> paymentDaoProvider,
      javax.inject.Provider<FirestorePathProvider> pathsProvider) {
    return new MaintenanceRepository_Factory(Providers.asDaggerProvider(fileDaoProvider), Providers.asDaggerProvider(personDaoProvider), Providers.asDaggerProvider(paymentDaoProvider), Providers.asDaggerProvider(pathsProvider));
  }

  public static MaintenanceRepository_Factory create(Provider<FileDao> fileDaoProvider,
      Provider<PersonDao> personDaoProvider, Provider<PaymentDao> paymentDaoProvider,
      Provider<FirestorePathProvider> pathsProvider) {
    return new MaintenanceRepository_Factory(fileDaoProvider, personDaoProvider, paymentDaoProvider, pathsProvider);
  }

  public static MaintenanceRepository newInstance(FileDao fileDao, PersonDao personDao,
      PaymentDao paymentDao, FirestorePathProvider paths) {
    return new MaintenanceRepository(fileDao, personDao, paymentDao, paths);
  }
}
