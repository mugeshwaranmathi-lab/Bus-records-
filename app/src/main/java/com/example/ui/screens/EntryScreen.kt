package com.example.ui.screens

import android.app.Activity
import android.app.TimePickerDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.BusEntryViewModel
import com.example.ui.theme.IosBackground
import com.example.ui.theme.IosBlue
import com.example.ui.theme.IosBorder
import com.example.ui.theme.IosCardBackground
import com.example.ui.theme.IosDarkBlue
import com.example.ui.theme.IosErrorRed
import com.example.ui.theme.IosLightBlueTint
import com.example.ui.theme.IosSubheadBlue
import com.example.ui.theme.IosTextPrimary
import com.example.ui.theme.IosTextSecondary
import com.example.ui.theme.IosTextTertiary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun EntryScreen(
    viewModel: BusEntryViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val formState by viewModel.formState.collectAsState()
    val scrollState = rememberScrollState()

    var showScannerDialog by remember { mutableStateOf(false) }
    var manualScanInput by remember { mutableStateOf("") }

    val scanLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val scannedContents = result.data?.getStringExtra("SCAN_RESULT")
            if (!scannedContents.isNullOrBlank()) {
                viewModel.onBusNoChange(scannedContents.trim())
                Toast.makeText(context, "Bus No Scanned: $scannedContents", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val isPickup = formState.tripType.equals("Pickup", ignoreCase = true)
    val themeColor = if (isPickup) IosBlue else IosDarkBlue

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(IosBackground)
    ) {
        // Heading Bar: Date - Shift - Trip (as requested by user)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = IosCardBackground),
            shape = RoundedCornerShape(0.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.size(36.dp).testTag("entry_back_button")
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
                        text = "${formState.date} - ${formState.shift} - ${formState.tripType}",
                        color = IosDarkBlue,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.2).sp
                    )
                    Text(
                        text = "Ashok Leyland - Ennore &bull; Contract: ${formState.contract}",
                        color = themeColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Contract Toggle Chip
                Surface(
                    color = IosLightBlueTint,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, IosBlue.copy(alpha = 0.3f)),
                    modifier = Modifier.clickable {
                        val nextContract = if (formState.contract == "NRL") "SBLT" else "NRL"
                        viewModel.onContractChange(nextContract)
                    }
                ) {
                    Text(
                        text = formState.contract,
                        color = IosDarkBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // ENTRY DETAILS FORM CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = IosCardBackground),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    // 1. Bus No - Manual / Scan
                    Text(
                        text = "Bus No *",
                        color = IosDarkBlue,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = formState.busNo,
                            onValueChange = { viewModel.onBusNoChange(it) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("bus_no_input"),
                            placeholder = { Text("e.g. TN-04-AL-1001", color = IosTextTertiary) },
                            leadingIcon = {
                                Icon(Icons.Default.DirectionsBus, contentDescription = null, tint = themeColor)
                            },
                            colors = iosTextFieldColors(themeColor),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                try {
                                    val intent = Intent("com.google.zxing.client.android.SCAN")
                                    intent.putExtra("SCAN_MODE", "QR_CODE,PRODUCT_MODE,CODE_128,CODE_39")
                                    scanLauncher.launch(intent)
                                } catch (e: ActivityNotFoundException) {
                                    showScannerDialog = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .height(54.dp)
                                .testTag("scan_bus_no_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = "Scan",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Scan", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Auto-lookup banner (shows vehicle history, drop -> pickup connection)
                    AnimatedVisibility(visible = formState.autoLookupInfo != null) {
                        Column {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = IosLightBlueTint,
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, IosBlue.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = IosBlue,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = formState.autoLookupInfo ?: "",
                                        color = IosDarkBlue,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 2. Driver Name
                    Text(
                        text = "Driver Name *",
                        color = IosDarkBlue,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = formState.driverName,
                        onValueChange = { viewModel.onDriverNameChange(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("driver_name_input"),
                        placeholder = { Text("e.g. K. Murugan", color = IosTextTertiary) },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = themeColor)
                        },
                        colors = iosTextFieldColors(themeColor),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 3. Route No & Name
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(modifier = Modifier.weight(0.4f)) {
                            Text(
                                text = "Route No *",
                                color = IosDarkBlue,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = formState.routeNo,
                                onValueChange = { viewModel.onRouteNoChange(it) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("route_no_input"),
                                placeholder = { Text("R-01", color = IosTextTertiary) },
                                colors = iosTextFieldColors(themeColor),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters)
                            )
                        }

                        Column(modifier = Modifier.weight(0.6f)) {
                            Text(
                                text = "Route Name",
                                color = IosDarkBlue,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = formState.routeName,
                                onValueChange = { viewModel.onRouteNameChange(it) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("route_name_input"),
                                placeholder = { Text("Tiruvottiyur to Gate 1", color = IosTextTertiary) },
                                colors = iosTextFieldColors(themeColor),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 4. Starting Time & Starting KM
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Starting KM
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Starting KM *",
                                color = IosDarkBlue,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = formState.startingKm,
                                onValueChange = { viewModel.onStartingKmChange(it) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("starting_km_input"),
                                placeholder = { Text("14220", color = IosTextTertiary) },
                                leadingIcon = {
                                    Icon(Icons.Default.Speed, contentDescription = null, tint = themeColor)
                                },
                                colors = iosTextFieldColors(themeColor),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }

                        // Starting Time
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Starting Time *",
                                    color = IosDarkBlue,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "NOW",
                                    color = themeColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .clickable { viewModel.setStartingTimeToNow() }
                                        .padding(2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = formState.startingTime,
                                onValueChange = { viewModel.onStartingTimeChange(it) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("starting_time_input"),
                                placeholder = { Text("05:30 AM", color = IosTextTertiary) },
                                trailingIcon = {
                                    IconButton(onClick = {
                                        val now = Calendar.getInstance()
                                        TimePickerDialog(context, { _, hour, minute ->
                                            val cal = Calendar.getInstance().apply {
                                                set(Calendar.HOUR_OF_DAY, hour)
                                                set(Calendar.MINUTE, minute)
                                            }
                                            viewModel.onStartingTimeChange(
                                                SimpleDateFormat("hh:mm a", Locale.getDefault()).format(cal.time)
                                            )
                                        }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), false).show()
                                    }) {
                                        Icon(Icons.Default.AccessTime, contentDescription = "Time", tint = themeColor)
                                    }
                                },
                                colors = iosTextFieldColors(themeColor),
                                singleLine = true
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 5. Ending Time & Ending KM
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Ending KM
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Ending KM *",
                                color = IosDarkBlue,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = formState.endingKm,
                                onValueChange = { viewModel.onEndingKmChange(it) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("ending_km_input"),
                                placeholder = { Text("14250", color = IosTextTertiary) },
                                leadingIcon = {
                                    Icon(Icons.Default.Speed, contentDescription = null, tint = themeColor)
                                },
                                colors = iosTextFieldColors(themeColor),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }

                        // Ending Time
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Ending Time *",
                                    color = IosDarkBlue,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "NOW",
                                    color = themeColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .clickable { viewModel.setEndingTimeToNow() }
                                        .padding(2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = formState.endingTime,
                                onValueChange = { viewModel.onEndingTimeChange(it) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("ending_time_input"),
                                placeholder = { Text("06:45 AM", color = IosTextTertiary) },
                                trailingIcon = {
                                    IconButton(onClick = {
                                        val now = Calendar.getInstance()
                                        TimePickerDialog(context, { _, hour, minute ->
                                            val cal = Calendar.getInstance().apply {
                                                set(Calendar.HOUR_OF_DAY, hour)
                                                set(Calendar.MINUTE, minute)
                                            }
                                            viewModel.onEndingTimeChange(
                                                SimpleDateFormat("hh:mm a", Locale.getDefault()).format(cal.time)
                                            )
                                        }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), false).show()
                                    }) {
                                        Icon(Icons.Default.AccessTime, contentDescription = "Time", tint = themeColor)
                                    }
                                },
                                colors = iosTextFieldColors(themeColor),
                                singleLine = true
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 6. RUNNING KM (Auto Calculated: Ending KM - Starting KM)
                    Surface(
                        color = IosLightBlueTint,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, IosBlue.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "RUNNING KM (AUTO CALCULATED)",
                                    color = IosTextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "Ending KM - Starting KM",
                                    color = IosTextTertiary,
                                    fontSize = 11.sp
                                )
                            }

                            Surface(
                                color = themeColor,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "${formatKm(formState.runningKm)} KM",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    modifier = Modifier
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                        .testTag("running_km_badge")
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 7. Remarks (Small Box Entry)
                    Text(
                        text = "Remarks",
                        color = IosDarkBlue,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = formState.remarks,
                        onValueChange = { viewModel.onRemarksChange(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(76.dp)
                            .testTag("remarks_input"),
                        placeholder = { Text("Gate check notes or security remarks", color = IosTextTertiary) },
                        colors = iosTextFieldColors(themeColor),
                        maxLines = 2
                    )

                    // Error Message
                    if (!formState.errorMessage.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            color = Color(0xFFFEE2E2),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, IosErrorRed.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = "Error",
                                    tint = IosErrorRed
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = formState.errorMessage ?: "",
                                    color = Color(0xFFB91C1C),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Save Button
                    Button(
                        onClick = { viewModel.saveEntry() },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("save_records_button")
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SAVE ${formState.tripType.uppercase()} RECORD",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.clearForm() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = IosTextSecondary),
                            border = androidx.compose.foundation.BorderStroke(1.dp, IosBorder),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("clear_form_button")
                        ) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Clear", fontSize = 13.sp)
                        }

                        OutlinedButton(
                            onClick = onNavigateBack,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = IosDarkBlue),
                            border = androidx.compose.foundation.BorderStroke(1.dp, IosDarkBlue),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("back_to_home_button")
                        ) {
                            Text("Back to Home", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }

    if (showScannerDialog) {
        AlertDialog(
            onDismissRequest = { showScannerDialog = false },
            title = {
                Text("Scan Bus No", color = IosDarkBlue, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        "No external scanner app detected. You can install a barcode scanner from Google Play or type the Bus No barcode value:",
                        color = IosTextPrimary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = manualScanInput,
                        onValueChange = { manualScanInput = it },
                        placeholder = { Text("e.g. TN-04-AL-1008") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = iosTextFieldColors(themeColor)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (manualScanInput.isNotBlank()) {
                            viewModel.onBusNoChange(manualScanInput.trim())
                        }
                        showScannerDialog = false
                        manualScanInput = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColor)
                ) {
                    Text("Apply Bus No")
                }
            },
            dismissButton = {
                TextButton(onClick = { showScannerDialog = false }) {
                    Text("Cancel", color = IosTextSecondary)
                }
            }
        )
    }
}

private fun formatKm(km: Double): String {
    return if (km % 1.0 == 0.0) {
        String.format("%.0f", km)
    } else {
        String.format("%.1f", km)
    }
}

@Composable
private fun iosTextFieldColors(activeColor: Color = IosBlue) = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = activeColor,
    unfocusedBorderColor = IosBorder,
    focusedTextColor = IosTextPrimary,
    unfocusedTextColor = IosTextPrimary,
    focusedContainerColor = IosCardBackground,
    unfocusedContainerColor = IosCardBackground,
    cursorColor = activeColor
)
