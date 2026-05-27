package com.moneymate.app.ui.viewmodel;

import com.moneymate.app.data.repository.DefaultPersonRepository;
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
public final class TemplateViewModel_Factory implements Factory<TemplateViewModel> {
  private final Provider<DefaultPersonRepository> repoProvider;

  public TemplateViewModel_Factory(Provider<DefaultPersonRepository> repoProvider) {
    this.repoProvider = repoProvider;
  }

  @Override
  public TemplateViewModel get() {
    return newInstance(repoProvider.get());
  }

  public static TemplateViewModel_Factory create(Provider<DefaultPersonRepository> repoProvider) {
    return new TemplateViewModel_Factory(repoProvider);
  }

  public static TemplateViewModel newInstance(DefaultPersonRepository repo) {
    return new TemplateViewModel(repo);
  }
}
