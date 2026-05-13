package com.mis.parentapp.features.me.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mis.parentapp.ui.theme.AppTypes
import com.mis.parentapp.utilities.cards.CategoryCard
import com.mis.parentapp.utilities.cards.dataclass.Category
import com.mis.parentapp.R

@Composable
fun YourEssentialsSection(onCategoryClick: (String) -> Unit){
    val categories = listOf(
        Category(title = stringResource(id = R.string.messages_btn_txt), icon = Icons.Filled.ChatBubble),
        Category(title = stringResource(id = R.string.announcements_btn_txt), icon = Icons.Filled.Campaign),
        Category(title = stringResource(id = R.string.meetings_btn_txt), icon = Icons.Filled.People),
        Category(title = stringResource(id = R.string.feedbacks_btn_txt), icon = Icons.Filled.Feedback)
    )
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ){
        EssentialSectionTitle()
        EssentialCategoryGrid(categories = categories, onCategoryClick = onCategoryClick)
    }
}

@Composable
fun EssentialCategoryGrid(categories: List<Category>, onCategoryClick: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.chunked(2).forEach { rowCategories ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowCategories.forEach { category ->
                    Box(modifier = Modifier.weight(1f)) {
                        CategoryCard(
                            category = category,
                            onClick = {
                                onCategoryClick(category.title)
                            }
                        )
                    }
                }
                if (rowCategories.size < 2) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun EssentialSectionTitle(){
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(id = R.string.section_title_essentials),
            style = AppTypes.type_H1,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
