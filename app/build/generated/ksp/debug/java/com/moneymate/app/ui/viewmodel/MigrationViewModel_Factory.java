package com.moneymate.app.ui.viewmodel;

import com.moneymate.app.utils.AppPreferences;
import com.moneymate.app.utils.FirestorePathProvider;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
    "cast"
})
public final class MigrationViewModel_Factory implements Factory<MigrationViewModel> {
  private final Provider<AppPreferences> prefsProvider;

  private final Provider<FirestorePathProvider> pathsProvider;

  public MigrationViewModel_Factory(Provider<AppPreferences> prefsProvider,
      Provider<FirestorePathProvider> pathsProvider) {
    this.prefsProvider = prefsProvider;
    this.pathsProvider = pathsProvider;
  }

  @Override
  public MigrationViewModel get() {
    return newInstance(prefsProvider.get(), pathsProvider.get());
  }

  public static MigrationViewModel_Factory create(Provider<AppPreferences> prefsProvider,
      Provider<FirestorePathProvider> pathsProvider) {
    return new MigrationViewModel_Factory(prefsProvider, pathsProvider);
  }

  public static MigrationViewModel newInstance(AppPreferences prefs, FirestorePathProvider paths) {
    return new MigrationViewModel(prefs, paths);
  }
}
