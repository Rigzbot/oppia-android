package org.oppia.android.domain.platformparameter.syncup

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.oppia.android.app.model.PlatformParameter
import org.oppia.android.app.utility.getVersionName
import org.oppia.android.data.backends.gae.api.PlatformParameterService
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.platformparameter.PlatformParameterController
import org.oppia.android.util.threading.BackgroundDispatcher
import javax.inject.Inject
import org.oppia.android.util.threading.BlockingDispatcher

class PlatformParameterSyncUpWorker private constructor(
  context: Context,
  params: WorkerParameters,
  private val platformParameterController: PlatformParameterController,
  private val platformParameterService: PlatformParameterService,
  private val oppiaLogger: OppiaLogger,
  private val backgroundDispatcher: CoroutineDispatcher
) : CoroutineWorker(context, params) {

  companion object {
    const val WORKER_TYPE_KEY = "worker_type_key"
    const val TAG = "PlatformParameterWorker.tag"
    const val PLATFORM_PARAMETER_WORKER = "platform_parameter_worker"
  }

  override suspend fun doWork(): Result {
    println("Worker - 0" + inputData.getString(WORKER_TYPE_KEY))
//    Log.d("PlatformParameter", "Worker - 0" + inputData.getString(WORKER_TYPE_KEY))
    return when (inputData.getString(WORKER_TYPE_KEY)) {
      PLATFORM_PARAMETER_WORKER -> withContext(backgroundDispatcher) {
        println("Worker - 1")
//        Log.d("PlatformParameter", "Worker - 1")
        refreshPlatformParameters() }
      else -> Result.failure()
    }
  }

  private fun parseNetworkResponse(response: Map<String, Any>): List<PlatformParameter> {
    val platformParameterList: MutableList<PlatformParameter> = mutableListOf()
    for (entry in response.entries) {
      val platformParameter = PlatformParameter.newBuilder().setName(entry.key)
      when (val value = entry.value) {
        is String -> platformParameter.string = value
        is Int -> platformParameter.integer = value
        is Boolean -> platformParameter.boolean = value
        else -> continue
      }
      platformParameterList.add(platformParameter.build())
    }
    return platformParameterList
  }

  private fun refreshPlatformParameters(): Result {
    println("Worker - 2")
//    Log.d("PlatformParameter", "Worker - 2")
    return try {
      println( "Worker - 3")
//      Log.d("PlatformParameter", "Worker - 3")
      val response = platformParameterService.getPlatformParametersByVersion(
        applicationContext.getVersionName()
      ).execute()
      println( "Worker - 4")
//      Log.d("PlatformParameter", "Worker - 4")
      val responseBody = checkNotNull(response.body())
      println( "Worker - 5")
//      Log.d("PlatformParameter", "Worker - 5")
      val platformParameterList = parseNetworkResponse(responseBody)
      println("Worker - 6")
//      Log.d("PlatformParameter", "Worker - 6")
      platformParameterController.updatePlatformParameterDatabase(platformParameterList)
      println( "Worker - 10")
//      Log.d("PlatformParameter", "Worker - 10")
      Result.success()
    } catch (e: Exception) {
      println("Worker - 11"+ e.message)
//      Log.d("PlatformParameter", "Worker - 11"+ e.message)
      oppiaLogger.e(TAG, "Failed to fetch the Platform Parameters", e)
      Result.failure()
    }
  }

  class Factory @Inject constructor(
    private val platformParameterController: PlatformParameterController,
    private val platformParameterService: PlatformParameterService,
    private val oppiaLogger: OppiaLogger,
    @BlockingDispatcher private val backgroundDispatcher: CoroutineDispatcher
  ) {
    fun create(context: Context, params: WorkerParameters): CoroutineWorker {
      return PlatformParameterSyncUpWorker(
        context,
        params,
        platformParameterController,
        platformParameterService,
        oppiaLogger,
        backgroundDispatcher
      )
    }
  }
}
