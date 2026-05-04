package com.hbazai.mycarservices.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hbazai.mycarservices.R
import com.hbazai.mycarservices.data.local.entity.CarEntity
import com.hbazai.mycarservices.data.local.entity.ServiceRecordEntity
import com.hbazai.mycarservices.ui.theme.*
import com.hbazai.mycarservices.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.*
import com.hbazai.mycarservices.screens.CarTextField
import androidx.compose.material.icons.filled.Delete

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddCar: () -> Unit,
    onAddService: (Int) -> Unit,
    onViewReports: () -> Unit,
    onSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val cars           by viewModel.cars.collectAsStateWithLifecycle()
    val latestServices by viewModel.latestServices.collectAsStateWithLifecycle()

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
                        Icon(
                            Icons.Default.List,
                            contentDescription = stringResource(R.string.nav_reports),
                            tint = PrimaryYellow
                        )
                    }
                    IconButton(onClick = onSettings) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = stringResource(R.string.nav_settings),
                            tint = PrimaryYellow
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick          = onAddCar,
                containerColor   = PrimaryYellow,
                contentColor     = OnPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.home_add_car))
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        if (cars.isEmpty()) {
            EmptyHomeState(
                onAddCar = onAddCar,
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                modifier            = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding      = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(cars) { car ->
                    CarCard(
                        car           = car,
                        latestService = latestServices[car.id],
                        onAddService  = { onAddService(car.id) },
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
    Box(
        modifier          = modifier.fillMaxSize(),
        contentAlignment  = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = "🚗", style = MaterialTheme.typography.headlineLarge)
            Text(
                text  = stringResource(R.string.home_no_cars),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = onAddCar,
                colors  = ButtonDefaults.buttonColors(
                    containerColor = PrimaryYellow,
                    contentColor   = OnPrimary
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
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
    onAddService: () -> Unit,
    onDeleteCar: () -> Unit
) {
    val now = System.currentTimeMillis()
    val sevenDays = 7 * 24 * 60 * 60 * 1000L
    val isOverdue = latestService != null && latestService.nextServiceDate < now
    val isDueSoon = latestService != null &&
            latestService.nextServiceDate in now..(now + sevenDays)

    val statusColor = when {
        isOverdue -> StatusOverdue
        isDueSoon -> StatusWarning
        else -> StatusOk
    }

    val statusText = when {
        isOverdue -> stringResource(R.string.home_overdue)
        isDueSoon -> stringResource(R.string.home_due_soon)
        else -> stringResource(R.string.home_ok)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // ── Header row ──────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = car.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryYellow
                    )
                    Text(
                        text = "${car.model} · ${car.year}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Status badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = statusText,
                        color = statusColor,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(Modifier.height(12.dp))

            // ── Stats row ──────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = stringResource(R.string.home_mileage),
                    value = "${car.currentMileage} km"
                )
                StatItem(
                    label = stringResource(R.string.home_last_service),
                    value = latestService?.let {
                        SimpleDateFormat("dd MMM yy", Locale.getDefault())
                            .format(Date(it.serviceDate))
                    } ?: "—"
                )
                StatItem(
                    label = stringResource(R.string.home_next_service),
                    value = latestService?.let {
                        SimpleDateFormat("dd MMM yy", Locale.getDefault())
                            .format(Date(it.nextServiceDate))
                    } ?: "—",
                    valueColor = statusColor
                )
            }

            Spacer(Modifier.height(12.dp))

            // ── Add service button ──────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onAddService,
                    modifier = Modifier.weight(1f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryYellow),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryYellow)
                ) {
                    Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.nav_add_service))
                }

                var showConfirm by remember { mutableStateOf(false) }

                OutlinedButton(
                    onClick = { showConfirm = true },
                    modifier = Modifier.weight(1f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StatusOverdue),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusOverdue)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.btn_delete))
                }

                if (showConfirm) {
                    AlertDialog(
                        onDismissRequest = { showConfirm = false },
                        title = { Text(stringResource(R.string.delete_car_title), color = PrimaryYellow) },
                        text = { Text(stringResource(R.string.delete_car_confirm)) },
                        confirmButton = {
                            TextButton(onClick = {
                                showConfirm = false
                                onDeleteCar()
                            }) {
                                Text(stringResource(R.string.btn_delete), color = StatusOverdue)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showConfirm = false }) {
                                Text(stringResource(R.string.btn_cancel))
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.surface
                    )
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
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = valueColor
        )
    }
}