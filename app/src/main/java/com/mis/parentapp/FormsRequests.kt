package com.mis.parentapp

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.mis.parentapp.ui.theme.ParentAppTheme


class FormsRequestsPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ParentAppTheme {

            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun FormsRequestsPagePreview() {
    ParentAppTheme {

    }
}