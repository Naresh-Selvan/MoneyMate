package com.moneymate.app.data.export;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class ExportManager_Factory implements Factory<ExportManager> {
  private final Provider<Context> contextProvider;

  public ExportManager_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public ExportManager get() {
    return newInstance(contextProvider.get());
  }

  public static ExportManager_Factory create(javax.inject.Provider<Context> contextProvider) {
    return new ExportManager_Factory(Providers.asDaggerProvider(contextProvider));
  }

  public static ExportManager_Factory create(Provider<Context> contextProvider) {
    return new ExportManager_Factory(contextProvider);
  }

  public static ExportManager newInstance(Context context) {
    return new ExportManager(context);
  }
}
