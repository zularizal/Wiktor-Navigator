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

package pl.org.seva.navigator.navigation

import android.arch.lifecycle.LifecycleService
import pl.org.seva.navigator.data.fb.FbWriter
import pl.org.seva.navigator.main.instance
import pl.org.seva.navigator.ui.createOngoingNotification

class NavigatorService : LifecycleService() {

    private val myLocationSource: MyLocationSource = instance()
    private val fbWriter: FbWriter = instance()

    override fun onStartCommand(intent: android.content.Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        myLocationSource initWithService this
        startForeground(ONGOING_NOTIFICATION_ID, createOngoingNotification())
        addMyLocationListener()

        return android.app.Service.START_STICKY
    }

    private fun addMyLocationListener() =
            myLocationSource.addLocationListener(lifecycle) { fbWriter writeMyLocation it }

    companion object {
        private const val ONGOING_NOTIFICATION_ID = 1
    }
}
