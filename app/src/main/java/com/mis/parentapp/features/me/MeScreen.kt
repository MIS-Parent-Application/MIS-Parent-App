package com.mis.parentapp.features.me

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mis.parentapp.R
import com.mis.parentapp.ui.theme.AppTypes
import com.mis.parentapp.ui.theme.ColorsDefaultTheme
import com.mis.parentapp.ui.theme.ParentAppTheme

@Composable
fun MeScreen(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color.White
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 32.dp)
        ) {
            item {
                ProfileHeader()
            }
            item {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 24.dp, horizontal = 16.dp),
                    color = Color(0xFFE0E0E0),
                    thickness = 1.dp
                )
            }
            item {
                MenuSection(
                    title = "Connect",
                    items = listOf(
                        MeMenuItem("Your student", Icons.Default.School),
                        MeMenuItem("Instructors", Icons.Default.Forum),
                        MeMenuItem("Dean", Icons.Default.ManageAccounts),
                        MeMenuItem("Registrar’s Office", Icons.AutoMirrored.Filled.MenuBook),
                        MeMenuItem("Guidance Office", Icons.Default.Explore),
                        MeMenuItem("School Clinic", Icons.Default.MedicalServices),
                        MeMenuItem("Cashier’s Office", Icons.Default.Payments)
                    )
                )
            }
            item {
                Spacer(modifier = Modifier.height(32.dp))
                MenuSection(
                    title = "Support",
                    items = listOf(
                        MeMenuItem("Schedule a meeting", Icons.Default.CalendarMonth),
                        MeMenuItem("Send school a feedback", Icons.Default.Feedback)
                    )
                )
            }
            item {
                Spacer(modifier = Modifier.height(32.dp))
                SettingsSection()
            }
        }
    }
}

@Composable
fun ProfileHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.student_image),
            contentDescription = "Parent Profile Photo",
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Jordan B. McClure",
                style = AppTypes.type_H2,
                color = ColorsDefaultTheme.color_On_surface,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
            Text(
                text = "Primary Guardian",
                style = AppTypes.type_Body_Small,
                color = Color.Gray,
                fontSize = 16.sp
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.clickable { }
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = ColorsDefaultTheme.color_Primary_green,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Verified account",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun MenuSection(title: String, items: List<MeMenuItem>) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = title,
            style = AppTypes.type_H1,
            color = Color(0xFF1B4D13),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        items.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = ColorsDefaultTheme.color_On_surface
                )
                Text(
                    text = item.label,
                    fontSize = 16.sp,
                    color = Color(0xFF1B4D13).copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun SettingsSection() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Settings",
            style = AppTypes.type_H1,
            color = Color(0xFF1B4D13),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MeSettingsCard(
                title = "Preferences",
                description = "Set your own preference in using this app.",
                icon = Icons.Default.Tune,
                modifier = Modifier.weight(1f)
            )
            MeSettingsCard(
                title = "Data Safety",
                description = "Your data, your rules.",
                icon = Icons.Default.Lock,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun MeSettingsCard(title: String, description: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(160.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = ColorsDefaultTheme.color_Surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = Color(0xFF1B4D13)
            )
            Text(
                text = title,
                style = AppTypes.type_H2,
                color = Color(0xFF1B4D13),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                fontSize = 12.sp,
                color = Color.Gray,
                lineHeight = 16.sp
            )
        }
    }
}

data class MeMenuItem(val label: String, val icon: ImageVector)

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun MeScreenPreview() {
    ParentAppTheme {
        MeScreen()
    }
}
