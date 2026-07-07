package com.hbazai.mycarservices.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hbazai.mycarservices.data.local.entity.CarEntity
import com.hbazai.mycarservices.data.repository.CarRepository
import com.hbazai.mycarservices.data.repository.ServiceRepository
import com.hbazai.mycarservices.ml.OilChangePoint
import com.hbazai.mycarservices.ml.OilChangePredictor
import com.hbazai.mycarservices.ml.OilPrediction
import com.hbazai.mycarservices.ui.ServiceCatalog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

sealed interface PredictionUiState {
    data object Idle : PredictionUiState
    data object Training : PredictionUiState
    data class NotEnoughData(val recordCount: Int) : PredictionUiState
    data class Ready(val prediction: OilPrediction) : PredictionUiState
}

@HiltViewModel
class PredictionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val serviceRepository: ServiceRepository,
    carRepository: CarRepository
) : ViewModel() {

    private val carId: Int = checkNotNull(savedStateHandle["carId"])

    val car: StateFlow<CarEntity?> = carRepository.getCarById(carId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _uiState = MutableStateFlow<PredictionUiState>(PredictionUiState.Idle)
    val uiState: StateFlow<PredictionUiState> = _uiState

    /** Trains the model on-device and predicts — only on explicit user request. */
    fun predict() {
        if (_uiState.value == PredictionUiState.Training) return
        viewModelScope.launch {
            _uiState.value = PredictionUiState.Training

            val oilType = ServiceCatalog.types.first { it.id == "oil_change" }
            val oilChanges = serviceRepository.getServicesByCarId(carId).first()
                .filter { oilType.matches(it.serviceType) }
                .map { OilChangePoint(it.serviceDate, it.mileageAtService) }

            // Brief pause so the on-device training state is visible to the user.
            delay(900)

            val prediction = withContext(Dispatchers.Default) {
                OilChangePredictor.train(oilChanges)
            }

            _uiState.value = prediction
                ?.let { PredictionUiState.Ready(it) }
                ?: PredictionUiState.NotEnoughData(oilChanges.size)
        }
    }
}
