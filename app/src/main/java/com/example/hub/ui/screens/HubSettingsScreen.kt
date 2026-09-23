package com.example.hub.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.R
import com.zubaluba.gamehub.ads.ConsentManager
import com.example.hub.data.HubPreferences
import com.example.hub.ui.components.UpdateDialog
import com.example.hub.ui.theme.HubColors
import com.example.update.UpdateChecker
import com.example.update.UpdateDownloader
import com.example.update.UpdateInstaller
import com.example.update.model.UpdateState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HubSettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val hubPreferences = remember { HubPreferences.getInstance(context) }

    val soundEnabled by hubPreferences.soundEnabledFlow.collectAsState()
    val vibrationEnabled by hubPreferences.vibrationEnabledFlow.collectAsState()

    val updateChecker = remember { UpdateChecker(context) }
    val updateDownloader = remember { UpdateDownloader(context) }
    val updateInstaller = remember { UpdateInstaller(context) }
    val consentManager = remember { ConsentManager.getInstance(context) }
    var updateState by remember { mutableStateOf<UpdateState>(UpdateState.Idle) }
    var isCheckingUpdates by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.hub_settings),
                        color = HubColors.HighText,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = HubColors.HighText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = HubColors.Void
                )
            )
        },
        containerColor = HubColors.Void,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(HubColors.Void)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // App Identity Card
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = HubColors.SurfaceMid),
                border = androidx.compose.foundation.BorderStroke(1.dp, HubColors.Hairline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(HubColors.HeroGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SportsEsports,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.game_hub_title),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = HubColors.HighText
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(
                            R.string.app_version_format,
                            BuildConfig.VERSION_NAME,
                            BuildConfig.VERSION_CODE
                        ),
                        style = MaterialTheme.typography.bodyMedium.copy(color = HubColors.LowText)
                    )
                }
            }

            // Controls Section (Sound & Vibration)
            Text(
                text = stringResource(R.string.settings),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = HubColors.HighText
                )
            )

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = HubColors.SurfaceLow),
                border = androidx.compose.foundation.BorderStroke(1.dp, HubColors.Hairline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Sound Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Filled.VolumeUp, contentDescription = null, tint = HubColors.SoftViolet)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = stringResource(R.string.sound_effects), color = HubColors.HighText)
                        }
                        Switch(
                            checked = soundEnabled,
                            onCheckedChange = { hubPreferences.setSoundEnabled(!soundEnabled) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = HubColors.PrimaryViolet
                            )
                        )
                    }

                    HorizontalDivider(color = HubColors.Hairline)

                    // Vibration Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Filled.Vibration, contentDescription = null, tint = HubColors.SoftViolet)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = stringResource(R.string.haptics), color = HubColors.HighText)
                        }
                        Switch(
                            checked = vibrationEnabled,
                            onCheckedChange = { hubPreferences.setVibrationEnabled(!vibrationEnabled) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = HubColors.PrimaryViolet
                            )
                        )
                    }
                }
            }

            // Privacy & Consent Preferences Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = HubColors.SurfaceLow),
                border = androidx.compose.foundation.BorderStroke(1.dp, HubColors.Hairline),
                modifier = Modifier.fillMaxWidth().testTag("privacy_settings_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SettingItem(
                        icon = Icons.Filled.Policy,
                        title = stringResource(R.string.privacy_options),
                        subtitle = stringResource(R.string.privacy_options_desc)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedButton(
                        onClick = {
                            val activity = context as? Activity
                            if (activity != null) {
                                consentManager.showPrivacyOptionsForm(activity) { error ->
                                    if (error != null) {
                                        Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("privacy_options_btn"),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = HubColors.Cyan
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, HubColors.Cyan.copy(alpha = 0.5f))
                    ) {
                        Icon(imageVector = Icons.Filled.Security, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.privacy_options))
                    }
                }
            }

            // Update Section
            Text(
                text = stringResource(R.string.check_for_updates),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = HubColors.HighText
                )
            )

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = HubColors.SurfaceLow),
                border = androidx.compose.foundation.BorderStroke(1.dp, HubColors.Hairline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "OTA Full APK Update System",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = HubColors.Cyan
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Game Hub checks for complete APK releases holding all games and engines without losing any of your saved levels or high scores.",
                        style = MaterialTheme.typography.bodySmall.copy(color = HubColors.LowText)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(HubColors.PlayButtonGradient)
                            .clickable(enabled = !isCheckingUpdates) {
                                coroutineScope.launch {
                                    isCheckingUpdates = true
                                    val result = updateChecker.checkForUpdates()
                                    isCheckingUpdates = false
                                    if (result is UpdateState.UpToDate) {
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.latest_version_message),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        updateState = result
                                    }
                                }
                            }
                            .padding(vertical = 12.dp)
                            .testTag("check_updates_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCheckingUpdates) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.CloudSync,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.check_for_updates),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // About Hub Section
            Text(
                text = stringResource(R.string.about_hub),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = HubColors.HighText
                )
            )

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = HubColors.SurfaceLow),
                border = androidx.compose.foundation.BorderStroke(1.dp, HubColors.Hairline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SettingItem(
                        icon = Icons.Filled.Security,
                        title = "Package ID",
                        subtitle = BuildConfig.APPLICATION_ID
                    )
                    HorizontalDivider(color = HubColors.Hairline)
                    SettingItem(
                        icon = Icons.Filled.Extension,
                        title = "Standalone Game Architecture",
                        subtitle = "Zero shared logic, isolated local databases"
                    )
                    HorizontalDivider(color = HubColors.Hairline)
                    SettingItem(
                        icon = Icons.Filled.CloudDownload,
                        title = "Distribution Model",
                        subtitle = "Single APK holding all published games"
                    )
                }
            }
        }
    }

    val currentUpdate = updateState
    if (currentUpdate !is UpdateState.Idle && currentUpdate !is UpdateState.UpToDate && currentUpdate !is UpdateState.Checking) {
        UpdateDialog(
            state = currentUpdate,
            onStartDownload = {
                val manifest = (currentUpdate as? UpdateState.UpdateAvailable)?.manifest
                if (manifest != null) {
                    coroutineScope.launch {
                        updateDownloader.downloadApk(manifest).collect { st ->
                            updateState = st
                        }
                    }
                }
            },
            onInstall = {
                val readyState = currentUpdate as? UpdateState.ReadyToInstall
                if (readyState != null) {
                    updateInstaller.installApk(readyState.apkFile)
                }
            },
            onDismiss = { updateState = UpdateState.Idle },
            onRetry = {
                coroutineScope.launch {
                    val res = updateChecker.checkForUpdates()
                    updateState = res
                }
            }
        )
    }
}

@Composable
private fun SettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(HubColors.SurfaceMid),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = HubColors.Cyan,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = HubColors.HighText
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(color = HubColors.LowText)
            )
        }
    }
}
