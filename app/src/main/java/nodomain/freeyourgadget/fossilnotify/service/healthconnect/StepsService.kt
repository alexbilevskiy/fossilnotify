package nodomain.freeyourgadget.fossilnotify.service.healthconnect

import android.content.Context
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

class StepsService(private val context: Context) {
    companion object {
        private const val TAG = "StepsService"
    }

    private val healthConnectClient = HealthConnectClient.getOrCreate(context)
    private val _stepsCount = MutableStateFlow(0)
    val stepsCount: StateFlow<Int> = _stepsCount

    val permissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class)
    )

    suspend fun hasPermissions(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                healthConnectClient.permissionController.getGrantedPermissions().containsAll(permissions)
            } catch (e: Exception) {
                Log.e(TAG, "Error checking permissions", e)
                false
            }
        }
    }

    suspend fun fetchTodaySteps(): Int {
        return withContext(Dispatchers.IO) {
            try {
                val now = LocalDateTime.now()
                val startOfDay = now.toLocalDate().atStartOfDay().toInstant(ZoneOffset.UTC)
                val endOfDay = now.toInstant(ZoneOffset.UTC)

                val response = healthConnectClient.readRecords(
                    ReadRecordsRequest(
                        StepsRecord::class,
                        timeRangeFilter = TimeRangeFilter.between(startOfDay, endOfDay)
                    )
                )

                val totalSteps = response.records.sumOf { it.count }
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