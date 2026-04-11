package com.mis.parentapp.features.services

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mis.parentapp.R
import com.mis.parentapp.ui.theme.AppTypes
import com.mis.parentapp.ui.theme.ColorsDefaultTheme
import com.mis.parentapp.ui.theme.ParentAppTheme

data class PeriodOption(val label: String, val displayText: String, val amountLabel: String)
data class FeeItem(val invoice: String, val item: String, val option: String, val date: String)

object PeriodData {
    val options = listOf(
        PeriodOption("Last year", "Showing data for the last 12 months.", "Total dues paid this year"),
        PeriodOption("Last month", "Showing data for the last 30 days.", "Total dues paid this month"),
        PeriodOption("Last week", "Showing data for the last 7 days.", "Total dues paid this week"),
        PeriodOption("Total", "Showing all time data.", "Overall total dues paid")
    )

    val lastYearFees = listOf(
        FeeItem("#02130001", "School uniform", "G-Cash", "03-10-25 | 9:00 AM"),
        FeeItem("#02130002", "P.E. uniform set", "G-Cash", "03-15-25 | 10:30 AM"),
        FeeItem("#02130003", "Workbook set", "Cash", "04-01-25 | 8:45 AM"),
        FeeItem("#02130004", "Laboratory fee", "G-Cash", "05-20-25 | 2:00 PM"),
        FeeItem("#02130005", "School ID", "Cash", "06-05-25 | 11:15 AM"),
        FeeItem("#02130006", "Yearbook fee", "G-Cash", "07-18-25 | 3:30 PM"),
        FeeItem("#02130007", "Field trip fee", "Cash", "08-22-25 | 9:15 AM"),
        FeeItem("#02130008", "Library fee", "G-Cash", "09-10-25 | 1:00 PM")
    )

    val lastMonthFees = listOf(
        FeeItem("#02134560", "School uniform", "G-Cash", "01-05-26 | 8:00 AM"),
        FeeItem("#02134561", "Workbook set", "Cash", "01-10-26 | 10:00 AM"),
        FeeItem("#02134562", "Laboratory fee", "G-Cash", "01-15-26 | 1:30 PM"),
        FeeItem("#02134563", "School ID", "Cash", "01-20-26 | 3:00 PM"),
        FeeItem("#02134564", "Library fee", "G-Cash", "01-28-26 | 11:00 AM")
    )

    val lastWeekFees = listOf(
        FeeItem("#02134565", "P.E. uniform set", "G-Cash", "02-25-26 | 2:00 PM"),
        FeeItem("#02134566", "Uniform set", "G-Cash", "02-25-26 | 2:16 PM"),
        FeeItem("#02134567", "Workbook set", "Cash", "02-26-26 | 9:30 AM")
    )

    val totalFees = lastYearFees + lastMonthFees + lastWeekFees
}

@Composable
fun ServicesScreen(modifier: Modifier = Modifier) {
    Body(modifier)
}

