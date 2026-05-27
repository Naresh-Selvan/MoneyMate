package com.moneymate.app.ui.viewmodel;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
    "cast"
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

  public static UpdateViewModel_Factory create(Provider<Context> contextProvider) {
    return new UpdateViewModel_Factory(contextProvider);
  }

  public static UpdateViewModel newInstance(Context context) {
    return new UpdateViewModel(context);
  }
}
