package com.hbazai.mycarservices.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hbazai.mycarservices.R
import com.hbazai.mycarservices.data.local.entity.ServiceRecordEntity
import com.hbazai.mycarservices.ui.theme.OnPrimary
import com.hbazai.mycarservices.ui.theme.PrimaryYellow
import com.hbazai.mycarservices.viewmodel.ReportViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.unit.dp
import com.hbazai.mycarservices.ui.theme.StatusOverdue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    onBack: () -> Unit,
    viewModel: ReportViewModel = hiltViewModel()
) {
    val context       = LocalContext.current
    val services      by viewModel.filteredServices.collectAsStateWithLifecycle()
    val totalCost     by viewModel.totalCost.collectAsStateWithLifecycle()
    val filter        by viewModel.activeFilter.collectAsStateWithLifecycle()
    val cars          by viewModel.cars.collectAsStateWithLifecycle()
    val selectedCarId by viewModel.selectedCarId.collectAsStateWithLifecycle()
    val pdfResult     by viewModel.pdfExportResult.collectAsStateWithLifecycle()

    // Show toast when PDF is done
    LaunchedEffect(pdfResult) {
        pdfResult?.let {
            Toast.makeText(
                context,
                context.getString(R.string.export_pdf_success),
                Toast.LENGTH_LONG
            ).show()
            viewModel.clearPdfResult()
        }
    }

    val filters = listOf(
        stringResource(R.string.reports_filter_all),
        stringResource(R.string.reports_filter_oil),
        stringResource(R.string.reports_filter_tires),
        stringResource(R.string.reports_filter_brakes)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.reports_title),
                        fontWeight = FontWeight.Bold,
                        color      = PrimaryYellow
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = PrimaryYellow)
                    }
                },
                actions = {
                    // PDF export — only show when a car is selected
                    if (selectedCarId != null) {
                        IconButton(onClick = {
                            val car = cars.find { it.id == selectedCarId }
                            if (car != null) viewModel.exportPdf(context, car)
                        }) {
                            Icon(
                                Icons.Default.PictureAsPdf,
                                contentDescription = stringResource(R.string.export_pdf),
                                tint = PrimaryYellow
                            )
                        }
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
        ) {
            // ── Car selector chips ────────────────────
            if (cars.isNotEmpty()) {
                LazyRow(
                    contentPadding        = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedCarId == null,
                            onClick  = { viewModel.selectCar(null) },
                            label    = { Text(stringResource(R.string.reports_filter_all)) },
                            colors   = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryYellow,
                                selectedLabelColor     = OnPrimary
                            )
                        )
                    }
                    items(cars) { car ->
                        FilterChip(
                            selected = selectedCarId == car.id,
                            onClick  = { viewModel.selectCar(car.id) },
                            label    = { Text(car.name) },
                            colors   = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryYellow,
                                selectedLabelColor     = OnPrimary
                            )
                        )
                    }
                }
            }

            // ── Summary cards ─────────────────────────
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryCard(
                    label    = stringResource(R.string.reports_total_services),
                    value    = services.size.toString(),
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    label    = stringResource(R.string.reports_total_cost),
                    value    = "€ ${"%.2f".format(totalCost)}",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(8.dp))

            // ── Filter chips ──────────────────────────
            LazyRow(
                contentPadding        = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filters) { f ->
                    FilterChip(
                        selected = filter == f,
                        onClick  = { viewModel.setFilter(f) },
                        label    = { Text(f) },
                        colors   = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryYellow,
                            selectedLabelColor     = OnPrimary
                        )
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            if (services.isEmpty()) {
                Box(
                    modifier         = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        stringResource(R.string.reports_no_records),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding      = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items = services, key = { it.id }) { service ->
                        ServiceHistoryCard(service = service, onDeleteService = { viewModel.deleteService(service) })
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape    = RoundedCornerShape(12.dp),
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier            = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text       = value,
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color      = PrimaryYellow
            )
            Text(
                text  = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ServiceHistoryCard(
    service: ServiceRecordEntity,
    onDeleteService: () -> Unit       // ADD
) {
    var showConfirm by remember { mutableStateOf(false) }
    val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        .format(Date(service.serviceDate))

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text(stringResource(R.string.delete_service_title), color = PrimaryYellow) },
            text  = { Text(stringResource(R.string.delete_service_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    showConfirm = false
                    onDeleteService()
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

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(12.dp),
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text       = service.serviceType,
                    fontWeight = FontWeight.Bold,
                    color      = PrimaryYellow
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text  = "€ ${"%.2f".format(service.cost)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick  = { showConfirm = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(R.string.btn_delete),
                            tint     = StatusOverdue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(4.dp))
            Text(
                text  = dateStr,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text  = "${service.mileageAtService} km",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (service.cause.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text  = "⚠ ${service.cause}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            if (service.providerName.isNotBlank()) {
                Text(
                    text  = "🔧 ${service.providerName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (service.notes.isNotBlank()) {
                Text(
                    text  = service.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}