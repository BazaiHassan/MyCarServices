package com.hbazai.mycarservices.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hbazai.mycarservices.data.local.entity.ServiceRecordEntity
import com.hbazai.mycarservices.data.repository.ServiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class ServiceViewModel @Inject constructor(
    private val serviceRepository: ServiceRepository
) : ViewModel() {

    fun addService(
        carId: Int,
        serviceType: String,
        mileageAtService: Int,
        nextServiceMileage: Int,
        cost: Double,
        notes: String,
        cause: String = "",
        imagePath: String = "",
        providerName: String = "",
        providerPhone: String = ""
    ) {
        viewModelScope.launch {
            val now             = System.currentTimeMillis()
            val nextServiceDate = now + TimeUnit.DAYS.toMillis(90)
            serviceRepository.insertService(
                ServiceRecordEntity(
                    carId              = carId,
                    serviceType        = serviceType,
                    serviceDate        = now,
                    mileageAtService   = mileageAtService,
                    nextServiceMileage = nextServiceMileage,
                    nextServiceDate    = nextServiceDate,
                    cost               = cost,
                    notes              = notes,
                    cause              = cause,
                    imagePath          = imagePath,
                    providerName       = providerName,
                    providerPhone      = providerPhone
                )
            )
        }
    }

    fun deleteService(service: ServiceRecordEntity) {
        viewModelScope.launch { serviceRepository.deleteService(service) }
    }
}