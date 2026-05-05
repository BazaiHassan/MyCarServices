package com.hbazai.mycarservices.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hbazai.mycarservices.R
import com.hbazai.mycarservices.ui.theme.OnPrimary
import com.hbazai.mycarservices.ui.theme.PrimaryYellow
import com.hbazai.mycarservices.util.AppPreferences
import com.hbazai.mycarservices.viewmodel.ServiceViewModel
import androidx.compose.foundation.shape.RoundedCornerShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditServiceScreen(
    serviceId: Int,
    onBack: () -> Unit,
    viewModel: ServiceViewModel = hiltViewModel()
) {
    val context  = LocalContext.current
    val currency = AppPreferences.getCurrency(context)
    val distUnit = AppPreferences.getDistanceUnit(context)

    val allServices by viewModel.allServices.collectAsStateWithLifecycle()
    val service     = allServices.find { it.id == serviceId }

    var mileageAtService by remember(service) { mutableStateOf(service?.mileageAtService?.toString() ?: "") }
    var nextMileage      by remember(service) { mutableStateOf(service?.nextServiceMileage?.toString() ?: "") }
    var cost             by remember(service) { mutableStateOf(service?.cost?.toString() ?: "") }
    var notes            by remember(service) { mutableStateOf(service?.notes ?: "") }
    var cause            by remember(service) { mutableStateOf(service?.cause ?: "") }
    var providerName     by remember(service) { mutableStateOf(service?.providerName ?: "") }
    var providerPhone    by remember(service) { mutableStateOf(service?.providerPhone ?: "") }
    var mileageError     by remember { mutableStateOf(false) }
    var nextMileageError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.edit_service_title),
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

        if (service == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryYellow)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Service type — read only, shown as label
            Card(
                shape  = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(stringResource(R.string.field_service_type), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(service.serviceType, color = PrimaryYellow, fontWeight = FontWeight.Bold)
                }
            }

            if (service.cause.isNotBlank() || service.serviceType.contains("Repair", ignoreCase = true)) {
                CarTextField(cause, { cause = it }, stringResource(R.string.field_cause))
            }

            CarTextField(
                value         = mileageAtService,
                onValueChange = { mileageAtService = it; mileageError = false },
                label         = "${stringResource(R.string.field_mileage_at_service)} ($distUnit)",
                isError       = mileageError,
                errorMsg      = stringResource(R.string.error_invalid_mileage),
                keyboardType  = KeyboardType.Number
            )

            CarTextField(
                value         = nextMileage,
                onValueChange = { nextMileage = it; nextMileageError = false },
                label         = "${stringResource(R.string.field_next_service_mileage)} ($distUnit)",
                isError       = nextMileageError,
                errorMsg      = stringResource(R.string.error_invalid_mileage),
                keyboardType  = KeyboardType.Number
            )

            CarTextField(
                value         = cost,
                onValueChange = { cost = it },
                label         = "$currency ${stringResource(R.string.field_cost)}",
                keyboardType  = KeyboardType.Decimal
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

            Text("Service Provider", fontWeight = FontWeight.Bold, color = PrimaryYellow, style = MaterialTheme.typography.titleSmall)

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

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    mileageError     = mileageAtService.toIntOrNull() == null
                    nextMileageError = nextMileage.toIntOrNull() == null
                    if (!mileageError && !nextMileageError) {
                        viewModel.updateService(
                            service.copy(
                                mileageAtService   = mileageAtService.toInt(),
                                nextServiceMileage = nextMileage.toInt(),
                                cost               = cost.toDoubleOrNull() ?: service.cost,
                                notes              = notes,
                                cause              = cause,
                                providerName       = providerName,
                                providerPhone      = providerPhone
                            )
                        )
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = PrimaryYellow, contentColor = OnPrimary
                )
            ) {
                Text(stringResource(R.string.btn_save), fontWeight = FontWeight.Bold)
            }
        }
    }
}