package com.moneymate.app.ui.viewmodel;

import com.moneymate.app.auth.AuditLogger;
import com.moneymate.app.data.local.dao.AppUserDao;
import com.moneymate.app.utils.AppPreferences;
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
public final class UserViewModel_Factory implements Factory<UserViewModel> {
  private final Provider<AppUserDao> appUserDaoProvider;

  private final Provider<AuditLogger> auditLoggerProvider;

  private final Provider<AppPreferences> appPreferencesProvider;

  public UserViewModel_Factory(Provider<AppUserDao> appUserDaoProvider,
      Provider<AuditLogger> auditLoggerProvider, Provider<AppPreferences> appPreferencesProvider) {
    this.appUserDaoProvider = appUserDaoProvider;
    this.auditLoggerProvider = auditLoggerProvider;
    this.appPreferencesProvider = appPreferencesProvider;
  }

  @Override
  public UserViewModel get() {
    return newInstance(appUserDaoProvider.get(), auditLoggerProvider.get(), appPreferencesProvider.get());
  }

  public static UserViewModel_Factory create(javax.inject.Provider<AppUserDao> appUserDaoProvider,
      javax.inject.Provider<AuditLogger> auditLoggerProvider,
      javax.inject.Provider<AppPreferences> appPreferencesProvider) {
    return new UserViewModel_Factory(Providers.asDaggerProvider(appUserDaoProvider), Providers.asDaggerProvider(auditLoggerProvider), Providers.asDaggerProvider(appPreferencesProvider));
  }

  public static UserViewModel_Factory create(Provider<AppUserDao> appUserDaoProvider,
      Provider<AuditLogger> auditLoggerProvider, Provider<AppPreferences> appPreferencesProvider) {
    return new UserViewModel_Factory(appUserDaoProvider, auditLoggerProvider, appPreferencesProvider);
  }

  public static UserViewModel newInstance(AppUserDao appUserDao, AuditLogger auditLogger,
      AppPreferences appPreferences) {
    return new UserViewModel(appUserDao, auditLogger, appPreferences);
  }
}
