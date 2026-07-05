package com.hbazai.mycarservices.screens

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Language
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hbazai.mycarservices.R
import com.hbazai.mycarservices.ui.theme.OnPrimary
import com.hbazai.mycarservices.ui.theme.PrimaryYellow
import com.hbazai.mycarservices.util.AppPreferences
import com.hbazai.mycarservices.util.LocaleHelper

@Composable
fun SetupScreen(onSetupDone: () -> Unit) {
    val context  = LocalContext.current
    val activity = context as? Activity

    val currencies    = listOf("تومان", "﷼", "€", "$", "£")
    val distanceUnits = listOf("km", "mile")

    val savedLanguage    = LocaleHelper.getSavedLanguage(context)
    var selectedCurrency by remember { mutableStateOf(AppPreferences.getCurrency(context)) }
    var selectedDistance by remember { mutableStateOf(AppPreferences.getDistanceUnit(context)) }

    // Persian default pairs naturally with Toman on a fresh install.
    LaunchedEffect(Unit) {
        if (savedLanguage == "fa" && AppPreferences.getCurrency(context) == "€") {
            selectedCurrency = "تومان"
            AppPreferences.saveCurrency(context, "تومان")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        Box(
            modifier = Modifier
                .padding(top = 16.dp)
                .size(88.dp)
                .background(PrimaryYellow, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.DirectionsCar, null,
                tint     = OnPrimary,
                modifier = Modifier.size(52.dp)
            )
        }

        Text(
            text       = stringResource(R.string.setup_title),
            style      = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.primary,
            textAlign  = TextAlign.Center
        )

        Text(
            text      = stringResource(R.string.setup_subtitle),
            style     = MaterialTheme.typography.bodyMedium,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        // ── Language — big tap cards ──────────────────
        SetupSection(icon = Icons.Default.Language, title = stringResource(R.string.setup_language)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                LocaleHelper.supportedLanguages.forEach { (code, displayName) ->
                    SetupChoiceCard(
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

        // ── Currency — one-tap chips ──────────────────
        SetupSection(icon = Icons.Default.Payments, title = stringResource(R.string.settings_currency)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                currencies.forEach { c ->
                    SetupChoiceCard(
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

        // ── Distance unit ─────────────────────────────
        SetupSection(icon = Icons.Default.Straighten, title = stringResource(R.string.settings_distance)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                distanceUnits.forEach { d ->
                    SetupChoiceCard(
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

        Spacer(Modifier.weight(1f))

        Button(
            onClick = {
                AppPreferences.saveCurrency(context, selectedCurrency)
                AppPreferences.saveDistanceUnit(context, selectedDistance)
                AppPreferences.markSetupDone(context)
                onSetupDone()
            },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp).height(56.dp),
            shape    = RoundedCornerShape(16.dp),
            colors   = ButtonDefaults.buttonColors(
                containerColor = PrimaryYellow,
                contentColor   = OnPrimary
            )
        ) {
            Text(stringResource(R.string.setup_start), fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
private fun SetupSection(
    icon: ImageVector,
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier            = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Text(
                title,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onBackground,
                style      = MaterialTheme.typography.titleMedium
            )
        }
        content()
    }
}

@Composable
private fun SetupChoiceCard(
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
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text       = text,
            color      = if (isSelected) OnPrimary else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
