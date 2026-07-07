package com.hbazai.mycarservices.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hbazai.mycarservices.R
import com.hbazai.mycarservices.util.DateFormatter
import com.hbazai.mycarservices.util.JalaliCalendar
import com.hbazai.mycarservices.util.LocaleHelper
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * Date field for logging services done in the past.
 * Opens a Jalali picker for Persian, Material's [DatePicker] otherwise.
 */
@Composable
fun ServiceDateField(
    valueMillis: Long,
    onDateChange: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context    = LocalContext.current
    val isJalali   = LocaleHelper.getSavedLanguage(context) == "fa"
    var showPicker by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { showPicker = true }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(Icons.Default.Event, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        Column(Modifier.weight(1f)) {
            Text(
                stringResource(R.string.field_service_date),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                DateFormatter.format(context, valueMillis),
                style      = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onSurface
            )
        }
        Icon(Icons.Default.ArrowDropDown, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }

    if (showPicker) {
        if (isJalali) {
            JalaliDatePickerDialog(
                initialMillis = valueMillis,
                onConfirm     = { showPicker = false; onDateChange(it) },
                onDismiss     = { showPicker = false }
            )
        } else {
            GregorianDatePickerDialog(
                initialMillis = valueMillis,
                onConfirm     = { showPicker = false; onDateChange(it) },
                onDismiss     = { showPicker = false }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  Material (Gregorian) picker — non-Persian languages
// ─────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GregorianDatePickerDialog(
    initialMillis: Long,
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val today = remember { System.currentTimeMillis() }
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initialMillis,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis <= today
        }
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                state.selectedDateMillis?.let { selected ->
                    // Picker returns UTC midnight; store the same day at local noon.
                    val localNoon = Instant.ofEpochMilli(selected)
                        .atZone(ZoneOffset.UTC).toLocalDate()
                        .atTime(12, 0).atZone(ZoneId.systemDefault())
                        .toInstant().toEpochMilli()
                    onConfirm(localNoon)
                } ?: onDismiss()
            }) { Text(stringResource(R.string.btn_ok)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.btn_cancel)) }
        }
    ) {
        DatePicker(state = state)
    }
}

// ─────────────────────────────────────────────────────────────────
//  Jalali picker — year / month / day dropdowns
// ─────────────────────────────────────────────────────────────────

@Composable
private fun JalaliDatePickerDialog(
    initialMillis: Long,
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val (initYear, initMonth, initDay) = remember(initialMillis) {
        JalaliCalendar.toJalali(initialMillis)
    }
    val (todayYear, _, _) = remember { JalaliCalendar.todayJalali() }

    var year  by remember { mutableIntStateOf(initYear) }
    var month by remember { mutableIntStateOf(initMonth) }
    var day   by remember { mutableIntStateOf(initDay) }

    val maxDay = JalaliCalendar.monthLength(year, month)
    if (day > maxDay) day = maxDay

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                stringResource(R.string.field_service_date),
                color      = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PickerDropdown(
                    label    = stringResource(R.string.date_day),
                    options  = (1..maxDay).map { it.toString() },
                    selected = day - 1,
                    onSelect = { day = it + 1 },
                    modifier = Modifier.weight(0.8f)
                )
                PickerDropdown(
                    label    = stringResource(R.string.date_month),
                    options  = JalaliCalendar.monthNames,
                    selected = month - 1,
                    onSelect = { month = it + 1 },
                    modifier = Modifier.weight(1.2f)
                )
                PickerDropdown(
                    label    = stringResource(R.string.date_year),
                    options  = (todayYear downTo todayYear - 30).map { it.toString() },
                    selected = todayYear - year,
                    onSelect = { year = todayYear - it },
                    modifier = Modifier.weight(1f)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(JalaliCalendar.toEpochMillis(year, month, day)) }) {
                Text(stringResource(R.string.btn_ok), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.btn_cancel)) }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PickerDropdown(
    label: String,
    options: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded         = expanded,
        onExpandedChange = { expanded = it },
        modifier         = modifier
    ) {
        OutlinedTextField(
            value         = options.getOrElse(selected) { "" },
            onValueChange = {},
            readOnly      = true,
            label         = { Text(label) },
            trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            singleLine    = true,
            modifier      = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
            colors        = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor  = MaterialTheme.colorScheme.primary
            )
        )
        ExposedDropdownMenu(
            expanded         = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEachIndexed { index, option ->
                DropdownMenuItem(
                    text    = { Text(option) },
                    onClick = { onSelect(index); expanded = false }
                )
            }
        }
    }
}
