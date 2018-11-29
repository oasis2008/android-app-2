package one.mixin.android.di.worker

import dagger.Subcomponent
import dagger.android.AndroidInjector
import one.mixin.android.worker.RefreshStickerWorker

@Subcomponent
interface RefreshStickerWorkerSubcomponent : AndroidInjector<RefreshStickerWorker> {
    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<RefreshStickerWorker>()
}