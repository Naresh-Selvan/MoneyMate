package com.moneymate.app.di;

import com.moneymate.app.data.local.AppDatabase;
import com.moneymate.app.data.local.dao.AuditLogDao;
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
public final class AppModule_ProvideAuditLogDaoFactory implements Factory<AuditLogDao> {
  private final Provider<AppDatabase> dbProvider;

  public AppModule_ProvideAuditLogDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public AuditLogDao get() {
    return provideAuditLogDao(dbProvider.get());
  }

  public static AppModule_ProvideAuditLogDaoFactory create(
      javax.inject.Provider<AppDatabase> dbProvider) {
    return new AppModule_ProvideAuditLogDaoFactory(Providers.asDaggerProvider(dbProvider));
  }

  public static AppModule_ProvideAuditLogDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new AppModule_ProvideAuditLogDaoFactory(dbProvider);
  }

  public static AuditLogDao provideAuditLogDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideAuditLogDao(db));
  }
}
