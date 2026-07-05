package com.hbazai.mycarservices.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hbazai.mycarservices.data.local.entity.CarEntity
import com.hbazai.mycarservices.data.local.entity.ServiceRecordEntity
import com.hbazai.mycarservices.data.repository.CarRepository
import com.hbazai.mycarservices.data.repository.ServiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ServiceViewModel @Inject constructor(
    private val serviceRepository: ServiceRepository,
    private val carRepository: CarRepository
) : ViewModel() {

    val allServices: StateFlow<List<ServiceRecordEntity>> =
        serviceRepository.getAllServices()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val carId = MutableStateFlow(-1)

    /** Car the service is being logged for — used to prefill mileage. */
    val car: StateFlow<CarEntity?> = carId
        .flatMapLatest { id -> carRepository.getCarById(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun loadCar(id: Int) {
        carId.value = id
    }

    /**
     * Saves one record per selected service in a single visit and bumps the
     * car's current mileage when the service mileage is ahead of it.
     */
    fun addServices(
        carId: Int,
        services: Map<String, Double>,
        mileageAtService: Int,
        nextServiceMileage: Int,
        notes: String = "",
        cause: String = "",
        providerName: String = "",
        providerPhone: String = ""
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            services.forEach { (serviceType, cost) ->
                serviceRepository.insertService(
                    ServiceRecordEntity(
                        carId = carId, serviceType = serviceType,
                        serviceDate = now, mileageAtService = mileageAtService,
                        nextServiceMileage = nextServiceMileage,
                        nextServiceDate = now + TimeUnit.DAYS.toMillis(90),
                        cost = cost, notes = notes, cause = cause,
                        providerName = providerName, providerPhone = providerPhone
                    )
                )
            }
            car.value?.let { current ->
                if (current.id == carId && mileageAtService > current.currentMileage) {
                    carRepository.updateCar(current.copy(currentMileage = mileageAtService))
                }
            }
        }
    }

    fun updateService(service: ServiceRecordEntity) {
        viewModelScope.launch { serviceRepository.updateService(service) }
    }

    fun deleteService(service: ServiceRecordEntity) {
        viewModelScope.launch { serviceRepository.deleteService(service) }
    }
}
