package com.moneymate.app.di;

import com.moneymate.app.data.local.dao.DefaultPersonDao;
import com.moneymate.app.data.repository.DefaultPersonRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class AppModule_ProvideDefaultPersonRepositoryFactory implements Factory<DefaultPersonRepository> {
  private final Provider<DefaultPersonDao> daoProvider;

  public AppModule_ProvideDefaultPersonRepositoryFactory(Provider<DefaultPersonDao> daoProvider) {
    this.daoProvider = daoProvider;
  }

  @Override
  public DefaultPersonRepository get() {
    return provideDefaultPersonRepository(daoProvider.get());
  }

  public static AppModule_ProvideDefaultPersonRepositoryFactory create(
      Provider<DefaultPersonDao> daoProvider) {
    return new AppModule_ProvideDefaultPersonRepositoryFactory(daoProvider);
  }

  public static DefaultPersonRepository provideDefaultPersonRepository(DefaultPersonDao dao) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideDefaultPersonRepository(dao));
  }
}
