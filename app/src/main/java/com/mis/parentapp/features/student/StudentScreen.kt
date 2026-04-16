package com.mis.parentapp.features.student

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch // ADDED: For closing the bottom sheet
import com.mis.parentapp.R
import com.mis.parentapp.ui.theme.AppTypes
import com.mis.parentapp.ui.theme.ColorsDefaultTheme
import com.mis.parentapp.ui.theme.ParentAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
// CHANGED: Added navigation parameters so the main app can switch screens
fun StudentScreen(
    modifier: Modifier = Modifier,
    onNavigateToAcademic: () -> Unit = {},
    onNavigateToAttendance: () -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope() // ADDED: To handle smooth bottom sheet closing

    Box(modifier = modifier.fillMaxSize().background(Color.White)) {
        // Cover Photo Background
        Image(
            painter = painterResource(id = R.drawable.student_image),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
            contentScale = ContentScale.Crop,
            colorFilter = ColorFilter.tint(Color.Black.copy(alpha = 0.3f), androidx.compose.ui.graphics.BlendMode.Darken)
        )

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                HeaderIcons(onMenuClick = { showBottomSheet = true })
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }

            item {
                SwitcherSection()
            }

            item {
                // Main Content Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .background(Color.White)
                        .padding(bottom = 32.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                        Spacer(modifier = Modifier.height(28.dp))
                        StudentProfileInfo()
                        Spacer(modifier = Modifier.height(28.dp))
                        HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(28.dp))
                        AcademicProgramSection()
                        Spacer(modifier = Modifier.height(32.dp))
                        ClassScheduleSection()
                        Spacer(modifier = Modifier.height(32.dp))
                        ContactsSection()
                    }
                }
            }
        }

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                containerColor = Color.White,
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                // CHANGED: Pass the click events into the menu content
                StudentMenuContent(
                    onAcademicClick = {
                        coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                showBottomSheet = false
                                onNavigateToAcademic()
                            }
                        }
                    },
                    onAttendanceClick = {
                        coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                showBottomSheet = false
                                onNavigateToAttendance()
                            }
                        }
                    }
                )
            }
        }
    }
}

// ... HeaderIcons remains exactly the same ...
@Composable
fun HeaderIcons(onMenuClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.school_logo),
            contentDescription = "School Logo",
            modifier = Modifier.size(56.dp)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Image(
                painter = painterResource(id = R.drawable.formkit_date),
                contentDescription = "Date",
                modifier = Modifier.size(32.dp),
                colorFilter = ColorFilter.tint(Color.White)
            )
            Image(
                painter = painterResource(id = R.drawable.ph_bell),
                contentDescription = "Notifications",
                modifier = Modifier.size(32.dp),
                colorFilter = ColorFilter.tint(Color.White)
            )
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Menu",
                tint = Color.White,
                modifier = Modifier
                    .size(32.dp)
                    .clickable { onMenuClick() }
            )
        }
    }
}

// CHANGED: Added click handlers
@Composable
fun StudentMenuContent(
    onAcademicClick: () -> Unit,
    onAttendanceClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, bottom = 48.dp, top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        StudentMenuItem(
            icon = Icons.Outlined.PersonOutline,
            title = "About Student",
            description = "Know the information that you student has.",
            onClick = { /* Handle About Student */ }
        )
        StudentMenuItem(
            icon = Icons.Default.School,
            title = "Monitor Academic",
            description = "Check the progress and milestone of your student.",
            onClick = onAcademicClick // Linked
        )
        StudentMenuItem(
            icon = Icons.Outlined.EventAvailable,
            title = "Track Attendance",
            description = "Be updated to your student attendance.",
            onClick = onAttendanceClick // Linked
        )
    }
}

// CHANGED: Added the onClick parameter
@Composable
fun StudentMenuItem(icon: ImageVector, title: String, description: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }, // Linked the click event here
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = ColorsDefaultTheme.color_On_surface
        )
        Column {
            Text(
                text = title,
                color = Color(0xFF1B4D13),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = description,
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
    }
}

// ... All other composables (SwitcherSection, StudentProfileInfo, AcademicProgramSection, ProgramItem, ClassScheduleSection, ScheduleCardSmall, ContactsSection, ContactItem) remain exactly the same ...
@Composable
fun SwitcherSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.studentswitcher),
            contentDescription = "Student Switcher",
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .border(2.dp, Color.White, CircleShape)
                .clickable { },
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.9f))
                .clickable { },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add",
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun StudentProfileInfo() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.student_image),
            contentDescription = "Student Photo",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .border(4.dp, Color.White, CircleShape),
            contentScale = ContentScale.Crop
        )
        Column {
            Text(
                text = "Nathaniel B. McClure",
                style = AppTypes.type_H2,
                color = ColorsDefaultTheme.color_On_surface
            )
            Text(
                text = "Student’s grade level",
                style = AppTypes.type_Body_Small,
                color = ColorsDefaultTheme.color_Outline
            )
            Text(
                text = "ID no.: XXXXXX",
                style = AppTypes.type_Caption,
                color = ColorsDefaultTheme.color_Outline
            )
        }
    }
}

