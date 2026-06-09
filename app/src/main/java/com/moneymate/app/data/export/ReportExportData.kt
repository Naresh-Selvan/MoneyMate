package com.moneymate.app.data.export

/**
 * Sealed class representing the data to be exported from any report screen.
 * Each variant holds structured data ready for PDF/Excel generation.
 */
sealed class ReportExportData {
    abstract val reportTitle: String
    abstract val fileLabel: String
    abstract val dateRange: String

    /**
     * A simple flat table with one header row, data rows, and an optional footer row.
     */
    data class TableReport(
        override val reportTitle: String,
        override val fileLabel: String,
        override val dateRange: String,
        val headers: List<String>,
        val rows: List<List<String>>,
        val footerRow: List<String>? = null
    ) : ReportExportData()

    /**
     * A report composed of multiple groups/sections, each with its own table.
     * Example: Combined Summary (Expenses section + Investments section).
     */
    data class GroupedReport(
        override val reportTitle: String,
        override val fileLabel: String,
        override val dateRange: String,
        val groups: List<ReportGroup>
    ) : ReportExportData()

    /**
     * Site Dashboard — key-value metrics with optional comparison.
     */
    data class SiteDashboardData(
        override val reportTitle: String,
        override val fileLabel: String,
        override val dateRange: String,
        val metrics: List<DashboardMetric>,
        val comparisonMetrics: List<DashboardMetric>? = null
    ) : ReportExportData()
}

data class ReportGroup(
    val groupLabel: String,
    val headers: List<String>,
    val rows: List<List<String>>,
    val subtotalRow: List<String>? = null
)

data class DashboardMetric(
    val label: String,
    val value: String
)

/** Thrown when export fails. */
class ExportException(message: String, cause: Throwable? = null) : Exception(message, cause)
