package one.mixin.android.worker

import android.content.Context
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import one.mixin.android.api.service.UserService
import one.mixin.android.extension.enqueueOneTimeRequest
import one.mixin.android.job.MixinJobManager
import one.mixin.android.repository.UserRepository
import javax.inject.Inject

class RefreshUserWorker(context: Context, parameters: WorkerParameters) : BaseWork(context, parameters) {

    companion object {
        const val USER_IDS = "user_ids"
        const val CONVERSATION_ID = "conversation_id"
    }

    @Inject
    lateinit var userService: UserService
    @Inject
    lateinit var userRepo: UserRepository
    @Inject
    lateinit var jobManager: MixinJobManager

    override fun onRun(): Result {
        val userIds = inputData.getStringArray(USER_IDS) ?: return Result.FAILURE
        val conversationId = inputData.getString(CONVERSATION_ID)
        val call = userService.getUsers(userIds.toList()).execute()
        val response = call.body()
        return if (response != null && response.isSuccess) {
            response.data?.let { data ->
                for (u in data) {
                    userRepo.upsert(u)
                }

                conversationId?.let {
                    WorkManager.getInstance().enqueueOneTimeRequest<GenerateAvatarWorker>(
                        workDataOf(GenerateAvatarWorker.GROUP_ID to conversationId))
                }
            }
            Result.SUCCESS
        } else {
            Result.FAILURE
        }
    }
}
