package com.pingwin.vpn

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.IconButton
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
fun AboutScreen(
    onBack: () -> Unit
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(
                    rememberScrollState()
                )
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
            modifier = Modifier.height(4.dp)
        )

        Text(
            text =
                stringResource(
                    R.string.about_title
                ),
            fontSize = 30.sp,
            color = Color(0xFF17191F)
        )

        Spacer(
            modifier = Modifier.height(30.dp)
        )

        Text(
            text = "pingwin",
            fontSize = 38.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF17191F)
        )

        Text(
            text =
                stringResource(
                    R.string.about_version_line
                ),
            fontSize = 15.sp,
            color = Color(0xFF777D89),
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(
            modifier = Modifier.height(28.dp)
        )

        Text(
            text =
                stringResource(
                    R.string.about_description
                ),
            fontSize = 17.sp,
            lineHeight = 25.sp,
            color = Color(0xFF343840)
        )

        Spacer(
            modifier = Modifier.height(28.dp)
        )

        Text(
            text =
                stringResource(
                    R.string.about_features_title
                ),
            fontSize = 19.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1A1C21)
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        AboutFeature(
            text =
                stringResource(
                    R.string.about_feature_connections
                )
        )

        AboutFeature(
            text =
                stringResource(
                    R.string.about_feature_routing
                )
        )

        AboutFeature(
            text =
                stringResource(
                    R.string.about_feature_automation
                )
        )

        AboutFeature(
            text =
                stringResource(
                    R.string.about_feature_macrodroid
                )
        )

        AboutFeature(
            text =
                stringResource(
                    R.string.about_feature_logs
                )
        )

        AboutFeature(
            text =
                stringResource(
                    R.string.about_feature_languages
                )
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        AboutRow(
            title =
                stringResource(
                    R.string.about_version
                ),
            value = "0.1.0"
        )

        AboutRow(
            title =
                stringResource(
                    R.string.about_core
                ),
            value = "sing-box"
        )

        AboutRow(
            title =
                stringResource(
                    R.string.about_protocol
                ),
            value = "VLESS"
        )

        Spacer(
            modifier = Modifier.height(28.dp)
        )

        Text(
            text = "© 2026 pingwin",
            fontSize = 14.sp,
            color = Color(0xFF8A8F99),
            modifier =
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 24.dp)
        )
    }
}

@Composable
private fun AboutFeature(
    text: String
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp),
        verticalAlignment =
            Alignment.Top
    ) {
        Text(
            text = "•",
            fontSize = 17.sp,
            lineHeight = 24.sp,
            color = Color(0xFF343840)
        )

        Spacer(
            modifier = Modifier.width(10.dp)
        )

        Text(
            text = text,
            modifier =
                Modifier.weight(1f),
            fontSize = 16.sp,
            lineHeight = 23.sp,
            color = Color(0xFF343840)
        )
    }
}

@Composable
private fun AboutRow(
    title: String,
    value: String
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
        verticalAlignment =
            Alignment.CenterVertically,
        horizontalArrangement =
            Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            color = Color(0xFF1A1C21)
        )

        Spacer(
            modifier = Modifier.width(20.dp)
        )

        Text(
            text = value,
            fontSize = 16.sp,
            color = Color(0xFF777D89)
        )
    }
}
