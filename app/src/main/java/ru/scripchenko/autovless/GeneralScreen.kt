package ru.scripchenko.autovless

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private const val GENERAL_PREFS = "general_settings"
private const val LANGUAGE_KEY = "language"
private const val LANGUAGE_ENGLISH = "en"
private const val LANGUAGE_RUSSIAN = "ru"

@Composable
fun GeneralScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current

    var selectedLanguage by
        remember {
            mutableStateOf(
                context
                    .getSharedPreferences(
                        GENERAL_PREFS,
                        Context.MODE_PRIVATE
                    )
                    .getString(
                        LANGUAGE_KEY,
                        LANGUAGE_ENGLISH
                    ) ?: LANGUAGE_ENGLISH
            )
        }

    var languageDialogVisible by
        remember {
            mutableStateOf(false)
        }

    fun selectLanguage(
        language: String
    ) {
        selectedLanguage = language

        context
            .getSharedPreferences(
                GENERAL_PREFS,
                Context.MODE_PRIVATE
            )
            .edit()
            .putString(
                LANGUAGE_KEY,
                language
            )
            .apply()

        languageDialogVisible = false
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(20.dp),
        verticalArrangement =
            Arrangement.spacedBy(16.dp)
    ) {
        androidx.compose.material3.IconButton(
            onClick = onBack
        ) {
            Text(
                text = "←",
                style =
                    MaterialTheme.typography.headlineMedium
            )
        }

        Text(
            text = "Общие",
            style =
                MaterialTheme.typography.headlineMedium
        )

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable {
                        languageDialogVisible = true
                    }
                    .padding(
                        vertical = 12.dp
                    ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Column(
                modifier =
                    Modifier.weight(1f),
                verticalArrangement =
                    Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Язык",
                    style =
                        MaterialTheme.typography.titleLarge
                )

                Text(
                    text =
                        if (
                            selectedLanguage ==
                                LANGUAGE_RUSSIAN
                        ) {
                            "Русский"
                        } else {
                            "English"
                        },
                    style =
                        MaterialTheme.typography.bodyMedium
                )
            }

            Text(
                text = "›",
                style =
                    MaterialTheme.typography.headlineSmall
            )
        }
    }

    if (languageDialogVisible) {
        AlertDialog(
            onDismissRequest = {
                languageDialogVisible = false
            },
            title = {
                Text("Язык")
            },
            text = {
                Column {
                    LanguageOption(
                        title = "English",
                        selected =
                            selectedLanguage ==
                                LANGUAGE_ENGLISH,
                        onClick = {
                            selectLanguage(
                                LANGUAGE_ENGLISH
                            )
                        }
                    )

                    LanguageOption(
                        title = "Русский",
                        selected =
                            selectedLanguage ==
                                LANGUAGE_RUSSIAN,
                        onClick = {
                            selectLanguage(
                                LANGUAGE_RUSSIAN
                            )
                        }
                    )
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = {
                        languageDialogVisible = false
                    }
                ) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
private fun LanguageOption(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(
                    onClick = onClick
                )
                .padding(
                    vertical = 8.dp
                ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )

        Text(
            text = title,
            modifier =
                Modifier.padding(
                    start = 8.dp
                )
        )
    }
}
