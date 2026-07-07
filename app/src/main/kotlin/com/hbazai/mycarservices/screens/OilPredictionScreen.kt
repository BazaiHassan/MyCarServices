package com.hbazai.mycarservices.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.OilBarrel
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hbazai.mycarservices.R
import com.hbazai.mycarservices.ml.OilPrediction
import com.hbazai.mycarservices.ui.theme.*
import com.hbazai.mycarservices.util.AppPreferences
import com.hbazai.mycarservices.util.DateFormatter
import com.hbazai.mycarservices.util.ltr
import com.hbazai.mycarservices.viewmodel.PredictionUiState
import com.hbazai.mycarservices.viewmodel.PredictionViewModel
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OilPredictionScreen(
    onBack: () -> Unit,
    viewModel: PredictionViewModel = hiltViewModel()
) {
    val car     by viewModel.car.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.predict_title),
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.primary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            car?.let {
                Text(
                    it.name,
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            when (val state = uiState) {
                is PredictionUiState.Idle          -> PredictIntroCard(onPredict = viewModel::predict)
                is PredictionUiState.Training      -> TrainingCard()
                is PredictionUiState.NotEnoughData -> NotEnoughDataCard(state.recordCount)
                is PredictionUiState.Ready         -> PredictionResult(
                    prediction     = state.prediction,
                    currentMileage = car?.currentMileage ?: 0
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  Idle / Training / Empty states
// ─────────────────────────────────────────────────────────────────

@Composable
private fun PredictIntroCard(onPredict: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(20.dp),
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier            = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .background(PrimaryYellow.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.AutoAwesome, null,
                    tint     = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(44.dp)
                )
            }
            Text(
                stringResource(R.string.predict_intro_title),
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onSurface
            )
            Text(
                stringResource(R.string.predict_intro_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick  = onPredict,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(16.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = PrimaryYellow,
                    contentColor   = OnPrimary
                )
            ) {
                Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.predict_button), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun TrainingCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(20.dp),
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier            = Modifier.fillMaxWidth().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            CircularProgressIndicator(
                color       = MaterialTheme.colorScheme.primary,
                strokeWidth = 4.dp,
                modifier    = Modifier.size(48.dp)
            )
            Text(
                stringResource(R.string.predict_training),
                style      = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun NotEnoughDataCard(recordCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(20.dp),
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier            = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.OilBarrel, null,
                tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(44.dp)
            )
            Text(
                stringResource(R.string.predict_not_enough, 3, recordCount),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  Result: prediction card + chart
// ─────────────────────────────────────────────────────────────────

@Composable
private fun PredictionResult(prediction: OilPrediction, currentMileage: Int) {
    val context      = LocalContext.current
    val distanceUnit = AppPreferences.getDistanceUnit(context)
    val now          = System.currentTimeMillis()

    val kmLeft   = prediction.predictedMileage - currentMileage
    val daysLeft = ((prediction.predictedDateMillis - now) / (24 * 60 * 60 * 1000L)).toInt()
    val overdue  = kmLeft <= 0 || daysLeft < 0

    AnimatedVisibility(
        visible = true,
        enter   = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 4 }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

            // ── AI badge ──────────────────────────────
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier              = Modifier
                    .background(PrimaryYellow.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    Icons.Default.AutoAwesome, null,
                    tint     = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    stringResource(R.string.predict_ai_badge),
                    style      = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.primary
                )
            }

            // ── Headline card ─────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(20.dp),
                colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        stringResource(R.string.predict_next_oil_change),
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        PredictionStat(
                            icon  = Icons.Default.Speed,
                            label = stringResource(R.string.predict_at_mileage),
                            value = ltr("${formatKm(prediction.predictedMileage)} $distanceUnit")
                        )
                        PredictionStat(
                            icon  = Icons.Default.Event,
                            label = stringResource(R.string.predict_on_date),
                            value = DateFormatter.formatShort(context, prediction.predictedDateMillis)
                        )
                    }

                    if (overdue) {
                        Text(
                            stringResource(R.string.predict_overdue),
                            style      = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color      = StatusOverdue,
                            modifier   = Modifier
                                .fillMaxWidth()
                                .background(StatusOverdue.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        )
                    } else {
                        Text(
                            stringResource(
                                R.string.predict_remaining,
                                ltr("${formatKm(kmLeft)} $distanceUnit"),
                                daysLeft
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // ── Confidence ────────────────────
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                stringResource(R.string.predict_confidence),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                ltr("${(prediction.confidence * 100).roundToInt()}٪"),
                                style      = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color      = MaterialTheme.colorScheme.primary
                            )
                        }
                        val animated by animateFloatAsState(
                            targetValue   = prediction.confidence,
                            animationSpec = tween(800),
                            label         = "confidence"
                        )
                        LinearProgressIndicator(
                            progress   = { animated },
                            modifier   = Modifier.fillMaxWidth().height(6.dp),
                            color      = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            strokeCap  = StrokeCap.Round
                        )
                    }
                }
            }

            // ── Chart card ────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(20.dp),
                colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        stringResource(R.string.predict_chart_title),
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onSurface
                    )
                    OilMileageChart(
                        prediction = prediction,
                        modifier   = Modifier.fillMaxWidth().height(220.dp)
                    )
                    ChartLegend()
                }
            }

            Text(
                stringResource(R.string.predict_disclaimer, prediction.trainedOnIntervals),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PredictionStat(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(2.dp))
        Text(
            value,
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun ChartLegend() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(Modifier.size(10.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
            Text(
                stringResource(R.string.predict_legend_history),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Canvas(Modifier.width(18.dp).height(10.dp)) {
                drawLine(
                    color       = ChartPredictColor,
                    start       = Offset(0f, size.height / 2),
                    end         = Offset(size.width, size.height / 2),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect  = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                Icon(
                    Icons.Default.AutoAwesome, null,
                    tint     = ChartPredictColor,
                    modifier = Modifier.size(11.dp)
                )
                Text(
                    stringResource(R.string.predict_legend_predicted),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Prediction accent — readable on both dark and light surfaces.
private val ChartPredictColor = Color(0xFF26A69A)

// ─────────────────────────────────────────────────────────────────
//  Chart
// ─────────────────────────────────────────────────────────────────

@Composable
private fun OilMileageChart(prediction: OilPrediction, modifier: Modifier = Modifier) {
    val context      = LocalContext.current
    val textMeasurer = rememberTextMeasurer()

    val lineColor  = MaterialTheme.colorScheme.primary
    val gridColor  = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.18f)
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val surface    = MaterialTheme.colorScheme.surface
    val labelStyle = TextStyle(fontSize = 10.sp, color = labelColor)

    val history   = prediction.history
    val allDates  = history.map { it.dateMillis } + prediction.predictedDateMillis
    val allKm     = history.map { it.mileage } + prediction.predictedMileage
    val xMin      = allDates.min()
    val xMax      = allDates.max()
    val yMinRaw   = allKm.min()
    val yMaxRaw   = allKm.max()
    val yPad      = ((yMaxRaw - yMinRaw) * 0.12f).coerceAtLeast(1f)
    val yMin      = yMinRaw - yPad
    val yMax      = yMaxRaw + yPad

    val startDateLabel   = DateFormatter.formatShort(context, xMin)
    val endDateLabel     = DateFormatter.formatShort(context, prediction.predictedDateMillis)
    val predictedKmLabel = formatKm(prediction.predictedMileage)

    // Charts read left-to-right regardless of app language.
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Canvas(modifier = modifier) {
            val padLeft   = 52.dp.toPx()
            val padRight  = 18.dp.toPx()
            val padTop    = 14.dp.toPx()
            val padBottom = 26.dp.toPx()
            val w = size.width - padLeft - padRight
            val h = size.height - padTop - padBottom

            fun x(date: Long): Float =
                padLeft + if (xMax == xMin) w / 2 else (date - xMin).toFloat() / (xMax - xMin).toFloat() * w
            fun y(km: Float): Float =
                padTop + (1f - (km - yMin) / (yMax - yMin)) * h

            // ── Grid + y labels (recessive) ───────────
            val gridSteps = 4
            for (i in 0..gridSteps) {
                val kmValue = yMin + (yMax - yMin) * i / gridSteps
                val yPos    = y(kmValue)
                drawLine(
                    color       = gridColor,
                    start       = Offset(padLeft, yPos),
                    end         = Offset(padLeft + w, yPos),
                    strokeWidth = 1f
                )
                val text = textMeasurer.measure(formatKm(kmValue.roundToInt()), labelStyle)
                drawText(
                    textLayoutResult = text,
                    topLeft = Offset(padLeft - text.size.width - 8.dp.toPx(), yPos - text.size.height / 2)
                )
            }

            // ── X labels: first record + predicted ────
            val startText = textMeasurer.measure(startDateLabel, labelStyle)
            drawText(startText, topLeft = Offset(padLeft, padTop + h + 8.dp.toPx()))
            val endText = textMeasurer.measure(endDateLabel, labelStyle)
            drawText(
                endText,
                topLeft = Offset(padLeft + w - endText.size.width, padTop + h + 8.dp.toPx())
            )

            val historyOffsets = history.map { Offset(x(it.dateMillis), y(it.mileage.toFloat())) }
            val predictedPoint = Offset(
                x(prediction.predictedDateMillis),
                y(prediction.predictedMileage.toFloat())
            )

            // ── Area fill under the history line ──────
            val areaPath = Path().apply {
                moveTo(historyOffsets.first().x, padTop + h)
                historyOffsets.forEach { lineTo(it.x, it.y) }
                lineTo(historyOffsets.last().x, padTop + h)
                close()
            }
            drawPath(
                path = areaPath,
                brush = Brush.verticalGradient(
                    colors = listOf(lineColor.copy(alpha = 0.20f), lineColor.copy(alpha = 0.02f)),
                    startY = padTop,
                    endY   = padTop + h
                )
            )

            // ── History line ──────────────────────────
            val linePath = Path().apply {
                moveTo(historyOffsets.first().x, historyOffsets.first().y)
                historyOffsets.drop(1).forEach { lineTo(it.x, it.y) }
            }
            drawPath(
                path  = linePath,
                color = lineColor,
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
            )

            // ── Dashed prediction segment ─────────────
            drawLine(
                color       = ChartPredictColor,
                start       = historyOffsets.last(),
                end         = predictedPoint,
                strokeWidth = 2.5.dp.toPx(),
                cap         = StrokeCap.Round,
                pathEffect  = PathEffect.dashPathEffect(floatArrayOf(12f, 10f))
            )

            // ── History markers (surface ring for overlap) ─
            historyOffsets.forEach { p ->
                drawCircle(color = surface,   radius = 6.dp.toPx(), center = p)
                drawCircle(color = lineColor, radius = 4.dp.toPx(), center = p)
            }

            // ── Predicted marker: glow + ring ─────────
            drawCircle(
                color  = ChartPredictColor.copy(alpha = 0.18f),
                radius = 12.dp.toPx(),
                center = predictedPoint
            )
            drawCircle(color = surface, radius = 7.dp.toPx(), center = predictedPoint)
            drawCircle(
                color  = ChartPredictColor,
                radius = 7.dp.toPx(),
                center = predictedPoint,
                style  = Stroke(width = 2.5.dp.toPx())
            )

            // ── Direct label on the predicted point ───
            val predText = textMeasurer.measure(
                predictedKmLabel,
                labelStyle.copy(color = ChartPredictColor, fontWeight = FontWeight.Bold)
            )
            val labelX = (predictedPoint.x - predText.size.width - 10.dp.toPx())
                .coerceAtLeast(padLeft)
            val labelY = (predictedPoint.y - predText.size.height - 10.dp.toPx())
                .coerceAtLeast(padTop)
            drawText(predText, topLeft = Offset(labelX, labelY))
        }
    }
}

private fun formatKm(value: Int): String = String.format(Locale.US, "%,d", value)
