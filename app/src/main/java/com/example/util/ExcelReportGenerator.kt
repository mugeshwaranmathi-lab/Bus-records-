package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.BusEntry
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

object ExcelReportGenerator {

    data class ReportSection(
        val heading: String,       // e.g. "Pickup / 2026-11-11 / NRL"
        val shift: String,         // "First", "Day", "Night"
        val tripType: String,      // "Pickup" or "Drop"
        val contract: String,      // "NRL" or "SBLT"
        val isPickup: Boolean,
        val items: List<SectionRow>,
        val totalRunningKm: Double
    )

    data class SectionRow(
        val driverName: String,
        val busNo: String,
        val routeNo: String,
        val routeName: String,
        val startingKm: String,
        val startingTime: String,
        val endingKm: String,
        val endingTime: String,
        val runningKm: Double,
        val remarks: String
    )

    data class MonthlyBusSummary(
        val busNo: String,
        val driverName: String,
        val contract: String,
        val totalTrips: Int,
        val totalKm: Double
    )

    fun getSections(
        date: String,
        entries: List<BusEntry>,
        shiftFilter: String = "ALL",     // "ALL", "First", "Day", "Night"
        tripFilter: String = "ALL",      // "ALL", "Pickup", "Drop"
        contractFilter: String = "ALL"   // "ALL", "NRL", "SBLT"
    ): List<ReportSection> {
        val shifts = if (shiftFilter == "ALL") listOf("First", "Day", "Night") else listOf(shiftFilter)
        val tripTypes = if (tripFilter == "ALL") listOf("Pickup", "Drop") else listOf(tripFilter)
        val contracts = if (contractFilter == "ALL") listOf("NRL", "SBLT") else listOf(contractFilter)

        val sections = mutableListOf<ReportSection>()

        for (shift in shifts) {
            for (trip in tripTypes) {
                for (cont in contracts) {
                    val matching = entries.filter {
                        it.shift.equals(shift, ignoreCase = true) &&
                        it.tripType.equals(trip, ignoreCase = true) &&
                        it.contract.equals(cont, ignoreCase = true)
                    }

                    if (matching.isNotEmpty()) {
                        val rows = matching.map {
                            SectionRow(
                                driverName = it.driverName,
                                busNo = it.busNo,
                                routeNo = it.routeNo,
                                routeName = it.routeName,
                                startingKm = it.startingKm,
                                startingTime = it.startingTime,
                                endingKm = it.endingKm,
                                endingTime = it.endingTime,
                                runningKm = it.runningKm,
                                remarks = it.remarks
                            )
                        }

                        // Heading format requested: Heading - Pickup / Date / Contract
                        val headingTitle = "$trip / $date / $cont ($shift Shift)"
                        sections.add(
                            ReportSection(
                                heading = headingTitle,
                                shift = shift,
                                tripType = trip,
                                contract = cont,
                                isPickup = trip.equals("Pickup", ignoreCase = true),
                                items = rows,
                                totalRunningKm = rows.sumOf { it.runningKm }
                            )
                        )
                    }
                }
            }
        }

        return sections
    }

    /**
     * Calculates Monthly Bus-wise Running KM:
     * How many KM each bus has run across the selected records/month.
     */
    fun calculateMonthlyBusSummary(entries: List<BusEntry>): List<MonthlyBusSummary> {
        return entries
            .groupBy { it.busNo.trim().uppercase() }
            .map { (busNo, busEntries) ->
                val latest = busEntries.last()
                val totalKm = busEntries.sumOf { it.runningKm }
                MonthlyBusSummary(
                    busNo = busNo,
                    driverName = latest.driverName,
                    contract = latest.contract,
                    totalTrips = busEntries.size,
                    totalKm = totalKm
                )
            }
            .sortedByDescending { it.totalKm }
    }

