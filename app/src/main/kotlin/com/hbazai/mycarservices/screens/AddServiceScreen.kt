package com.hbazai.mycarservices.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.hbazai.mycarservices.R
import com.hbazai.mycarservices.ui.theme.OnPrimary
import com.hbazai.mycarservices.ui.theme.PrimaryYellow
import com.hbazai.mycarservices.util.AppPreferences
import com.hbazai.mycarservices.viewmodel.ServiceViewModel

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

    val allServiceTypes = listOf(
        stringResource(R.string.service_oil_change),
        stringResource(R.string.service_gearbox_oil),
        stringResource(R.string.service_timing_belt),
        stringResource(R.string.service_tire_rotation),
        stringResource(R.string.service_brake_check),
        stringResource(R.string.service_air_filter),
        stringResource(R.string.service_car_repair),
        stringResource(R.string.service_custom)
    )

    // Map of serviceType -> price (null means not checked)
    val checkedServices = remember {
        mutableStateMapOf<String, Double?>()
    }

    // Which service is currently asking for price
    var pendingPriceService by remember { mutableStateOf<String?>(null) }
    var priceInput          by remember { mutableStateOf("") }

    // Shared fields
    var mileageAtService by remember { mutableStateOf("") }
    var nextMileage      by remember { mutableStateOf("") }
    var notes            by remember { mutableStateOf("") }
    var cause            by remember { mutableStateOf("") }
    var providerName     by remember { mutableStateOf("") }
    var providerPhone    by remember { mutableStateOf("") }
    var imageUri         by remember { mutableStateOf<Uri?>(null) }
    var mileageError     by remember { mutableStateOf(false) }
    var nextMileageError by remember { mutableStateOf(false) }
    var serviceError     by remember { mutableStateOf(false) }

    val hasRepair = checkedServices.keys.any {
        it.contains("Repair", ignoreCase = true) ||
        it.contains("Reparatur", ignoreCase = true) ||
        it.contains("تعمیر")
    }

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> imageUri = uri }

    // ── Price dialog ──────────────────────────────
    if (pendingPriceService != null) {
        Dialog(onDismissRequest = {
            // user dismissed without entering price — uncheck
            checkedServices.remove(pendingPriceService)
            pendingPriceService = null
            priceInput = ""
        }) {
            Card(
                shape  = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier            = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text       = pendingPriceService ?: "",
                        fontWeight = FontWeight.Bold,
                        color      = PrimaryYellow,
                        style      = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text  = "Enter the cost for this service ($currency)",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedTextField(
                        value         = priceInput,
                        onValueChange = { priceInput = it },
                        label         = { Text("Cost ($currency)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine    = true,
                        modifier      = Modifier.fillMaxWidth(),
                        colors        = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryYellow,
                            focusedLabelColor  = PrimaryYellow,
                            cursorColor        = PrimaryYellow
                        )
                    )
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick  = {
                                checkedServices.remove(pendingPriceService)
                                pendingPriceService = null
                                priceInput = ""
                            },
                            modifier = Modifier.weight(1f)
                        ) { Text(stringResource(R.string.btn_cancel)) }

                        Button(
                            onClick = {
                                val price = priceInput.toDoubleOrNull() ?: 0.0
                                checkedServices[pendingPriceService!!] = price
                                pendingPriceService = null
                                priceInput = ""
                            },
                            modifier = Modifier.weight(1f),
                            colors   = ButtonDefaults.buttonColors(
                                containerColor = PrimaryYellow,
                                contentColor   = OnPrimary
                            )
                        ) { Text(stringResource(R.string.btn_save)) }
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.add_service_title),
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ── Service checkboxes ────────────────────
            Text(
                text       = stringResource(R.string.field_service_type),
                fontWeight = FontWeight.Bold,
                color      = PrimaryYellow,
                style      = MaterialTheme.typography.titleMedium
            )

            if (serviceError) {
                Text(
                    text  = "Please select at least one service",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Card(
                shape  = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    allServiceTypes.forEach { serviceType ->
                        val isChecked = checkedServices.containsKey(serviceType)
                        val price     = checkedServices[serviceType]

                        Row(
                            modifier          = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isChecked) {
                                        checkedServices.remove(serviceType)
                                    } else {
                                        // open price dialog
                                        priceInput          = ""
                                        pendingPriceService = serviceType
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked         = isChecked,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        priceInput          = ""
                                        pendingPriceService = serviceType
                                    } else {
                                        checkedServices.remove(serviceType)
                                    }
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = PrimaryYellow,
                                    checkmarkColor = OnPrimary
                                )
                            )
                            Text(
                                text     = serviceType,
                                modifier = Modifier.weight(1f),
                                color    = MaterialTheme.colorScheme.onSurface
                            )
                            if (isChecked && price != null) {
                                Text(
                                    text  = "$currency ${"%.2f".format(price)}",
                                    color = PrimaryYellow,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // ── Cause — only if repair is checked ─────
            if (hasRepair) {
                CarTextField(
                    value         = cause,
                    onValueChange = { cause = it },
                    label         = stringResource(R.string.field_cause)
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

            // ── Shared fields ─────────────────────────
            CarTextField(
                value         = mileageAtService,
                onValueChange = { mileageAtService = it; mileageError = false },
                label         = "${stringResource(R.string.field_mileage_at_service)} ($distanceUnit)",
                isError       = mileageError,
                errorMsg      = stringResource(R.string.error_invalid_mileage),
                keyboardType  = KeyboardType.Number
            )

            CarTextField(
                value         = nextMileage,
                onValueChange = { nextMileage = it; nextMileageError = false },
                label         = "${stringResource(R.string.field_next_service_mileage)} ($distanceUnit)",
                isError       = nextMileageError,
                errorMsg      = stringResource(R.string.error_invalid_mileage),
                keyboardType  = KeyboardType.Number
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

            Text(
                text       = "Service Provider",
                fontWeight = FontWeight.Bold,
                color      = PrimaryYellow,
                style      = MaterialTheme.typography.titleSmall
            )

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

            // ── Photo ─────────────────────────────────
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { imagePicker.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    AsyncImage(
                        model            = imageUri,
                        contentDescription = null,
                        contentScale     = ContentScale.Crop,
                        modifier         = Modifier.fillMaxSize()
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.CameraAlt, null,
                            tint     = PrimaryYellow,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            stringResource(R.string.field_add_photo),
                            color    = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    serviceError     = checkedServices.isEmpty()
                    mileageError     = mileageAtService.toIntOrNull() == null
                    nextMileageError = nextMileage.toIntOrNull() == null

                    if (!serviceError && !mileageError && !nextMileageError) {
                        // Save one record per checked service
                        checkedServices.forEach { (serviceType, price) ->
                            viewModel.addService(
                                carId              = carId,
                                serviceType        = serviceType,
                                mileageAtService   = mileageAtService.toInt(),
                                nextServiceMileage = nextMileage.toInt(),
                                cost               = price ?: 0.0,
                                notes              = notes,
                                cause              = cause,
                                imagePath          = imageUri?.toString() ?: "",
                                providerName       = providerName,
                                providerPhone      = providerPhone
                            )
                        }
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = PrimaryYellow,
                    contentColor   = OnPrimary
                )
            ) {
                Text(stringResource(R.string.btn_save), fontWeight = FontWeight.Bold)
            }
        }
    }
}