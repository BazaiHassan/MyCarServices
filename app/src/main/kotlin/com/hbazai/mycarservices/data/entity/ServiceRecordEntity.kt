package com.hbazai.mycarservices.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "service_records",
    foreignKeys = [
        ForeignKey(
            entity = CarEntity::class,
            parentColumns = ["id"],
            childColumns = ["carId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("carId")]
)
data class ServiceRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val carId: Int,
    val serviceType: String,
    val serviceDate: Long,
    val mileageAtService: Int,
    val nextServiceMileage: Int,
    val nextServiceDate: Long,
    val cost: Double,
    val notes: String,
    val cause: String = "",
    val imagePath: String = "",
    val providerName: String = "",
    val providerPhone: String = "",
    val isNotified: Boolean = false
)