package com.pingwin.vpn

import android.Manifest
import android.app.DownloadManager
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun UpdatesScreen(
    onBack: () -> Unit
) {
    val context =
        LocalContext.current

    val scope =
        rememberCoroutineScope()

    var autoCheckEnabled by
        remember {
            mutableStateOf(
                UpdateSettingsStore.isAutoCheckEnabled(
                    context
                )
            )
        }

    var pendingAutoEnable by
        remember {
            mutableStateOf(false)
        }

    val notificationPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (
                granted &&
                pendingAutoEnable
            ) {
                autoCheckEnabled = true

                UpdateSettingsStore.setAutoCheckEnabled(
                    context,
                    true
                )

                UpdateScheduler.sync(
                    context
                )
            }

            pendingAutoEnable = false
        }

    var checking by
        remember {
            mutableStateOf(false)
        }

    var checked by
        remember {
            mutableStateOf(false)
        }

    var failed by
        remember {
            mutableStateOf(false)
        }

    var latestRelease by
        remember {
            mutableStateOf<UpdateRelease?>(null)
        }

    var updateAvailable by
        remember {
            mutableStateOf(false)
        }

    var downloading by
        remember {
            mutableStateOf(false)
        }

    var downloadFailed by
        remember {
            mutableStateOf(false)
        }

    var downloadProgress by
        remember {
            mutableStateOf<Int?>(null)
        }

    var installPermissionNeeded by
        remember {
            mutableStateOf(false)
        }

    var downloadedApk by
        remember {
            mutableStateOf<File?>(null)
        }

    val activity =
        context as? ComponentActivity

    DisposableEffect(
        activity,
        installPermissionNeeded,
        downloadedApk
    ) {
        if (activity == null) {
            return@DisposableEffect onDispose { }
        }

        val observer =
            object : DefaultLifecycleObserver {
                override fun onResume(
                    owner: LifecycleOwner
                ) {
                    val apkFile =
                        downloadedApk

                    if (
                        installPermissionNeeded &&
                        apkFile != null &&
                        apkFile.exists() &&
                        UpdateInstaller.canInstallPackages(
                            context
                        )
                    ) {
                        installPermissionNeeded = false

                        UpdateInstaller.clearStoredDownload(
                            context
                        )

                        downloadedApk = null

                        UpdateInstaller.installApk(
                            context,
                            apkFile
                        )
                    }
                }
            }

        activity.lifecycle.addObserver(
            observer
        )

        onDispose {
            activity.lifecycle.removeObserver(
                observer
            )
        }
    }

    LaunchedEffect(Unit) {
        val savedVersion =
            UpdateSettingsStore.getAvailableVersion(
                context
            )

        if (
            savedVersion != null &&
            UpdateChecker.isNewerVersion(
                remoteVersion = savedVersion,
                currentVersion = BuildConfig.VERSION_NAME
            )
        ) {
            val result =
                withContext(Dispatchers.IO) {
                    runCatching {
                        UpdateChecker.getLatestRelease()
                    }
                }

            result.onSuccess { release ->
                latestRelease = release
                updateAvailable =
                    UpdateChecker.isNewerVersion(
                        remoteVersion = release.version,
                        currentVersion = BuildConfig.VERSION_NAME
                    )

                if (updateAvailable) {
                    UpdateSettingsStore.setAvailableVersion(
                        context,
                        release.version
                    )
                } else {
                    UpdateSettingsStore.clearAvailableVersion(
                        context
                    )
                }

                checked = true
            }
        } else if (savedVersion != null) {
            UpdateSettingsStore.clearAvailableVersion(
                context
            )
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            val state =
                withContext(
                    Dispatchers.IO
                ) {
                    UpdateInstaller.getDownloadState(
                        context.applicationContext
                    )
                }

            when (state?.status) {
                DownloadManager.STATUS_PENDING,
                DownloadManager.STATUS_RUNNING,
                DownloadManager.STATUS_PAUSED -> {
                    downloading = true
                    downloadFailed = false
                    downloadProgress =
                        state.progress
                }

                DownloadManager.STATUS_SUCCESSFUL -> {
                    downloading = false
                    downloadProgress = 100

                    val apkFile =
                        state.apkFile

                    if (
                        apkFile != null &&
                        apkFile.exists()
                    ) {
                        downloadedApk =
                            apkFile

                        val resumed =
                            activity
                                ?.lifecycle
                                ?.currentState
                                ?.isAtLeast(
                                    androidx.lifecycle.Lifecycle.State.RESUMED
                                ) == true

                        if (resumed) {
                            if (
                                UpdateInstaller.canInstallPackages(
                                    context
                                )
                            ) {
                                UpdateInstaller.clearStoredDownload(
                                    context
                                )

                                downloadedApk = null

                                UpdateInstaller.installApk(
                                    context,
                                    apkFile
                                )
                            } else if (
                                !installPermissionNeeded
                            ) {
                                installPermissionNeeded = true

                                UpdateInstaller.openInstallPermission(
                                    context
                                )
                            }
                        }
                    }
                }

                DownloadManager.STATUS_FAILED -> {
                    downloading = false
                    downloadProgress = null
                    downloadFailed = true

                    UpdateInstaller.clearStoredDownload(
                        context
                    )
                }
            }

            delay(500)
        }
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
            modifier = Modifier.height(4.dp)
        )

        Text(
            text =
                stringResource(
                    R.string.updates_title
                ),
            fontSize = 30.sp,
            color = Color(0xFF17191F)
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text =
                stringResource(
                    R.string.updates_current_version,
                    BuildConfig.VERSION_NAME
                ),
            fontSize = 15.sp,
            lineHeight = 21.sp,
            color = Color(0xFF777D89)
        )

        Spacer(
            modifier = Modifier.height(24.dp)
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
                    text =
                        stringResource(
                            R.string.updates_auto_check
                        ),
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1A1C21)
                )

                Text(
                    text =
                        stringResource(
                            R.string.updates_auto_check_subtitle
                        ),
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = Color(0xFF777D89),
                    modifier =
                        Modifier.padding(
                            top = 4.dp
                        )
                )
            }

            Switch(
                modifier =
                    Modifier.padding(start = 16.dp),
                checked =
                    autoCheckEnabled,
                onCheckedChange = { enabled ->
                    if (!enabled) {
                        autoCheckEnabled = false

                        UpdateSettingsStore.setAutoCheckEnabled(
                            context,
                            false
                        )

                        UpdateScheduler.sync(
                            context
                        )

                        return@Switch
                    }

                    val notificationGranted =
                        Build.VERSION.SDK_INT <
                            Build.VERSION_CODES.TIRAMISU ||
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) ==
                            PackageManager.PERMISSION_GRANTED

                    if (notificationGranted) {
                        autoCheckEnabled = true

                        UpdateSettingsStore.setAutoCheckEnabled(
                            context,
                            true
                        )

                        UpdateScheduler.sync(
                            context
                        )
                    } else {
                        pendingAutoEnable = true

                        notificationPermissionLauncher.launch(
                            Manifest.permission.POST_NOTIFICATIONS
                        )
                    }
                }
            )
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Button(
            modifier =
                Modifier.fillMaxWidth(),
            enabled =
                !checking &&
                    !downloading,
            onClick = {
                checking = true
                checked = false
                failed = false
                downloadFailed = false
                installPermissionNeeded = false

                scope.launch {
                    val result =
                        withContext(
                            Dispatchers.IO
                        ) {
                            runCatching {
                                UpdateChecker.getLatestRelease()
                            }
                        }

                    result
                        .onSuccess { release ->
                            latestRelease =
                                release

                            updateAvailable =
                                UpdateChecker.isNewerVersion(
                                    remoteVersion =
                                        release.version,
                                    currentVersion =
                                        BuildConfig.VERSION_NAME
                                )

                            if (updateAvailable) {
                                UpdateSettingsStore.setAvailableVersion(
                                    context,
                                    release.version
                                )
                            } else {
                                UpdateSettingsStore.clearAvailableVersion(
                                    context
                                )
                            }

                            checked = true
                        }
                        .onFailure {
                            failed = true
                        }

                    checking = false
                }
            }
        ) {
            Text(
                text =
                    if (checking) {
                        stringResource(
                            R.string.updates_checking
                        )
                    } else {
                        stringResource(
                            R.string.updates_check
                        )
                    }
            )
        }

        if (checking) {
            CircularProgressIndicator()
        }

        if (failed) {
            Text(
                text =
                    stringResource(
                        R.string.updates_error
                    ),
                color =
                    MaterialTheme.colorScheme.error
            )
        } else if (checked) {
            val release =
                latestRelease

            if (
                updateAvailable &&
                release != null
            ) {
        Spacer(
            modifier = Modifier.height(10.dp)
        )

                Text(
                    text =
                        stringResource(
                            R.string.updates_available,
                            release.version
                        ),
                    style =
                        MaterialTheme.typography.bodyLarge
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                if (!downloading) {
                Button(
                    modifier =
                        Modifier.fillMaxWidth(),
                    enabled = !downloading,
                    onClick = {
                        downloadFailed = false
                        installPermissionNeeded = false

                        val existingApk =
                            downloadedApk

                        if (
                            existingApk != null &&
                            existingApk.exists()
                        ) {
                            if (
                                UpdateInstaller.canInstallPackages(
                                    context
                                )
                            ) {
                                UpdateInstaller.installApk(
                                    context,
                                    existingApk
                                )
                            } else {
                                installPermissionNeeded = true

                                UpdateInstaller.openInstallPermission(
                                    context
                                )
                            }
                        } else {
                            downloadFailed = false
                            downloading = true
                            downloadProgress = 0

                            runCatching {
                                UpdateInstaller.startDownload(
                                    context.applicationContext,
                                    release.apkUrl
                                )
                            }.onFailure {
                                downloading = false
                                downloadProgress = null
                                downloadFailed = true
                            }
                        }
                    }
                ) {
                    Text(
                        text =
                            if (downloading) {
                                stringResource(
                                    R.string.updates_downloading
                                )
                            } else {
                                stringResource(
                                    R.string.updates_download
                                )
                            }
                    )
                }

                }
                if (downloading) {
                    val progress =
                        downloadProgress ?: 0

                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clip(
                                    RoundedCornerShape(26.dp)
                                )
                                .background(
                                    Color(0xFF10285A)
                                )
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(
                                        progress / 100f
                                    )
                                    .background(
                                        Color(0xFF3568C0)
                                    )
                        )

                        Text(
                            text =
                                stringResource(
                                    R.string.updates_downloading
                                ) + " $progress%",
                            color = Color.White,
                            modifier =
                                Modifier.align(
                                    Alignment.Center
                                )
                        )
                    }
                }

                if (downloadFailed) {
                    Text(
                        text =
                            stringResource(
                                R.string.updates_download_error
                            ),
                        color =
                            MaterialTheme.colorScheme.error
                    )
                }

                if (installPermissionNeeded) {
                    Text(
                        text =
                            stringResource(
                                R.string.updates_install_permission
                            ),
                        style =
                            MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                Text(
                    text =
                        stringResource(
                            R.string.updates_up_to_date
                        ),
                    style =
                        MaterialTheme.typography.bodyLarge
                )
            }
        }

        Spacer(
            modifier =
                Modifier.height(8.dp)
        )
    }
}
