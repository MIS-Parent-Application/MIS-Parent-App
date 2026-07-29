package com.mis.parentapp.features.me.settings

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mis.parentapp.features.me.UserProfileViewModel
import com.mis.parentapp.utils.images.InitialsImageFallback
import com.mis.parentapp.utils.images.RemoteImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    userProfileViewModel: UserProfileViewModel = viewModel(),
    onSaveSuccess: () -> Unit = {}
) {
    var name by remember { mutableStateOf(userProfileViewModel.fullName) }
    var email by remember { mutableStateOf(userProfileViewModel.email) }
    var phone by remember { mutableStateOf(userProfileViewModel.phoneNumber) }
    val context = LocalContext.current
    
    var showImageOptions by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    val isSaving = userProfileViewModel.isSavingProfile

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let {
            val inputStream = context.contentResolver.openInputStream(it)
            userProfileViewModel.updateProfileImage(inputStream, it)
        }
    }

    // Error handling
    LaunchedEffect(userProfileViewModel.errorMessage) {
        userProfileViewModel.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            userProfileViewModel.errorMessage = null
        }
    }

    // Sync state when data loads
    LaunchedEffect(userProfileViewModel.fullName) { if (!isSaving) name = userProfileViewModel.fullName }
    LaunchedEffect(userProfileViewModel.email) { if (!isSaving) email = userProfileViewModel.email }
    LaunchedEffect(userProfileViewModel.phoneNumber) { if (!isSaving) phone = userProfileViewModel.phoneNumber }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            val imageModifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)

            if (userProfileViewModel.profileBitmap != null) {
                Image(
                    bitmap = userProfileViewModel.profileBitmap!!,
                    contentDescription = null,
                    modifier = imageModifier,
                    contentScale = ContentScale.Crop
                )
            } else if (!userProfileViewModel.profileImageUrl.isNullOrBlank()) {
                RemoteImage(
                    url = userProfileViewModel.profileImageUrl,
                    fallbackRes = userProfileViewModel.profileImageRes,
                    contentDescription = null,
                    modifier = imageModifier,
                    contentScale = ContentScale.Crop,
                    fallbackContent = {
                        InitialsImageFallback(
                            name = userProfileViewModel.fullName,
                            modifier = imageModifier
                        )
                    }
                )
            } else {
                InitialsImageFallback(
                    name = userProfileViewModel.fullName,
                    modifier = imageModifier
                )
            }
            
            Surface(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable(enabled = !isSaving) { 
                        showImageOptions = true
                    },
                color = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.padding(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            readOnly = true,
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Address") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            enabled = !isSaving,
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone Number") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            enabled = !isSaving,
            singleLine = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { 
                userProfileViewModel.updateProfile(name, email, phone) {
                    Toast.makeText(context, "Changes saved successfully", Toast.LENGTH_SHORT).show()
                    onSaveSuccess()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = !isSaving,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(text = "Save Changes", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showImageOptions) {
        ModalBottomSheet(
            onDismissRequest = { showImageOptions = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Profile Picture",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                ListItem(
                    headlineContent = { Text("Upload New Photo") },
                    leadingContent = { Icon(Icons.Default.PhotoLibrary, contentDescription = null) },
                    modifier = Modifier.clickable {
                        showImageOptions = false
                        launcher.launch("image/*")
                    }
                )
                
                if (userProfileViewModel.profileBitmap != null || !userProfileViewModel.profileImageUrl.isNullOrBlank()) {
                    ListItem(
                        headlineContent = { Text("Remove Photo", color = MaterialTheme.colorScheme.error) },
                        leadingContent = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                        modifier = Modifier.clickable {
                            showImageOptions = false
                            userProfileViewModel.deleteProfileImage()
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
