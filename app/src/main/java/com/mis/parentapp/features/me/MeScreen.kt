package com.mis.parentapp.features.me

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mis.parentapp.R
import com.mis.parentapp.features.me.sections.SettingsSection
import com.mis.parentapp.features.me.sections.YourEssentialsSection
import com.mis.parentapp.ui.theme.ParentAppTheme
import com.mis.parentapp.utilities.bars.MeTopBar

@Composable
fun MeScreen(modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {MeTopBar()}
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(vertical = 32.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp) // 🔥 longer image
                ) {

                    // 🔥 BACKGROUND IMAGE WITH ROUNDED BOTTOM
                    Image(
                        painter = painterResource(id = R.drawable.bgpic),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(
                                RoundedCornerShape(
                                    bottomStart = 32.dp,
                                    bottomEnd = 32.dp
                                )
                            )
                    )
                }
            }
            item {
                YourEssentialsSection()
            }
            item {
                SettingsSection()
            }
        }
    }
}


@Preview(showBackground = true, widthDp = 360)
@Composable
private fun MeScreenPreview() {
    ParentAppTheme {
        MeScreen()
    }
}
