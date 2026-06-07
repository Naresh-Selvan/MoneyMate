package com.moneymate.app.ui.viewmodel;

import com.moneymate.app.data.repository.BookAdjustmentRepository;
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
public final class BookAdjustmentViewModel_Factory implements Factory<BookAdjustmentViewModel> {
  private final Provider<BookAdjustmentRepository> repositoryProvider;

  public BookAdjustmentViewModel_Factory(Provider<BookAdjustmentRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public BookAdjustmentViewModel get() {
    return newInstance(repositoryProvider.get());
  }

  public static BookAdjustmentViewModel_Factory create(
      Provider<BookAdjustmentRepository> repositoryProvider) {
    return new BookAdjustmentViewModel_Factory(repositoryProvider);
  }

  public static BookAdjustmentViewModel newInstance(BookAdjustmentRepository repository) {
    return new BookAdjustmentViewModel(repository);
  }
}
