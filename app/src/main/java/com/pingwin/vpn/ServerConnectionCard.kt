package com.pingwin.vpn

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun ServerConnectionCard(
    connection: SavedConnection,
    location: ServerLocation?
) {
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
            Modifier.fillMaxWidth()
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 18.dp,
                        vertical = 16.dp
                    ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Text(
                text =
                    location?.flagEmoji
                        ?.takeIf {
                            it.isNotBlank()
                        }
                        ?: "🌐",
                style =
                    MaterialTheme.typography.headlineMedium
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

            Spacer(
                modifier =
                    Modifier.width(12.dp)
            )

            Text(
                text = "VLESS",
                style =
                    MaterialTheme.typography.labelLarge,
                fontWeight =
                    FontWeight.SemiBold
            )

            Spacer(
                modifier =
                    Modifier.width(10.dp)
            )

            Text(
                text = "›",
                style =
                    MaterialTheme.typography.headlineSmall
            )
        }
    }
}
