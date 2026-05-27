package com.moneymate.app.data.repository;

import com.moneymate.app.data.local.dao.EditRequestDao;
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
public final class EditRequestRepository_Factory implements Factory<EditRequestRepository> {
  private final Provider<EditRequestDao> editRequestDaoProvider;

  public EditRequestRepository_Factory(Provider<EditRequestDao> editRequestDaoProvider) {
    this.editRequestDaoProvider = editRequestDaoProvider;
  }

  @Override
  public EditRequestRepository get() {
    return newInstance(editRequestDaoProvider.get());
  }

  public static EditRequestRepository_Factory create(
      Provider<EditRequestDao> editRequestDaoProvider) {
    return new EditRequestRepository_Factory(editRequestDaoProvider);
  }

  public static EditRequestRepository newInstance(EditRequestDao editRequestDao) {
    return new EditRequestRepository(editRequestDao);
  }
}
