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

import android.app.Application
import android.content.Intent
import android.os.Build
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import pl.org.seva.navigator.contact.*
import pl.org.seva.navigator.contact.room.contactDao
import pl.org.seva.navigator.contact.room.contactsDatabase

import pl.org.seva.navigator.contact.room.insert
import pl.org.seva.navigator.debug.debug
import pl.org.seva.navigator.debug.isDebugMode
import pl.org.seva.navigator.navigation.NavigatorService
import pl.org.seva.navigator.profile.clearCurrentUser
import pl.org.seva.navigator.profile.isLoggedIn
import pl.org.seva.navigator.profile.setCurrent
import pl.org.seva.navigator.ui.notificationChannels

class Bootstrap(private val application: Application) {

    private var isServiceRunning = false

    fun boot() {
        FirebaseAuth.getInstance().currentUser?.setCurrent()
        instance<ActivityRecognitionSource>() initGoogleApiClient application
        with(contactsDatabase().contactDao) {
            contacts() addAll getAll().map { it.value() }
        }
        setDynamicShortcuts(application)
        if (isLoggedIn()) {
            addFriendshipListener()
            startNavigatorService()
        }
        notificationChannels().create()
    }

    fun login(user: FirebaseUser) {
        fun downloadFriendsFromCloud() =
                friendshipObservable().downloadFriendsFromCloud(
                        onFriendFound = {
                            addContact(it)
                            contactDao() insert it
                        }, onCompleted = { setDynamicShortcuts(application) })

        user.setCurrent()
        addFriendshipListener()
        downloadFriendsFromCloud()
        startNavigatorService()
        if (isDebugMode()) {
            debug().start()
        }
    }

    fun logout() {
        stopNavigatorService()
        cleanFriendshipListeners()
        contactDao().deleteAll()
        clearAllContacts()
        clearCurrentUser()
        setDynamicShortcuts(application)
    }


    fun startNavigatorService() {
        if (isServiceRunning) {
            return
        }
        startService(Intent(application.baseContext, NavigatorService::class.java))
        isServiceRunning = true
    }

    private fun startService(intent: Intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            application.startForegroundService(intent)
        } else {
            application.startService(intent)
        }
    }

    fun stopNavigatorService() {
        if (!isServiceRunning) {
            return
        }
        application.stopService(Intent(application.baseContext, NavigatorService::class.java))
        isServiceRunning = false
    }
}
