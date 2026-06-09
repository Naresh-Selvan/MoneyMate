package com.moneymate.app;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.hilt.work.HiltWorkerFactory;
import androidx.hilt.work.WorkerAssistedFactory;
import androidx.hilt.work.WorkerFactoryModule_ProvideFactoryFactory;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.moneymate.app.auth.AuditLogger;
import com.moneymate.app.auth.SessionManager;
import com.moneymate.app.data.export.ExportManager;
import com.moneymate.app.data.local.AppDatabase;
import com.moneymate.app.data.local.dao.AppUserDao;
import com.moneymate.app.data.local.dao.AreaDao;
import com.moneymate.app.data.local.dao.AuditLogDao;
import com.moneymate.app.data.local.dao.BookAdjustmentDao;
import com.moneymate.app.data.local.dao.DefaultPersonDao;
import com.moneymate.app.data.local.dao.EditRequestDao;
import com.moneymate.app.data.local.dao.ExpenseCategoryDao;
import com.moneymate.app.data.local.dao.ExpenseDao;
import com.moneymate.app.data.local.dao.FileDao;
import com.moneymate.app.data.local.dao.InvestmentDao;
import com.moneymate.app.data.local.dao.InvestmentTypeDao;
import com.moneymate.app.data.local.dao.PaymentDao;
import com.moneymate.app.data.local.dao.PersonDao;
import com.moneymate.app.data.repository.AreaRepository;
import com.moneymate.app.data.repository.BookAdjustmentRepository;
import com.moneymate.app.data.repository.DefaultPersonRepository;
import com.moneymate.app.data.repository.EditRequestRepository;
import com.moneymate.app.data.repository.ExpenseRepository;
import com.moneymate.app.data.repository.InvestmentRepository;
import com.moneymate.app.data.repository.LoanFileRepository;
import com.moneymate.app.data.repository.MaintenanceRepository;
import com.moneymate.app.data.repository.PaymentRepository;
import com.moneymate.app.data.repository.PersonRepository;
import com.moneymate.app.di.AppModule_ProvideAppUserDaoFactory;
import com.moneymate.app.di.AppModule_ProvideAreaDaoFactory;
import com.moneymate.app.di.AppModule_ProvideAuditLogDaoFactory;
import com.moneymate.app.di.AppModule_ProvideBookAdjustmentDaoFactory;
import com.moneymate.app.di.AppModule_ProvideDatabaseFactory;
import com.moneymate.app.di.AppModule_ProvideDefaultPersonDaoFactory;
import com.moneymate.app.di.AppModule_ProvideDefaultPersonRepositoryFactory;
import com.moneymate.app.di.AppModule_ProvideEditRequestDaoFactory;
import com.moneymate.app.di.AppModule_ProvideExpenseCategoryDaoFactory;
import com.moneymate.app.di.AppModule_ProvideExpenseDaoFactory;
import com.moneymate.app.di.AppModule_ProvideFileDaoFactory;
import com.moneymate.app.di.AppModule_ProvideFirestorePathProviderFactory;
import com.moneymate.app.di.AppModule_ProvideInvestmentDaoFactory;
import com.moneymate.app.di.AppModule_ProvideInvestmentTypeDaoFactory;
import com.moneymate.app.di.AppModule_ProvideMaintenanceRepositoryFactory;
import com.moneymate.app.di.AppModule_ProvidePaymentDaoFactory;
import com.moneymate.app.di.AppModule_ProvidePersonDaoFactory;
import com.moneymate.app.notifications.NotificationChannelManager;
import com.moneymate.app.notifications.NotificationHelper;
import com.moneymate.app.notifications.WorkerScheduler;
import com.moneymate.app.notifications.workers.AboutToCloseWorker;
import com.moneymate.app.notifications.workers.AboutToCloseWorker_AssistedFactory;
import com.moneymate.app.notifications.workers.BadLoanAlertWorker;
import com.moneymate.app.notifications.workers.BadLoanAlertWorker_AssistedFactory;
import com.moneymate.app.notifications.workers.DailyCollectionReminderWorker;
import com.moneymate.app.notifications.workers.DailyCollectionReminderWorker_AssistedFactory;
import com.moneymate.app.notifications.workers.LoanReminderWorker;
import com.moneymate.app.notifications.workers.LoanReminderWorker_AssistedFactory;
import com.moneymate.app.ui.viewmodel.AuthViewModel;
import com.moneymate.app.ui.viewmodel.AuthViewModel_HiltModules;
import com.moneymate.app.ui.viewmodel.AuthViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.moneymate.app.ui.viewmodel.AuthViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.moneymate.app.ui.viewmodel.BookAdjustmentViewModel;
import com.moneymate.app.ui.viewmodel.BookAdjustmentViewModel_HiltModules;
import com.moneymate.app.ui.viewmodel.BookAdjustmentViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.moneymate.app.ui.viewmodel.BookAdjustmentViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.moneymate.app.ui.viewmodel.CollectionViewModel;
import com.moneymate.app.ui.viewmodel.CollectionViewModel_HiltModules;
import com.moneymate.app.ui.viewmodel.CollectionViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.moneymate.app.ui.viewmodel.CollectionViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.moneymate.app.ui.viewmodel.EditRequestViewModel;
import com.moneymate.app.ui.viewmodel.EditRequestViewModel_HiltModules;
import com.moneymate.app.ui.viewmodel.EditRequestViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.moneymate.app.ui.viewmodel.EditRequestViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.moneymate.app.ui.viewmodel.ExpenseViewModel;
import com.moneymate.app.ui.viewmodel.ExpenseViewModel_HiltModules;
import com.moneymate.app.ui.viewmodel.ExpenseViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.moneymate.app.ui.viewmodel.ExpenseViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.moneymate.app.ui.viewmodel.ExportViewModel;
import com.moneymate.app.ui.viewmodel.ExportViewModel_HiltModules;
import com.moneymate.app.ui.viewmodel.ExportViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.moneymate.app.ui.viewmodel.ExportViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.moneymate.app.ui.viewmodel.FileInsightsViewModel;
import com.moneymate.app.ui.viewmodel.FileInsightsViewModel_HiltModules;
import com.moneymate.app.ui.viewmodel.FileInsightsViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.moneymate.app.ui.viewmodel.FileInsightsViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.moneymate.app.ui.viewmodel.InvestmentViewModel;
import com.moneymate.app.ui.viewmodel.InvestmentViewModel_HiltModules;
import com.moneymate.app.ui.viewmodel.InvestmentViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.moneymate.app.ui.viewmodel.InvestmentViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.moneymate.app.ui.viewmodel.LicenseViewModel;
import com.moneymate.app.ui.viewmodel.LicenseViewModel_HiltModules;
import com.moneymate.app.ui.viewmodel.LicenseViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.moneymate.app.ui.viewmodel.LicenseViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.moneymate.app.ui.viewmodel.LoanFileViewModel;
import com.moneymate.app.ui.viewmodel.LoanFileViewModel_HiltModules;
import com.moneymate.app.ui.viewmodel.LoanFileViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.moneymate.app.ui.viewmodel.LoanFileViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.moneymate.app.ui.viewmodel.MigrationViewModel;
import com.moneymate.app.ui.viewmodel.MigrationViewModel_HiltModules;
import com.moneymate.app.ui.viewmodel.MigrationViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.moneymate.app.ui.viewmodel.MigrationViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.moneymate.app.ui.viewmodel.PaymentViewModel;
import com.moneymate.app.ui.viewmodel.PaymentViewModel_HiltModules;
import com.moneymate.app.ui.viewmodel.PaymentViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.moneymate.app.ui.viewmodel.PaymentViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.moneymate.app.ui.viewmodel.PersonViewModel;
import com.moneymate.app.ui.viewmodel.PersonViewModel_HiltModules;
import com.moneymate.app.ui.viewmodel.PersonViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.moneymate.app.ui.viewmodel.PersonViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.moneymate.app.ui.viewmodel.ReportViewModel;
import com.moneymate.app.ui.viewmodel.ReportViewModel_HiltModules;
import com.moneymate.app.ui.viewmodel.ReportViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.moneymate.app.ui.viewmodel.ReportViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.moneymate.app.ui.viewmodel.RestoreViewModel;
import com.moneymate.app.ui.viewmodel.RestoreViewModel_HiltModules;
import com.moneymate.app.ui.viewmodel.RestoreViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.moneymate.app.ui.viewmodel.RestoreViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.moneymate.app.ui.viewmodel.SessionViewModel;
import com.moneymate.app.ui.viewmodel.SessionViewModel_HiltModules;
import com.moneymate.app.ui.viewmodel.SessionViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.moneymate.app.ui.viewmodel.SessionViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.moneymate.app.ui.viewmodel.SettingsViewModel;
import com.moneymate.app.ui.viewmodel.SettingsViewModel_HiltModules;
import com.moneymate.app.ui.viewmodel.SettingsViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.moneymate.app.ui.viewmodel.SettingsViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.moneymate.app.ui.viewmodel.TemplateViewModel;
import com.moneymate.app.ui.viewmodel.TemplateViewModel_HiltModules;
import com.moneymate.app.ui.viewmodel.TemplateViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.moneymate.app.ui.viewmodel.TemplateViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.moneymate.app.ui.viewmodel.UpdateViewModel;
import com.moneymate.app.ui.viewmodel.UpdateViewModel_HiltModules;
import com.moneymate.app.ui.viewmodel.UpdateViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.moneymate.app.ui.viewmodel.UpdateViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.moneymate.app.ui.viewmodel.UploadViewModel;
import com.moneymate.app.ui.viewmodel.UploadViewModel_HiltModules;
import com.moneymate.app.ui.viewmodel.UploadViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.moneymate.app.ui.viewmodel.UploadViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.moneymate.app.ui.viewmodel.UserViewModel;
import com.moneymate.app.ui.viewmodel.UserViewModel_HiltModules;
import com.moneymate.app.ui.viewmodel.UserViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.moneymate.app.ui.viewmodel.UserViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.moneymate.app.utils.AppPreferences;
import com.moneymate.app.utils.FirestorePathProvider;
import dagger.hilt.android.ActivityRetainedLifecycle;
import dagger.hilt.android.ViewModelLifecycle;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.internal.builders.FragmentComponentBuilder;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.android.internal.builders.ViewComponentBuilder;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.hilt.android.internal.builders.ViewWithFragmentComponentBuilder;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories_InternalFactoryFactory_Factory;
import dagger.hilt.android.internal.managers.ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory;
import dagger.hilt.android.internal.managers.SavedStateHandleHolder;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideContextFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.LazyClassKeyMap;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.SingleCheck;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

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
public final class DaggerMoneyMateApplication_HiltComponents_SingletonC {
  private DaggerMoneyMateApplication_HiltComponents_SingletonC() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private ApplicationContextModule applicationContextModule;

