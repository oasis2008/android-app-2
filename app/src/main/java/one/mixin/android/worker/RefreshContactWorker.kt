package one.mixin.android.worker

import android.content.Context
import androidx.work.WorkerParameters
import one.mixin.android.api.service.ContactService
import one.mixin.android.repository.UserRepository
import one.mixin.android.vo.User
import javax.inject.Inject

class RefreshContactWorker(context: Context, parameters: WorkerParameters) : BaseWork(context, parameters) {

    @Inject
    lateinit var contactService: ContactService
    @Inject
    lateinit var userRepo: UserRepository

    override fun onRun(): Result {
        val response = contactService.friends().execute().body()
        return if (response != null && response.isSuccess && response.data != null) {
            val users = response.data as List<User>
            users.forEach {
                if (it.app != null) {
                    it.appId = it.app!!.appId
                    userRepo.insertApp(it.app!!)
                }
                userRepo.upsert(it)
            }
            Result.SUCCESS
        } else {
            Result.FAILURE
        }
    }
}