package one.mixin.android.di.worker

import androidx.work.Worker
import dagger.Binds
import dagger.Module
import dagger.android.AndroidInjector
import dagger.multibindings.IntoMap
import one.mixin.android.work.RefreshAccountWorker
import one.mixin.android.work.RefreshAssetsWorker
import one.mixin.android.work.RefreshContactWorker
import one.mixin.android.work.RefreshFcmWorker

@Module(
    subcomponents = [RefreshAccountWorkerSubcomponent::class,
        RefreshContactWorkerSubcomponent::class,
        RefreshAssetsWorkerSubcomponent::class,
        RefreshFcmWorkerSubcomponent::class]
)
abstract class WorkerModule {
    @Binds
    @IntoMap
    @WorkerKey(RefreshAccountWorker::class)
    abstract fun bindRefreshAccountWorker(builder: RefreshAccountWorkerSubcomponent.Builder): AndroidInjector.Factory<out Worker>

    @Binds
    @IntoMap
    @WorkerKey(RefreshContactWorker::class)
    abstract fun bindRefreshContactWorker(builder: RefreshContactWorkerSubcomponent.Builder): AndroidInjector.Factory<out Worker>

    @Binds
    @IntoMap
    @WorkerKey(RefreshAssetsWorker::class)
    abstract fun bindRefreshAssertsWorker(builder: RefreshAssetsWorkerSubcomponent.Builder): AndroidInjector.Factory<out Worker>

    @Binds
    @IntoMap
    @WorkerKey(RefreshFcmWorker::class)
    abstract fun bindRefreshFcmsWorker(builder: RefreshFcmWorkerSubcomponent.Builder): AndroidInjector.Factory<out Worker>
}