package com.moneymate.app.di;

import com.moneymate.app.data.local.dao.FileDao;
import com.moneymate.app.data.local.dao.PaymentDao;
import com.moneymate.app.data.local.dao.PersonDao;
import com.moneymate.app.data.repository.MaintenanceRepository;
import com.moneymate.app.utils.FirestorePathProvider;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class AppModule_ProvideMaintenanceRepositoryFactory implements Factory<MaintenanceRepository> {
  private final Provider<FileDao> fileDaoProvider;

  private final Provider<PersonDao> personDaoProvider;

  private final Provider<PaymentDao> paymentDaoProvider;

  private final Provider<FirestorePathProvider> pathsProvider;

  public AppModule_ProvideMaintenanceRepositoryFactory(Provider<FileDao> fileDaoProvider,
      Provider<PersonDao> personDaoProvider, Provider<PaymentDao> paymentDaoProvider,
      Provider<FirestorePathProvider> pathsProvider) {
    this.fileDaoProvider = fileDaoProvider;
    this.personDaoProvider = personDaoProvider;
    this.paymentDaoProvider = paymentDaoProvider;
    this.pathsProvider = pathsProvider;
  }

  @Override
  public MaintenanceRepository get() {
    return provideMaintenanceRepository(fileDaoProvider.get(), personDaoProvider.get(), paymentDaoProvider.get(), pathsProvider.get());
  }

  public static AppModule_ProvideMaintenanceRepositoryFactory create(
      Provider<FileDao> fileDaoProvider, Provider<PersonDao> personDaoProvider,
      Provider<PaymentDao> paymentDaoProvider, Provider<FirestorePathProvider> pathsProvider) {
    return new AppModule_ProvideMaintenanceRepositoryFactory(fileDaoProvider, personDaoProvider, paymentDaoProvider, pathsProvider);
  }

  public static MaintenanceRepository provideMaintenanceRepository(FileDao fileDao,
      PersonDao personDao, PaymentDao paymentDao, FirestorePathProvider paths) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideMaintenanceRepository(fileDao, personDao, paymentDao, paths));
  }
}
