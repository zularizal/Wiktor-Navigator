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

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

fun observe(lifecycle: Lifecycle, subscription: () -> Disposable) =
        lifecycle.addObserver(RxLifecycleObserver(subscription))

fun <T> Observable<T>.subscribeWithComposite(cd: CompositeDisposable, onNext: (T) -> Unit) {
    cd.add(subscribe(onNext))
}

private class RxLifecycleObserver(private val subscription: () -> Disposable) : LifecycleObserver {
    private lateinit var disposable: Disposable

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private fun onStart() { disposable = subscription() }
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private fun onStop() = disposable.dispose()
}
