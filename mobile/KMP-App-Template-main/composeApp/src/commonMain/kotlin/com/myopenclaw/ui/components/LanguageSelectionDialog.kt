package com.myopenclaw.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.myopenclaw.util.localizedString
import com.myopenclaw.util.StringKeys

/**
 * Supported languages in the app
 */
data class Language(
    val name: String,
    val code: String
)

val supportedLanguages = listOf(
    Language("English", "en"),
    Language("Spanish", "es"),
    Language("French", "fr"),
    Language("German", "de"),
    Language("Portuguese", "pt"),
    Language("Italian", "it"),
    Language("Japanese", "ja"),
    Language("Chinese (Simplified)", "zh"),
    Language("Korean", "ko"),
    Language("Arabic", "ar")
)

/**
 * Language selection dialog
 *
 * @param currentLanguage The currently selected language name
 * @param onLanguageSelected Callback when a language is selected
 * @param onDismiss Callback when dialog is dismissed
 */
@Composable
fun LanguageSelectionDialog(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1A1C23)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Dialog Title
                Text(
                    text = localizedString(StringKeys.SELECT_LANGUAGE, currentLanguage),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Language List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                ) {
                    items(supportedLanguages) { language ->
                        LanguageItem(
                            language = language,
                            isSelected = language.name == currentLanguage,
                            onClick = {
                                onLanguageSelected(language.name)
                                onDismiss()
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Cancel Button
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = localizedString(StringKeys.CANCEL, currentLanguage),
                        color = Color(0xFF4A9FFF)
                    )
                }
            }
        }
    }
}

@Composable
private fun LanguageItem(
    language: Language,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(
                color = if (isSelected) Color(0xFF2A2D35) else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = language.name,
            fontSize = 16.sp,
            color = if (isSelected) Color(0xFF4A9FFF) else Color.White,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )

        if (isSelected) {
            // Checkmark indicator
            Text(
                text = "✓",
                fontSize = 18.sp,
                color = Color(0xFF4A9FFF),
                fontWeight = FontWeight.Bold
            )
        }
    }
}
