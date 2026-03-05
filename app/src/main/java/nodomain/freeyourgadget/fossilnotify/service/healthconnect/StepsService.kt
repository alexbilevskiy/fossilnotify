package nodomain.freeyourgadget.fossilnotify.service.healthconnect

import android.content.Context
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.time.TimeRangeFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId

class StepsService(private val context: Context) {
    companion object {
        private const val TAG = "StepsService"
    }

    private val healthConnectClient = HealthConnectClient.getOrCreate(context)
    private val _stepsCount = MutableStateFlow(0)

    suspend fun fetchTodaySteps(): Int {
        return withContext(Dispatchers.IO) {
            try {
                val now = Instant.now()
                val startOfDay = now.atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant()

                val response = healthConnectClient.aggregate(
                    AggregateRequest(
                        metrics = setOf(StepsRecord.COUNT_TOTAL),
                        timeRangeFilter = TimeRangeFilter.between(startOfDay, now)
                    )
                )

                val totalSteps = response[StepsRecord.COUNT_TOTAL] ?: 0L
                _stepsCount.value = totalSteps.toInt()
                Log.d(TAG, "Today's steps: $totalSteps")
                totalSteps.toInt()
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching steps", e)
                0
            }
        }
    }
}