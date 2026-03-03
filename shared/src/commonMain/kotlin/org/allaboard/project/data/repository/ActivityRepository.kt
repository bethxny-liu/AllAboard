package org.allaboard.project.data.repository

import org.allaboard.project.domain.Activity

interface ActivityRepository {
    suspend fun getActivity(activityId: String): Activity?
    suspend fun getActivitiesForTrip(tripId: String): List<Activity>
    suspend fun addActivity(tripId: String, activity: Activity)
    suspend fun updateActivity(activity: Activity)
    suspend fun deleteActivity(activityId: String)
}
