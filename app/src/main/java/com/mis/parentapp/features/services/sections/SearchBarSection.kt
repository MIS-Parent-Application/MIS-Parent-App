package com.mis.parentapp.features.services.sections

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mis.parentapp.R
import com.mis.parentapp.network.Child
import androidx.compose.ui.text.TextStyle
import androidx.navigation.NavController

@Composable
fun SearchBarSection(
    selectedStudent: Child?,
    navController: NavController,
    onProfileClick: () -> Unit,
    onQrClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    var searchQuery by remember { mutableStateOf("") }

    // Navigation actions based on search
    fun handleSearchNavigation(query: String) {

        when {

            query.contains("document", ignoreCase = true) -> {
                navController.navigate("DocumentScreen")
            }

            query.contains("faq", ignoreCase = true) ||
                    query.contains("faqs", ignoreCase = true) ||
                    query.contains("question", ignoreCase = true) -> {
                navController.navigate("FAQsScreen")
            }

            query.contains("form", ignoreCase = true) ||
                    query.contains("request", ignoreCase = true) -> {
                navController.navigate("FormsAndRequestScreen")
            }

            query.contains("payment", ignoreCase = true) ||
                    query.contains("option", ignoreCase = true) -> {
                navController.navigate("PaymentOptionScreen")
            }
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        // Search Field
        BasicTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it

                // Automatically navigate when keywords are typed
                handleSearchNavigation(it)
            },
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp
            ),
            modifier = Modifier.weight(1f),
            singleLine = true,
            decorationBox = { innerTextField ->

                // Placeholder
                if (searchQuery.isEmpty()) {
                    Text(
                        text = "Search forms or documents",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontSize = 14.sp
                    )
                }

                innerTextField()
            }
        )

        Spacer(modifier = Modifier.width(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Profile Picture
            Image(
                painter = painterResource(id = R.drawable.student_image),
                contentDescription = "Profile",
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable { onProfileClick() },
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            // QR Code Icon
            Icon(
                imageVector = Icons.Default.QrCodeScanner,
                contentDescription = "QR Scanner",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onQrClick() }
            )
        }
    }
}