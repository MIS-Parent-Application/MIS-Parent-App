package com.mis.parentapp.features.me.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mis.parentapp.R
import com.mis.parentapp.ui.theme.AppTypes
import com.mis.parentapp.utils.cards.CategoryCard
import com.mis.parentapp.utils.cards.dataclass.Category

@Composable
fun SettingsSection(onCategoryClick: (String) -> Unit){
    val categories = listOf(
        Category(title = stringResource(id = R.string.preferences_btn_txt), icon = Icons.Filled.Palette),
        Category(title = stringResource(id = R.string.data_safety_btn_txt), icon = Icons.Filled.Shield),
        Category(title = stringResource(id = R.string.edit_profile_btn_txt), icon = Icons.Filled.Person),
        Category(title = stringResource(id = R.string.sign_out_btn_txt), icon = Icons.Filled.Logout)
    )
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ){
        SettingsSectionTitle()
        SettingsCategoryGrid(categories = categories, onCategoryClick = onCategoryClick)
    }
}

@Composable
fun SettingsCategoryGrid(categories: List<Category>, onCategoryClick: (String) -> Unit) {
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
fun SettingsSectionTitle(){
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(id = R.string.section_title_settings),
            style = AppTypes.type_H1,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
