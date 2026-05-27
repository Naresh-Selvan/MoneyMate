package com.moneymate.app.data.repository;

import com.moneymate.app.data.local.dao.FileDao;
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

  public LoanFileRepository_Factory(Provider<FileDao> fileDaoProvider) {
    this.fileDaoProvider = fileDaoProvider;
  }

  @Override
  public LoanFileRepository get() {
    return newInstance(fileDaoProvider.get());
  }

  public static LoanFileRepository_Factory create(Provider<FileDao> fileDaoProvider) {
    return new LoanFileRepository_Factory(fileDaoProvider);
  }

  public static LoanFileRepository newInstance(FileDao fileDao) {
    return new LoanFileRepository(fileDao);
  }
}
