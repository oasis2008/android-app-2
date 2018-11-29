package one.mixin.android.di.worker

import dagger.Subcomponent
import dagger.android.AndroidInjector
import one.mixin.android.worker.RefreshConversationWorker

@Subcomponent
interface RefreshConversationWorkerSubcomponent : AndroidInjector<RefreshConversationWorker> {
    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<RefreshConversationWorker>()
}