package com.moneymate.app.di;

import com.moneymate.app.data.local.AppDatabase;
import com.moneymate.app.data.local.dao.FileDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class AppModule_ProvideFileDaoFactory implements Factory<FileDao> {
  private final Provider<AppDatabase> dbProvider;

  public AppModule_ProvideFileDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public FileDao get() {
    return provideFileDao(dbProvider.get());
  }

  public static AppModule_ProvideFileDaoFactory create(
      javax.inject.Provider<AppDatabase> dbProvider) {
    return new AppModule_ProvideFileDaoFactory(Providers.asDaggerProvider(dbProvider));
  }

  public static AppModule_ProvideFileDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new AppModule_ProvideFileDaoFactory(dbProvider);
  }

  public static FileDao provideFileDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideFileDao(db));
  }
}
