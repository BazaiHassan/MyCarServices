package com.hbazai.mycarservices.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hbazai.mycarservices.data.local.entity.CarEntity
import com.hbazai.mycarservices.data.local.entity.ServiceRecordEntity
import com.hbazai.mycarservices.data.repository.CarRepository
import com.hbazai.mycarservices.data.repository.ServiceRepository
import com.hbazai.mycarservices.util.PdfExporter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val serviceRepository: ServiceRepository,
    private val carRepository: CarRepository
) : ViewModel() {

    private val _activeFilter  = MutableStateFlow("All")
    val activeFilter: StateFlow<String> = _activeFilter

    private val _selectedCarId = MutableStateFlow<Int?>(null)
    val selectedCarId: StateFlow<Int?> = _selectedCarId

    val cars: StateFlow<List<CarEntity>> = carRepository.getAllCars()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _allServices: StateFlow<List<ServiceRecordEntity>> =
        serviceRepository.getAllServices()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Services for selected car only — used to build dynamic filter chips
    val allServicesForCar: StateFlow<List<ServiceRecordEntity>> =
        combine(_allServices, _selectedCarId) { services, carId ->
            if (carId != null) services.filter { it.carId == carId } else services
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredServices: StateFlow<List<ServiceRecordEntity>> =
        combine(_allServices, _activeFilter, _selectedCarId) { services, filter, carId ->
            var result = if (carId != null) services.filter { it.carId == carId } else services
            result = if (filter == "All") result else result.filter { it.serviceType == filter }
            result
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalCost: StateFlow<Double> = filteredServices
        .map { it.sumOf { s -> s.cost } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    private val _pdfExportResult = MutableStateFlow<File?>(null)
    val pdfExportResult: StateFlow<File?> = _pdfExportResult

    fun setFilter(filter: String) { _activeFilter.value = filter }
    fun selectCar(carId: Int?)   { _selectedCarId.value = carId; _activeFilter.value = "All" }

    fun exportPdf(context: Context, car: CarEntity) {
        viewModelScope.launch {
            _pdfExportResult.value = PdfExporter.exportCarHistory(context, car, filteredServices.value)
        }
    }

    fun deleteService(service: ServiceRecordEntity) {
        viewModelScope.launch { serviceRepository.deleteService(service) }
    }

    fun clearPdfResult() { _pdfExportResult.value = null }
}