    /**
     * Generates Excel (.xls) file with:
     * - Headings: Heading - Pickup / Date / Contract
     * - Columns: Sl - Driver Name - Bus No - Route No - Route Name - Starting KM - time - Ending KM - time - Running Km - Remarks
     * - Monthly Bus-wise KM summary table!
     */
    fun generateExcelFile(
        context: Context,
        date: String,
        entries: List<BusEntry>,
        shiftFilter: String = "ALL",
        tripFilter: String = "ALL",
        contractFilter: String = "ALL"
    ): File {
        val reportsDir = File(context.cacheDir, "reports")
        if (!reportsDir.exists()) reportsDir.mkdirs()

        val fileName = "Bus_Entry_${date.replace("/", "-")}.xls"
        val file = File(reportsDir, fileName)

        val sections = getSections(date, entries, shiftFilter, tripFilter, contractFilter)
        val monthlySummary = calculateMonthlyBusSummary(entries)

        val htmlContent = buildString {
            append("""
                <!DOCTYPE html>
                <html>
                <head>
                <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
                <style>
                    body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Arial, sans-serif; margin: 20px; color: #1C1C1E; background-color: #FFFFFF; }
                    .org-header { font-size: 18pt; font-weight: bold; color: #004080; margin-bottom: 2px; }
                    .org-sub { font-size: 14pt; font-weight: bold; color: #007AFF; margin-bottom: 4px; }
                    .report-date { font-size: 11pt; font-weight: bold; color: #3C3C43; margin-bottom: 18px; }
                    .section-header { font-size: 13pt; font-weight: bold; padding: 8px 12px; margin-top: 20px; margin-bottom: 8px; border-radius: 4px; color: #FFFFFF; }
                    .pickup-header { background-color: #007AFF; }
                    .drop-header { background-color: #004080; }
                    .summary-header { background-color: #107C41; color: #FFFFFF; }
                    table { border-collapse: collapse; width: 100%; margin-bottom: 20px; font-size: 10pt; }
                    th { color: #FFFFFF; font-weight: bold; text-align: center; border: 1px solid #1C1C1E; padding: 7px 5px; }
                    th.th-pickup { background-color: #007AFF; }
                    th.th-drop { background-color: #004080; }
                    th.th-summary { background-color: #107C41; }
                    td { border: 1px solid #CBD5E1; padding: 6px 5px; text-align: left; }
                    td.center { text-align: center; }
                    td.number { text-align: right; }
                    tr:nth-child(even) td { background-color: #F8F9FA; }
                    tr.total-row td { background-color: #E2E8F0; font-weight: bold; color: #004080; }
                    .footer-note { font-size: 9pt; color: #8E8E93; margin-top: 24px; border-top: 1px solid #D1D1D6; padding-top: 8px; }
                </style>
                </head>
                <body>
                <div class="org-header">Ashok Leyland - Ennore</div>
                <div class="org-sub">Bus Record &bull; Transport Security</div>
                <div class="report-date">Date: $date | Shift: $shiftFilter | Trip: $tripFilter | Contract: $contractFilter</div>
            """.trimIndent())

            if (sections.isEmpty()) {
                append("<p style='font-style: italic; color: #8E8E93;'>No bus movements recorded matching current filters.</p>")
            } else {
                for (section in sections) {
                    val bannerClass = if (section.isPickup) "pickup-header" else "drop-header"
                    val thClass = if (section.isPickup) "th-pickup" else "th-drop"

                    append("<div class=\"section-header $bannerClass\">Heading - ${section.heading}</div>")
                    append("""
                        <table>
                        <thead>
                            <tr>
                                <th class="$thClass">Sl</th>
                                <th class="$thClass">Driver Name</th>
                                <th class="$thClass">Bus No</th>
                                <th class="$thClass">Route No</th>
                                <th class="$thClass">Route Name</th>
                                <th class="$thClass">Starting KM</th>
                                <th class="$thClass">time</th>
                                <th class="$thClass">Ending KM</th>
                                <th class="$thClass">time</th>
                                <th class="$thClass">Running Km</th>
                                <th class="$thClass">Remarks</th>
                            </tr>
                        </thead>
                        <tbody>
                    """.trimIndent())

                    var totalKm = 0.0
                    section.items.forEachIndexed { index, row ->
                        totalKm += row.runningKm
                        append("<tr>")
                        append("<td class=\"center\">${index + 1}</td>")
                        append("<td>${escapeHtml(row.driverName)}</td>")
                        append("<td><b>${escapeHtml(row.busNo)}</b></td>")
                        append("<td class=\"center\">${escapeHtml(row.routeNo)}</td>")
                        append("<td>${escapeHtml(row.routeName)}</td>")
                        append("<td class=\"number\">${escapeHtml(row.startingKm)}</td>")
                        append("<td class=\"center\">${escapeHtml(row.startingTime)}</td>")
                        append("<td class=\"number\">${escapeHtml(row.endingKm)}</td>")
                        append("<td class=\"center\">${escapeHtml(row.endingTime)}</td>")
                        append("<td class=\"number\"><b>${formatKm(row.runningKm)}</b></td>")
                        append("<td>${escapeHtml(row.remarks)}</td>")
                        append("</tr>\n")
                    }

                    append("""
                        <tr class="total-row">
                            <td colspan="9" style="text-align: right; padding-right: 10px;"><b>TOTAL RUNNING KM:</b></td>
                            <td class="number"><b>${formatKm(totalKm)}</b></td>
                            <td></td>
                        </tr>
                    """.trimIndent())

                    append("</tbody></table>")
                }
            }

            // Monthly Bus-wise Running KM Summary Table
            if (monthlySummary.isNotEmpty()) {
                append("<div class=\"section-header summary-header\">Monthly Bus Running KM (Bus-Wise Total Distance)</div>")
                append("""
                    <table>
                    <thead>
                        <tr>
                            <th class="th-summary">Sl</th>
                            <th class="th-summary">Bus No</th>
                            <th class="th-summary">Driver Name</th>
                            <th class="th-summary">Contract</th>
                            <th class="th-summary">Total Trips</th>
                            <th class="th-summary">Total Running KM</th>
                        </tr>
                    </thead>
                    <tbody>
                """.trimIndent())

                var grandTotalKm = 0.0
                monthlySummary.forEachIndexed { idx, item ->
                    grandTotalKm += item.totalKm
                    append("<tr>")
                    append("<td class=\"center\">${idx + 1}</td>")
                    append("<td><b>${escapeHtml(item.busNo)}</b></td>")
                    append("<td>${escapeHtml(item.driverName)}</td>")
                    append("<td class=\"center\"><b>${escapeHtml(item.contract)}</b></td>")
                    append("<td class=\"center\">${item.totalTrips}</td>")
                    append("<td class=\"number\"><b>${formatKm(item.totalKm)} KM</b></td>")
                    append("</tr>\n")
                }

                append("""
                    <tr class="total-row">
                        <td colspan="5" style="text-align: right; padding-right: 10px;"><b>GRAND TOTAL RUNNING KM:</b></td>
                        <td class="number"><b>${formatKm(grandTotalKm)} KM</b></td>
                    </tr>
                """.trimIndent())

                append("</tbody></table>")
            }

            append("""
                <div class="footer-note">
                    Ashok Leyland - Ennore &bull; Bus Record &bull; Transport Security
                </div>
                </body>
                </html>
            """.trimIndent())
        }

        FileOutputStream(file).use { fos ->
            OutputStreamWriter(fos, StandardCharsets.UTF_8).use { writer ->
                writer.write(htmlContent)
                writer.flush()
            }
        }

        return file
    }

