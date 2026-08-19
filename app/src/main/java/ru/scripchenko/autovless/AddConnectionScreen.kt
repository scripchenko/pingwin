package ru.scripchenko.autovless

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun AddConnectionScreen(
    onBack: () -> Unit,
    onScanQr: () -> Unit,
    onAdded: () -> Unit
) {
    val context = LocalContext.current

    var link by remember {
        mutableStateOf("")
    }

    var error by remember {
        mutableStateOf<String?>(null)
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(20.dp),
        verticalArrangement =
            Arrangement.spacedBy(16.dp)
    ) {
        IconButton(
            onClick = onBack
        ) {
            Text(
                text = "←",
                style =
                    MaterialTheme.typography.headlineMedium
            )
        }

        Text(
            text = "Добавить подключение",
            style =
                MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Вставьте VLESS-ссылку.",
            style =
                MaterialTheme.typography.bodyMedium
        )

        OutlinedTextField(
            value = link,
            onValueChange = {
                link = it
                error = null
            },
            modifier =
                Modifier.fillMaxWidth(),
            label = {
                Text("VLESS-ссылка")
            },
            minLines = 4
        )

        OutlinedButton(
            modifier =
                Modifier.fillMaxWidth(),
            onClick = onScanQr
        ) {
            Text("Сканировать QR-код")
        }

        OutlinedButton(
            modifier =
                Modifier.fillMaxWidth(),
            onClick = {
                val clipboard =
                    context.getSystemService(
                        Context.CLIPBOARD_SERVICE
                    ) as ClipboardManager

                val text =
                    clipboard.primaryClip
                        ?.getItemAt(0)
                        ?.coerceToText(context)
                        ?.toString()
                        ?.trim()
                        .orEmpty()

                if (text.isNotEmpty()) {
                    link = text
                    error = null
                }
            }
        ) {
            Text("Вставить из буфера")
        }

        Button(
            modifier =
                Modifier.fillMaxWidth(),
            enabled =
                link.isNotBlank(),
            onClick = {
                runCatching {
                    ConnectionStore.add(
                        context = context,
                        link = link
                    )
                }
                    .onSuccess {
                        error = null
                        onAdded()
                    }
                    .onFailure {
                        error =
                            it.message
                                ?: "Не удалось добавить подключение"
                    }
            }
        ) {
            Text("Добавить")
        }

        error?.let {
            Text(
                text = it,
                color =
                    MaterialTheme.colorScheme.error,
                style =
                    MaterialTheme.typography.bodyMedium
            )
        }
    }
}
