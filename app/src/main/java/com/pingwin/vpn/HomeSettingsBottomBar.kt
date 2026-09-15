package com.pingwin.vpn

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeSettingsBottomBar(
    settingsSelected: Boolean,
    onHomeClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Surface(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(26.dp),
        color =
            Color.White,
        shadowElevation = 3.dp
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 12.dp,
                        vertical = 8.dp
                    ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            BottomBarItem(
                modifier =
                    Modifier.weight(1f),
                selected =
                    !settingsSelected,
                icon = "⌂",
                text =
                    stringResource(
                        R.string.settings_home
                    ),
                onClick = onHomeClick
            )

            BottomBarItem(
                modifier =
                    Modifier.weight(1f),
                selected =
                    settingsSelected,
                icon = "⚙",
                text =
                    stringResource(
                        R.string.settings_title
                    ),
                onClick = onSettingsClick
            )
        }
    }
}

@Composable
private fun BottomBarItem(
    modifier: Modifier,
    selected: Boolean,
    icon: String,
    text: String,
    onClick: () -> Unit
) {
    Surface(
        modifier =
            modifier
                .clickable(
                    enabled = !selected,
                    onClick = onClick
                ),
        shape =
            RoundedCornerShape(18.dp),
        color =
            if (selected) {
                Color(0xFFE9EEFF)
            } else {
                Color.Transparent
            }
    ) {
        Row(
            modifier =
                Modifier.padding(
                    horizontal = 14.dp,
                    vertical = 10.dp
                ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                fontSize = 22.sp,
                color =
                    if (selected) {
                        Color(0xFF2450C8)
                    } else {
                        Color(0xFF555B67)
                    }
            )

            Spacer(
                modifier =
                    Modifier.width(8.dp)
            )

            Text(
                text = text,
                color =
                    if (selected) {
                        Color(0xFF2450C8)
                    } else {
                        Color(0xFF555B67)
                    },
                fontSize = 15.sp,
                fontWeight =
                    if (selected) {
                        FontWeight.SemiBold
                    } else {
                        FontWeight.Normal
                    }
            )
        }
    }
}
