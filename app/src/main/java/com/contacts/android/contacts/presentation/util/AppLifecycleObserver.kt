package com.contacts.android.contacts.presentation.util

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.contacts.android.contacts.data.preferences.UserPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLifecycleObserver @Inject constructor(
    private val userPreferences: UserPreferences
) : DefaultLifecycleObserver {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        // App entered foreground (either from cold start or background)
        scope.launch {
            userPreferences.incrementAppOpenCount()
        }
    }
}
