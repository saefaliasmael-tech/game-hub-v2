package com.example.watersort.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.watersort.core.repository.GameRepository
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    repository: GameRepository,
    onNavigateBack: () -> Unit
) {
    val profile by repository.profileFlow.collectAsStateWithLifecycle(initialValue = null)
    val scope = rememberCoroutineScope()
    var showResetDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF0F172A))
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.testTag("settings_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Text(
                    text = stringResource(R.string.settings),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )

                Spacer(modifier = Modifier.width(48.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Audio & Haptic Controls Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingToggleRow(
                        icon = Icons.Default.VolumeUp,
                        title = stringResource(R.string.sound_effects),
                        checked = profile?.soundEnabled ?: true,
                        onCheckedChange = { checked ->
                            scope.launch { repository.updateSettings(sound = checked) }
                        },
                        testTag = "toggle_sound"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    SettingToggleRow(
                        icon = Icons.Default.MusicNote,
                        title = stringResource(R.string.music),
                        checked = profile?.musicEnabled ?: true,
                        onCheckedChange = { checked ->
                            scope.launch { repository.updateSettings(music = checked) }
                        },
                        testTag = "toggle_music"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    SettingToggleRow(
                        icon = Icons.Default.Vibration,
                        title = stringResource(R.string.haptics),
                        checked = profile?.hapticEnabled ?: true,
                        onCheckedChange = { checked ->
                            scope.launch { repository.updateSettings(haptic = checked) }
                        },
                        testTag = "toggle_haptics"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Accessibility Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingToggleRow(
                        icon = Icons.Default.Palette,
                        title = stringResource(R.string.color_blind_mode),
                        checked = profile?.colorBlindEnabled ?: false,
                        onCheckedChange = { checked ->
                            scope.launch { repository.updateSettings(colorBlind = checked) }
                        },
                        testTag = "toggle_color_blind"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Language Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.language),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val isArabic = profile?.languageCode == "ar"
                        Button(
                            onClick = {
                                scope.launch {
                                    repository.updateSettings(language = "en")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!isArabic) Color(0xFF0284C7) else Color(0xFF334155)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("lang_en_button")
                        ) {
                            Text("English")
                        }

                        Button(
                            onClick = {
                                scope.launch {
                                    repository.updateSettings(language = "ar")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isArabic) Color(0xFF0284C7) else Color(0xFF334155)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("lang_ar_button")
                        ) {
                            Text("العربية")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Reset Progress Danger Button
            OutlinedButton(
                onClick = { showResetDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("reset_progress_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF43F5E))
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.reset_progress),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Reset Confirmation Dialog
        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                title = { Text(stringResource(R.string.reset_progress), color = Color(0xFFF43F5E)) },
                text = { Text(stringResource(R.string.reset_progress_confirm), color = Color(0xFFCBD5E1)) },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                repository.resetAllProgress()
                                showResetDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48))
                    ) {
                        Text(stringResource(R.string.yes))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDialog = false }) {
                        Text(stringResource(R.string.no), color = Color.White)
                    }
                },
                containerColor = Color(0xFF1E293B)
            )
        }
    }
}

@Composable
fun SettingToggleRow(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF38BDF8),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF0284C7),
                uncheckedThumbColor = Color(0xFF94A3B8),
                uncheckedTrackColor = Color(0xFF334155)
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}
