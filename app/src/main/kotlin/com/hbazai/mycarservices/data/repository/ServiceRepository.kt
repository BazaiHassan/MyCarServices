package com.hbazai.mycarservices.data.repository

import com.hbazai.mycarservices.data.local.dao.ServiceRecordDao
import com.hbazai.mycarservices.data.local.entity.ServiceRecordEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServiceRepository @Inject constructor(
    private val dao: ServiceRecordDao
) {
    fun getServicesByCarId(carId: Int): Flow<List<ServiceRecordEntity>> =
        dao.getServicesByCarId(carId)

    fun getAllServices(): Flow<List<ServiceRecordEntity>> =
        dao.getAllServices()

    suspend fun getDueServices(thresholdDate: Long, currentMileage: Int) =
        dao.getDueServices(thresholdDate, currentMileage)

    suspend fun getLatestServiceForCar(carId: Int) =
        dao.getLatestServiceForCar(carId)

    suspend fun insertService(service: ServiceRecordEntity): Long =
        dao.insertService(service)

    suspend fun updateService(service: ServiceRecordEntity) =
        dao.updateService(service)

    suspend fun deleteService(service: ServiceRecordEntity) =
        dao.deleteService(service)

    suspend fun markAsNotified(serviceId: Int) =
        dao.markAsNotified(serviceId)
}