@Composable
fun Body(modifier: Modifier = Modifier) {
    var selectedIndex by remember { mutableStateOf(1) }

    val feeList = when (selectedIndex) {
        0 -> PeriodData.lastYearFees
        1 -> PeriodData.lastMonthFees
        2 -> PeriodData.lastWeekFees
        3 -> PeriodData.totalFees
        else -> PeriodData.lastMonthFees
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.Start,
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        item { HeaderSection() }

        item { FilterButtonsSection() }

        item {
            Image(
                painter = painterResource(id = R.drawable.program),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.FillWidth
            )
        }

        item { ContributionDuesSection() }

        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .wrapContentWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                PeriodData.options.forEachIndexed { index, option ->
                    val isSelected = index == selectedIndex
                    Button(
                        onClick = { selectedIndex = index },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) ColorsDefaultTheme.color_Primary_green else Color(0xFFF5F5F5),
                            contentColor = if (isSelected) Color.White else ColorsDefaultTheme.color_Surface_on_surface
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(text = option.label, style = AppTypes.type_M3_label_small)
                    }
                }
            }
        }

        item {
            Text(
                text = PeriodData.options[selectedIndex].displayText,
                style = AppTypes.type_M3_label_small,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        item {
            Text(
                text = PeriodData.options[selectedIndex].amountLabel,
                style = AppTypes.type_M3_label_small,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        if (feeList.isEmpty()) {
            item {
                Text(
                    text = "No records found.",
                    style = AppTypes.type_M3_label_small,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        } else {
            items(count = feeList.size, key = { feeList[it].invoice }) { index ->
                FeeCard(
                    invoice = feeList[index].invoice,
                    item = feeList[index].item,
                    option = feeList[index].option,
                    date = feeList[index].date,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
fun HeaderSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.school_logo),
            contentDescription = "School Logo",
            modifier = Modifier
                .size(56.dp)
                .clickable { }
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.formkit_date),
                contentDescription = "Date",
                modifier = Modifier
                    .size(32.dp)
                    .clickable { }
            )
            Image(
                painter = painterResource(id = R.drawable.ph_bell),
                contentDescription = "Notifications",
                modifier = Modifier
                    .size(32.dp)
                    .clickable { }
            )
            Image(
                painter = painterResource(id = R.drawable.studentswitcher),
                contentDescription = "Student Switcher",
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable { },
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun FilterButtonsSection() {
    var selectedFilter by remember { mutableStateOf("Accounting") }

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .horizontalScroll(rememberScrollState())
    ) {
        listOf("Accounting", "Forms & documents", "Payment options").forEach { label ->
            val isSelected = label == selectedFilter
            Button(
                onClick = { selectedFilter = label },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) ColorsDefaultTheme.color_Primary_green else Color(0xFFF5F5F5),
                    contentColor = if (isSelected) Color.White else ColorsDefaultTheme.color_Surface_on_surface
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text(text = label, style = AppTypes.type_M3_label_small)
            }
        }
    }
}

@Composable
fun ContributionDuesSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Miscellaneous Expenses",
            color = Color(0xFF1B4D13),
            style = AppTypes.type_H2,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard(
                label = "School uniform",
                value = "200",
                iconRes = R.drawable.hugeicons_school,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "P.E. uniform",
                value = "200",
                iconRes = R.drawable.material_symbols_light_physical_therapy_outline,
                modifier = Modifier.weight(1f)
            )
        }
        Button(
            onClick = { },
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ColorsDefaultTheme.color_Primary_green,
                contentColor = ColorsDefaultTheme.color_Yellow
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
        ) {
            Text(text = "Pay all", style = AppTypes.type_M3_label_small)
        }
    }
}

@Composable
fun StatCard(label: String, value: String, iconRes: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(140.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(ColorsDefaultTheme.color_Surface)
            .padding(16.dp)
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier
                .size(32.dp)
                .align(Alignment.TopStart),
            colorFilter = ColorFilter.tint(ColorsDefaultTheme.color_Primary_green)
        )
        Text(
            text = label,
            style = AppTypes.type_Caption,
            color = Color(0xFF1C1B1F),
            modifier = Modifier.align(Alignment.TopEnd)
        )
        Text(
            text = value,
            color = Color(0xFF1B4D13),
            style = TextStyle(fontSize = 40.sp, fontWeight = FontWeight.Bold),
            modifier = Modifier.align(Alignment.BottomStart)
        )
    }
}

@Composable
fun FeeCard(invoice: String, item: String, option: String, date: String, modifier: Modifier = Modifier) {
    val borderColor = Color(0xFF1B4D13)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ColorsDefaultTheme.color_Surface)
            .drawBehind {
                drawRoundRect(
                    color = borderColor,
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    ),
                    cornerRadius = CornerRadius(16.dp.toPx())
                )
            }
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF1B4D13),
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "PAID",
                    style = AppTypes.type_H2,
                    color = Color(0xFF1B4D13),
                    fontWeight = FontWeight.Bold
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "Invoice receipt", style = AppTypes.type_Caption, color = Color.Gray)
                Text(text = invoice, style = AppTypes.type_Caption, color = Color(0xFF1B4D13))
            }
            Icon(
                imageVector = Icons.Default.Download,
                contentDescription = "Download",
                tint = Color(0xFF1B4D13),
                modifier = Modifier.size(24.dp)
            )
        }

        HorizontalDivider(color = Color(0xFFE0E0E0))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            InfoColumn("Purchased item", item)
            InfoColumn("Payment option", option)
            InfoColumn("Paid date", date, Alignment.End)
        }
    }
}

@Composable
fun InfoColumn(label: String, value: String, alignment: Alignment.Horizontal = Alignment.Start) {
    Column(horizontalAlignment = alignment) {
        Text(text = label, style = AppTypes.type_Caption, color = Color.Gray)
        Text(text = value, style = AppTypes.type_Body_Small, color = Color.Black)
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun BodyPreview() {
    ParentAppTheme {
        ServicesScreen()
    }
}