    /**
     * Generates standard CSV (Sheet format) file for easy import into Google Sheets / Excel.
     */
    fun generateCsvFile(
        context: Context,
        date: String,
        entries: List<BusEntry>,
        shiftFilter: String = "ALL",
        tripFilter: String = "ALL",
        contractFilter: String = "ALL"
    ): File {
        val reportsDir = File(context.cacheDir, "reports")
        if (!reportsDir.exists()) reportsDir.mkdirs()

        val fileName = "Bus_Entry_${date.replace("/", "-")}.csv"
        val file = File(reportsDir, fileName)

        val sections = getSections(date, entries, shiftFilter, tripFilter, contractFilter)
        val monthlySummary = calculateMonthlyBusSummary(entries)

        FileOutputStream(file).use { fos ->
            OutputStreamWriter(fos, StandardCharsets.UTF_8).use { writer ->
                writer.write("Ashok Leyland - Ennore,Bus Record,Transport Security\n")
                writer.write("Date: $date,Shift: $shiftFilter,Trip: $tripFilter,Contract: $contractFilter\n\n")

                for (sec in sections) {
                    writer.write("Heading - ${sec.heading}\n")
                    writer.write("Sl,Driver Name,Bus No,Route No,Route Name,Starting KM,time,Ending KM,time,Running Km,Remarks\n")
                    sec.items.forEachIndexed { index, row ->
                        writer.write("${index + 1},\"${escapeCsv(row.driverName)}\",\"${escapeCsv(row.busNo)}\",\"${escapeCsv(row.routeNo)}\",\"${escapeCsv(row.routeName)}\",${row.startingKm},\"${row.startingTime}\",${row.endingKm},\"${row.endingTime}\",${row.runningKm},\"${escapeCsv(row.remarks)}\"\n")
                    }
                    writer.write(",,,,,,,,,TOTAL RUNNING KM,${sec.totalRunningKm}\n\n")
                }

                if (monthlySummary.isNotEmpty()) {
                    writer.write("Monthly Bus Running KM (Bus-Wise Total Distance)\n")
                    writer.write("Sl,Bus No,Driver Name,Contract,Total Trips,Total Running KM\n")
                    monthlySummary.forEachIndexed { idx, item ->
                        writer.write("${idx + 1},\"${escapeCsv(item.busNo)}\",\"${escapeCsv(item.driverName)}\",\"${item.contract}\",${item.totalTrips},${item.totalKm}\n")
                    }
                    val grandTotal = monthlySummary.sumOf { it.totalKm }
                    writer.write(",,,,GRAND TOTAL RUNNING KM,$grandTotal\n")
                }

                writer.flush()
            }
        }

        return file
    }

    fun createEmailIntent(
        context: Context,
        date: String,
        entries: List<BusEntry>,
        shiftFilter: String = "ALL",
        tripFilter: String = "ALL",
        contractFilter: String = "ALL"
    ): Intent {
        val excelFile = generateExcelFile(context, date, entries, shiftFilter, tripFilter, contractFilter)
        val fileUri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            excelFile
        )

