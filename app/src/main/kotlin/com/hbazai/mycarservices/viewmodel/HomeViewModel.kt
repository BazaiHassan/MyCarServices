package com.hbazai.mycarservices.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hbazai.mycarservices.data.local.entity.CarEntity
import com.hbazai.mycarservices.data.local.entity.ServiceRecordEntity
import com.hbazai.mycarservices.data.repository.CarRepository
import com.hbazai.mycarservices.data.repository.ServiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val carRepository: CarRepository,
    private val serviceRepository: ServiceRepository
) : ViewModel() {

    val cars: StateFlow<List<CarEntity>> = carRepository.getAllCars()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _latestServices = MutableStateFlow<Map<Int, ServiceRecordEntity>>(emptyMap())
    val latestServices: StateFlow<Map<Int, ServiceRecordEntity>> = _latestServices

    init {
        viewModelScope.launch {
            cars.collect { carList ->
                val map = mutableMapOf<Int, ServiceRecordEntity>()
                carList.forEach { car ->
                    serviceRepository.getLatestServiceForCar(car.id)?.let {
                        map[car.id] = it
                    }
                }
                _latestServices.value = map
            }
        }
    }

    fun addCar(
        name: String,
        model: String,
        year: Int,
        licensePlate: String,
        mileage: Int,
        imagePath: String = ""
    ) {
        viewModelScope.launch {
            carRepository.insertCar(
                CarEntity(
                    name           = name,
                    model          = model,
                    year           = year,
                    licensePlate   = licensePlate,
                    currentMileage = mileage,
                    imagePath      = imagePath
                )
            )
        }
    }

    fun deleteCar(car: CarEntity) {
        viewModelScope.launch { carRepository.deleteCar(car) }
    }
}