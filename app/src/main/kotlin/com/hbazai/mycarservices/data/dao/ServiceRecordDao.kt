package com.hbazai.mycarservices.data.local.dao

import androidx.room.*
import com.hbazai.mycarservices.data.local.entity.ServiceRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ServiceRecordDao {

    @Query("SELECT * FROM service_records WHERE carId = :carId ORDER BY serviceDate DESC")
    fun getServicesByCarId(carId: Int): Flow<List<ServiceRecordEntity>>

    @Query("SELECT * FROM service_records ORDER BY serviceDate DESC")
    fun getAllServices(): Flow<List<ServiceRecordEntity>>

    @Query("""
        SELECT * FROM service_records 
        WHERE (nextServiceDate <= :thresholdDate OR nextServiceMileage <= :currentMileage)
        AND isNotified = 0
    """)
    suspend fun getDueServices(
        thresholdDate: Long,
        currentMileage: Int
    ): List<ServiceRecordEntity>

    @Query("SELECT * FROM service_records WHERE carId = :carId ORDER BY serviceDate DESC LIMIT 1")
    suspend fun getLatestServiceForCar(carId: Int): ServiceRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertService(service: ServiceRecordEntity): Long

    @Update
    suspend fun updateService(service: ServiceRecordEntity)

    @Delete
    suspend fun deleteService(service: ServiceRecordEntity)

    @Query("UPDATE service_records SET isNotified = 1 WHERE id = :serviceId")
    suspend fun markAsNotified(serviceId: Int)
}