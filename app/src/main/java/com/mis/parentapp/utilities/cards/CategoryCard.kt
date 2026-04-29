package com.mis.parentapp.utilities.cards

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mis.parentapp.ui.theme.ColorsDefaultTheme
import com.mis.parentapp.utilities.cards.dataclass.Category

@Composable
fun CategoryCard(
    category: Category,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        // Adjust this color to match your specific dark theme surface color
        colors = CardDefaults.cardColors(containerColor = ColorsDefaultTheme.color_Surface),
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = category.title,
                color = ColorsDefaultTheme.color_On_surface,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )

            Icon(
               imageVector = category.icon,
               contentDescription = category.title,
               tint = ColorsDefaultTheme.color_Primary_green,
                modifier = Modifier.size(32.dp)
           )
        }
    }
}