package org.allaboard.project.data.repository

import io.ktor.client.plugins.ClientRequestException
import org.allaboard.project.data.network.ApiClient
import org.allaboard.project.domain.Activity

/**
 * Real ActivityRepository implementation backed by your backend server.
 *
 * All requests automatically include the Supabase JWT (if logged in).
 */
class ActivityRepositoryImpl : ActivityRepository {

    override suspend fun getActivity(activityId: String): Activity? {
        return try {
            ApiClient.get<Activity>("/activities/$activityId")
        } catch (e: ClientRequestException) {
            if (e.response.status.value == 404) null else throw e
        }
    }

    override suspend fun getActivitiesForTrip(tripId: String): List<Activity> {
        return ApiClient.get<List<Activity>>("/trips/$tripId/activities")
    }

    override suspend fun addActivity(tripId: String, activity: Activity) {
        ApiClient.post<Activity, Activity>("/trips/$tripId/activities", activity)
    }

    override suspend fun updateActivity(activity: Activity) {
        ApiClient.patch<Activity, Activity>("/activities/${activity.id}", activity)
    }

    override suspend fun deleteActivity(activityId: String) {
        ApiClient.deleteNoBody("/activities/$activityId")
    }
}
