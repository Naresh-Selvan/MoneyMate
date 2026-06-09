package com.moneymate.app.auth;

import com.moneymate.app.data.local.dao.AuditLogDao;
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
public final class AuditLogger_Factory implements Factory<AuditLogger> {
  private final Provider<AuditLogDao> auditLogDaoProvider;

  private final Provider<SessionManager> sessionManagerProvider;

  public AuditLogger_Factory(Provider<AuditLogDao> auditLogDaoProvider,
      Provider<SessionManager> sessionManagerProvider) {
    this.auditLogDaoProvider = auditLogDaoProvider;
    this.sessionManagerProvider = sessionManagerProvider;
  }

  @Override
  public AuditLogger get() {
    return newInstance(auditLogDaoProvider.get(), sessionManagerProvider.get());
  }

  public static AuditLogger_Factory create(javax.inject.Provider<AuditLogDao> auditLogDaoProvider,
      javax.inject.Provider<SessionManager> sessionManagerProvider) {
    return new AuditLogger_Factory(Providers.asDaggerProvider(auditLogDaoProvider), Providers.asDaggerProvider(sessionManagerProvider));
  }

  public static AuditLogger_Factory create(Provider<AuditLogDao> auditLogDaoProvider,
      Provider<SessionManager> sessionManagerProvider) {
    return new AuditLogger_Factory(auditLogDaoProvider, sessionManagerProvider);
  }

  public static AuditLogger newInstance(AuditLogDao auditLogDao, SessionManager sessionManager) {
    return new AuditLogger(auditLogDao, sessionManager);
  }
}
