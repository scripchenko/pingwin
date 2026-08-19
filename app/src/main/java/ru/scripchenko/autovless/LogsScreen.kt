package ru.scripchenko.autovless

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LogsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current

    var detailedEnabled by remember {
        mutableStateOf(
            DiagnosticLogStore.isDetailedEnabled(
                context
            )
        )
    }

    var entries by remember {
        mutableStateOf(
            DiagnosticLogStore.entries(
                context
            )
        )
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(
                    horizontal = 20.dp
                )
    ) {
        IconButton(
            onClick = onBack
        ) {
            Text(
                text = "←",
                fontSize = 30.sp,
                color = Color(0xFF17191F)
            )
        }

        Spacer(
            modifier =
                Modifier.height(4.dp)
        )

        Text(
            text = "Логи",
            fontSize = 30.sp,
            color = Color(0xFF17191F)
        )

        Spacer(
            modifier =
                Modifier.height(24.dp)
        )

        Row(
            modifier =
                Modifier.fillMaxWidth(),
            verticalAlignment =
                Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.SpaceBetween
        ) {
            Column(
                modifier =
                    Modifier.weight(1f)
            ) {
                Text(
                    text = "Подробное логирование",
                    fontSize = 18.sp,
                    color = Color(0xFF1A1C21)
                )

                Text(
                    text =
                        if (detailedEnabled) {
                            "Включено"
                        } else {
                            "Выключено"
                        },
                    fontSize = 14.sp,
                    color = Color(0xFF777D89),
                    modifier =
                        Modifier.padding(
                            top = 3.dp
                        )
                )
            }

            Switch(
                checked = detailedEnabled,
                onCheckedChange = { enabled ->
                    detailedEnabled = enabled

                    DiagnosticLogStore
                        .setDetailedEnabled(
                            context,
                            enabled
                        )
                }
            )
        }

        Spacer(
            modifier =
                Modifier.height(24.dp)
        )

        Row(
            modifier =
                Modifier.fillMaxWidth(),
            verticalAlignment =
                Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.SpaceBetween
        ) {
            Text(
                text = "Последние события",
                fontSize = 18.sp,
                color = Color(0xFF1A1C21)
            )

            OutlinedButton(
                onClick = {
                    entries =
                        DiagnosticLogStore.entries(
                            context
                        )
                }
            ) {
                Text("Обновить")
            }
        }

        Spacer(
            modifier =
                Modifier.height(12.dp)
        )

        if (entries.isEmpty()) {
            Text(
                text = "Журнал пока пуст.",
                fontSize = 15.sp,
                color = Color(0xFF777D89)
            )
        } else {
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(
                            rememberScrollState()
                        ),
                verticalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {
                entries.forEach { entry ->
                    Text(
                        text = entry,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        fontFamily =
                            FontFamily.Monospace,
                        color =
                            Color(0xFF40444D)
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    modifier =
                        Modifier.weight(1f),
                    onClick = {
                        copyLogs(
                            context,
                            entries
                        )
                    }
                ) {
                    Text("Скопировать")
                }

                OutlinedButton(
                    modifier =
                        Modifier.weight(1f),
                    onClick = {
                        shareLogs(
                            context,
                            entries
                        )
                    }
                ) {
                    Text("Поделиться")
                }
            }

            Spacer(
                modifier =
                    Modifier.height(10.dp)
            )

            OutlinedButton(
                modifier =
                    Modifier.fillMaxWidth(),
                onClick = {
                    DiagnosticLogStore.clear(
                        context
                    )

                    entries = emptyList()
                }
            ) {
                Text("Очистить журнал")
            }
        }

        Spacer(
            modifier =
                Modifier.height(20.dp)
        )
    }
}

private fun copyLogs(
    context: Context,
    entries: List<String>
) {
    val clipboard =
        context.getSystemService(
            Context.CLIPBOARD_SERVICE
        ) as ClipboardManager

    clipboard.setPrimaryClip(
        ClipData.newPlainText(
            "pingwin logs",
            buildLogText(entries)
        )
    )

    Toast.makeText(
        context,
        "Логи скопированы",
        Toast.LENGTH_SHORT
    ).show()
}

private fun shareLogs(
    context: Context,
    entries: List<String>
) {
    runCatching {
        val logDir =
            File(
                context.cacheDir,
                "logs"
            ).apply {
                mkdirs()
            }

        val stamp =
            SimpleDateFormat(
                "yyyy-MM-dd_HH-mm-ss",
                Locale.US
            ).format(
                Date()
            )

        val file =
            File(
                logDir,
                "pingwin-log-$stamp.txt"
            )

        file.writeText(
            buildLogText(entries),
            Charsets.UTF_8
        )

        val uri =
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

        val sendIntent =
            Intent(
                Intent.ACTION_SEND
            ).apply {
                type = "text/plain"
                putExtra(
                    Intent.EXTRA_STREAM,
                    uri
                )
                addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }

        context.startActivity(
            Intent.createChooser(
                sendIntent,
                "Поделиться логами"
            )
        )
    }.onFailure {
        Toast.makeText(
            context,
            "Не удалось создать файл логов",
            Toast.LENGTH_LONG
        ).show()
    }
}

private fun buildLogText(
    entries: List<String>
): String {
    val created =
        SimpleDateFormat(
            "dd.MM.yyyy HH:mm:ss",
            Locale.getDefault()
        ).format(
            Date()
        )

    return buildString {
        appendLine("pingwin 0.1.0")
        appendLine(
            "Android ${Build.VERSION.RELEASE} " +
                "(SDK ${Build.VERSION.SDK_INT})"
        )
        appendLine(
            "Устройство: ${Build.MANUFACTURER} ${Build.MODEL}"
        )
        appendLine(
            "Создано: $created"
        )
        appendLine()
        appendLine("Журнал:")
        append(
            entries.joinToString("\n")
        )
    }
}