@Composable
fun AcademicProgramSection() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Academic Program",
            style = AppTypes.type_H1,
            color = ColorsDefaultTheme.color_Primary_green_container
        )
        ProgramItem(icon = Icons.Default.School, text = "Bachelor of Science in Information Technology")
        ProgramItem(icon = Icons.Default.Star, text = "BSIT - 3rd year")
        ProgramItem(icon = Icons.Default.Verified, text = "Officially enrolled for A.Y. 2025-2026")
    }
}

@Composable
fun ProgramItem(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ColorsDefaultTheme.color_On_surface,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = text,
            style = AppTypes.type_Body_Small,
            color = ColorsDefaultTheme.color_On_surface.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun ClassScheduleSection() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Class Schedule",
                style = AppTypes.type_H1,
                color = ColorsDefaultTheme.color_Primary_green_container
            )
            Icon(
                imageVector = Icons.Default.GridView,
                contentDescription = "Schedule View",
                tint = ColorsDefaultTheme.color_Outline,
                modifier = Modifier.size(28.dp)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ScheduleCardSmall(
                status = "Now",
                subject = "MATH 101",
                room = "Room 402",
                time = "10:00 - 11:30 AM",
                iconRes = R.drawable.basil_current_location_outline,
                isHighlight = true,
                modifier = Modifier.weight(1f)
            )
            ScheduleCardSmall(
                status = "Up next",
                subject = "VACANT",
                room = "-",
                time = "11:30 - 1:30 PM",
                iconRes = R.drawable.ic_outline_watch_later,
                isHighlight = false,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun ScheduleCardSmall(
    status: String,
    subject: String,
    room: String,
    time: String,
    iconRes: Int,
    isHighlight: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .requiredHeight(140.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (isHighlight) ColorsDefaultTheme.color_Primary_green_container else ColorsDefaultTheme.color_Surface)
            .padding(16.dp)
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier
                .size(32.dp)
                .align(Alignment.TopStart),
            colorFilter = ColorFilter.tint(ColorsDefaultTheme.color_Primary_on_green)
        )
        Text(
            text = status,
            fontSize = 12.sp,
            color = if (isHighlight) Color.White.copy(alpha = 0.7f) else ColorsDefaultTheme.color_Outline,
            modifier = Modifier.align(Alignment.TopEnd)
        )
        Text(
            text = subject,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = if (isHighlight) Color.White else ColorsDefaultTheme.color_On_surface,
            modifier = Modifier.align(Alignment.CenterStart)
        )
        Column(modifier = Modifier.align(Alignment.BottomStart)) {
            Text(
                text = room,
                fontSize = 14.sp,
                color = if (isHighlight) Color.White else ColorsDefaultTheme.color_On_surface
            )
            Text(
                text = time,
                fontSize = 12.sp,
                color = if (isHighlight) Color.White.copy(alpha = 0.9f) else ColorsDefaultTheme.color_On_surface
            )
        }
    }
}

@Composable
fun ContactsSection() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Contacts",
            style = AppTypes.type_H1,
            color = ColorsDefaultTheme.color_Primary_green_container
        )
        ContactItem(name = "John Doe B. McClure", relation = "Parent", phone = "+63 1234567890", isEmergency = false)
        ContactItem(name = "Thomas B. McClure", relation = "Emergency contact", phone = "+63 1234567890", isEmergency = true)

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { },
                modifier = Modifier.weight(1f).height(40.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ColorsDefaultTheme.color_Primary_green_container)
            ) {
                Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text("Make a call", style = AppTypes.type_M3_label_small)
            }
            Button(
                onClick = { },
                modifier = Modifier.weight(1f).height(40.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5F5F5), contentColor = ColorsDefaultTheme.color_On_surface)
            ) {
                Text("Edit contacts", style = AppTypes.type_M3_label_small)
            }
        }
    }
}

@Composable
fun ContactItem(name: String, relation: String, phone: String, isEmergency: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(painterResource(id = R.drawable.fluent_person_16_regular), contentDescription = null, modifier = Modifier.size(24.dp))
            Text(text = name, style = AppTypes.type_Body_Small, fontWeight = FontWeight.Bold)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isEmergency) ColorsDefaultTheme.color_Error else ColorsDefaultTheme.color_Primary_green_container)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(text = relation, color = Color.White, style = AppTypes.type_Caption, fontSize = 10.sp)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(painterResource(id = R.drawable.mdi_light_phone), contentDescription = null, modifier = Modifier.size(24.dp))
            Text(text = phone, style = AppTypes.type_Body_Small, color = ColorsDefaultTheme.color_Outline)
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun StudentScreenPreview() {
    ParentAppTheme {
        StudentScreen()
    }
}