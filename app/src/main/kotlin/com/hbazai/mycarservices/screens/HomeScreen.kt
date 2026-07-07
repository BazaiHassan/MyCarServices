package com.hbazai.mycarservices.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hbazai.mycarservices.R
import com.hbazai.mycarservices.data.local.entity.CarEntity
import com.hbazai.mycarservices.data.local.entity.ServiceRecordEntity
import com.hbazai.mycarservices.ui.theme.*
import com.hbazai.mycarservices.util.AppPreferences
import com.hbazai.mycarservices.util.DateFormatter
import com.hbazai.mycarservices.util.ltr
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
    onPredict: (Int) -> Unit,
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
                        color      = MaterialTheme.colorScheme.primary
                    )
                },
                actions = {
                    IconButton(onClick = onViewReports) {
                        Icon(Icons.Default.History, stringResource(R.string.nav_reports), tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Default.Settings, stringResource(R.string.nav_settings), tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick        = onAddCar,
                containerColor = PrimaryYellow,
                contentColor   = OnPrimary,
                icon           = { Icon(Icons.Default.Add, null) },
                text           = { Text(stringResource(R.string.home_add_car), fontWeight = FontWeight.Bold) }
            )
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
                        onDeleteCar   = { viewModel.deleteCar(car) },
                        onPredict     = { onPredict(car.id) }
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier            = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(112.dp)
                    .background(PrimaryYellow.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.DirectionsCar, null,
                    tint     = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(64.dp)
                )
            }
            Text(
                stringResource(R.string.home_no_cars),
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onBackground
            )
            Text(
                stringResource(R.string.home_no_cars_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = onAddCar,
                shape   = RoundedCornerShape(16.dp),
                colors  = ButtonDefaults.buttonColors(
                    containerColor = PrimaryYellow, contentColor = OnPrimary
                ),
                modifier = Modifier.height(52.dp)
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.home_add_car), fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
    onDeleteCar: () -> Unit,
    onPredict: () -> Unit
) {
    val context   = LocalContext.current
    val now       = System.currentTimeMillis()
    val sevenDays = 7 * 24 * 60 * 60 * 1000L
    val dueSoonKm = 500

    val isOverdue = latestService != null &&
            (latestService.nextServiceDate < now ||
             car.currentMileage >= latestService.nextServiceMileage)
    val isDueSoon = !isOverdue && latestService != null &&
            (latestService.nextServiceDate in now..(now + sevenDays) ||
             latestService.nextServiceMileage - car.currentMileage in 0..dueSoonKm)

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
                Text(stringResource(R.string.delete_car_title), color = MaterialTheme.colorScheme.primary)
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
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // ── Header ────────────────────────────────
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(PrimaryYellow, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.DirectionsCar, null,
                        tint     = OnPrimary,
                        modifier = Modifier.size(30.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        car.name,
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onSurface
                    )
                    val subtitle = listOf(car.model, car.year.takeIf { it > 0 }?.toString().orEmpty())
                        .filter { it.isNotBlank() }
                        .joinToString(" · ")
                    if (subtitle.isNotBlank()) {
                        Text(
                            subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (car.licensePlate.isNotBlank()) {
                        Text(
                            car.licensePlate,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                StatusPill(text = statusText, color = statusColor)
            }

            Spacer(Modifier.height(14.dp))

            // ── Stats ─────────────────────────────────
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        RoundedCornerShape(14.dp)
                    )
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    icon  = Icons.Default.Speed,
                    label = stringResource(R.string.home_mileage),
                    value = ltr("${car.currentMileage} $distanceUnit")
                )
                StatItem(
                    icon  = Icons.Default.History,
                    label = stringResource(R.string.home_last_service),
                    value = latestService?.let {
                        DateFormatter.formatShort(context, it.serviceDate)
                    } ?: "—"
                )
                StatItem(
                    icon       = Icons.Default.Event,
                    label      = stringResource(R.string.home_next_service),
                    value      = latestService?.let {
                        ltr("${it.nextServiceMileage} $distanceUnit")
                    } ?: "—",
                    valueColor = statusColor
                )
            }

            Spacer(Modifier.height(12.dp))

            // ── Actions ───────────────────────────────
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick  = onAddService,
                    modifier = Modifier.weight(1f).height(46.dp),
                    shape    = RoundedCornerShape(14.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = PrimaryYellow,
                        contentColor   = OnPrimary
                    )
                ) {
                    Icon(Icons.Default.Build, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.home_log_service), fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = onPredict) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        stringResource(R.string.predict_title),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onEditCar) {
                    Icon(Icons.Default.Edit, stringResource(R.string.btn_edit), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(Icons.Default.Delete, stringResource(R.string.btn_delete), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun StatusPill(text: String, color: Color) {
    Text(
        text       = text,
        color      = color,
        fontSize   = 11.sp,
        fontWeight = FontWeight.Bold,
        modifier   = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

@Composable
fun StatItem(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.height(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(2.dp))
        Text(
            value,
            style      = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color      = valueColor
        )
    }
}
