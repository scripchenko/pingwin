package ru.scripchenko.autovless

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun ConnectionListScreen(
    connections: List<SavedConnection>,
    selectedId: String?,
    onBack: () -> Unit,
    onSelect: (SavedConnection) -> Unit,
    onAdd: () -> Unit
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(20.dp),
        verticalArrangement =
            Arrangement.spacedBy(14.dp)
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
            text = "Подключения",
            style =
                MaterialTheme.typography.headlineMedium
        )

        if (connections.isEmpty()) {
            Text(
                text = "Сохранённых подключений пока нет.",
                style =
                    MaterialTheme.typography.bodyLarge
            )
        } else {
            connections.forEach { connection ->
                val profile =
                    runCatching {
                        VlessProfile.parse(
                            connection.link
                        )
                    }.getOrNull()

                val host =
                    profile?.host
                        ?.takeIf {
                            it.isNotBlank()
                        }
                        ?: "—"

                Card(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelect(connection)
                            }
                ) {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {
                        Text(
                            text =
                                if (connection.id == selectedId) {
                                    "●"
                                } else {
                                    "○"
                                },
                            style =
                                MaterialTheme.typography.titleLarge
                        )

                        Spacer(
                            modifier =
                                Modifier.width(14.dp)
                        )

                        Column(
                            modifier =
                                Modifier.weight(1f),
                            verticalArrangement =
                                Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = connection.name,
                                style =
                                    MaterialTheme.typography.titleMedium,
                                fontWeight =
                                    FontWeight.SemiBold,
                                maxLines = 1,
                                overflow =
                                    TextOverflow.Ellipsis
                            )

                            Text(
                                text = host,
                                style =
                                    MaterialTheme.typography.bodyMedium
                            )
                        }

                        Text(
                            text = "VLESS",
                            style =
                                MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }

        Button(
            modifier =
                Modifier.fillMaxWidth(),
            onClick = onAdd
        ) {
            Text("Добавить подключение")
        }
    }
}
