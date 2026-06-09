package com.moneymate.app.auth;

import com.moneymate.app.data.local.dao.AppUserDao;
import com.moneymate.app.data.local.dao.AuditLogDao;
import com.moneymate.app.utils.AppPreferences;
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
public final class SessionManager_Factory implements Factory<SessionManager> {
  private final Provider<AppPreferences> appPreferencesProvider;

  private final Provider<AppUserDao> appUserDaoProvider;

  private final Provider<AuditLogDao> auditLogDaoProvider;

  public SessionManager_Factory(Provider<AppPreferences> appPreferencesProvider,
      Provider<AppUserDao> appUserDaoProvider, Provider<AuditLogDao> auditLogDaoProvider) {
    this.appPreferencesProvider = appPreferencesProvider;
    this.appUserDaoProvider = appUserDaoProvider;
    this.auditLogDaoProvider = auditLogDaoProvider;
  }

  @Override
  public SessionManager get() {
    return newInstance(appPreferencesProvider.get(), appUserDaoProvider.get(), auditLogDaoProvider.get());
  }

  public static SessionManager_Factory create(
      javax.inject.Provider<AppPreferences> appPreferencesProvider,
      javax.inject.Provider<AppUserDao> appUserDaoProvider,
      javax.inject.Provider<AuditLogDao> auditLogDaoProvider) {
    return new SessionManager_Factory(Providers.asDaggerProvider(appPreferencesProvider), Providers.asDaggerProvider(appUserDaoProvider), Providers.asDaggerProvider(auditLogDaoProvider));
  }

  public static SessionManager_Factory create(Provider<AppPreferences> appPreferencesProvider,
      Provider<AppUserDao> appUserDaoProvider, Provider<AuditLogDao> auditLogDaoProvider) {
    return new SessionManager_Factory(appPreferencesProvider, appUserDaoProvider, auditLogDaoProvider);
  }

  public static SessionManager newInstance(AppPreferences appPreferences, AppUserDao appUserDao,
      AuditLogDao auditLogDao) {
    return new SessionManager(appPreferences, appUserDao, auditLogDao);
  }
}
