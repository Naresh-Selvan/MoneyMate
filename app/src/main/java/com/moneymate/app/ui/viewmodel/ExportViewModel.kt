package com.moneymate.app.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneymate.app.data.export.ExportManager
import com.moneymate.app.data.export.ReportExportData
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

enum class ExportFormat { PDF, EXCEL }
enum class ShareTarget { GENERIC, WHATSAPP }

sealed class ExportState {
    object Idle : ExportState()
    object Exporting : ExportState()
    data class Done(val uri: Uri, val format: ExportFormat) : ExportState()
    data class Error(val message: String) : ExportState()
}

@HiltViewModel
class ExportViewModel @Inject constructor(
    private val exportManager: ExportManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _exportState = MutableStateFlow<ExportState>(ExportState.Idle)
    val exportState: StateFlow<ExportState> = _exportState

    private var currentExportData: ReportExportData? = null

    /** Set the data that will be exported on the next [exportCurrentReport] call. */
    fun setCurrentExportData(data: ReportExportData) {
        currentExportData = data
    }

    /** Export the previously-set data in the given format. */
    fun exportCurrentReport(format: ExportFormat) {
        val data = currentExportData ?: run {
            _exportState.value = ExportState.Error("No report data available")
            return
        }

        _exportState.value = ExportState.Exporting
        viewModelScope.launch {
            try {
                val uri = withContext(Dispatchers.IO) {
                    when (format) {
                        ExportFormat.PDF -> exportManager.exportToPdf(data)
                        ExportFormat.EXCEL -> exportManager.exportToExcel(data)
                    }
                }
                _exportState.value = ExportState.Done(uri, format)
            } catch (e: Exception) {
                _exportState.value = ExportState.Error(e.message ?: "Export failed")
            }
        }
    }

    /** Share the exported file via generic share or WhatsApp. */
    fun shareExportedFile(target: ShareTarget) {
        val state = _exportState.value
        if (state !is ExportState.Done) return

        val mimeType = when (state.format) {
            ExportFormat.PDF -> "application/pdf"
            ExportFormat.EXCEL -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        }

        when (target) {
            ShareTarget.GENERIC -> exportManager.shareGeneric(state.uri, mimeType)
            ShareTarget.WHATSAPP -> exportManager.shareViaWhatsApp(state.uri, mimeType)
        }
    }

    fun resetExportState() {
        _exportState.value = ExportState.Idle
    }

    fun dismissError() {
        _exportState.value = ExportState.Idle
    }
}
