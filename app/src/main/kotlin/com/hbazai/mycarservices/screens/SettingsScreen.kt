package com.hbazai.mycarservices.screens

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hbazai.mycarservices.R
import com.hbazai.mycarservices.ui.theme.OnPrimary
import com.hbazai.mycarservices.ui.theme.PrimaryYellow
import com.hbazai.mycarservices.util.LocaleHelper
import androidx.compose.material.icons.automirrored.filled.ArrowBack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {

    val context = LocalContext.current
    val activity = context as? Activity

    var notificationsEnabled by remember { mutableStateOf(true) }

    val savedCode = LocaleHelper.getSavedLanguage(context)
    val initialLang = when (savedCode) {
        "de" -> "Deutsch"
        "fa" -> "فارسی"
        else -> "English"
    }
    var selectedLanguage by remember { mutableStateOf(initialLang) }

    val languages = listOf("English", "Deutsch", "فارسی")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.settings_title),
                        fontWeight = FontWeight.Bold,
                        color      = PrimaryYellow
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = PrimaryYellow)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Language ──────────────────────────────
            SettingsSectionLabel(stringResource(R.string.settings_language))

            Card(
                shape  = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                languages.forEach { lang ->
                    Row(
                        modifier          = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedLanguage == lang,
                            onClick  = {
                                selectedLanguage = lang
                                val code = LocaleHelper.getLanguageCode(lang)
                                LocaleHelper.setLocale(context, code)
                                // Restart activity to apply locale
                                activity?.recreate()
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = PrimaryYellow
                            )
                        )
                        Text(
                            text     = lang,
                            modifier = Modifier.padding(start = 8.dp),
                            color    = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // ── Notifications ─────────────────────────
            SettingsSectionLabel(stringResource(R.string.settings_notifications))

            Card(
                shape  = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.settings_notify_enabled),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Switch(
                        checked         = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it },
                        colors          = SwitchDefaults.colors(
                            checkedThumbColor = OnPrimary,
                            checkedTrackColor = PrimaryYellow
                        )
                    )
                }
            }

            // ── About ─────────────────────────────────
            SettingsSectionLabel(stringResource(R.string.settings_about))

            Card(
                shape  = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        stringResource(R.string.app_name),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        stringResource(R.string.settings_version),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsSectionLabel(text: String) {
    Text(
        text       = text,
        style      = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color      = PrimaryYellow
    )
}