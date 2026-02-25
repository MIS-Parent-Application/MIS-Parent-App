package com.example.parentapp

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parentapp.ui.theme.ParentAppTheme

class StudentProfilePage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ParentAppTheme {

            }
        }
    }
}


@Composable
fun StudentProfile() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Header(
            "Student Profile",
            "View and manage student information"
        )
        Spacer(Modifier.height(20.dp))
        Surface (
            color = Color.White,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, Color.Gray)
        ) {
            Box(
                modifier = Modifier.padding(20.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Student Profile"
                        )
                    }
                    Spacer(
                        modifier = Modifier.height(20.dp)
                    )
                    Image(
                        painter = painterResource(R.drawable.profile_2398783_1280),
                        contentDescription = "Profile Picture",
                        modifier = Modifier.size(100.dp)
                    )
                    Spacer(
                        modifier = Modifier.height(20.dp)
                    )
                    Text(
                        text = "FirstName LastName",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Roll number"
                    )
                    Tags()
                    Spacer(
                        modifier = Modifier.height(20.dp)
                    )
                    Column {
                        Info(R.drawable.baseline_email_24, "example@email.com")
                        Info(R.drawable.baseline_local_phone_24, "+Telephoneee")
                        Info(R.drawable.baseline_calendar_month_24, "Admitted: August 17, 2020")
                        Info(R.drawable.baseline_person_24, "Blood type")
                        Info(
                            R.drawable.baseline_location_on_24,
                            "281 Street, Barangay, City, Zipcode"
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun Tags() {
    Text(
        // TODO: tags 
        text = "TODO: Tags Here"
    )
}


@Composable
fun Info(icon: Int, info: String) {
    Row{
        Image(
            painter = painterResource(icon),
            contentDescription = "$info icon"
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = info
        )
    }
}


@Preview(showBackground = true)
@Composable
fun StudentProfilePreview() {
    ParentAppTheme {
        StudentProfile()
    }
}