package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ExcelBorder
import com.example.ui.theme.ExcelHeaderDropBg
import com.example.ui.theme.ExcelHeaderPickupBg
import com.example.ui.theme.ExcelHeaderText
import com.example.ui.theme.ExcelRowEven
import com.example.ui.theme.ExcelRowOdd
import com.example.ui.theme.IosBlue
import com.example.ui.theme.IosDarkBlue
import com.example.ui.theme.IosTextPrimary
import com.example.util.ExcelReportGenerator.SectionRow

@Composable
fun ExcelTableView(
    rows: List<SectionRow>,
    isPickup: Boolean = false,
    onDeleteRow: ((SectionRow) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (rows.isEmpty()) return

    val headerBg = if (isPickup) ExcelHeaderPickupBg else ExcelHeaderDropBg
    val scrollState = rememberScrollState()

    // Exact 11 Columns in the requested order:
    // Sl - Driver Name - Bus No - Route No - Route Name - Starting KM - time - Ending KM - time - Running Km - Remarks
    val colSl = 48.dp
    val colDriver = 135.dp
    val colBusNo = 125.dp
    val colRouteNo = 80.dp
    val colRouteName = 150.dp
    val colStartKm = 95.dp
    val colStartTime = 95.dp
    val colEndKm = 95.dp
    val colEndTime = 95.dp
    val colRunningKm = 100.dp
    val colRemarks = 130.dp
    val colActions = 52.dp

    val totalWidth = colSl + colDriver + colBusNo + colRouteNo + colRouteName +
            colStartKm + colStartTime + colEndKm + colEndTime + colRunningKm + colRemarks +
            if (onDeleteRow != null) colActions else 0.dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, ExcelBorder, RoundedCornerShape(8.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
        ) {
            Column(modifier = Modifier.width(totalWidth)) {
                // Table Header Row: Exact Order
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(headerBg),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ExcelHeaderCell("Sl", colSl)
                    ExcelHeaderCell("Driver Name", colDriver)
                    ExcelHeaderCell("Bus No", colBusNo)
                    ExcelHeaderCell("Route No", colRouteNo)
                    ExcelHeaderCell("Route Name", colRouteName)
                    ExcelHeaderCell("Starting KM", colStartKm)
                    ExcelHeaderCell("time", colStartTime)
                    ExcelHeaderCell("Ending KM", colEndKm)
                    ExcelHeaderCell("time", colEndTime)
                    ExcelHeaderCell("Running Km", colRunningKm)
                    ExcelHeaderCell("Remarks", colRemarks)
                    if (onDeleteRow != null) {
                        ExcelHeaderCell("Action", colActions)
                    }
                }

                // Table Data Rows
                var totalRunningKm = 0.0
                rows.forEachIndexed { index, row ->
                    totalRunningKm += row.runningKm
                    val rowBg = if (index % 2 == 0) ExcelRowEven else ExcelRowOdd

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(rowBg),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ExcelDataCell("${index + 1}", colSl, align = TextAlign.Center)
                        ExcelDataCell(row.driverName, colDriver)
                        ExcelDataCell(row.busNo, colBusNo, isBold = true)
                        ExcelDataCell(row.routeNo, colRouteNo, align = TextAlign.Center)
                        ExcelDataCell(row.routeName, colRouteName)
                        ExcelDataCell(row.startingKm.ifBlank { "-" }, colStartKm, align = TextAlign.Right)
                        ExcelDataCell(row.startingTime.ifBlank { "-" }, colStartTime, align = TextAlign.Center)
                        ExcelDataCell(row.endingKm.ifBlank { "-" }, colEndKm, align = TextAlign.Right)
                        ExcelDataCell(row.endingTime.ifBlank { "-" }, colEndTime, align = TextAlign.Center)
                        ExcelDataCell(formatKm(row.runningKm), colRunningKm, align = TextAlign.Right, isBold = true, textColor = IosBlue)
                        ExcelDataCell(row.remarks.ifBlank { "-" }, colRemarks)
                        if (onDeleteRow != null) {
                            Box(
                                modifier = Modifier
                                    .width(colActions)
                                    .border(0.5.dp, ExcelBorder)
                                    .padding(vertical = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                IconButton(
                                    onClick = { onDeleteRow(row) },
                                    modifier = Modifier.height(30.dp).width(30.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete entry",
                                        tint = Color(0xFFFF3B30)
                                    )
                                }
                            }
                        }
                    }
                }

                // Total Summary Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE2E8F0)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val labelWidth = colSl + colDriver + colBusNo + colRouteNo + colRouteName +
                            colStartKm + colStartTime + colEndKm + colEndTime
                    Box(
                        modifier = Modifier
                            .width(labelWidth)
                            .border(0.5.dp, ExcelBorder)
                            .padding(horizontal = 8.dp, vertical = 7.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Text(
                            text = "TOTAL RUNNING KM:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = IosDarkBlue
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(colRunningKm)
                            .border(0.5.dp, ExcelBorder)
                            .padding(horizontal = 6.dp, vertical = 7.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Text(
                            text = formatKm(totalRunningKm),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = IosBlue
                        )
                    }

                    val remainingWidth = colRemarks + if (onDeleteRow != null) colActions else 0.dp
                    Box(
                        modifier = Modifier
                            .width(remainingWidth)
                            .border(0.5.dp, ExcelBorder)
                            .height(31.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ExcelHeaderCell(
    text: String,
    width: Dp
) {
    Box(
        modifier = Modifier
            .width(width)
            .border(0.5.dp, Color(0xFF1C1C1E))
            .padding(horizontal = 6.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = ExcelHeaderText,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ExcelDataCell(
    text: String,
    width: Dp,
    align: TextAlign = TextAlign.Start,
    isBold: Boolean = false,
    textColor: Color = IosTextPrimary
) {
    Box(
        modifier = Modifier
            .width(width)
            .border(0.5.dp, ExcelBorder)
            .padding(horizontal = 6.dp, vertical = 8.dp),
        contentAlignment = when (align) {
            TextAlign.Center -> Alignment.Center
            TextAlign.Right, TextAlign.End -> Alignment.CenterEnd
            else -> Alignment.CenterStart
        }
    ) {
        Text(
            text = text,
            color = textColor,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            fontSize = 12.sp,
            textAlign = align,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
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
