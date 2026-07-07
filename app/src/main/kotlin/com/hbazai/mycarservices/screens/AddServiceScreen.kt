package com.hbazai.mycarservices.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hbazai.mycarservices.R
import com.hbazai.mycarservices.ui.ServiceCatalog
import com.hbazai.mycarservices.ui.ServiceType
import com.hbazai.mycarservices.ui.theme.OnPrimary
import com.hbazai.mycarservices.ui.theme.PrimaryYellow
import com.hbazai.mycarservices.util.AppPreferences
import com.hbazai.mycarservices.util.ltr
import com.hbazai.mycarservices.viewmodel.ServiceViewModel

private const val NEXT_SERVICE_INTERVAL = 5000

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddServiceScreen(
    carId: Int,
    onBack: () -> Unit,
    viewModel: ServiceViewModel = hiltViewModel()
) {
    val context      = LocalContext.current
    val currency     = AppPreferences.getCurrency(context)
    val distanceUnit = AppPreferences.getDistanceUnit(context)

    LaunchedEffect(carId) { viewModel.loadCar(carId) }
    val car by viewModel.car.collectAsStateWithLifecycle()

    // Selected service labels -> cost text (empty = free/unknown)
    val selected = remember { mutableStateMapOf<String, String>() }

    var mileage          by remember { mutableStateOf("") }
    var nextMileage      by remember { mutableStateOf("") }
    var serviceDate      by remember { mutableStateOf(System.currentTimeMillis()) }
    var dateEdited       by remember { mutableStateOf(false) }
    var nextEdited       by remember { mutableStateOf(false) }
    var mileagePrefilled by remember { mutableStateOf(false) }
    var showDetails      by remember { mutableStateOf(false) }
    var notes            by remember { mutableStateOf("") }
    var cause            by remember { mutableStateOf("") }
    var providerName     by remember { mutableStateOf("") }
    var providerPhone    by remember { mutableStateOf("") }
    var mileageError     by remember { mutableStateOf(false) }
    var serviceError     by remember { mutableStateOf(false) }

    // Prefill mileage from the car once, and suggest the next service mileage.
    LaunchedEffect(car) {
        val c = car ?: return@LaunchedEffect
        if (!mileagePrefilled) {
            mileagePrefilled = true
            if (c.currentMileage > 0) {
                mileage     = c.currentMileage.toString()
                nextMileage = (c.currentMileage + NEXT_SERVICE_INTERVAL).toString()
            }
        }
    }

    val hasRepair = selected.keys.any { ServiceCatalog.isRepair(it) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.add_service_title),
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.primary
                    )
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
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.background, shadowElevation = 8.dp) {
                Button(
                    onClick = {
                        serviceError = selected.isEmpty()
                        mileageError = mileage.toIntOrNull() == null
                        if (!serviceError && !mileageError) {
                            val m = mileage.toInt()
                            viewModel.addServices(
                                carId              = carId,
                                services           = selected.mapValues { it.value.toDoubleOrNull() ?: 0.0 },
                                mileageAtService   = m,
                                nextServiceMileage = nextMileage.toIntOrNull() ?: (m + NEXT_SERVICE_INTERVAL),
                                notes              = notes,
                                cause              = cause,
                                providerName       = providerName,
                                providerPhone      = providerPhone,
                                serviceDateMillis  = if (dateEdited) serviceDate else null
                            )
                            onBack()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp)
                        .height(56.dp),
                    shape  = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryYellow,
                        contentColor   = OnPrimary
                    )
                ) {
                    Text(
                        text = if (selected.isEmpty()) stringResource(R.string.btn_save)
                               else "${stringResource(R.string.btn_save)} (${selected.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize   = 16.sp
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Car header ────────────────────────────
            car?.let { c ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(12.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(PrimaryYellow, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.DirectionsCar, null, tint = OnPrimary)
                    }
                    Column(Modifier.weight(1f)) {
                        Text(c.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            ltr("${c.currentMileage} $distanceUnit"),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(Icons.Default.Speed, null, tint = MaterialTheme.colorScheme.primary)
                }
            }

            // ── Service icon grid ─────────────────────
            Text(
                text       = stringResource(R.string.add_service_select_hint),
                fontWeight = FontWeight.Bold,
                color      = if (serviceError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                style      = MaterialTheme.typography.titleMedium
            )

            ServiceCatalog.types.chunked(4).forEach { row ->
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    row.forEach { type ->
                        val label = stringResource(type.labelRes)
                        ServiceTile(
                            type       = type,
                            label      = label,
                            isSelected = selected.containsKey(label),
                            onToggle   = {
                                serviceError = false
                                if (selected.containsKey(label)) selected.remove(label)
                                else selected[label] = ""
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }

            // ── Selected services & optional cost ─────
            AnimatedVisibility(visible = selected.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text       = stringResource(R.string.selected_services),
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.primary,
                        style      = MaterialTheme.typography.titleSmall
                    )
                    ServiceCatalog.types.forEach { type ->
                        val label = stringResource(type.labelRes)
                        if (selected.containsKey(label)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(type.icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                                Text(
                                    label,
                                    modifier = Modifier.weight(1f),
                                    style    = MaterialTheme.typography.bodyMedium,
                                    color    = MaterialTheme.colorScheme.onSurface
                                )
                                OutlinedTextField(
                                    value         = selected[label] ?: "",
                                    onValueChange = { selected[label] = it },
                                    label         = { Text(stringResource(R.string.field_cost_optional), fontSize = 10.sp) },
                                    suffix        = { Text(currency, fontSize = 12.sp) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine    = true,
                                    modifier      = Modifier.width(150.dp),
                                    textStyle     = MaterialTheme.typography.bodyMedium,
                                    colors        = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        focusedLabelColor  = MaterialTheme.colorScheme.primary,
                                        cursorColor        = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // ── Service date (defaults to today, editable for past services) ──
            ServiceDateField(
                valueMillis  = serviceDate,
                onDateChange = { serviceDate = it; dateEdited = true }
            )

            // ── Mileage (prefilled) ───────────────────
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CarTextField(
                    value         = mileage,
                    onValueChange = {
                        mileage      = it
                        mileageError = false
                        if (!nextEdited) {
                            nextMileage = it.toIntOrNull()
                                ?.let { m -> (m + NEXT_SERVICE_INTERVAL).toString() } ?: ""
                        }
                    },
                    label        = "${stringResource(R.string.field_mileage_at_service)} ($distanceUnit)",
                    isError      = mileageError,
                    errorMsg     = stringResource(R.string.error_invalid_mileage),
                    keyboardType = KeyboardType.Number,
                    modifier     = Modifier.weight(1f)
                )
                CarTextField(
                    value         = nextMileage,
                    onValueChange = { nextMileage = it; nextEdited = true },
                    label         = "${stringResource(R.string.field_next_service_mileage)} ($distanceUnit)",
                    keyboardType  = KeyboardType.Number,
                    modifier      = Modifier.weight(1f)
                )
            }

            // ── Collapsible extra details ─────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { showDetails = !showDetails }
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    stringResource(R.string.more_details),
                    color      = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    if (showDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    null, tint = MaterialTheme.colorScheme.primary
                )
            }

            AnimatedVisibility(visible = showDetails || hasRepair) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (hasRepair) {
                        CarTextField(
                            value         = cause,
                            onValueChange = { cause = it },
                            label         = stringResource(R.string.field_cause)
                        )
                    }
                    if (showDetails) {
                        CarTextField(
                            value         = providerName,
                            onValueChange = { providerName = it },
                            label         = stringResource(R.string.field_provider_name)
                        )
                        CarTextField(
                            value         = providerPhone,
                            onValueChange = { providerPhone = it },
                            label         = stringResource(R.string.field_provider_phone),
                            keyboardType  = KeyboardType.Phone
                        )
                        CarTextField(
                            value         = notes,
                            onValueChange = { notes = it },
                            label         = stringResource(R.string.field_notes)
                        )
                    }
                }
            }

            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
private fun ServiceTile(
    type: ServiceType,
    label: String,
    isSelected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val background by animateColorAsState(
        if (isSelected) PrimaryYellow else MaterialTheme.colorScheme.surface,
        label = "tileBg"
    )
    val contentColor = if (isSelected) OnPrimary else MaterialTheme.colorScheme.onSurfaceVariant

    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(background)
                .border(
                    BorderStroke(
                        1.dp,
                        if (isSelected) PrimaryYellow else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    RoundedCornerShape(18.dp)
                )
                .clickable { onToggle() }
                .padding(vertical = 14.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(type.icon, null, tint = contentColor, modifier = Modifier.size(34.dp))
            Text(
                text       = label,
                color      = contentColor,
                fontSize   = 11.sp,
                lineHeight = 13.sp,
                maxLines   = 2,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                textAlign  = TextAlign.Center
            )
        }
        if (isSelected) {
            Icon(
                Icons.Default.CheckCircle, null,
                tint     = OnPrimary,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(16.dp)
            )
        }
    }
}
