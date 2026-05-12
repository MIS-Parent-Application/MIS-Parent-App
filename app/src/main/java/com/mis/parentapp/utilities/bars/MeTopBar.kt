package com.mis.parentapp.utilities.bars

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mis.parentapp.R

@Composable
fun MeTopBar(){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp), // More compact padding
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.school_logo),
            contentDescription = "School Logo",
            modifier = Modifier.size(40.dp) // Smaller logo
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

            IconButton(
                onClick = {},
                modifier = Modifier
                    .size(36.dp) // Smaller button size
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = stringResource(R.string.content_disc_acad_cal),
                    modifier = Modifier.size(20.dp), // Smaller icon
                    tint = Color.White
                )
            }

            IconButton(
                onClick = {},
                modifier = Modifier
                    .size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = stringResource(R.string.content_disc_notif),
                    modifier = Modifier.size(20.dp),
                    tint = Color.White
                )
            }

            IconButton(
                onClick = {},
                modifier = Modifier
                    .size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = stringResource(R.string.content_disc_about),
                    modifier = Modifier.size(20.dp),
                    tint = Color.White
                )
            }

        }
    }
}