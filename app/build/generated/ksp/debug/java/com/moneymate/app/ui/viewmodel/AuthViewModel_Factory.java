package com.moneymate.app.ui.viewmodel;

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
public final class AuthViewModel_Factory implements Factory<AuthViewModel> {
  private final Provider<AppPreferences> prefsProvider;

  public AuthViewModel_Factory(Provider<AppPreferences> prefsProvider) {
    this.prefsProvider = prefsProvider;
  }

  @Override
  public AuthViewModel get() {
    return newInstance(prefsProvider.get());
  }

  public static AuthViewModel_Factory create(javax.inject.Provider<AppPreferences> prefsProvider) {
    return new AuthViewModel_Factory(Providers.asDaggerProvider(prefsProvider));
  }

  public static AuthViewModel_Factory create(Provider<AppPreferences> prefsProvider) {
    return new AuthViewModel_Factory(prefsProvider);
  }

  public static AuthViewModel newInstance(AppPreferences prefs) {
    return new AuthViewModel(prefs);
  }
}
