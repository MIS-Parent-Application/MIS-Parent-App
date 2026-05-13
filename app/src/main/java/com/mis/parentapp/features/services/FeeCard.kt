package com.mis.parentapp.features.services

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mis.parentapp.ui.theme.AppTypes

@Composable
fun FeeCard(
    invoice: String,
    item: String,
    option: String,
    date: String,
    onDownload: () -> Unit = {}
) {
    val borderColor = MaterialTheme.colorScheme.primary
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
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
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(text = "PAID", style = AppTypes.type_H2, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "Invoice receipt", style = AppTypes.type_Caption, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Text(text = invoice, style = AppTypes.type_Caption, color = MaterialTheme.colorScheme.primary)
            }
            Icon(
                imageVector = Icons.Default.Download,
                contentDescription = "Download",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onDownload() }
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

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
        Text(text = label, style = AppTypes.type_Caption, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Text(text = value, style = AppTypes.type_Body_Small, color = MaterialTheme.colorScheme.onSurface)
    }
}
