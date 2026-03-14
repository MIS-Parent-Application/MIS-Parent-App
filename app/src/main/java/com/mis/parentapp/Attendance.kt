package com.mis.parentapp

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.mis.parentapp.ui.theme.ParentAppTheme

/**
 * [AttendancePage] is an [AppCompatActivity] that serves as the entry point
 * for the attendance-related features of the Parent App.
 *
 * It initializes the UI using Jetpack Compose and applies the [ParentAppTheme].
 */
class AttendancePage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ParentAppTheme {

            }
        }
    }
}

/**
 * A preview function for the [AttendancePage] UI components.
 * This allows developers to visualize the attendance screen layout within Android Studio.
 */
@Preview(showBackground = true)
@Composable
fun AttendancePagePreview() {
    ParentAppTheme {
    }
}
