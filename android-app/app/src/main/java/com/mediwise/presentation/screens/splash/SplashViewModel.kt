package com.mediwise.presentation.screens.splash

import androidx.lifecycle.ViewModel
import com.mediwise.core.datastore.SessionDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    sessionDataStore: SessionDataStore
) : ViewModel() {
    val isLoggedIn = sessionDataStore.isLoggedIn
}
