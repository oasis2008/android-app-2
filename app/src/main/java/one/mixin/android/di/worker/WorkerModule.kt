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
import one.mixin.android.work.RefreshSnapshotsWorker
import one.mixin.android.work.RefreshStickerAlbumWorker
import one.mixin.android.work.RefreshUserSnapshotsWorker

@Module(
    subcomponents = [RefreshAccountWorkerSubcomponent::class,
        RefreshContactWorkerSubcomponent::class,
        RefreshAssetsWorkerSubcomponent::class,
        RefreshFcmWorkerSubcomponent::class,
        RefreshStickerAlbumWorkerSubcomponent::class,
        RefreshSnapshotsWorkerSubcomponent::class,
        RefreshUserSnapshotsWorkerSubcomponent::class]
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

    @Binds
    @IntoMap
    @WorkerKey(RefreshStickerAlbumWorker::class)
    abstract fun bindRefreshStickerAlbumWorker(builder: RefreshStickerAlbumWorkerSubcomponent.Builder): AndroidInjector.Factory<out Worker>

    @Binds
    @IntoMap
    @WorkerKey(RefreshSnapshotsWorker::class)
    abstract fun bindRefreshSnapshotsWorker(builder: RefreshSnapshotsWorkerSubcomponent.Builder): AndroidInjector.Factory<out Worker>

    @Binds
    @IntoMap
    @WorkerKey(RefreshUserSnapshotsWorker::class)
    abstract fun bindRefreshUserSnapshotsWorker(builder: RefreshUserSnapshotsWorkerSubcomponent.Builder): AndroidInjector.Factory<out Worker>
}