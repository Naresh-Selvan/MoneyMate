package com.moneymate.app.data.repository;

import com.moneymate.app.data.local.dao.FileDao;
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
public final class LoanFileRepository_Factory implements Factory<LoanFileRepository> {
  private final Provider<FileDao> fileDaoProvider;

  private final Provider<FirestorePathProvider> pathsProvider;

  public LoanFileRepository_Factory(Provider<FileDao> fileDaoProvider,
      Provider<FirestorePathProvider> pathsProvider) {
    this.fileDaoProvider = fileDaoProvider;
    this.pathsProvider = pathsProvider;
  }

  @Override
  public LoanFileRepository get() {
    return newInstance(fileDaoProvider.get(), pathsProvider.get());
  }

  public static LoanFileRepository_Factory create(javax.inject.Provider<FileDao> fileDaoProvider,
      javax.inject.Provider<FirestorePathProvider> pathsProvider) {
    return new LoanFileRepository_Factory(Providers.asDaggerProvider(fileDaoProvider), Providers.asDaggerProvider(pathsProvider));
  }

  public static LoanFileRepository_Factory create(Provider<FileDao> fileDaoProvider,
      Provider<FirestorePathProvider> pathsProvider) {
    return new LoanFileRepository_Factory(fileDaoProvider, pathsProvider);
  }

  public static LoanFileRepository newInstance(FileDao fileDao, FirestorePathProvider paths) {
    return new LoanFileRepository(fileDao, paths);
  }
}
