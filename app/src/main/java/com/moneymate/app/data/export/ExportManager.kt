package com.moneymate.app.data.export

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import org.apache.poi.ss.usermodel.*
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min

/**
 * Singleton service that generates PDF and Excel exports from [ReportExportData]
 * and provides share intents (generic share + WhatsApp).
 *
 * All file I/O runs on the caller's thread — the caller must use Dispatchers.IO.
 */
@Singleton
class ExportManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val exportsDir: File
        get() = File(context.filesDir, "exports").also { it.mkdirs() }

    private val dataStamp: String
        get() = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())

    /** Authorised file-provider authority for export sharing. */
    private val authority = "${context.packageName}.export_provider"

    // ── Public API ──────────────────────────────────────────────────────────

    /**
     * Generates a PDF from the given [data] and returns a shareable [Uri].
     * Throws [ExportException] on failure.
     */
    fun exportToPdf(data: ReportExportData): Uri {
        val fileName = "${sanitiseFileName(data.reportTitle)}_${dataStamp}.pdf"
        val file = File(exportsDir, fileName)
        try {
            generatePdf(data, file)
            return FileProvider.getUriForFile(context, authority, file)
        } catch (e: Exception) {
            throw ExportException("PDF generation failed: ${e.message}", e)
        }
    }

    /**
     * Generates an Excel (.xlsx) from the given [data] and returns a shareable [Uri].
     * Throws [ExportException] on failure.
     */
    fun exportToExcel(data: ReportExportData): Uri {
        val fileName = "${sanitiseFileName(data.reportTitle)}_${dataStamp}.xlsx"
        val file = File(exportsDir, fileName)
        try {
            generateExcel(data, file)
            return FileProvider.getUriForFile(context, authority, file)
        } catch (e: Exception) {
            throw ExportException("Excel generation failed: ${e.message}", e)
        }
    }

    /**
     * Fires an Android share Intent targeting WhatsApp.
     * Falls back to a generic share chooser if WhatsApp is not installed.
     */
    fun shareViaWhatsApp(uri: Uri, mimeType: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_STREAM, uri)
            type = mimeType
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            `package` = "com.whatsapp"
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            // WhatsApp not installed — fall back to generic share
            shareGeneric(uri, mimeType)
        }
    }

    /** Fires a generic Android share Intent. */
    fun shareGeneric(uri: Uri, mimeType: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_STREAM, uri)
            type = mimeType
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Report"))
    }

    /** Deletes export files older than 7 days. Call from Application.onCreate(). */
    fun cleanOldExports() {
        val cutoff = System.currentTimeMillis() - 7L * 24L * 60L * 60L * 1000L
        exportsDir.listFiles()?.forEach { file ->
            if (file.lastModified() < cutoff) file.delete()
        }
    }

    // ── Private: PDF generation (iText7) ────────────────────────────────────

    private fun generatePdf(data: ReportExportData, file: File) {
        // Use com.itextpdf.kernel.pdf.PdfWriter + PdfDocument + Document
        val writer = com.itextpdf.kernel.pdf.PdfWriter(file)
        val pdfDoc = com.itextpdf.kernel.pdf.PdfDocument(writer)
        val doc = com.itextpdf.layout.Document(pdfDoc)
        try {
            doc.setMargins(20f, 20f, 20f, 20f)

            // ── Header ──
            doc.add(com.itextpdf.layout.element.Paragraph("MoneyMate")
                .setFontSize(18f).setBold())
            doc.add(com.itextpdf.layout.element.Paragraph(data.reportTitle)
                .setFontSize(14f).setBold())
            doc.add(com.itextpdf.layout.element.Paragraph(
                "File: ${data.fileLabel}  |  Period: ${data.dateRange}  |  Generated: ${dataStamp}"
            ).setFontSize(10f).setFontColor(com.itextpdf.kernel.colors.ColorConstants.GRAY))
            doc.add(com.itextpdf.layout.element.LineSeparator(
                com.itextpdf.kernel.pdf.canvas.draw.SolidLine()
            ))

            when (data) {
                is ReportExportData.TableReport -> renderTable(doc, data.headers, data.rows, data.footerRow)
                is ReportExportData.GroupedReport -> renderGrouped(doc, data)
                is ReportExportData.SiteDashboardData -> renderDashboard(doc, data)
            }

            // ── Footer note ──
            doc.add(com.itextpdf.layout.element.Paragraph("Generated by MoneyMate")
                .setFontSize(8f).setFontColor(com.itextpdf.kernel.colors.ColorConstants.GRAY)
                .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.RIGHT))
        } finally {
            doc.close()
        }
    }

    private fun renderTable(
        doc: com.itextpdf.layout.Document,
        headers: List<String>,
        rows: List<List<String>>,
        footerRow: List<String>?
    ) {
        val table = com.itextpdf.layout.element.Table(headers.size).useAllAvailableWidth()

        // Header row — dark background, white text
        headers.forEach { h ->
            val cell = com.itextpdf.layout.element.Cell().add(
                com.itextpdf.layout.element.Paragraph(h).setBold().setFontColor(com.itextpdf.kernel.colors.ColorConstants.WHITE)
            )
            cell.setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.DARK_GRAY)
            table.addCell(cell)
        }

        // Data rows — alternating shading
        rows.forEachIndexed { idx, row ->
            val bg = if (idx % 2 == 0)
                com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY
            else
                com.itextpdf.kernel.colors.ColorConstants.WHITE
            row.forEach { value ->
                table.addCell(com.itextpdf.layout.element.Cell().add(
                    com.itextpdf.layout.element.Paragraph(value)
                ).setBackgroundColor(bg))
            }
        }

        // Footer row
        if (footerRow != null) {
            footerRow.forEach { f ->
                table.addCell(com.itextpdf.layout.element.Cell().add(
                    com.itextpdf.layout.element.Paragraph(f).setBold()
                ).setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY))
            }
        }

        doc.add(table)
    }

    private fun renderGrouped(doc: com.itextpdf.layout.Document, data: ReportExportData.GroupedReport) {
        data.groups.forEach { group ->
            doc.add(com.itextpdf.layout.element.Paragraph(group.groupLabel).setFontSize(12f).setBold())
            renderTable(doc, group.headers, group.rows, group.subtotalRow)
            doc.add(com.itextpdf.layout.element.Paragraph(" ")) // spacer
        }
    }

    private fun renderDashboard(doc: com.itextpdf.layout.Document, data: ReportExportData.SiteDashboardData) {
        val table = com.itextpdf.layout.element.Table(2).useAllAvailableWidth()
        table.addHeaderCell(com.itextpdf.layout.element.Cell().add(
            com.itextpdf.layout.element.Paragraph("Metric").setBold()
        ).setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.DARK_GRAY))
        table.addHeaderCell(com.itextpdf.layout.element.Cell().add(
            com.itextpdf.layout.element.Paragraph("Value").setBold()
        ).setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.DARK_GRAY))

        data.metrics.forEach { m ->
            table.addCell(m.label)
            table.addCell(m.value)
        }
        doc.add(table)

        if (data.comparisonMetrics != null) {
            doc.add(com.itextpdf.layout.element.Paragraph("Comparison").setFontSize(12f).setBold())
            val compTable = com.itextpdf.layout.element.Table(2).useAllAvailableWidth()
            data.comparisonMetrics.forEach { m ->
                compTable.addCell(m.label)
                compTable.addCell(m.value)
            }
            doc.add(compTable)
        }
    }

    // ── Private: Excel generation (Apache POI) ──────────────────────────────

    private fun generateExcel(data: ReportExportData, file: File) {
        val wb = XSSFWorkbook()
        try {
            val boldStyle = wb.createCellStyle().apply {
            val boldFont = wb.createFont()
            boldFont.setBold(true)
            setFont(boldFont)
            fillForegroundColor = IndexedColors.GREY_25_PERCENT.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
        }
        val headerStyle = wb.createCellStyle().apply {
            val boldWhiteFont = wb.createFont()
            boldWhiteFont.setBold(true)
            boldWhiteFont.color = IndexedColors.WHITE.index
            setFont(boldWhiteFont)
            fillForegroundColor = IndexedColors.DARK_BLUE.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
        }
            val currencyStyle = wb.createCellStyle().apply {
                dataFormat = wb.createDataFormat().getFormat("#,##0.00")
            }

            when (data) {
                is ReportExportData.TableReport -> {
                    val sheet = wb.createSheet("Report Data")
                    writeSheet(sheet, data, boldStyle, headerStyle, currencyStyle)
                }
                is ReportExportData.GroupedReport -> {
                    val sheet = wb.createSheet("Report Data")
                    var currentRow = 0
                    data.groups.forEach { group ->
                        val groupRow = sheet.createRow(currentRow++)
                        groupRow.createCell(0).setCellValue(group.groupLabel)
                        groupRow.getCell(0).cellStyle = boldStyle

                        val headerRow = sheet.createRow(currentRow++)
                        group.headers.forEachIndexed { i, h ->
                            headerRow.createCell(i).apply {
                                setCellValue(h)
                                cellStyle = headerStyle
                            }
                        }

                        group.rows.forEach { row ->
                            val dataRow = sheet.createRow(currentRow++)
                            row.forEachIndexed { i, v ->
                                dataRow.createCell(i).setCellValue(v)
                            }
                        }

                        if (group.subtotalRow != null) {
                            val subRow = sheet.createRow(currentRow++)
                            group.subtotalRow.forEachIndexed { i, v ->
                                subRow.createCell(i).apply {
                                    setCellValue(v)
                                    cellStyle = boldStyle
                                }
                            }
                        }
                        currentRow++ // blank spacer
                    }

                    // Create Summary sheet
                    val summary = wb.createSheet("Summary")
                    var sr = 0
                    data.groups.forEach { group ->
                        val labelRow = summary.createRow(sr++)
                        labelRow.createCell(0).setCellValue(group.groupLabel)
                        labelRow.getCell(0).cellStyle = boldStyle
                        group.subtotalRow?.forEachIndexed { i, v ->
                            val row = summary.createRow(sr++)
                            row.createCell(0).setCellValue(group.headers.getOrElse(i) { "" })
                            row.createCell(1).setCellValue(v)
                        }
                        sr++ // spacer
                    }

                    autoSizeColumns(sheet)
                    autoSizeColumns(summary)
                    sheet.createFreezePane(0, 4)
                }
                is ReportExportData.SiteDashboardData -> {
                    val sheet = wb.createSheet("Report Data")
                    var rowNum = 0
                    // Title
                    val titleRow = sheet.createRow(rowNum++); titleRow.createCell(0).setCellValue("MoneyMate — ${data.reportTitle}"); titleRow.getCell(0).cellStyle = boldStyle
                    val infoRow = sheet.createRow(rowNum++); infoRow.createCell(0).setCellValue("File: ${data.fileLabel} | Period: ${data.dateRange} | Generated: $dataStamp")
                    rowNum++ // blank

                    data.metrics.forEach { m ->
                        val r = sheet.createRow(rowNum++)
                        r.createCell(0).setCellValue(m.label)
                        r.createCell(1).setCellValue(m.value)
                    }

                    data.comparisonMetrics?.let { comp ->
                        rowNum++ // blank
                        val compTitle = sheet.createRow(rowNum++); compTitle.createCell(0).setCellValue("Comparison"); compTitle.getCell(0).cellStyle = boldStyle
                        comp.forEach { m ->
                            val r = sheet.createRow(rowNum++)
                            r.createCell(0).setCellValue(m.label)
                            r.createCell(1).setCellValue(m.value)
                        }
                    }

                    autoSizeColumns(sheet)
                    sheet.createFreezePane(0, 4)
                }
            }

            FileOutputStream(file).use { wb.write(it) }
        } finally {
            wb.close()
        }
    }

    private fun writeSheet(
        sheet: Sheet,
        data: ReportExportData.TableReport,
        boldStyle: CellStyle,
        headerStyle: CellStyle,
        currencyStyle: CellStyle
    ) {
        var rowNum = 0
        // Row 1: Title
        val titleRow = sheet.createRow(rowNum++)
        titleRow.createCell(0).setCellValue("MoneyMate — ${data.reportTitle}")
        titleRow.getCell(0).cellStyle = boldStyle

        // Row 2: Info
        val infoRow = sheet.createRow(rowNum++)
        infoRow.createCell(0).setCellValue("File: ${data.fileLabel} | Period: ${data.dateRange} | Generated: $dataStamp")

        rowNum++ // blank row

        // Header row
        val headerRow = sheet.createRow(rowNum++)
        data.headers.forEachIndexed { i, h ->
            headerRow.createCell(i).apply {
                setCellValue(h)
                cellStyle = headerStyle
            }
        }

        // Data rows
        data.rows.forEach { row ->
            val dataRow = sheet.createRow(rowNum++)
            row.forEachIndexed { i, v ->
                dataRow.createCell(i).setCellValue(v)
            }
        }

        // Footer row
        data.footerRow?.let { footer ->
            val footRow = sheet.createRow(rowNum++)
            footer.forEachIndexed { i, v ->
                footRow.createCell(i).apply {
                    setCellValue(v)
                    cellStyle = boldStyle
                }
            }
        }

        // Auto-filter on header row
        if (data.rows.isNotEmpty()) {
            sheet.setAutoFilter(
                CellRangeAddress(3, 3 + data.rows.size, 0, data.headers.size - 1)
            )
        }

        autoSizeColumns(sheet)
        sheet.createFreezePane(0, 4)
    }

    private fun autoSizeColumns(sheet: Sheet) {
        if (sheet.getPhysicalNumberOfRows() == 0) return
        val headerRow = sheet.getRow(0) ?: return
        val lastCol = headerRow.lastCellNum?.toInt()?.minus(1) ?: 0
        for (i in 0..lastCol) {
            try {
                sheet.autoSizeColumn(i)
                if (sheet.getColumnWidth(i) > 60 * 256) {
                    sheet.setColumnWidth(i, 60 * 256)
                }
            } catch (_: Exception) {
                // Skip columns that can't be auto-sized
            }
        }
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private fun sanitiseFileName(name: String): String {
        return name.replace(Regex("[^a-zA-Z0-9_\\- ]"), "").trim().replace(" ", "_")
            .take(60)
    }
}