    private Builder() {
    }

    public Builder applicationContextModule(ApplicationContextModule applicationContextModule) {
      this.applicationContextModule = Preconditions.checkNotNull(applicationContextModule);
      return this;
    }

    public MoneyMateApplication_HiltComponents.SingletonC build() {
      Preconditions.checkBuilderRequirement(applicationContextModule, ApplicationContextModule.class);
      return new SingletonCImpl(applicationContextModule);
    }
  }

  private static final class ActivityRetainedCBuilder implements MoneyMateApplication_HiltComponents.ActivityRetainedC.Builder {
    private final SingletonCImpl singletonCImpl;

    private SavedStateHandleHolder savedStateHandleHolder;

    private ActivityRetainedCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ActivityRetainedCBuilder savedStateHandleHolder(
        SavedStateHandleHolder savedStateHandleHolder) {
      this.savedStateHandleHolder = Preconditions.checkNotNull(savedStateHandleHolder);
      return this;
    }

    @Override
    public MoneyMateApplication_HiltComponents.ActivityRetainedC build() {
      Preconditions.checkBuilderRequirement(savedStateHandleHolder, SavedStateHandleHolder.class);
      return new ActivityRetainedCImpl(singletonCImpl, savedStateHandleHolder);
    }
  }

  private static final class ActivityCBuilder implements MoneyMateApplication_HiltComponents.ActivityC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private Activity activity;

    private ActivityCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ActivityCBuilder activity(Activity activity) {
      this.activity = Preconditions.checkNotNull(activity);
      return this;
    }

    @Override
    public MoneyMateApplication_HiltComponents.ActivityC build() {
      Preconditions.checkBuilderRequirement(activity, Activity.class);
      return new ActivityCImpl(singletonCImpl, activityRetainedCImpl, activity);
    }
  }

  private static final class FragmentCBuilder implements MoneyMateApplication_HiltComponents.FragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private Fragment fragment;

    private FragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public FragmentCBuilder fragment(Fragment fragment) {
      this.fragment = Preconditions.checkNotNull(fragment);
      return this;
    }

    @Override
    public MoneyMateApplication_HiltComponents.FragmentC build() {
      Preconditions.checkBuilderRequirement(fragment, Fragment.class);
      return new FragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragment);
    }
  }

  private static final class ViewWithFragmentCBuilder implements MoneyMateApplication_HiltComponents.ViewWithFragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private View view;

    private ViewWithFragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;
    }

    @Override
    public ViewWithFragmentCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public MoneyMateApplication_HiltComponents.ViewWithFragmentC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewWithFragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl, view);
    }
  }

  private static final class ViewCBuilder implements MoneyMateApplication_HiltComponents.ViewC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private View view;

    private ViewCBuilder(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public ViewCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public MoneyMateApplication_HiltComponents.ViewC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, view);
    }
  }

  private static final class ViewModelCBuilder implements MoneyMateApplication_HiltComponents.ViewModelC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private SavedStateHandle savedStateHandle;

    private ViewModelLifecycle viewModelLifecycle;

    private ViewModelCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ViewModelCBuilder savedStateHandle(SavedStateHandle handle) {
      this.savedStateHandle = Preconditions.checkNotNull(handle);
      return this;
    }

    @Override
    public ViewModelCBuilder viewModelLifecycle(ViewModelLifecycle viewModelLifecycle) {
      this.viewModelLifecycle = Preconditions.checkNotNull(viewModelLifecycle);
      return this;
    }

    @Override
    public MoneyMateApplication_HiltComponents.ViewModelC build() {
      Preconditions.checkBuilderRequirement(savedStateHandle, SavedStateHandle.class);
      Preconditions.checkBuilderRequirement(viewModelLifecycle, ViewModelLifecycle.class);
      return new ViewModelCImpl(singletonCImpl, activityRetainedCImpl, savedStateHandle, viewModelLifecycle);
    }
  }

  private static final class ServiceCBuilder implements MoneyMateApplication_HiltComponents.ServiceC.Builder {
    private final SingletonCImpl singletonCImpl;

    private Service service;

    private ServiceCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ServiceCBuilder service(Service service) {
      this.service = Preconditions.checkNotNull(service);
      return this;
    }

    @Override
    public MoneyMateApplication_HiltComponents.ServiceC build() {
      Preconditions.checkBuilderRequirement(service, Service.class);
      return new ServiceCImpl(singletonCImpl, service);
    }
  }

  private static final class ViewWithFragmentCImpl extends MoneyMateApplication_HiltComponents.ViewWithFragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private final ViewWithFragmentCImpl viewWithFragmentCImpl = this;

    private ViewWithFragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;


    }
  }

  private static final class FragmentCImpl extends MoneyMateApplication_HiltComponents.FragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl = this;

    private FragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        Fragment fragmentParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return activityCImpl.getHiltInternalFactoryFactory();
    }

    @Override
    public ViewWithFragmentComponentBuilder viewWithFragmentComponentBuilder() {
      return new ViewWithFragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl);
    }
  }

  private static final class ViewCImpl extends MoneyMateApplication_HiltComponents.ViewC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final ViewCImpl viewCImpl = this;

    private ViewCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }
  }

  private static final class ActivityCImpl extends MoneyMateApplication_HiltComponents.ActivityC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl = this;

    private ActivityCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, Activity activityParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;


    }

    @Override
    public void injectMainActivity(MainActivity mainActivity) {
      injectMainActivity2(mainActivity);
    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return DefaultViewModelFactories_InternalFactoryFactory_Factory.newInstance(getViewModelKeys(), new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl));
    }

    @Override
    public Map<Class<?>, Boolean> getViewModelKeys() {
      return LazyClassKeyMap.<Boolean>of(ImmutableMap.<String, Boolean>builderWithExpectedSize(21).put(AuthViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, AuthViewModel_HiltModules.KeyModule.provide()).put(BookAdjustmentViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, BookAdjustmentViewModel_HiltModules.KeyModule.provide()).put(CollectionViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, CollectionViewModel_HiltModules.KeyModule.provide()).put(EditRequestViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, EditRequestViewModel_HiltModules.KeyModule.provide()).put(ExpenseViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, ExpenseViewModel_HiltModules.KeyModule.provide()).put(ExportViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, ExportViewModel_HiltModules.KeyModule.provide()).put(FileInsightsViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, FileInsightsViewModel_HiltModules.KeyModule.provide()).put(InvestmentViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, InvestmentViewModel_HiltModules.KeyModule.provide()).put(LicenseViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, LicenseViewModel_HiltModules.KeyModule.provide()).put(LoanFileViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, LoanFileViewModel_HiltModules.KeyModule.provide()).put(MigrationViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, MigrationViewModel_HiltModules.KeyModule.provide()).put(PaymentViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, PaymentViewModel_HiltModules.KeyModule.provide()).put(PersonViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, PersonViewModel_HiltModules.KeyModule.provide()).put(ReportViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, ReportViewModel_HiltModules.KeyModule.provide()).put(RestoreViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, RestoreViewModel_HiltModules.KeyModule.provide()).put(SessionViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, SessionViewModel_HiltModules.KeyModule.provide()).put(SettingsViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, SettingsViewModel_HiltModules.KeyModule.provide()).put(TemplateViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, TemplateViewModel_HiltModules.KeyModule.provide()).put(UpdateViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, UpdateViewModel_HiltModules.KeyModule.provide()).put(UploadViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, UploadViewModel_HiltModules.KeyModule.provide()).put(UserViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, UserViewModel_HiltModules.KeyModule.provide()).build());
    }

    @Override
    public ViewModelComponentBuilder getViewModelComponentBuilder() {
      return new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public FragmentComponentBuilder fragmentComponentBuilder() {
      return new FragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public ViewComponentBuilder viewComponentBuilder() {
      return new ViewCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @CanIgnoreReturnValue
    private MainActivity injectMainActivity2(MainActivity instance) {
      MainActivity_MembersInjector.injectAppPreferences(instance, singletonCImpl.appPreferencesProvider.get());
      MainActivity_MembersInjector.injectWorkerScheduler(instance, singletonCImpl.workerSchedulerProvider.get());
      MainActivity_MembersInjector.injectAuditLogger(instance, singletonCImpl.auditLoggerProvider.get());
      return instance;
    }
  }

  private static final class ViewModelCImpl extends MoneyMateApplication_HiltComponents.ViewModelC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ViewModelCImpl viewModelCImpl = this;

    private Provider<AuthViewModel> authViewModelProvider;

    private Provider<BookAdjustmentViewModel> bookAdjustmentViewModelProvider;

    private Provider<CollectionViewModel> collectionViewModelProvider;

    private Provider<EditRequestViewModel> editRequestViewModelProvider;

    private Provider<ExpenseViewModel> expenseViewModelProvider;

    private Provider<ExportViewModel> exportViewModelProvider;

    private Provider<FileInsightsViewModel> fileInsightsViewModelProvider;

    private Provider<InvestmentViewModel> investmentViewModelProvider;

    private Provider<LicenseViewModel> licenseViewModelProvider;

    private Provider<LoanFileViewModel> loanFileViewModelProvider;

    private Provider<MigrationViewModel> migrationViewModelProvider;

    private Provider<PaymentViewModel> paymentViewModelProvider;

    private Provider<PersonViewModel> personViewModelProvider;

    private Provider<ReportViewModel> reportViewModelProvider;

    private Provider<RestoreViewModel> restoreViewModelProvider;

    private Provider<SessionViewModel> sessionViewModelProvider;

    private Provider<SettingsViewModel> settingsViewModelProvider;

    private Provider<TemplateViewModel> templateViewModelProvider;

    private Provider<UpdateViewModel> updateViewModelProvider;

    private Provider<UploadViewModel> uploadViewModelProvider;

    private Provider<UserViewModel> userViewModelProvider;

    private ViewModelCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, SavedStateHandle savedStateHandleParam,
        ViewModelLifecycle viewModelLifecycleParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;

      initialize(savedStateHandleParam, viewModelLifecycleParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandle savedStateHandleParam,
        final ViewModelLifecycle viewModelLifecycleParam) {
      this.authViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 0);
      this.bookAdjustmentViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 1);
      this.collectionViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 2);
      this.editRequestViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 3);
      this.expenseViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 4);
      this.exportViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 5);
      this.fileInsightsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 6);
      this.investmentViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 7);
      this.licenseViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 8);
      this.loanFileViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 9);
      this.migrationViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 10);
      this.paymentViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 11);
      this.personViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 12);
      this.reportViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 13);
      this.restoreViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 14);
      this.sessionViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 15);
      this.settingsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 16);
      this.templateViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 17);
      this.updateViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 18);
      this.uploadViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 19);
      this.userViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 20);
    }

    @Override
    public Map<Class<?>, javax.inject.Provider<ViewModel>> getHiltViewModelMap() {
      return LazyClassKeyMap.<javax.inject.Provider<ViewModel>>of(ImmutableMap.<String, javax.inject.Provider<ViewModel>>builderWithExpectedSize(21).put(AuthViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) authViewModelProvider)).put(BookAdjustmentViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) bookAdjustmentViewModelProvider)).put(CollectionViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) collectionViewModelProvider)).put(EditRequestViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) editRequestViewModelProvider)).put(ExpenseViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) expenseViewModelProvider)).put(ExportViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) exportViewModelProvider)).put(FileInsightsViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) fileInsightsViewModelProvider)).put(InvestmentViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) investmentViewModelProvider)).put(LicenseViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) licenseViewModelProvider)).put(LoanFileViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) loanFileViewModelProvider)).put(MigrationViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) migrationViewModelProvider)).put(PaymentViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) paymentViewModelProvider)).put(PersonViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) personViewModelProvider)).put(ReportViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) reportViewModelProvider)).put(RestoreViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) restoreViewModelProvider)).put(SessionViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) sessionViewModelProvider)).put(SettingsViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) settingsViewModelProvider)).put(TemplateViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) templateViewModelProvider)).put(UpdateViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) updateViewModelProvider)).put(UploadViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) uploadViewModelProvider)).put(UserViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) userViewModelProvider)).build());
    }

    @Override
    public Map<Class<?>, Object> getHiltViewModelAssistedMap() {
      return ImmutableMap.<Class<?>, Object>of();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final ViewModelCImpl viewModelCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          ViewModelCImpl viewModelCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.viewModelCImpl = viewModelCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.moneymate.app.ui.viewmodel.AuthViewModel 
          return (T) new AuthViewModel(singletonCImpl.appPreferencesProvider.get());

          case 1: // com.moneymate.app.ui.viewmodel.BookAdjustmentViewModel 
          return (T) new BookAdjustmentViewModel(singletonCImpl.bookAdjustmentRepositoryProvider.get());

          case 2: // com.moneymate.app.ui.viewmodel.CollectionViewModel 
          return (T) new CollectionViewModel(singletonCImpl.personRepositoryProvider.get(), singletonCImpl.paymentRepositoryProvider.get(), singletonCImpl.provideFirestorePathProvider.get());

          case 3: // com.moneymate.app.ui.viewmodel.EditRequestViewModel 
          return (T) new EditRequestViewModel(singletonCImpl.editRequestRepositoryProvider.get());

          case 4: // com.moneymate.app.ui.viewmodel.ExpenseViewModel 
          return (T) new ExpenseViewModel(singletonCImpl.expenseRepositoryProvider.get());

          case 5: // com.moneymate.app.ui.viewmodel.ExportViewModel 
          return (T) new ExportViewModel(singletonCImpl.exportManagerProvider.get(), ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 6: // com.moneymate.app.ui.viewmodel.FileInsightsViewModel 
          return (T) new FileInsightsViewModel(singletonCImpl.personRepositoryProvider.get(), singletonCImpl.paymentRepositoryProvider.get());

          case 7: // com.moneymate.app.ui.viewmodel.InvestmentViewModel 
          return (T) new InvestmentViewModel(singletonCImpl.investmentRepositoryProvider.get());

          case 8: // com.moneymate.app.ui.viewmodel.LicenseViewModel 
          return (T) new LicenseViewModel(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.appPreferencesProvider.get());

          case 9: // com.moneymate.app.ui.viewmodel.LoanFileViewModel 
          return (T) new LoanFileViewModel(singletonCImpl.loanFileRepositoryProvider.get(), singletonCImpl.provideMaintenanceRepositoryProvider.get(), singletonCImpl.auditLoggerProvider.get());

          case 10: // com.moneymate.app.ui.viewmodel.MigrationViewModel 
          return (T) new MigrationViewModel(singletonCImpl.appPreferencesProvider.get(), singletonCImpl.provideFirestorePathProvider.get());

          case 11: // com.moneymate.app.ui.viewmodel.PaymentViewModel 
          return (T) new PaymentViewModel(singletonCImpl.paymentRepositoryProvider.get(), singletonCImpl.personRepositoryProvider.get(), singletonCImpl.notificationHelperProvider.get(), singletonCImpl.appPreferencesProvider.get(), singletonCImpl.auditLoggerProvider.get(), ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 12: // com.moneymate.app.ui.viewmodel.PersonViewModel 
          return (T) new PersonViewModel(singletonCImpl.personRepositoryProvider.get(), singletonCImpl.auditLoggerProvider.get(), singletonCImpl.sessionManagerProvider.get());

          case 13: // com.moneymate.app.ui.viewmodel.ReportViewModel 
          return (T) new ReportViewModel(singletonCImpl.paymentRepositoryProvider.get(), singletonCImpl.personRepositoryProvider.get(), singletonCImpl.expenseRepositoryProvider.get(), singletonCImpl.investmentRepositoryProvider.get());

          case 14: // com.moneymate.app.ui.viewmodel.RestoreViewModel 
          return (T) new RestoreViewModel(singletonCImpl.loanFileRepositoryProvider.get(), singletonCImpl.personRepositoryProvider.get(), singletonCImpl.paymentRepositoryProvider.get(), singletonCImpl.expenseRepositoryProvider.get(), singletonCImpl.investmentRepositoryProvider.get(), singletonCImpl.provideFirestorePathProvider.get());

          case 15: // com.moneymate.app.ui.viewmodel.SessionViewModel 
          return (T) new SessionViewModel(singletonCImpl.sessionManagerProvider.get());

          case 16: // com.moneymate.app.ui.viewmodel.SettingsViewModel 
          return (T) new SettingsViewModel(singletonCImpl.appPreferencesProvider.get(), singletonCImpl.workerSchedulerProvider.get());

          case 17: // com.moneymate.app.ui.viewmodel.TemplateViewModel 
          return (T) new TemplateViewModel(singletonCImpl.provideDefaultPersonRepositoryProvider.get());

          case 18: // com.moneymate.app.ui.viewmodel.UpdateViewModel 
          return (T) new UpdateViewModel(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 19: // com.moneymate.app.ui.viewmodel.UploadViewModel 
          return (T) new UploadViewModel(singletonCImpl.loanFileRepositoryProvider.get(), singletonCImpl.personRepositoryProvider.get(), singletonCImpl.paymentRepositoryProvider.get(), singletonCImpl.expenseRepositoryProvider.get(), singletonCImpl.investmentRepositoryProvider.get(), singletonCImpl.provideFirestorePathProvider.get());

          case 20: // com.moneymate.app.ui.viewmodel.UserViewModel 
          return (T) new UserViewModel(singletonCImpl.appUserDao(), singletonCImpl.auditLoggerProvider.get(), singletonCImpl.appPreferencesProvider.get());

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ActivityRetainedCImpl extends MoneyMateApplication_HiltComponents.ActivityRetainedC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl = this;

    private Provider<ActivityRetainedLifecycle> provideActivityRetainedLifecycleProvider;

    private ActivityRetainedCImpl(SingletonCImpl singletonCImpl,
        SavedStateHandleHolder savedStateHandleHolderParam) {
      this.singletonCImpl = singletonCImpl;

      initialize(savedStateHandleHolderParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandleHolder savedStateHandleHolderParam) {
      this.provideActivityRetainedLifecycleProvider = DoubleCheck.provider(new SwitchingProvider<ActivityRetainedLifecycle>(singletonCImpl, activityRetainedCImpl, 0));
    }

    @Override
    public ActivityComponentBuilder activityComponentBuilder() {
      return new ActivityCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public ActivityRetainedLifecycle getActivityRetainedLifecycle() {
      return provideActivityRetainedLifecycleProvider.get();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // dagger.hilt.android.ActivityRetainedLifecycle 
          return (T) ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory.provideActivityRetainedLifecycle();

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ServiceCImpl extends MoneyMateApplication_HiltComponents.ServiceC {
    private final SingletonCImpl singletonCImpl;

    private final ServiceCImpl serviceCImpl = this;

    private ServiceCImpl(SingletonCImpl singletonCImpl, Service serviceParam) {
      this.singletonCImpl = singletonCImpl;


    }
  }

  private static final class SingletonCImpl extends MoneyMateApplication_HiltComponents.SingletonC {
    private final ApplicationContextModule applicationContextModule;

    private final SingletonCImpl singletonCImpl = this;

    private Provider<ExportManager> exportManagerProvider;

    private Provider<AppDatabase> provideDatabaseProvider;

    private Provider<NotificationHelper> notificationHelperProvider;

    private Provider<AppPreferences> appPreferencesProvider;

    private Provider<AboutToCloseWorker_AssistedFactory> aboutToCloseWorker_AssistedFactoryProvider;

    private Provider<BadLoanAlertWorker_AssistedFactory> badLoanAlertWorker_AssistedFactoryProvider;

    private Provider<DailyCollectionReminderWorker_AssistedFactory> dailyCollectionReminderWorker_AssistedFactoryProvider;

    private Provider<LoanReminderWorker_AssistedFactory> loanReminderWorker_AssistedFactoryProvider;

    private Provider<NotificationChannelManager> notificationChannelManagerProvider;

    private Provider<AreaRepository> areaRepositoryProvider;

    private Provider<FirestorePathProvider> provideFirestorePathProvider;

    private Provider<PersonRepository> personRepositoryProvider;

    private Provider<WorkerScheduler> workerSchedulerProvider;

    private Provider<SessionManager> sessionManagerProvider;

    private Provider<AuditLogger> auditLoggerProvider;

    private Provider<BookAdjustmentRepository> bookAdjustmentRepositoryProvider;

    private Provider<PaymentRepository> paymentRepositoryProvider;

    private Provider<EditRequestRepository> editRequestRepositoryProvider;

    private Provider<ExpenseRepository> expenseRepositoryProvider;

    private Provider<InvestmentRepository> investmentRepositoryProvider;

    private Provider<LoanFileRepository> loanFileRepositoryProvider;

    private Provider<MaintenanceRepository> provideMaintenanceRepositoryProvider;

    private Provider<DefaultPersonRepository> provideDefaultPersonRepositoryProvider;

    private SingletonCImpl(ApplicationContextModule applicationContextModuleParam) {
      this.applicationContextModule = applicationContextModuleParam;
      initialize(applicationContextModuleParam);

    }

    private PersonDao personDao() {
      return AppModule_ProvidePersonDaoFactory.providePersonDao(provideDatabaseProvider.get());
    }

    private PaymentDao paymentDao() {
      return AppModule_ProvidePaymentDaoFactory.providePaymentDao(provideDatabaseProvider.get());
    }

    private Map<String, javax.inject.Provider<WorkerAssistedFactory<? extends ListenableWorker>>> mapOfStringAndProviderOfWorkerAssistedFactoryOf(
        ) {
      return ImmutableMap.<String, javax.inject.Provider<WorkerAssistedFactory<? extends ListenableWorker>>>of("com.moneymate.app.notifications.workers.AboutToCloseWorker", ((Provider) aboutToCloseWorker_AssistedFactoryProvider), "com.moneymate.app.notifications.workers.BadLoanAlertWorker", ((Provider) badLoanAlertWorker_AssistedFactoryProvider), "com.moneymate.app.notifications.workers.DailyCollectionReminderWorker", ((Provider) dailyCollectionReminderWorker_AssistedFactoryProvider), "com.moneymate.app.notifications.workers.LoanReminderWorker", ((Provider) loanReminderWorker_AssistedFactoryProvider));
    }

    private HiltWorkerFactory hiltWorkerFactory() {
      return WorkerFactoryModule_ProvideFactoryFactory.provideFactory(mapOfStringAndProviderOfWorkerAssistedFactoryOf());
    }

    private AreaDao areaDao() {
      return AppModule_ProvideAreaDaoFactory.provideAreaDao(provideDatabaseProvider.get());
    }

    private BookAdjustmentDao bookAdjustmentDao() {
      return AppModule_ProvideBookAdjustmentDaoFactory.provideBookAdjustmentDao(provideDatabaseProvider.get());
    }

    private EditRequestDao editRequestDao() {
      return AppModule_ProvideEditRequestDaoFactory.provideEditRequestDao(provideDatabaseProvider.get());
    }

    private ExpenseDao expenseDao() {
      return AppModule_ProvideExpenseDaoFactory.provideExpenseDao(provideDatabaseProvider.get());
    }

    private ExpenseCategoryDao expenseCategoryDao() {
      return AppModule_ProvideExpenseCategoryDaoFactory.provideExpenseCategoryDao(provideDatabaseProvider.get());
    }

    private InvestmentDao investmentDao() {
      return AppModule_ProvideInvestmentDaoFactory.provideInvestmentDao(provideDatabaseProvider.get());
    }

    private InvestmentTypeDao investmentTypeDao() {
      return AppModule_ProvideInvestmentTypeDaoFactory.provideInvestmentTypeDao(provideDatabaseProvider.get());
    }

    private FileDao fileDao() {
      return AppModule_ProvideFileDaoFactory.provideFileDao(provideDatabaseProvider.get());
    }

    private DefaultPersonDao defaultPersonDao() {
      return AppModule_ProvideDefaultPersonDaoFactory.provideDefaultPersonDao(provideDatabaseProvider.get());
    }

    @SuppressWarnings("unchecked")
    private void initialize(final ApplicationContextModule applicationContextModuleParam) {
      this.exportManagerProvider = DoubleCheck.provider(new SwitchingProvider<ExportManager>(singletonCImpl, 0));
      this.provideDatabaseProvider = DoubleCheck.provider(new SwitchingProvider<AppDatabase>(singletonCImpl, 2));
      this.notificationHelperProvider = DoubleCheck.provider(new SwitchingProvider<NotificationHelper>(singletonCImpl, 3));
      this.appPreferencesProvider = DoubleCheck.provider(new SwitchingProvider<AppPreferences>(singletonCImpl, 4));
      this.aboutToCloseWorker_AssistedFactoryProvider = SingleCheck.provider(new SwitchingProvider<AboutToCloseWorker_AssistedFactory>(singletonCImpl, 1));
      this.badLoanAlertWorker_AssistedFactoryProvider = SingleCheck.provider(new SwitchingProvider<BadLoanAlertWorker_AssistedFactory>(singletonCImpl, 5));
      this.dailyCollectionReminderWorker_AssistedFactoryProvider = SingleCheck.provider(new SwitchingProvider<DailyCollectionReminderWorker_AssistedFactory>(singletonCImpl, 6));
      this.loanReminderWorker_AssistedFactoryProvider = SingleCheck.provider(new SwitchingProvider<LoanReminderWorker_AssistedFactory>(singletonCImpl, 7));
      this.notificationChannelManagerProvider = DoubleCheck.provider(new SwitchingProvider<NotificationChannelManager>(singletonCImpl, 8));
      this.areaRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<AreaRepository>(singletonCImpl, 9));
      this.provideFirestorePathProvider = DoubleCheck.provider(new SwitchingProvider<FirestorePathProvider>(singletonCImpl, 11));
      this.personRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<PersonRepository>(singletonCImpl, 10));
      this.workerSchedulerProvider = DoubleCheck.provider(new SwitchingProvider<WorkerScheduler>(singletonCImpl, 12));
      this.sessionManagerProvider = DoubleCheck.provider(new SwitchingProvider<SessionManager>(singletonCImpl, 14));
      this.auditLoggerProvider = DoubleCheck.provider(new SwitchingProvider<AuditLogger>(singletonCImpl, 13));
      this.bookAdjustmentRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<BookAdjustmentRepository>(singletonCImpl, 15));
      this.paymentRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<PaymentRepository>(singletonCImpl, 16));
      this.editRequestRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<EditRequestRepository>(singletonCImpl, 17));
      this.expenseRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<ExpenseRepository>(singletonCImpl, 18));
      this.investmentRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<InvestmentRepository>(singletonCImpl, 19));
      this.loanFileRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<LoanFileRepository>(singletonCImpl, 20));
      this.provideMaintenanceRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<MaintenanceRepository>(singletonCImpl, 21));
      this.provideDefaultPersonRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<DefaultPersonRepository>(singletonCImpl, 22));
    }

    @Override
    public void injectMoneyMateApplication(MoneyMateApplication moneyMateApplication) {
      injectMoneyMateApplication2(moneyMateApplication);
    }

    @Override
    public AreaRepository areaRepository() {
      return areaRepositoryProvider.get();
    }

    @Override
    public PersonRepository personRepository() {
      return personRepositoryProvider.get();
    }

    @Override
    public AppUserDao appUserDao() {
      return AppModule_ProvideAppUserDaoFactory.provideAppUserDao(provideDatabaseProvider.get());
    }

    @Override
    public AuditLogDao auditLogDao() {
      return AppModule_ProvideAuditLogDaoFactory.provideAuditLogDao(provideDatabaseProvider.get());
    }

    @Override
    public Set<Boolean> getDisableFragmentGetContextFix() {
      return ImmutableSet.<Boolean>of();
    }

    @Override
    public ActivityRetainedComponentBuilder retainedComponentBuilder() {
      return new ActivityRetainedCBuilder(singletonCImpl);
    }

    @Override
    public ServiceComponentBuilder serviceComponentBuilder() {
      return new ServiceCBuilder(singletonCImpl);
    }

    @CanIgnoreReturnValue
    private MoneyMateApplication injectMoneyMateApplication2(MoneyMateApplication instance) {
      MoneyMateApplication_MembersInjector.injectExportManager(instance, exportManagerProvider.get());
      MoneyMateApplication_MembersInjector.injectWorkerFactory(instance, hiltWorkerFactory());
      MoneyMateApplication_MembersInjector.injectChannelManager(instance, notificationChannelManagerProvider.get());
      return instance;
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.moneymate.app.data.export.ExportManager 
          return (T) new ExportManager(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 1: // com.moneymate.app.notifications.workers.AboutToCloseWorker_AssistedFactory 
          return (T) new AboutToCloseWorker_AssistedFactory() {
            @Override
            public AboutToCloseWorker create(Context context, WorkerParameters params) {
              return new AboutToCloseWorker(context, params, singletonCImpl.personDao(), singletonCImpl.notificationHelperProvider.get(), singletonCImpl.appPreferencesProvider.get());
            }
          };

          case 2: // com.moneymate.app.data.local.AppDatabase 
          return (T) AppModule_ProvideDatabaseFactory.provideDatabase(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 3: // com.moneymate.app.notifications.NotificationHelper 
          return (T) new NotificationHelper(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 4: // com.moneymate.app.utils.AppPreferences 
          return (T) new AppPreferences(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 5: // com.moneymate.app.notifications.workers.BadLoanAlertWorker_AssistedFactory 
          return (T) new BadLoanAlertWorker_AssistedFactory() {
            @Override
            public BadLoanAlertWorker create(Context context2, WorkerParameters params2) {
              return new BadLoanAlertWorker(context2, params2, singletonCImpl.paymentDao(), singletonCImpl.notificationHelperProvider.get(), singletonCImpl.appPreferencesProvider.get());
            }
          };

          case 6: // com.moneymate.app.notifications.workers.DailyCollectionReminderWorker_AssistedFactory 
          return (T) new DailyCollectionReminderWorker_AssistedFactory() {
            @Override
            public DailyCollectionReminderWorker create(Context context3,
                WorkerParameters params3) {
              return new DailyCollectionReminderWorker(context3, params3, singletonCImpl.paymentDao(), singletonCImpl.notificationHelperProvider.get());
            }
          };

          case 7: // com.moneymate.app.notifications.workers.LoanReminderWorker_AssistedFactory 
          return (T) new LoanReminderWorker_AssistedFactory() {
            @Override
            public LoanReminderWorker create(Context context4, WorkerParameters params4) {
              return new LoanReminderWorker(context4, params4, singletonCImpl.notificationHelperProvider.get(), singletonCImpl.appPreferencesProvider.get());
            }
          };

          case 8: // com.moneymate.app.notifications.NotificationChannelManager 
          return (T) new NotificationChannelManager(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 9: // com.moneymate.app.data.repository.AreaRepository 
          return (T) new AreaRepository(singletonCImpl.areaDao());

          case 10: // com.moneymate.app.data.repository.PersonRepository 
          return (T) new PersonRepository(singletonCImpl.personDao(), singletonCImpl.provideFirestorePathProvider.get());

          case 11: // com.moneymate.app.utils.FirestorePathProvider 
          return (T) AppModule_ProvideFirestorePathProviderFactory.provideFirestorePathProvider(singletonCImpl.appPreferencesProvider.get());

          case 12: // com.moneymate.app.notifications.WorkerScheduler 
          return (T) new WorkerScheduler(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.appPreferencesProvider.get());

          case 13: // com.moneymate.app.auth.AuditLogger 
          return (T) new AuditLogger(singletonCImpl.auditLogDao(), singletonCImpl.sessionManagerProvider.get());

          case 14: // com.moneymate.app.auth.SessionManager 
          return (T) new SessionManager(singletonCImpl.appPreferencesProvider.get(), singletonCImpl.appUserDao(), singletonCImpl.auditLogDao());

          case 15: // com.moneymate.app.data.repository.BookAdjustmentRepository 
          return (T) new BookAdjustmentRepository(singletonCImpl.bookAdjustmentDao());

          case 16: // com.moneymate.app.data.repository.PaymentRepository 
          return (T) new PaymentRepository(singletonCImpl.paymentDao(), singletonCImpl.personDao(), singletonCImpl.provideFirestorePathProvider.get());

          case 17: // com.moneymate.app.data.repository.EditRequestRepository 
          return (T) new EditRequestRepository(singletonCImpl.editRequestDao());

          case 18: // com.moneymate.app.data.repository.ExpenseRepository 
          return (T) new ExpenseRepository(singletonCImpl.expenseDao(), singletonCImpl.expenseCategoryDao());

          case 19: // com.moneymate.app.data.repository.InvestmentRepository 
          return (T) new InvestmentRepository(singletonCImpl.investmentDao(), singletonCImpl.investmentTypeDao());

          case 20: // com.moneymate.app.data.repository.LoanFileRepository 
          return (T) new LoanFileRepository(singletonCImpl.fileDao(), singletonCImpl.provideFirestorePathProvider.get());

          case 21: // com.moneymate.app.data.repository.MaintenanceRepository 
          return (T) AppModule_ProvideMaintenanceRepositoryFactory.provideMaintenanceRepository(singletonCImpl.fileDao(), singletonCImpl.personDao(), singletonCImpl.paymentDao(), singletonCImpl.provideFirestorePathProvider.get());

          case 22: // com.moneymate.app.data.repository.DefaultPersonRepository 
          return (T) AppModule_ProvideDefaultPersonRepositoryFactory.provideDefaultPersonRepository(singletonCImpl.defaultPersonDao());

          default: throw new AssertionError(id);
        }
      }
    }
  }
}
