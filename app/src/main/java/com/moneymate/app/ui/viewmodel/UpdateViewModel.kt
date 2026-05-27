package com.moneymate.app.ui.viewmodel

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import javax.inject.Inject

// ── Replace these two with your GitHub details ────────────────────────────────
private const val GITHUB_OWNER = "Naresh-Selvan"
private const val GITHUB_REPO  = "MoneyMate"
// ─────────────────────────────────────────────────────────────────────────────

data class UpdateInfo(
    val latestVersion: String,   // e.g. "1.2"
    val versionCode: Int,        // e.g. 5
    val apkUrl: String,          // direct APK download URL
    val releaseNotes: String
)

sealed class UpdateState {
    object Idle         : UpdateState()
    object Checking     : UpdateState()
    data class Available(val info: UpdateInfo) : UpdateState()
    object Downloading  : UpdateState()
    data class ReadyToInstall(val filePath: String) : UpdateState()
    data class Error(val message: String) : UpdateState()
    object UpToDate     : UpdateState()
}

@HiltViewModel
class UpdateViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState

    /** Call on app launch — silently checks, only surfaces if update available */
    fun checkForUpdate(currentVersionCode: Int) {
        if (_updateState.value is UpdateState.Checking) return
        _updateState.value = UpdateState.Checking
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val apiUrl = "https://api.github.com/repos/$GITHUB_OWNER/$GITHUB_REPO/releases/latest"
                val json = URL(apiUrl).readText()
                val obj = JSONObject(json)

                val tagName      = obj.getString("tag_name")          // e.g. "v1.2-5"
                val releaseNotes = obj.optString("body", "")

                // Tag format: v{versionName}-{versionCode}  e.g. "v1.2-5"
                val stripped = tagName.removePrefix("v")               // "1.2-5"
                val parts    = stripped.split("-")
                val vName    = parts.getOrElse(0) { "1.0" }           // "1.2"
                val vCode    = parts.getOrElse(1) { "1" }.toIntOrNull() ?: 1

                // Find the APK asset
                val assets   = obj.getJSONArray("assets")
                var apkUrl   = ""
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    if (asset.getString("name").endsWith(".apk")) {
                        apkUrl = asset.getString("browser_download_url")
                        break
                    }
                }

                withContext(Dispatchers.Main) {
                    if (apkUrl.isBlank()) {
                        _updateState.value = UpdateState.UpToDate
                    } else if (vCode > currentVersionCode) {
                        _updateState.value = UpdateState.Available(
                            UpdateInfo(vName, vCode, apkUrl, releaseNotes)
                        )
                    } else {
                        _updateState.value = UpdateState.UpToDate
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    // Silently fail — don't bother the user if check fails
                    _updateState.value = UpdateState.Idle
                }
            }
        }
    }

    /** Download APK using DownloadManager and track completion */
    fun downloadAndInstall(info: UpdateInfo) {
        _updateState.value = UpdateState.Downloading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val fileName = "MoneyMate-${info.latestVersion}.apk"
                val request = DownloadManager.Request(Uri.parse(info.apkUrl))
                    .setTitle("MoneyMate Update")
                    .setDescription("Downloading v${info.latestVersion}…")
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                    .setMimeType("application/vnd.android.package-archive")
                    .setAllowedNetworkTypes(
                        DownloadManager.Request.NETWORK_WIFI or
                                DownloadManager.Request.NETWORK_MOBILE
                    )

                val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                val downloadId = dm.enqueue(request)

                // Poll until done
                var downloading = true
                while (downloading) {
                    val query = DownloadManager.Query().setFilterById(downloadId)
                    val cursor = dm.query(query)
                    if (cursor.moveToFirst()) {
                        val statusCol  = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                        val uriCol     = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
                        val status     = cursor.getInt(statusCol)
                        when (status) {
                            DownloadManager.STATUS_SUCCESSFUL -> {
                                val localUri = cursor.getString(uriCol)
                                withContext(Dispatchers.Main) {
                                    _updateState.value = UpdateState.ReadyToInstall(localUri)
                                }
                                downloading = false
                            }
                            DownloadManager.STATUS_FAILED -> {
                                withContext(Dispatchers.Main) {
                                    _updateState.value = UpdateState.Error("Download failed. Check your connection and try again.")
                                }
                                downloading = false
                            }
                            else -> kotlinx.coroutines.delay(1000)
                        }
                    } else {
                        downloading = false
                    }
                    cursor.close()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _updateState.value = UpdateState.Error(e.message ?: "Unknown error")
                }
            }
        }
    }

    fun dismiss() {
        _updateState.value = UpdateState.Idle
    }

    fun resetToAvailable(info: UpdateInfo) {
        _updateState.value = UpdateState.Available(info)
    }
}