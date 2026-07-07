package com.hbazai.mycarservices.ml

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OilChangePredictorTest {

    private val day = 24 * 60 * 60 * 1000L

    private fun point(dayIndex: Int, mileage: Int) =
        OilChangePoint(dateMillis = dayIndex * day, mileage = mileage)

    @Test
    fun `returns null with fewer than three records`() {
        assertNull(OilChangePredictor.train(listOf(point(0, 10000), point(90, 15000))))
    }

    @Test
    fun `regular habit predicts one interval ahead`() {
        // Every 90 days, every 5000 km.
        val records = listOf(
            point(0, 10000),
            point(90, 15000),
            point(180, 20000),
            point(270, 25000)
        )
        val p = OilChangePredictor.train(records)!!

        assertEquals(30000, p.predictedMileage)
        // ~90 days after the last change.
        val predictedDay = p.predictedDateMillis / day
        assertTrue("predicted day $predictedDay", predictedDay in 355L..365L)
        // Perfectly regular history → high confidence.
        assertTrue(p.confidence > 0.9f)
        assertEquals(3, p.trainedOnIntervals)
    }

    @Test
    fun `irregular habit lowers confidence but stays in range`() {
        val records = listOf(
            point(0, 10000),
            point(60, 12000),
            point(200, 21000),
            point(230, 23000)
        )
        val p = OilChangePredictor.train(records)!!

        assertTrue(p.predictedMileage > 23000)
        assertTrue(p.predictedDateMillis > records.last().dateMillis)
        assertTrue(p.confidence in 0.3f..0.95f)
    }

    @Test
    fun `unsorted input is handled`() {
        val records = listOf(
            point(180, 20000),
            point(0, 10000),
            point(90, 15000)
        )
        val p = OilChangePredictor.train(records)!!
        assertEquals(25000, p.predictedMileage)
    }
}
