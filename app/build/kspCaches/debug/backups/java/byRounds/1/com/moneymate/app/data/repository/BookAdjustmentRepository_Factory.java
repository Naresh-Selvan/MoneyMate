package com.moneymate.app.data.repository;

import com.moneymate.app.data.local.dao.BookAdjustmentDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
    "cast"
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
      Provider<BookAdjustmentDao> bookAdjustmentDaoProvider) {
    return new BookAdjustmentRepository_Factory(bookAdjustmentDaoProvider);
  }

  public static BookAdjustmentRepository newInstance(BookAdjustmentDao bookAdjustmentDao) {
    return new BookAdjustmentRepository(bookAdjustmentDao);
  }
}
