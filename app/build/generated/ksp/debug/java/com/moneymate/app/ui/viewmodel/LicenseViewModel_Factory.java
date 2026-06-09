package com.moneymate.app.ui.viewmodel;

import android.content.Context;
import com.moneymate.app.utils.AppPreferences;
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
public final class LicenseViewModel_Factory implements Factory<LicenseViewModel> {
  private final Provider<Context> contextProvider;

  private final Provider<AppPreferences> prefsProvider;

  public LicenseViewModel_Factory(Provider<Context> contextProvider,
      Provider<AppPreferences> prefsProvider) {
    this.contextProvider = contextProvider;
    this.prefsProvider = prefsProvider;
  }

  @Override
  public LicenseViewModel get() {
    return newInstance(contextProvider.get(), prefsProvider.get());
  }

  public static LicenseViewModel_Factory create(javax.inject.Provider<Context> contextProvider,
      javax.inject.Provider<AppPreferences> prefsProvider) {
    return new LicenseViewModel_Factory(Providers.asDaggerProvider(contextProvider), Providers.asDaggerProvider(prefsProvider));
  }

  public static LicenseViewModel_Factory create(Provider<Context> contextProvider,
      Provider<AppPreferences> prefsProvider) {
    return new LicenseViewModel_Factory(contextProvider, prefsProvider);
  }

  public static LicenseViewModel newInstance(Context context, AppPreferences prefs) {
    return new LicenseViewModel(context, prefs);
  }
}
