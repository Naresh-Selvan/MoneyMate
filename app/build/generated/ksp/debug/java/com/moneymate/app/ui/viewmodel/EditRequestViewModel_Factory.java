package com.moneymate.app.ui.viewmodel;

import com.moneymate.app.data.repository.EditRequestRepository;
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
public final class EditRequestViewModel_Factory implements Factory<EditRequestViewModel> {
  private final Provider<EditRequestRepository> repositoryProvider;

  public EditRequestViewModel_Factory(Provider<EditRequestRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public EditRequestViewModel get() {
    return newInstance(repositoryProvider.get());
  }

  public static EditRequestViewModel_Factory create(
      Provider<EditRequestRepository> repositoryProvider) {
    return new EditRequestViewModel_Factory(repositoryProvider);
  }

  public static EditRequestViewModel newInstance(EditRequestRepository repository) {
    return new EditRequestViewModel(repository);
  }
}
