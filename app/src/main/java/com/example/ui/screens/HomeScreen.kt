package com.example.ui.screens

import android.app.DatePickerDialog
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import com.example.ui.getTodayDateString
import com.example.ui.theme.IosBackground
import com.example.ui.theme.IosBlue
import com.example.ui.theme.IosBorder
import com.example.ui.theme.IosCardBackground
import com.example.ui.theme.IosDarkBlue
import com.example.ui.theme.IosLightBlueTint
import com.example.ui.theme.IosSegmentedBg
import com.example.ui.theme.IosSubheadBlue
import com.example.ui.theme.IosTextSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: BusEntryViewModel,
    onNavigateToEntry: () -> Unit,
    onNavigateToShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val formState by viewModel.formState.collectAsState()
    val allDateEntries by viewModel.entriesForSelectedDate.collectAsState()
    val scrollState = rememberScrollState()

    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val selectedCalendar = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }
            val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedCalendar.time)
            viewModel.onDateChange(formattedDate)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(IosBackground)
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("home_screen_container")
    ) {
        // Ashok Leyland - Ennore / Bus Record / Transport Security Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = IosCardBackground),
            shape = RoundedCornerShape(14.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Text(
                    text = "Ashok Leyland - Ennore",
                    color = IosDarkBlue,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.4).sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Bus Record",
                    color = IosBlue,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Transport Security",
                    color = IosSubheadBlue,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // HOME SCREEN SELECTIONS:
        // 1. DATE - Today() Selection
        // 2. SHIFT - First / Day / Night
        // 3. TRIP - Pickup / Drop
        // 4. CONTRACT - NRL / SBLT
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = IosCardBackground),
            shape = RoundedCornerShape(14.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // 1. DATE (Today auto-select + picker)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "DATE",
                            color = IosTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.5.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = formState.date,
                                color = IosDarkBlue,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (formState.date == getTodayDateString()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = IosLightBlueTint,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "Today",
                                        color = IosBlue,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = { datePickerDialog.show() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = IosBlue),
                        border = androidx.compose.foundation.BorderStroke(1.dp, IosBlue),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("home_change_date_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Date",
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Select Date", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 2. SHIFT SELECTION (First / Day / Night)
                Text(
                    text = "SHIFT",
                    color = IosTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(IosSegmentedBg)
                        .padding(3.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("First", "Day", "Night").forEach { shiftOption ->
                            val isSelected = formState.shift.equals(shiftOption, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) IosCardBackground else Color.Transparent)
                                    .clickable { viewModel.onShiftChange(shiftOption) }
                                    .testTag("home_shift_$shiftOption"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = shiftOption,
                                    color = if (isSelected) IosDarkBlue else IosTextSecondary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 3. TRIP SELECTION (Pickup / Drop)
                Text(
                    text = "TRIP SELECTION",
                    color = IosTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(IosSegmentedBg)
                        .padding(3.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("Pickup", "Drop").forEach { tripOption ->
                            val isSelected = formState.tripType.equals(tripOption, ignoreCase = true)
                            val activeBg = if (tripOption.equals("Pickup", ignoreCase = true)) IosBlue else IosDarkBlue
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) activeBg else Color.Transparent)
                                    .clickable { viewModel.onTripTypeChange(tripOption) }
                                    .testTag("home_trip_$tripOption"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tripOption.uppercase(Locale.getDefault()),
                                    color = if (isSelected) Color.White else IosTextSecondary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 4. CONTRACT SELECTION (NRL / SBLT)
                Text(
                    text = "CONTRACT",
                    color = IosTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(IosSegmentedBg)
                        .padding(3.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("NRL", "SBLT").forEach { contractOption ->
                            val isSelected = formState.contract.equals(contractOption, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) IosCardBackground else Color.Transparent)
                                    .clickable { viewModel.onContractChange(contractOption) }
                                    .testTag("home_contract_$contractOption"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = contractOption,
                                    color = if (isSelected) IosDarkBlue else IosTextSecondary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // PRIMARY ACTION BUTTON: ENTER BUS DETAILS (Opens Entry Screen)
        val isPickup = formState.tripType.equals("Pickup", ignoreCase = true)
        val activeThemeColor = if (isPickup) IosBlue else IosDarkBlue

        Button(
            onClick = onNavigateToEntry,
            colors = ButtonDefaults.buttonColors(containerColor = activeThemeColor),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("open_entry_screen_button")
        ) {
            Icon(imageVector = Icons.Default.DirectionsBus, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "ENTER ${formState.tripType.uppercase()} (${formState.shift} &bull; ${formState.contract})",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
        }

        Spacer(modifier = Modifier.height(14.dp))

        // SHARE BUTTON (Opens Share Screen)
        OutlinedButton(
            onClick = onNavigateToShare,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = IosDarkBlue),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, IosDarkBlue),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("home_share_button")
        ) {
            Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "SHARE RECORDS (WhatsApp / Mail / Excel / CSV)",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Today's Movements Quick Overview Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = IosCardBackground),
            shape = RoundedCornerShape(14.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "TODAY'S LOGGED MOVEMENTS",
                    color = IosTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("First", "Day", "Night").forEach { shiftName ->
                        val shiftCount = allDateEntries.count { it.shift.equals(shiftName, ignoreCase = true) }
                        Surface(
                            color = IosLightBlueTint,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = shiftName,
                                    color = IosDarkBlue,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "$shiftCount buses",
                                    color = IosBlue,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}
