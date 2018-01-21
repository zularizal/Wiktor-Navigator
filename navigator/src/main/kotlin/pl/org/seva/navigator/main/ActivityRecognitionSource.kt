/*
 * Copyright (C) 2017 Wiktor Nizio
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.org.seva.navigator.main

import android.app.PendingIntent
import android.arch.lifecycle.Lifecycle
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle

import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.DetectedActivity

import io.reactivex.subjects.PublishSubject

fun activityRecognition() = instance<ActivityRecognitionSource>()

open class ActivityRecognitionSource :
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private var initialized = false
    private var stationary = false
    private var googleApiClient: GoogleApiClient? = null
    private lateinit var context: Context
    private var activityRecognitionReceiver : BroadcastReceiver? = null

    infix fun initGoogleApiClient(context: Context) {
        if (initialized) {
            return
        }
        this.context = context
        googleApiClient?:let {
            googleApiClient = GoogleApiClient.Builder(context)
                    .addApi(ActivityRecognition.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build()
            googleApiClient!!.connect()
        }

        initialized = true
    }

    override fun onConnected(bundle: Bundle?) {
        registerReceiver()
        val intent = Intent(ACTIVITY_RECOGNITION_INTENT)

        val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT)
        ActivityRecognition.getClient(context).requestActivityUpdates(
                ACTIVITY_RECOGNITION_INTERVAL_MS,
                pendingIntent)
    }

    override fun onConnectionSuspended(i: Int) = unregisterReceiver()

    private fun registerReceiver() {
        activityRecognitionReceiver = activityRecognitionReceiver?: ActivityRecognitionReceiver()
        context.registerReceiver(activityRecognitionReceiver, IntentFilter(ACTIVITY_RECOGNITION_INTENT))
    }

    private fun unregisterReceiver() {
        activityRecognitionReceiver?: return
        context.unregisterReceiver(activityRecognitionReceiver)
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) = Unit

    fun listen(lifecycle: Lifecycle, onStationary: () -> Unit, onMoving: () -> Unit) {
        if (stationary) {
            onStationary()
        } else {
            onMoving()
        }
        lifecycle.observe { stationarySubject.subscribe { onStationary() } }
        lifecycle.observe { movingSubject.subscribe { onMoving() } }
    }

    private fun onDeviceStationary() {
        stationary = true
        stationarySubject.onNext(0)
    }

    private fun onDeviceMoving() {
        stationary = false
        movingSubject.onNext(0)
    }

    private inner class ActivityRecognitionReceiver: BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            if (ActivityRecognitionResult.hasResult(intent)) {
                val result = ActivityRecognitionResult.extractResult(intent)
                if (result probably DetectedActivity.STILL) {
                    onDeviceStationary()
                } else {
                    onDeviceMoving()
                }
            }
        }

        private infix fun ActivityRecognitionResult.probably(activity: Int) =
                mostProbableActivity.type == activity &&
                        getActivityConfidence(activity) >= MIN_CONFIDENCE
    }

    companion object {

        private const val ACTIVITY_RECOGNITION_INTENT = "activity_recognition_intent"
        private const val ACTIVITY_RECOGNITION_INTERVAL_MS = 1000L
        /** The device is only stationary if confidence >= this level. */
        private const val MIN_CONFIDENCE = 80 // 70 does not work on LG G6 Android 7.0

        private val stationarySubject = PublishSubject.create<Any>()
        private val movingSubject = PublishSubject.create<Any>()
    }
}
