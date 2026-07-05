package com.hbazai.mycarservices.ui

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CarRepair
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.OilBarrel
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TireRepair
import androidx.compose.ui.graphics.vector.ImageVector
import com.hbazai.mycarservices.R

/**
 * Canonical list of service types with their icons.
 * Records store the localized label, so [iconFor] matches stored labels
 * (from any past language) back to an icon by keyword.
 */
data class ServiceType(
    val id: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector,
    private val keywords: List<String>
) {
    fun matches(label: String): Boolean =
        keywords.any { label.contains(it, ignoreCase = true) }
}

object ServiceCatalog {

    val types = listOf(
        ServiceType(
            id = "oil_change", labelRes = R.string.service_oil_change,
            icon = Icons.Filled.OilBarrel,
            keywords = listOf("Oil Change", "Ölwechsel", "تعویض روغن")
        ),
        ServiceType(
            id = "gearbox_oil", labelRes = R.string.service_gearbox_oil,
            icon = Icons.Filled.Settings,
            keywords = listOf("Gearbox", "Getriebe", "گیربکس")
        ),
        ServiceType(
            id = "timing_belt", labelRes = R.string.service_timing_belt,
            icon = Icons.Filled.Loop,
            keywords = listOf("Timing", "Zahnriemen", "تسمه")
        ),
        ServiceType(
            id = "tire_rotation", labelRes = R.string.service_tire_rotation,
            icon = Icons.Filled.TireRepair,
            keywords = listOf("Tire", "Reifen", "لاستیک")
        ),
        ServiceType(
            id = "brake_check", labelRes = R.string.service_brake_check,
            icon = Icons.Filled.Album,
            keywords = listOf("Brake", "Brems", "ترمز")
        ),
        ServiceType(
            id = "air_filter", labelRes = R.string.service_air_filter,
            icon = Icons.Filled.Air,
            keywords = listOf("Air Filter", "Luftfilter", "فیلتر")
        ),
        ServiceType(
            id = "car_repair", labelRes = R.string.service_car_repair,
            icon = Icons.Filled.CarRepair,
            keywords = listOf("Repair", "Reparatur", "تعمیر")
        ),
        ServiceType(
            id = "custom", labelRes = R.string.service_custom,
            icon = Icons.Filled.Handyman,
            keywords = listOf("Custom", "Benutzerdefiniert", "دلخواه")
        )
    )

    /** Icon for a stored (localized) service label; falls back to a wrench. */
    fun iconFor(label: String): ImageVector =
        types.firstOrNull { it.matches(label) }?.icon ?: Icons.Filled.Build

    fun isRepair(label: String): Boolean =
        types.first { it.id == "car_repair" }.matches(label)
}
