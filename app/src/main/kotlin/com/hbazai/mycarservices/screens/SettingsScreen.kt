package com.hbazai.mycarservices.screens

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hbazai.mycarservices.R
import com.hbazai.mycarservices.ui.theme.OnPrimary
import com.hbazai.mycarservices.ui.theme.PrimaryYellow
import com.hbazai.mycarservices.util.AppPreferences
import com.hbazai.mycarservices.util.LocaleHelper
import com.hbazai.mycarservices.util.ThemeMode
import com.hbazai.mycarservices.util.ThemePreference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context  = LocalContext.current
    val activity = context as? Activity

    val savedLanguage    = LocaleHelper.getSavedLanguage(context)
    var selectedCurrency by remember { mutableStateOf(AppPreferences.getCurrency(context)) }
    var selectedDistance by remember { mutableStateOf(AppPreferences.getDistanceUnit(context)) }
    var notificationsEnabled by remember { mutableStateOf(true) }

    val currencies    = listOf("تومان", "﷼", "€", "$", "£")
    val distanceUnits = listOf("km", "mile")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.settings_title), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = MaterialTheme.colorScheme.primary)
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // ── Language ──────────────────────────────
            SettingsSection(icon = Icons.Default.Language, title = stringResource(R.string.settings_language)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    LocaleHelper.supportedLanguages.forEach { (code, displayName) ->
                        ChoiceCard(
                            text       = displayName,
                            isSelected = savedLanguage == code,
                            modifier   = Modifier.weight(1f),
                            onClick    = {
                                if (savedLanguage != code) {
                                    LocaleHelper.setLocale(context, code)
                                    activity?.recreate()
                                }
                            }
                        )
                    }
                }
            }

            // ── Theme ─────────────────────────────────
            SettingsSection(icon = Icons.Default.Palette, title = stringResource(R.string.settings_theme)) {
                val themeOptions = listOf(
                    ThemeMode.SYSTEM to stringResource(R.string.settings_theme_system),
                    ThemeMode.LIGHT  to stringResource(R.string.settings_theme_light),
                    ThemeMode.DARK   to stringResource(R.string.settings_theme_dark),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    themeOptions.forEach { (mode, label) ->
                        ChoiceCard(
                            text       = label,
                            isSelected = ThemePreference.mode == mode,
                            modifier   = Modifier.weight(1f),
                            onClick    = { ThemePreference.setMode(context, mode) }
                        )
                    }
                }
            }

            // ── Currency ──────────────────────────────
            SettingsSection(icon = Icons.Default.Payments, title = stringResource(R.string.settings_currency)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    currencies.forEach { c ->
                        ChoiceCard(
                            text       = c,
                            isSelected = selectedCurrency == c,
                            modifier   = Modifier.weight(1f),
                            onClick    = {
                                selectedCurrency = c
                                AppPreferences.saveCurrency(context, c)
                            }
                        )
                    }
                }
            }

            // ── Distance unit ─────────────────────────
            SettingsSection(icon = Icons.Default.Straighten, title = stringResource(R.string.settings_distance)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    distanceUnits.forEach { d ->
                        ChoiceCard(
                            text       = d,
                            isSelected = selectedDistance == d,
                            modifier   = Modifier.weight(1f),
                            onClick    = {
                                selectedDistance = d
                                AppPreferences.saveDistanceUnit(context, d)
                            }
                        )
                    }
                }
            }

            // ── Notifications ─────────────────────────
            SettingsSection(icon = Icons.Default.Notifications, title = stringResource(R.string.settings_notifications)) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.settings_notify_enabled), color = MaterialTheme.colorScheme.onSurface)
                    Switch(
                        checked = notificationsEnabled, onCheckedChange = { notificationsEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = OnPrimary, checkedTrackColor = PrimaryYellow)
                    )
                }
            }

            // ── About ─────────────────────────────────
            SettingsSection(icon = Icons.Default.Info, title = stringResource(R.string.settings_about)) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(stringResource(R.string.app_name), color = MaterialTheme.colorScheme.onSurface)
                    Text(stringResource(R.string.settings_version), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    icon: ImageVector,
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Text(
                title,
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onBackground
            )
        }
        content()
    }
}

@Composable
private fun ChoiceCard(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) PrimaryYellow else MaterialTheme.colorScheme.surface)
            .border(
                BorderStroke(
                    1.dp,
                    if (isSelected) PrimaryYellow else MaterialTheme.colorScheme.surfaceVariant
                ),
                RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text       = text,
            color      = if (isSelected) OnPrimary else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            maxLines   = 1
        )
    }
}
