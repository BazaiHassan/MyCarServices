package com.hbazai.mycarservices.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cars")
data class CarEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val model: String,
    val year: Int,
    val licensePlate: String,
    val currentMileage: Int,
    val imagePath: String = ""
)