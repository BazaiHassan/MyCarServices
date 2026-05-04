package com.hbazai.mycarservices.data.repository

import com.hbazai.mycarservices.data.local.dao.CarDao
import com.hbazai.mycarservices.data.local.entity.CarEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CarRepository @Inject constructor(
    private val carDao: CarDao
) {
    fun getAllCars(): Flow<List<CarEntity>> =
        carDao.getAllCars()

    fun getCarById(carId: Int): Flow<CarEntity?> =
        carDao.getCarById(carId)

    suspend fun insertCar(car: CarEntity): Long =
        carDao.insertCar(car)

    suspend fun updateCar(car: CarEntity) =
        carDao.updateCar(car)

    suspend fun deleteCar(car: CarEntity) =
        carDao.deleteCar(car)
}