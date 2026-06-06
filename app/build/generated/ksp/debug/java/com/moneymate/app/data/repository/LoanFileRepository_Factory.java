package com.moneymate.app.data.repository;

import com.moneymate.app.data.local.dao.FileDao;
import com.moneymate.app.data.local.dao.PaymentDao;
import com.moneymate.app.data.local.dao.PersonDao;
import com.moneymate.app.utils.FirestorePathProvider;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class LoanFileRepository_Factory implements Factory<LoanFileRepository> {
  private final Provider<FileDao> fileDaoProvider;

  private final Provider<PersonDao> personDaoProvider;

  private final Provider<PaymentDao> paymentDaoProvider;

  private final Provider<FirestorePathProvider> pathsProvider;

  public LoanFileRepository_Factory(Provider<FileDao> fileDaoProvider,
      Provider<PersonDao> personDaoProvider, Provider<PaymentDao> paymentDaoProvider,
      Provider<FirestorePathProvider> pathsProvider) {
    this.fileDaoProvider = fileDaoProvider;
    this.personDaoProvider = personDaoProvider;
    this.paymentDaoProvider = paymentDaoProvider;
    this.pathsProvider = pathsProvider;
  }

  @Override
  public LoanFileRepository get() {
    return newInstance(fileDaoProvider.get(), personDaoProvider.get(), paymentDaoProvider.get(), pathsProvider.get());
  }

  public static LoanFileRepository_Factory create(Provider<FileDao> fileDaoProvider,
      Provider<PersonDao> personDaoProvider, Provider<PaymentDao> paymentDaoProvider,
      Provider<FirestorePathProvider> pathsProvider) {
    return new LoanFileRepository_Factory(fileDaoProvider, personDaoProvider, paymentDaoProvider, pathsProvider);
  }

  public static LoanFileRepository newInstance(FileDao fileDao, PersonDao personDao,
      PaymentDao paymentDao, FirestorePathProvider paths) {
    return new LoanFileRepository(fileDao, personDao, paymentDao, paths);
  }
}
