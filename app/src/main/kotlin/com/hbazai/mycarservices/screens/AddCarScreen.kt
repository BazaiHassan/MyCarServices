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
import androidx.compose.ui.platform.LocalContext
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
import com.hbazai.mycarservices.viewmodel.HomeViewModel
import com.hbazai.mycarservices.screens.CarTextField


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCarScreen(
    onBack: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    var name         by remember { mutableStateOf("") }
    var model        by remember { mutableStateOf("") }
    var year         by remember { mutableStateOf("") }
    var plate        by remember { mutableStateOf("") }
    var mileage      by remember { mutableStateOf("") }
    var imageUri     by remember { mutableStateOf<Uri?>(null) }
    var nameError    by remember { mutableStateOf(false) }
    var modelError   by remember { mutableStateOf(false) }
    var yearError    by remember { mutableStateOf(false) }
    var mileageError by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> imageUri = uri }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.add_car_title),
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

            // ── Car image picker ──────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { imagePicker.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    AsyncImage(
                        model             = imageUri,
                        contentDescription = stringResource(R.string.field_car_photo),
                        contentScale      = ContentScale.Crop,
                        modifier          = Modifier.fillMaxSize()
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint   = PrimaryYellow,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            stringResource(R.string.field_add_photo),
                            color    = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            CarTextField(
                value         = name,
                onValueChange = { name = it; nameError = false },
                label         = stringResource(R.string.field_car_name),
                isError       = nameError,
                errorMsg      = stringResource(R.string.error_required_field)
            )
            CarTextField(
                value         = model,
                onValueChange = { model = it; modelError = false },
                label         = stringResource(R.string.field_car_model),
                isError       = modelError,
                errorMsg      = stringResource(R.string.error_required_field)
            )
            CarTextField(
                value         = year,
                onValueChange = { year = it; yearError = false },
                label         = stringResource(R.string.field_car_year),
                isError       = yearError,
                errorMsg      = stringResource(R.string.error_invalid_year),
                keyboardType  = KeyboardType.Number
            )
            CarTextField(
                value         = plate,
                onValueChange = { plate = it },
                label         = stringResource(R.string.field_license_plate)
            )
            CarTextField(
                value         = mileage,
                onValueChange = { mileage = it; mileageError = false },
                label         = stringResource(R.string.field_current_mileage),
                isError       = mileageError,
                errorMsg      = stringResource(R.string.error_invalid_mileage),
                keyboardType  = KeyboardType.Number
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    nameError    = name.isBlank()
                    modelError   = model.isBlank()
                    yearError    = year.toIntOrNull() == null
                    mileageError = mileage.toIntOrNull() == null
                    if (!nameError && !modelError && !yearError && !mileageError) {
                        viewModel.addCar(
                            name         = name,
                            model        = model,
                            year         = year.toInt(),
                            licensePlate = plate,
                            mileage      = mileage.toInt(),
                            imagePath    = imageUri?.toString() ?: ""
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