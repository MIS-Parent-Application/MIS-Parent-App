package com.example.parentapp

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parentapp.ui.theme.ParentAppTheme


class CommonComposables : AppCompatActivity() {
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
fun Header(mainHeader: String, subHeader: String) {
    Column {
        Text(
            text = mainHeader,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = subHeader,
            color = Color.DarkGray
        )
    }
}


@Composable
fun TextWithBG(text: String, bgColor: Color) {
    Text(
        text = text,
        color = Color.White,
        modifier = Modifier.background(bgColor, shape = RoundedCornerShape(8.dp))
            .padding(
                top = 3.dp,
                bottom = 3.dp,
                start = 15.dp,
                end = 15.dp
            )
    )
}


@Composable
fun StatBox(icon: Int, name: String, stat: String, comment: String) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color.Gray)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
                .padding(20.dp)
        ) {
            Column(

            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = name,
                        modifier = Modifier.weight(1f)
                    )
                    Image(
                        painter = painterResource(icon),
                        contentDescription = "$name icon",
                    )
                }
                Spacer(
                    modifier = Modifier.height(30.dp)
                )
                Column(

                ) {
                    Text(
                        text = stat,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = comment,
                        fontSize = 14.sp,
                        color = Color(28, 156, 37)
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun StatBoxPreview() {
    ParentAppTheme() {
        TextWithBG("text", Color.Blue)
    }
}