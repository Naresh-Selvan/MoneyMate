package com.moneymate.app.data.repository;

import com.moneymate.app.data.local.dao.BookAdjustmentDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
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
public final class BookAdjustmentRepository_Factory implements Factory<BookAdjustmentRepository> {
  private final Provider<BookAdjustmentDao> bookAdjustmentDaoProvider;

  public BookAdjustmentRepository_Factory(Provider<BookAdjustmentDao> bookAdjustmentDaoProvider) {
    this.bookAdjustmentDaoProvider = bookAdjustmentDaoProvider;
  }

  @Override
  public BookAdjustmentRepository get() {
    return newInstance(bookAdjustmentDaoProvider.get());
  }

  public static BookAdjustmentRepository_Factory create(
      javax.inject.Provider<BookAdjustmentDao> bookAdjustmentDaoProvider) {
    return new BookAdjustmentRepository_Factory(Providers.asDaggerProvider(bookAdjustmentDaoProvider));
  }

  public static BookAdjustmentRepository_Factory create(
      Provider<BookAdjustmentDao> bookAdjustmentDaoProvider) {
    return new BookAdjustmentRepository_Factory(bookAdjustmentDaoProvider);
  }

  public static BookAdjustmentRepository newInstance(BookAdjustmentDao bookAdjustmentDao) {
    return new BookAdjustmentRepository(bookAdjustmentDao);
  }
}
