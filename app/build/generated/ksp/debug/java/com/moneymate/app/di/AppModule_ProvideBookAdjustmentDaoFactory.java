package com.moneymate.app.di;

import com.moneymate.app.data.local.AppDatabase;
import com.moneymate.app.data.local.dao.BookAdjustmentDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class AppModule_ProvideBookAdjustmentDaoFactory implements Factory<BookAdjustmentDao> {
  private final Provider<AppDatabase> dbProvider;

  public AppModule_ProvideBookAdjustmentDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public BookAdjustmentDao get() {
    return provideBookAdjustmentDao(dbProvider.get());
  }

  public static AppModule_ProvideBookAdjustmentDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new AppModule_ProvideBookAdjustmentDaoFactory(dbProvider);
  }

  public static BookAdjustmentDao provideBookAdjustmentDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideBookAdjustmentDao(db));
  }
}
