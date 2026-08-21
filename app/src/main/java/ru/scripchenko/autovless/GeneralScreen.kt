package ru.scripchenko.autovless

import android.app.Activity
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun GeneralScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current

    var selectedLanguage by
        remember {
            mutableStateOf(
                AppLocale.currentLanguage(
                    context
                )
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

        AppLocale.saveLanguage(
            context = context,
            language = language
        )

        languageDialogVisible = false

        (context as? Activity)
            ?.recreate()
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
            text =
                stringResource(
                    R.string.general_title
                ),
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
                    text =
                        stringResource(
                            R.string.language_title
                        ),
                    style =
                        MaterialTheme.typography.titleLarge
                )

                Text(
                    text =
                        if (
                            selectedLanguage ==
                                AppLocale.RUSSIAN
                        ) {
                            stringResource(
                                R.string.language_russian
                            )
                        } else {
                            stringResource(
                                R.string.language_english
                            )
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
                Text(
                    stringResource(
                        R.string.language_title
                    )
                )
            },
            text = {
                Column {
                    LanguageOption(
                        title =
                            stringResource(
                                R.string.language_english
                            ),
                        selected =
                            selectedLanguage ==
                                AppLocale.ENGLISH,
                        onClick = {
                            selectLanguage(
                                AppLocale.ENGLISH
                            )
                        }
                    )

                    LanguageOption(
                        title =
                            stringResource(
                                R.string.language_russian
                            ),
                        selected =
                            selectedLanguage ==
                                AppLocale.RUSSIAN,
                        onClick = {
                            selectLanguage(
                                AppLocale.RUSSIAN
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
                    Text(
                        stringResource(
                            R.string.cancel
                        )
                    )
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
