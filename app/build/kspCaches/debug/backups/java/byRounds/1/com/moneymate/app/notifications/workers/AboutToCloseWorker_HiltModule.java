package com.moneymate.app.notifications.workers;

import androidx.hilt.work.WorkerAssistedFactory;
import androidx.work.ListenableWorker;
import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.codegen.OriginatingElement;
import dagger.hilt.components.SingletonComponent;
import dagger.multibindings.IntoMap;
import dagger.multibindings.StringKey;
import javax.annotation.processing.Generated;

@Generated("androidx.hilt.AndroidXHiltProcessor")
@Module
@InstallIn(SingletonComponent.class)
@OriginatingElement(
    topLevelClass = AboutToCloseWorker.class
)
public interface AboutToCloseWorker_HiltModule {
  @Binds
  @IntoMap
  @StringKey("com.moneymate.app.notifications.workers.AboutToCloseWorker")
  WorkerAssistedFactory<? extends ListenableWorker> bind(
      AboutToCloseWorker_AssistedFactory factory);
}
