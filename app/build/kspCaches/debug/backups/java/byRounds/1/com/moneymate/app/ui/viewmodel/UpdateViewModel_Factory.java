package com.moneymate.app.ui.viewmodel;

import android.content.Context;
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
public final class UpdateViewModel_Factory implements Factory<UpdateViewModel> {
  private final Provider<Context> contextProvider;

  public UpdateViewModel_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public UpdateViewModel get() {
    return newInstance(contextProvider.get());
  }

  public static UpdateViewModel_Factory create(javax.inject.Provider<Context> contextProvider) {
    return new UpdateViewModel_Factory(Providers.asDaggerProvider(contextProvider));
  }

  public static UpdateViewModel_Factory create(Provider<Context> contextProvider) {
    return new UpdateViewModel_Factory(contextProvider);
  }

  public static UpdateViewModel newInstance(Context context) {
    return new UpdateViewModel(context);
  }
}
