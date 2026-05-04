package com.hbazai.mycarservices.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.hbazai.mycarservices.data.local.entity.CarEntity
import com.hbazai.mycarservices.data.local.entity.ServiceRecordEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfExporter {

    private const val PAGE_WIDTH  = 595   // A4 width in points
    private const val PAGE_HEIGHT = 842   // A4 height in points
    private const val MARGIN      = 40f
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val fileFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())

    fun exportCarHistory(
        context: Context,
        car: CarEntity,
        services: List<ServiceRecordEntity>
    ): File? {
        return try {
            val document = PdfDocument()
            var pageNumber    = 1
            var yPosition     = 0f
            var canvas: Canvas
            var page: PdfDocument.Page

            fun newPage(): Pair<PdfDocument.Page, Canvas> {
                val pageInfo = PdfDocument.PageInfo.Builder(
                    PAGE_WIDTH, PAGE_HEIGHT, pageNumber++
                ).create()
                val p = document.startPage(pageInfo)
                return Pair(p, p.canvas)
            }

            val (firstPage, firstCanvas) = newPage()
            page   = firstPage
            canvas = firstCanvas
            yPosition = MARGIN

            // ── Title ─────────────────────────────────
            val titlePaint = Paint().apply {
                color     = Color.parseColor("#FFD600")
                textSize  = 24f
                isFakeBoldText = true
            }
            canvas.drawText("Car Service History Report", MARGIN, yPosition, titlePaint)
            yPosition += 36f

            // ── Car info ──────────────────────────────
            val headerPaint = Paint().apply {
                color    = Color.BLACK
                textSize = 14f
                isFakeBoldText = true
            }
            val bodyPaint = Paint().apply {
                color    = Color.DKGRAY
                textSize = 12f
            }
            val linePaint = Paint().apply {
                color       = Color.parseColor("#FFD600")
                strokeWidth = 1.5f
            }

            canvas.drawText("Vehicle Information", MARGIN, yPosition, headerPaint)
            yPosition += 6f
            canvas.drawLine(MARGIN, yPosition, PAGE_WIDTH - MARGIN, yPosition, linePaint)
            yPosition += 16f

            listOf(
                "Name"          to car.name,
                "Model"         to car.model,
                "Year"          to car.year.toString(),
                "License Plate" to car.licensePlate,
                "Mileage"       to "${car.currentMileage} km"
            ).forEach { (label, value) ->
                canvas.drawText("$label: $value", MARGIN, yPosition, bodyPaint)
                yPosition += 18f
            }

            yPosition += 10f
            canvas.drawText(
                "Generated on: ${dateFormat.format(Date())}",
                MARGIN, yPosition,
                bodyPaint.apply { color = Color.GRAY; textSize = 10f }
            )
            yPosition += 24f

            // ── Services ─────────────────────────────
            bodyPaint.textSize = 12f
            bodyPaint.color    = Color.DKGRAY

            canvas.drawText("Service Records (${services.size})", MARGIN, yPosition, headerPaint)
            yPosition += 6f
            canvas.drawLine(MARGIN, yPosition, PAGE_WIDTH - MARGIN, yPosition, linePaint)
            yPosition += 20f

            services.forEach { service ->
                // Check if we need a new page
                if (yPosition > PAGE_HEIGHT - 160f) {
                    document.finishPage(page)
                    val (newPage, newCanvas) = newPage()
                    page      = newPage
                    canvas    = newCanvas
                    yPosition = MARGIN
                }

                // Service block background
                val bgPaint = Paint().apply {
                    color = Color.parseColor("#F5F5F5")
                    style = Paint.Style.FILL
                }
                canvas.drawRect(
                    MARGIN, yPosition - 14f,
                    (PAGE_WIDTH - MARGIN).toFloat(), yPosition + 100f,
                    bgPaint
                )

                // Service type header
                val typePaint = Paint().apply {
                    color          = Color.parseColor("#1A1A1A")
                    textSize       = 13f
                    isFakeBoldText = true
                }
                canvas.drawText(service.serviceType, MARGIN + 8f, yPosition, typePaint)
                yPosition += 18f

                listOf(
                    "Date"          to dateFormat.format(Date(service.serviceDate)),
                    "Mileage"       to "${service.mileageAtService} km",
                    "Next Service"  to "${service.nextServiceMileage} km / ${dateFormat.format(Date(service.nextServiceDate))}",
                    "Cost"          to "€ ${"%.2f".format(service.cost)}"
                ).forEach { (label, value) ->
                    canvas.drawText("$label: $value", MARGIN + 8f, yPosition, bodyPaint)
                    yPosition += 16f
                }

                if (service.cause.isNotBlank()) {
                    canvas.drawText("Cause: ${service.cause}", MARGIN + 8f, yPosition, bodyPaint)
                    yPosition += 16f
                }
                if (service.providerName.isNotBlank()) {
                    canvas.drawText("Provider: ${service.providerName}", MARGIN + 8f, yPosition, bodyPaint)
                    yPosition += 16f
                }
                if (service.providerPhone.isNotBlank()) {
                    canvas.drawText("Phone: ${service.providerPhone}", MARGIN + 8f, yPosition, bodyPaint)
                    yPosition += 16f
                }
                if (service.notes.isNotBlank()) {
                    canvas.drawText("Notes: ${service.notes}", MARGIN + 8f, yPosition, bodyPaint)
                    yPosition += 16f
                }

                yPosition += 12f
            }

            document.finishPage(page)

            // ── Save to Downloads ─────────────────────
            val fileName  = "CarService_${car.name}_${fileFormat.format(Date())}.pdf"
            val downloads = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS
            )
            val file = File(downloads, fileName)
            document.writeTo(FileOutputStream(file))
            document.close()
            file

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}