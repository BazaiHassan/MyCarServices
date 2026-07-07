package com.hbazai.mycarservices.ml

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlin.math.sqrt

/** One historical oil change: when it happened and the odometer reading. */
data class OilChangePoint(
    val dateMillis: Long,
    val mileage: Int
)

/** Result of on-device training + inference. */
data class OilPrediction(
    val history: List<OilChangePoint>,
    val predictedMileage: Int,
    val predictedDateMillis: Long,
    val dailyKmRate: Double,
    /** 0..1 — how consistent the past intervals are. */
    val confidence: Float,
    /** Number of intervals the model was trained on. */
    val trainedOnIntervals: Int
)

/**
 * Tiny on-device ML model for predicting the next oil change.
 *
 * Two learners are trained on the phone, on the user's own records:
 *  1. A distance-weighted k-nearest-neighbors regressor over past
 *     oil-change intervals predicts the next interval length in km
 *     (recent, similar intervals weigh more).
 *  2. An ordinary-least-squares linear regression of odometer vs. time
 *     estimates the daily driving rate, which converts the predicted
 *     km interval into a calendar date.
 *
 * Training is instantaneous for this data size and happens only when the
 * user explicitly asks for a prediction.
 */
object OilChangePredictor {

    const val MIN_RECORDS = 3
    private const val DAY_MILLIS = 24 * 60 * 60 * 1000L

    private data class Interval(
        val startMileage: Double,
        val deltaKm: Double,
        val recency: Double // 1.0 = most recent
    )

    fun train(records: List<OilChangePoint>): OilPrediction? {
        val points = records
            .filter { it.mileage > 0 && it.dateMillis > 0 }
            .sortedBy { it.dateMillis }
            .distinctBy { it.dateMillis to it.mileage }
        if (points.size < MIN_RECORDS) return null

        // ── Build training samples: consecutive oil-change intervals ──
        val intervals = points.zipWithNext { a, b ->
            Interval(
                startMileage = a.mileage.toDouble(),
                deltaKm      = (b.mileage - a.mileage).toDouble(),
                recency      = 0.0 // filled below
            )
        }.filter { it.deltaKm > 0 }
        if (intervals.size < 2) return null

        val samples = intervals.mapIndexed { i, itv ->
            itv.copy(recency = (i + 1).toDouble() / intervals.size)
        }

        // ── Learner 1: distance-weighted kNN over intervals ──────────
        val last = points.last()
        val predictedDeltaKm = knnPredictDelta(samples, query = last.mileage.toDouble())

        // ── Learner 2: OLS regression mileage ~ days → daily km rate ─
        val dailyRate = olsDailyRate(points)
            ?: (points.last().mileage - points.first().mileage).toDouble() /
               max(1.0, (points.last().dateMillis - points.first().dateMillis).toDouble() / DAY_MILLIS)
        if (dailyRate <= 0) return null

        val predictedMileage = (last.mileage + predictedDeltaKm).roundToInt()
        val daysUntil        = predictedDeltaKm / dailyRate
        val predictedDate    = last.dateMillis + (daysUntil * DAY_MILLIS).roundToLong()

        return OilPrediction(
            history             = points,
            predictedMileage    = predictedMileage,
            predictedDateMillis = predictedDate,
            dailyKmRate         = dailyRate,
            confidence          = confidenceFrom(samples.map { it.deltaKm }),
            trainedOnIntervals  = samples.size
        )
    }

    /**
     * Distance-weighted kNN regression. Feature = odometer at interval start
     * (captures wear-dependent habits); weight = recency / (distance + ε) so
     * recent and similar intervals dominate the estimate.
     */
    private fun knnPredictDelta(samples: List<Interval>, query: Double): Double {
        val range = max(
            1.0,
            samples.maxOf { it.startMileage } - samples.minOf { it.startMileage }
        )
        val k = min(3, samples.size)
        val neighbors = samples
            .sortedBy { abs(it.startMileage - query) }
            .take(k)

        var weightSum = 0.0
        var valueSum  = 0.0
        neighbors.forEach { n ->
            val dist   = abs(n.startMileage - query) / range
            val weight = n.recency / (dist + 0.1)
            weightSum += weight
            valueSum  += weight * n.deltaKm
        }
        return valueSum / weightSum
    }

    /** OLS slope of mileage over days; null when the fit is degenerate. */
    private fun olsDailyRate(points: List<OilChangePoint>): Double? {
        val t0 = points.first().dateMillis
        val xs = points.map { (it.dateMillis - t0).toDouble() / DAY_MILLIS }
        val ys = points.map { it.mileage.toDouble() }

        val n     = xs.size
        val meanX = xs.average()
        val meanY = ys.average()
        var num   = 0.0
        var den   = 0.0
        for (i in 0 until n) {
            num += (xs[i] - meanX) * (ys[i] - meanY)
            den += (xs[i] - meanX) * (xs[i] - meanX)
        }
        if (den <= 0.0) return null
        val slope = num / den
        return if (slope > 0) slope else null
    }

    /**
     * Confidence from the coefficient of variation of past intervals:
     * perfectly regular habits → high confidence, erratic → low.
     */
    private fun confidenceFrom(deltas: List<Double>): Float {
        val mean = deltas.average()
        if (mean <= 0) return 0.3f
        val variance = deltas.sumOf { (it - mean) * (it - mean) } / deltas.size
        val cv       = sqrt(variance) / mean
        return (1.0 - cv).coerceIn(0.3, 0.95).toFloat()
    }
}
