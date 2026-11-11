package com.example.ui.screens

import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.BusEntryViewModel
import com.example.ui.components.ExcelTableView
import com.example.ui.theme.IosBackground
import com.example.ui.theme.IosBlue
import com.example.ui.theme.IosBorder
import com.example.ui.theme.IosCardBackground
import com.example.ui.theme.IosDarkBlue
import com.example.ui.theme.IosLightBlueTint
import com.example.ui.theme.IosSegmentedBg
import com.example.ui.theme.IosSubheadBlue
import com.example.ui.theme.IosTextPrimary
import com.example.ui.theme.IosTextSecondary
import com.example.ui.theme.IosTextTertiary
import com.example.util.ExcelReportGenerator
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun ShareReportScreen(
    viewModel: BusEntryViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedDate by viewModel.selectedReportDate.collectAsState()
    val allDateEntries by viewModel.entriesForSelectedDate.collectAsState()
    val allMonthEntries by viewModel.allMonthEntries.collectAsState()

    val shiftFilter by viewModel.selectedShiftFilter.collectAsState()
    val tripFilter by viewModel.selectedTripFilter.collectAsState()
    val contractFilter by viewModel.selectedContractFilter.collectAsState()

    val scrollState = rememberScrollState()

    val shiftSlideTabs = listOf("ALL", "First", "Day", "Night")

    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val cal = Calendar.getInstance().apply { set(year, month, dayOfMonth) }
            val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
            viewModel.onReportDateChange(formattedDate)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val displayedSections = ExcelReportGenerator.getSections(
        date = selectedDate,
        entries = allDateEntries,
        shiftFilter = shiftFilter,
        tripFilter = tripFilter,
        contractFilter = contractFilter
    )

    val monthlyBusSummaries = ExcelReportGenerator.calculateMonthlyBusSummary(
        if (allMonthEntries.isNotEmpty()) allMonthEntries else allDateEntries
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(IosBackground)
    ) {
        // iOS Navigation Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = IosCardBackground),
            shape = RoundedCornerShape(0.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("share_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = IosDarkBlue
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "SHARE SCREEN",
                            color = IosDarkBlue,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.2).sp
                        )
                        Text(
                            text = "Ashok Leyland - Ennore &bull; Transport Security",
                            color = IosSubheadBlue,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Date Selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(IosBackground)
                            .border(1.dp, IosBorder, RoundedCornerShape(8.dp))
                            .clickable { datePickerDialog.show() }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag("report_date_picker_trigger")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = IosBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = selectedDate,
                            color = IosDarkBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Text(
                        text = "${allDateEntries.size} Trips Logged",
                        color = IosBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Secondary Filter Controls: TRIP (ALL / Pickup / Drop) & CONTRACT (ALL / NRL / SBLT)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Trip filter chips
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "TRIP",
                            color = IosTextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(IosSegmentedBg)
                                .padding(2.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            listOf("ALL", "Pickup", "Drop").forEach { opt ->
                                val isSelected = tripFilter.equals(opt, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(if (isSelected) IosCardBackground else Color.Transparent)
                                        .clickable { viewModel.onTripFilterChange(opt) }
                                        .testTag("filter_trip_$opt"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = opt,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) IosDarkBlue else IosTextSecondary
                                    )
                                }
                            }
                        }
                    }

                    // Contract filter chips
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "CONTRACT",
                            color = IosTextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(IosSegmentedBg)
                                .padding(2.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            listOf("ALL", "NRL", "SBLT").forEach { opt ->
                                val isSelected = contractFilter.equals(opt, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(if (isSelected) IosCardBackground else Color.Transparent)
                                        .clickable { viewModel.onContractFilterChange(opt) }
                                        .testTag("filter_contract_$opt"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = opt,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) IosDarkBlue else IosTextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // SHARING ACTION BUTTONS:
                // Row 1: WHATSAPP and MAIL
                // Row 2: EXCEL and CSV
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // WhatsApp Button
                    Button(
                        onClick = {
                            if (allDateEntries.isEmpty()) {
                                Toast.makeText(context, "No records to share for $selectedDate", Toast.LENGTH_SHORT).show()
                            } else {
                                try {
                                    val waIntent = ExcelReportGenerator.createWhatsAppIntent(
                                        context,
                                        selectedDate,
                                        allDateEntries,
                                        shiftFilter,
                                        tripFilter,
                                        contractFilter
                                    )
                                    context.startActivity(waIntent)
                                } catch (e: ActivityNotFoundException) {
                                    val generalIntent = ExcelReportGenerator.createGeneralShareIntent(
                                        context,
                                        selectedDate,
                                        allDateEntries
                                    )
                                    context.startActivity(generalIntent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Share error: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("whatsapp_share_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "WhatsApp",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "WhatsApp",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    // Mail Button
                    Button(
                        onClick = {
                            if (allDateEntries.isEmpty()) {
                                Toast.makeText(context, "No records to email for $selectedDate", Toast.LENGTH_SHORT).show()
                            } else {
                                try {
                                    val emailIntent = ExcelReportGenerator.createEmailIntent(
                                        context,
                                        selectedDate,
                                        allDateEntries,
                                        shiftFilter,
                                        tripFilter,
                                        contractFilter
                                    )
                                    context.startActivity(emailIntent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Email error: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IosDarkBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("email_share_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Mail",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "MAIL",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Row 2: EXCEL & CSV export buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            if (allDateEntries.isEmpty()) {
                                Toast.makeText(context, "No records to export", Toast.LENGTH_SHORT).show()
                            } else {
                                try {
                                    val intent = ExcelReportGenerator.createGeneralShareIntent(
                                        context,
                                        selectedDate,
                                        allDateEntries
                                    )
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF107C41)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF107C41)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("export_excel_button")
                    ) {
                        Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("EXCEL (.xls)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            if (allDateEntries.isEmpty()) {
                                Toast.makeText(context, "No records to export", Toast.LENGTH_SHORT).show()
                            } else {
                                try {
                                    val intent = ExcelReportGenerator.createCsvShareIntent(
                                        context,
                                        selectedDate,
                                        allDateEntries,
                                        shiftFilter,
                                        tripFilter,
                                        contractFilter
                                    )
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = IosBlue),
                        border = androidx.compose.foundation.BorderStroke(1.dp, IosBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("export_csv_button")
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("CSV (Sheet)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Slide Tabs: ALL / First / Day / Night
        ScrollableTabRow(
            selectedTabIndex = shiftSlideTabs.indexOf(shiftFilter).coerceAtLeast(0),
            containerColor = IosCardBackground,
            contentColor = IosBlue,
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                val tabIndex = shiftSlideTabs.indexOf(shiftFilter).coerceAtLeast(0)
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[tabIndex]),
                    color = IosBlue,
                    height = 3.dp
                )
            }
        ) {
            shiftSlideTabs.forEach { tabName ->
                val label = if (tabName == "ALL") "All Shifts" else "$tabName Shift"
                val isSelected = shiftFilter.equals(tabName, ignoreCase = true)
                Tab(
                    selected = isSelected,
                    onClick = { viewModel.onShiftFilterChange(tabName) },
                    text = {
                        Text(
                            text = label,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp,
                            color = if (isSelected) IosBlue else IosTextSecondary
                        )
                    },
                    modifier = Modifier.testTag("filter_tab_$tabName")
                )
            }
        }

        HorizontalDivider(color = IosBorder, thickness = 0.5.dp)

        // Main Report List & Tables
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            if (displayedSections.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = IosCardBackground),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, IosBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No bus movements recorded matching current filters for $selectedDate",
                            color = IosTextTertiary,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                displayedSections.forEach { section ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Section Header: Heading - Pickup / Date / Contract OR Heading - Drop / Date / Contract
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(if (section.isPickup) IosBlue else IosDarkBlue)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Heading - ${section.heading}",
                                color = if (section.isPickup) IosBlue else IosDarkBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "(${section.items.size} buses)",
                                color = IosTextSecondary,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Excel Table: Sl - Driver Name - Bus No - Route No - Route Name - Starting KM - time - Ending KM - time - Running Km - Remarks
                        ExcelTableView(
                            rows = section.items,
                            isPickup = section.isPickup
                        )

                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Monthly Bus Running KM Table (Bus-wise Total Distance)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = IosCardBackground),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, IosBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "MONTHLY BUS RUNNING KM (BUS-WISE)",
                        color = IosDarkBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Tracks buses running across shifts and cumulative mileage",
                        color = IosTextSecondary,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (monthlyBusSummaries.isEmpty()) {
                        Text(
                            text = "No movements recorded yet this month.",
                            color = IosTextTertiary,
                            fontSize = 12.sp
                        )
                    } else {
                        monthlyBusSummaries.forEachIndexed { idx, item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${idx + 1}.",
                                        color = IosTextSecondary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        modifier = Modifier.width(22.dp)
                                    )
                                    Column {
                                        Text(
                                            text = item.busNo,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = IosDarkBlue
                                        )
                                        Text(
                                            text = "${item.driverName} &bull; ${item.contract} &bull; ${item.totalTrips} trips",
                                            fontSize = 11.sp,
                                            color = IosTextSecondary
                                        )
                                    }
                                }

                                Surface(
                                    color = IosLightBlueTint,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "${formatKm(item.totalKm)} KM",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = IosBlue,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            if (idx < monthlyBusSummaries.size - 1) {
                                HorizontalDivider(color = IosBorder.copy(alpha = 0.5f), thickness = 0.5.dp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Transport Security Footer Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = IosCardBackground),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, IosBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Text(
                        text = "Transport Security,",
                        color = IosDarkBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Ashok Leyland - Ennore",
                        color = IosBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Direct file share available via WhatsApp, Email, Excel (.xls), and CSV Sheet.",
                        color = IosTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

private fun formatKm(km: Double): String {
    return if (km % 1.0 == 0.0) {
        String.format("%.0f", km)
    } else {
        String.format("%.1f", km)
    }
}
