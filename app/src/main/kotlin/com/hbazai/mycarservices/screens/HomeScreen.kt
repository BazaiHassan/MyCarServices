package com.hbazai.mycarservices.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hbazai.mycarservices.R
import com.hbazai.mycarservices.data.local.entity.CarEntity
import com.hbazai.mycarservices.data.local.entity.ServiceRecordEntity
import com.hbazai.mycarservices.ui.theme.*
import com.hbazai.mycarservices.util.AppPreferences
import com.hbazai.mycarservices.util.DateFormatter
import com.hbazai.mycarservices.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddCar: () -> Unit,
    onAddService: (Int) -> Unit,
    onCarClick: (Int) -> Unit,
    onEditCar: (Int) -> Unit,
    onViewReports: () -> Unit,
    onSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val context        = LocalContext.current
    val cars           by viewModel.cars.collectAsStateWithLifecycle()
    val latestServices by viewModel.latestServices.collectAsStateWithLifecycle()
    val distanceUnit   = AppPreferences.getDistanceUnit(context)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text       = stringResource(R.string.home_title),
                        fontWeight = FontWeight.Bold,
                        color      = PrimaryYellow
                    )
                },
                actions = {
                    IconButton(onClick = onViewReports) {
                        Icon(Icons.Default.List, null, tint = PrimaryYellow)
                    }
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Default.Settings, null, tint = PrimaryYellow)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick        = onAddCar,
                containerColor = PrimaryYellow,
                contentColor   = OnPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        if (cars.isEmpty()) {
            EmptyHomeState(onAddCar = onAddCar, modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier            = Modifier.fillMaxSize().padding(padding),
                contentPadding      = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(cars, key = { it.id }) { car ->
                    CarCard(
                        car           = car,
                        latestService = latestServices[car.id],
                        distanceUnit  = distanceUnit,
                        onCarClick    = { onCarClick(car.id) },
                        onAddService  = { onAddService(car.id) },
                        onEditCar     = { onEditCar(car.id) },
                        onDeleteCar   = { viewModel.deleteCar(car) }
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun EmptyHomeState(onAddCar: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("🚗", style = MaterialTheme.typography.headlineLarge)
            Text(
                stringResource(R.string.home_no_cars),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = onAddCar,
                colors  = ButtonDefaults.buttonColors(
                    containerColor = PrimaryYellow, contentColor = OnPrimary
                )
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.home_add_car), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun CarCard(
    car: CarEntity,
    latestService: ServiceRecordEntity?,
    distanceUnit: String,
    onCarClick: () -> Unit,
    onAddService: () -> Unit,
    onEditCar: () -> Unit,
    onDeleteCar: () -> Unit
) {
    val context   = LocalContext.current
    val now       = System.currentTimeMillis()
    val sevenDays = 7 * 24 * 60 * 60 * 1000L

    val isOverdue = latestService != null && latestService.nextServiceDate < now
    val isDueSoon = latestService != null &&
            latestService.nextServiceDate in now..(now + sevenDays)

    val statusColor = when {
        isOverdue -> StatusOverdue
        isDueSoon -> StatusWarning
        else      -> StatusOk
    }
    val statusText = when {
        isOverdue -> stringResource(R.string.home_overdue)
        isDueSoon -> stringResource(R.string.home_due_soon)
        else      -> stringResource(R.string.home_ok)
    }

    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor   = MaterialTheme.colorScheme.surface,
            title = {
                Text(stringResource(R.string.delete_car_title), color = PrimaryYellow)
            },
            text = { Text(stringResource(R.string.delete_car_confirm)) },
            confirmButton = {
                TextButton(onClick = { showDeleteConfirm = false; onDeleteCar() }) {
                    Text(stringResource(R.string.btn_delete), color = StatusOverdue)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }

    Card(
        modifier  = Modifier.fillMaxWidth().clickable { onCarClick() },
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // ── Header ────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        car.name,
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color      = PrimaryYellow
                    )
                    Text(
                        "${car.model} · ${car.year}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (car.licensePlate.isNotBlank()) {
                        Text(
                            car.licensePlate,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                OutlinedButton(
                    onClick  = onEditCar,
                    modifier = Modifier.weight(0.5f),
                    border   = androidx.compose.foundation.BorderStroke(1.dp, PrimaryYellow),
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryYellow)
                ) {
                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(2.dp))
                    Text(stringResource(R.string.btn_edit))
                }
                // Box(
                //     modifier = Modifier
                //         .clip(RoundedCornerShape(20.dp))
                //         .background(statusColor.copy(alpha = 0.15f))
                //         .padding(horizontal = 12.dp, vertical = 4.dp)
                // ) {
                //     Text(
                //         statusText,
                //         color      = statusColor,
                //         style      = MaterialTheme.typography.labelLarge,
                //         fontWeight = FontWeight.Bold
                //     )
                // }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(Modifier.height(12.dp))

            // ── Stats ─────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = stringResource(R.string.home_mileage),
                    value = "${car.currentMileage} $distanceUnit"
                )
                StatItem(
                    label = stringResource(R.string.home_last_service),
                    value = latestService?.let {
                        DateFormatter.formatShort(context, it.serviceDate)
                    } ?: "—"
                )
                StatItem(
                    label      = stringResource(R.string.home_next_service),
                    value      = latestService?.let {
                        DateFormatter.formatShort(context, it.nextServiceDate)
                    } ?: "—",
                    valueColor = statusColor
                )
            }

            Spacer(Modifier.height(12.dp))

            // ── Action buttons ────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick  = onAddService,
                    modifier = Modifier.weight(1f),
                    border   = androidx.compose.foundation.BorderStroke(1.dp, PrimaryYellow),
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryYellow)
                ) {
                    Icon(Icons.Default.Build, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.nav_add_service))
                }

                OutlinedButton(
                    onClick  = { showDeleteConfirm = true },
                    modifier = Modifier.weight(1f),
                    border   = androidx.compose.foundation.BorderStroke(1.dp, StatusOverdue),
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = StatusOverdue)
                ) {
                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.btn_delete))
                }
            }
        }
    }
}

@Composable
fun StatItem(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Text(
            value,
            style      = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color      = valueColor
        )
    }
}