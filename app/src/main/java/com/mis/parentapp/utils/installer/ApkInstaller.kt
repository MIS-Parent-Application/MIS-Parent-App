package com.mis.parentapp.utils.installer

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File

/**
 * A simple UI component that triggers the APK installation.
 * Pass the downloaded [File] to this Composable to let the user start the update.
 */
@Composable
fun UpdateScreen(downloadedFile: File?) {
    val context = LocalContext.current // Grabs the current Android Context safely

    Button(
        onClick = {
            if (downloadedFile != null && downloadedFile.exists()) {
                // Call your object utility cleanly
                ApkInstaller.install(context, downloadedFile)
            }
        }
    ) {
        Text("Install Update")
    }
}

object ApkInstaller {

    fun install(context: Context, apkFile: File) {
        // Handle the "Unknown Sources" check seamlessly right before running the installer
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!context.packageManager.canRequestPackageInstalls()) {
                val settingsIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(settingsIntent)
                return // Stop execution so they can grant permission first
            }
        }

        // Process the installer intent
        val apkUri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        context.startActivity(intent)
    }
}