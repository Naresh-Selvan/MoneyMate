package com.moneymate.app.ui.viewmodel;

import android.content.Context;
import com.moneymate.app.data.export.ExportManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
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
public final class ExportViewModel_Factory implements Factory<ExportViewModel> {
  private final Provider<ExportManager> exportManagerProvider;

  private final Provider<Context> contextProvider;

  public ExportViewModel_Factory(Provider<ExportManager> exportManagerProvider,
      Provider<Context> contextProvider) {
    this.exportManagerProvider = exportManagerProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public ExportViewModel get() {
    return newInstance(exportManagerProvider.get(), contextProvider.get());
  }

  public static ExportViewModel_Factory create(
      javax.inject.Provider<ExportManager> exportManagerProvider,
      javax.inject.Provider<Context> contextProvider) {
    return new ExportViewModel_Factory(Providers.asDaggerProvider(exportManagerProvider), Providers.asDaggerProvider(contextProvider));
  }

  public static ExportViewModel_Factory create(Provider<ExportManager> exportManagerProvider,
      Provider<Context> contextProvider) {
    return new ExportViewModel_Factory(exportManagerProvider, contextProvider);
  }

  public static ExportViewModel newInstance(ExportManager exportManager, Context context) {
    return new ExportViewModel(exportManager, context);
  }
}
