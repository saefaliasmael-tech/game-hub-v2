package com.example.hub.ui.screens

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.R
import com.example.hub.ui.components.UpdateDialog
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

    val updateChecker = remember { UpdateChecker(context) }
    val updateDownloader = remember { UpdateDownloader(context) }
    val updateInstaller = remember { UpdateInstaller(context) }
    var updateState by remember { mutableStateOf<UpdateState>(UpdateState.Idle) }
    var isCheckingUpdates by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.hub_settings)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // App Identity Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.SportsEsports,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = stringResource(R.string.game_hub_title),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(
                            R.string.app_version_format,
                            BuildConfig.VERSION_NAME,
                            BuildConfig.VERSION_CODE
                        ),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            // Update Section
            Text(
                text = stringResource(R.string.check_for_updates),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "OTA Full APK Update System",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Game Hub checks for full application APK updates containing all games and engines without losing any of your saved levels or progress.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isCheckingUpdates = true
                                val result = updateChecker.checkForUpdates()
                                isCheckingUpdates = false
                                if (result is UpdateState.UpToDate) {
                                    Toast.makeText(context, context.getString(R.string.latest_version_message), Toast.LENGTH_SHORT).show()
                                } else {
                                    updateState = result
                                }
                            }
                        },
                        enabled = !isCheckingUpdates,
                        modifier = Modifier.fillMaxWidth().testTag("check_updates_btn")
                    ) {
                        if (isCheckingUpdates) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.checking_updates))
                        } else {
                            Icon(imageVector = Icons.Filled.CloudSync, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.check_for_updates))
                        }
                    }
                }
            }

            // About Hub Section
            Text(
                text = stringResource(R.string.about_hub),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                ),
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
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    SettingItem(
                        icon = Icons.Filled.Extension,
                        title = "Standalone Game Architecture",
                        subtitle = "Zero shared logic, isolated local databases"
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    SettingItem(
                        icon = Icons.Filled.CloudDownload,
                        title = "Distribution Model",
                        subtitle = "Single APK holding all published games"
                    )
                }
            }
        }
    }

    // Update Dialog
    UpdateDialog(
        state = updateState,
        onStartDownload = {
            val manifest = (updateState as? UpdateState.UpdateAvailable)?.manifest
            if (manifest != null) {
                coroutineScope.launch {
                    updateDownloader.downloadApk(manifest).collect { state ->
                        updateState = state
                    }
                }
            }
        },
        onInstall = {
            val readyState = updateState as? UpdateState.ReadyToInstall
            if (readyState != null) {
                updateInstaller.installApk(readyState.apkFile)
            }
        },
        onDismiss = {
            updateState = UpdateState.Idle
        },
        onRetry = {
            coroutineScope.launch {
                updateState = UpdateState.Checking
                updateState = updateChecker.checkForUpdates()
            }
        }
    )
}

@Composable
private fun SettingItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
