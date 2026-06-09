package com.moneymate.app.ui.viewmodel

import android.content.Context
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneymate.app.utils.AppConfig
import com.moneymate.app.utils.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class LicenseState {
    object Idle : LicenseState()
    object Submitting : LicenseState()
    object Approved : LicenseState()
    data class Error(val message: String) : LicenseState()
}

@HiltViewModel
class LicenseViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefs: AppPreferences
) : ViewModel() {

    private val _licenseState = MutableStateFlow<LicenseState>(LicenseState.Idle)
    val licenseState: StateFlow<LicenseState> = _licenseState.asStateFlow()

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json()
        }
    }

    init {
        // Generate and store device ID on first launch
        if (prefs.deviceId.isBlank()) {
            val androidId = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID
            ) ?: ""
            prefs.deviceId = androidId.uppercase().take(8)
        }
        // Set install time if not set
        if (prefs.installTime == 0L) {
            prefs.installTime = System.currentTimeMillis()
        }
    }

    fun getDeviceId(): String = prefs.deviceId

    fun getActivationStatus(): String = prefs.activationStatus

    fun getActivationPlan(): String = prefs.activationPlan

    fun getActivationExpiry(): Long = prefs.activationExpiry

    fun getActivatedEmail(): String = prefs.activatedEmail

    fun isTrialActive(): Boolean {
        if (prefs.activationStatus == "active") return false
        if (prefs.activationStatus == "expired") return false
        // If no activation set or trial, check if trial period has expired
        if (prefs.activationStatus != "trial" && prefs.activationStatus != "") return false
        val expiry = prefs.installTime + (30L * 24 * 60 * 60 * 1000)
        return System.currentTimeMillis() < expiry
    }

    fun getTrialDaysRemaining(): Long {
        val expiry = prefs.installTime + (30L * 24 * 60 * 60 * 1000)
        val remaining = (expiry - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)
        return maxOf(0, remaining)
    }

    fun isLicenseExpired(): Boolean {
        if (prefs.activationStatus == "expired") return true
        if (prefs.activationStatus == "trial" && prefs.activationExpiry > 0 && System.currentTimeMillis() >= prefs.activationExpiry) return true
        // Fallback: if activationStatus is empty and installTime + 30 days has passed, treat as expired
        if (prefs.activationStatus.isBlank() && prefs.installTime > 0) {
            val trialEnd = prefs.installTime + (30L * 24 * 60 * 60 * 1000)
            if (System.currentTimeMillis() >= trialEnd) return true
        }
        return false
    }

    fun submitTransaction(
        deviceId: String,
        email: String,
        plan: String,
        transactionId: String
    ) {
        _licenseState.value = LicenseState.Submitting
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = client.post(AppConfig.LICENSE_SERVER_URL) {
                    contentType(ContentType.Application.Json)
                    setBody(
                        mapOf(
                            "deviceId" to deviceId,
                            "email" to email,
                            "plan" to plan,
                            "transactionId" to transactionId
                        )
                    )
                }
                if (response.status == HttpStatusCode.OK) {
                    val body = response.body<Map<String, Any>>()
                    val status = body["status"] as? String ?: ""
                    if (status == "approved") {
                        val expiry = (body["expiry"] as? Number)?.toLong() ?: 0L
                        prefs.activationStatus = "active"
                        prefs.activationPlan = plan
                        prefs.activationExpiry = expiry
                        prefs.activatedEmail = email
                        _licenseState.value = LicenseState.Approved
                    } else {
                        _licenseState.value = LicenseState.Error("Server returned: $status")
                    }
                } else {
                    _licenseState.value = LicenseState.Error("Server error: ${response.status}")
                }
            } catch (e: Exception) {
                _licenseState.value = LicenseState.Error(e.message ?: "Network error. Check your connection.")
            }
        }
    }

    fun startTrial() {
        val expiry = prefs.installTime + (30L * 24 * 60 * 60 * 1000)
        prefs.activationStatus = "trial"
        prefs.activationExpiry = expiry
        prefs.activationPlan = "starter"
    }

    fun resetState() {
        _licenseState.value = LicenseState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        client.close()
    }
}
