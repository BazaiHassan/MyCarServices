package com.hbazai.mycarservices.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.hbazai.mycarservices.R
import com.hbazai.mycarservices.ui.theme.OnPrimary
import com.hbazai.mycarservices.ui.theme.PrimaryYellow
import com.hbazai.mycarservices.viewmodel.ServiceViewModel
import com.hbazai.mycarservices.screens.CarTextField


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddServiceScreen(
    carId: Int,
    serviceId: Int,
    onBack: () -> Unit,
    viewModel: ServiceViewModel = hiltViewModel()
) {
    val serviceTypes = listOf(
        stringResource(R.string.service_oil_change),
        stringResource(R.string.service_gearbox_oil),
        stringResource(R.string.service_tire_rotation),
        stringResource(R.string.service_brake_check),
        stringResource(R.string.service_air_filter),
        stringResource(R.string.service_car_repair),
        stringResource(R.string.service_custom)
    )

    var selectedType     by remember { mutableStateOf(serviceTypes[0]) }
    var expanded         by remember { mutableStateOf(false) }
    var mileageAtService by remember { mutableStateOf("") }
    var nextMileage      by remember { mutableStateOf("") }
    var cost             by remember { mutableStateOf("") }
    var notes            by remember { mutableStateOf("") }
    var cause            by remember { mutableStateOf("") }
    var providerName     by remember { mutableStateOf("") }
    var providerPhone    by remember { mutableStateOf("") }
    var imageUri         by remember { mutableStateOf<Uri?>(null) }
    var mileageError     by remember { mutableStateOf(false) }
    var nextMileageError by remember { mutableStateOf(false) }

    val isRepair = selectedType.contains("Repair", ignoreCase = true) ||
                   selectedType.contains("Reparatur", ignoreCase = true) ||
                   selectedType.contains("تعمیر", ignoreCase = true)

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> imageUri = uri }

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
                        Icon(Icons.Default.ArrowBack, null, tint = PrimaryYellow)
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

            // ── Service type dropdown ─────────────────
            ExposedDropdownMenuBox(
                expanded         = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value         = selectedType,
                    onValueChange = {},
                    readOnly      = true,
                    label         = { Text(stringResource(R.string.field_service_type)) },
                    trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier      = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryYellow,
                        focusedLabelColor  = PrimaryYellow
                    )
                )
                ExposedDropdownMenu(
                    expanded         = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    serviceTypes.forEach { type ->
                        DropdownMenuItem(
                            text    = { Text(type) },
                            onClick = {
                                selectedType = type
                                expanded     = false
                            }
                        )
                    }
                }
            }

            // ── Cause field — only for repairs ────────
            if (isRepair) {
                CarTextField(
                    value         = cause,
                    onValueChange = { cause = it },
                    label         = stringResource(R.string.field_cause)
                )
            }

            CarTextField(
                value         = mileageAtService,
                onValueChange = { mileageAtService = it; mileageError = false },
                label         = stringResource(R.string.field_mileage_at_service),
                isError       = mileageError,
                errorMsg      = stringResource(R.string.error_invalid_mileage),
                keyboardType  = KeyboardType.Number
            )

            CarTextField(
                value         = nextMileage,
                onValueChange = { nextMileage = it; nextMileageError = false },
                label         = stringResource(R.string.field_next_service_mileage),
                isError       = nextMileageError,
                errorMsg      = stringResource(R.string.error_invalid_mileage),
                keyboardType  = KeyboardType.Number
            )

            CarTextField(
                value         = cost,
                onValueChange = { cost = it },
                label         = stringResource(R.string.field_cost),
                keyboardType  = KeyboardType.Decimal
            )

            // ── Provider info ─────────────────────────
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Text(
                text       = stringResource(R.string.field_provider_name),
                style      = MaterialTheme.typography.labelLarge,
                color      = PrimaryYellow,
                fontWeight = FontWeight.Bold
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

            // ── Service photo ─────────────────────────
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
                        model              = imageUri,
                        contentDescription = null,
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier.fillMaxSize()
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = null,
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
                    mileageError     = mileageAtService.toIntOrNull() == null
                    nextMileageError = nextMileage.toIntOrNull() == null
                    if (!mileageError && !nextMileageError) {
                        viewModel.addService(
                            carId              = carId,
                            serviceType        = selectedType,
                            mileageAtService   = mileageAtService.toInt(),
                            nextServiceMileage = nextMileage.toInt(),
                            cost               = cost.toDoubleOrNull() ?: 0.0,
                            notes              = notes,
                            cause              = cause,
                            imagePath          = imageUri?.toString() ?: "",
                            providerName       = providerName,
                            providerPhone      = providerPhone
                        )
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