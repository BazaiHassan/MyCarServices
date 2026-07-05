package com.hbazai.mycarservices.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hbazai.mycarservices.R
import com.hbazai.mycarservices.ui.theme.OnPrimary
import com.hbazai.mycarservices.ui.theme.PrimaryYellow
import com.hbazai.mycarservices.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCarScreen(
    carId: Int,
    onBack: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val cars by viewModel.cars.collectAsStateWithLifecycle()
    val car  = cars.find { it.id == carId }

    var name         by remember(car) { mutableStateOf(car?.name ?: "") }
    var model        by remember(car) { mutableStateOf(car?.model ?: "") }
    var year         by remember(car) { mutableStateOf(car?.year?.takeIf { it > 0 }?.toString() ?: "") }
    var plate        by remember(car) { mutableStateOf(car?.licensePlate ?: "") }
    var mileage      by remember(car) { mutableStateOf(car?.currentMileage?.toString() ?: "") }

    var nameError    by remember { mutableStateOf(false) }
    var yearError    by remember { mutableStateOf(false) }
    var mileageError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.edit_car_title),
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
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        if (car == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── Big car icon header ───────────────────
            Box(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .size(96.dp)
                    .background(PrimaryYellow.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.DirectionsCar, null,
                    tint     = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(56.dp)
                )
            }

            CarTextField(
                value         = name,
                onValueChange = { name = it; nameError = false },
                label         = stringResource(R.string.field_car_name),
                isError       = nameError,
                errorMsg      = stringResource(R.string.error_required_field)
            )
            CarTextField(
                value         = mileage,
                onValueChange = { mileage = it; mileageError = false },
                label         = stringResource(R.string.field_current_mileage),
                isError       = mileageError,
                errorMsg      = stringResource(R.string.error_invalid_mileage),
                keyboardType  = KeyboardType.Number
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CarTextField(
                    value         = model,
                    onValueChange = { model = it },
                    label         = stringResource(R.string.field_car_model),
                    modifier      = Modifier.weight(1f)
                )
                CarTextField(
                    value         = year,
                    onValueChange = { year = it; yearError = false },
                    label         = stringResource(R.string.field_car_year),
                    isError       = yearError,
                    errorMsg      = stringResource(R.string.error_invalid_year),
                    keyboardType  = KeyboardType.Number,
                    modifier      = Modifier.weight(1f)
                )
            }
            CarTextField(
                value         = plate,
                onValueChange = { plate = it },
                label         = stringResource(R.string.field_license_plate)
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    nameError    = name.isBlank()
                    yearError    = year.isNotBlank() && year.toIntOrNull() == null
                    mileageError = mileage.isNotBlank() && mileage.toIntOrNull() == null
                    if (!nameError && !yearError && !mileageError) {
                        viewModel.updateCar(
                            car.copy(
                                name           = name.trim(),
                                model          = model.trim(),
                                year           = year.toIntOrNull() ?: 0,
                                licensePlate   = plate.trim(),
                                currentMileage = mileage.toIntOrNull() ?: 0
                            )
                        )
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape    = RoundedCornerShape(16.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = PrimaryYellow, contentColor = OnPrimary
                )
            ) {
                Text(stringResource(R.string.btn_save), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