        val sections = getSections(date, entries, shiftFilter, tripFilter, contractFilter)
        val totalBuses = entries.size
        val totalRunningKm = entries.sumOf { it.runningKm }

        val emailSubject = "Bus Record Report - $date - Ashok Leyland Ennore"
        val emailBody = """
Dear Sir/Madam,

Please find attached the official Daily Bus Record for Date: $date.

Company: Ashok Leyland - Ennore
Department: Transport Security
Filter: Shift ($shiftFilter) | Trip ($tripFilter) | Contract ($contractFilter)

Summary:
• Total Bus Movements: $totalBuses
• Total Running Distance: ${formatKm(totalRunningKm)} KM
• Active Sections: ${sections.size}

Attached Excel file includes complete Sl - Driver Name - Bus No - Route No - Route Name - Starting KM - time - Ending KM - time - Running Km - Remarks, along with Monthly Bus-wise Running KM summary.

Regards,
Transport Security,
Ashok Leyland - Ennore
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.ms-excel"
            putExtra(Intent.EXTRA_SUBJECT, emailSubject)
            putExtra(Intent.EXTRA_TEXT, emailBody)
            putExtra(Intent.EXTRA_STREAM, fileUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        return Intent.createChooser(intent, "Send Bus Record via Email")
    }

    fun createWhatsAppIntent(
        context: Context,
        date: String,
        entries: List<BusEntry>,
        shiftFilter: String = "ALL",
        tripFilter: String = "ALL",
        contractFilter: String = "ALL"
    ): Intent {
        val excelFile = generateExcelFile(context, date, entries, shiftFilter, tripFilter, contractFilter)
        val fileUri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            excelFile
        )

        val sections = getSections(date, entries, shiftFilter, tripFilter, contractFilter)
        val totalRunningKm = entries.sumOf { it.runningKm }

        val message = buildString {
            append("*ASHOK LEYLAND - ENNORE*\n")
            append("*Bus Record - Transport Security*\n")
            append("📅 *Date:* $date | Filter: $shiftFilter / $tripFilter / $contractFilter\n\n")

            if (sections.isEmpty()) {
                append("_No bus movements recorded._\n")
            } else {
                for (section in sections) {
                    append("🚍 *${section.heading}* (${section.items.size} buses | Total: ${formatKm(section.totalRunningKm)} KM)\n")
                    section.items.forEachIndexed { idx, item ->
                        append("${idx + 1}. *${item.busNo}* (${item.driverName}) | R-${item.routeNo} | Start: ${item.startingKm} KM (${item.startingTime}) &rarr; End: ${item.endingKm} KM (${item.endingTime}) | *Run: ${formatKm(item.runningKm)} KM*\n")
                    }
                    append("\n")
                }
            }
            append("📊 *Total Distance:* ${formatKm(totalRunningKm)} KM\n")
            append("━━━━━━━━━━━━━━━\n")
            append("Excel spreadsheet attached.\n")
            append("_Transport Security, Ashok Leyland - Ennore_")
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.ms-excel"
            setPackage("com.whatsapp")
            putExtra(Intent.EXTRA_TEXT, message)
            putExtra(Intent.EXTRA_STREAM, fileUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        return intent
    }

    fun createCsvShareIntent(
        context: Context,
        date: String,
        entries: List<BusEntry>,
        shiftFilter: String = "ALL",
        tripFilter: String = "ALL",
        contractFilter: String = "ALL"
    ): Intent {
        val csvFile = generateCsvFile(context, date, entries, shiftFilter, tripFilter, contractFilter)
        val fileUri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            csvFile
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, fileUri)
            putExtra(Intent.EXTRA_SUBJECT, "Bus Record CSV - $date - Ashok Leyland Ennore")
            putExtra(Intent.EXTRA_TEXT, "Ashok Leyland - Ennore Transport Security Bus Record CSV for $date.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        return Intent.createChooser(intent, "Share CSV Sheet")
    }

    fun createGeneralShareIntent(
        context: Context,
        date: String,
        entries: List<BusEntry>
    ): Intent {
        val excelFile = generateExcelFile(context, date, entries)
        val fileUri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            excelFile
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.ms-excel"
            putExtra(Intent.EXTRA_STREAM, fileUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        return Intent.createChooser(intent, "Share Bus Record File")
    }

    private fun formatKm(km: Double): String {
        return if (km % 1.0 == 0.0) {
            String.format("%.0f", km)
        } else {
            String.format("%.1f", km)
        }
    }

    private fun escapeHtml(text: String): String {
        return text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }

    private fun escapeCsv(text: String): String {
        return text.replace("\"", "\"\"")
    }
}
