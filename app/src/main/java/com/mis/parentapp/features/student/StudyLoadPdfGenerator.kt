package com.mis.parentapp.features.student

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.itextpdf.text.BaseColor
import com.itextpdf.text.Chunk
import com.itextpdf.text.Document
import com.itextpdf.text.Element
import com.itextpdf.text.Font
import com.itextpdf.text.Image
import com.itextpdf.text.PageSize
import com.itextpdf.text.Paragraph
import com.itextpdf.text.Phrase
import com.itextpdf.text.Rectangle
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import com.mis.parentapp.R
import com.mis.parentapp.network.Child
import com.mis.parentapp.network.StudyLoadSubject
import java.io.ByteArrayOutputStream
import java.io.OutputStream

class StudyLoadPdfGenerator {
    fun createPdfContent(
        context: Context,
        outputStream: OutputStream,
        student: Child,
        subjects: List<StudyLoadSubject>
    ) {
        val document = Document(PageSize.A4, 28f, 28f, 36f, 32f)
        PdfWriter.getInstance(document, outputStream)
        document.open()

        val green = BaseColor(33, 92, 24)
        val lightGreen = BaseColor(246, 253, 231)
        val titleFont = Font(Font.FontFamily.HELVETICA, 18f, Font.BOLD, green)
        val headerFont = Font(Font.FontFamily.HELVETICA, 9f, Font.BOLD, BaseColor.WHITE)
        val boldFont = Font(Font.FontFamily.HELVETICA, 9f, Font.BOLD, BaseColor.BLACK)
        val normalFont = Font(Font.FontFamily.HELVETICA, 8f, Font.NORMAL, BaseColor.BLACK)
        val smallFont = Font(Font.FontFamily.HELVETICA, 7f, Font.NORMAL, BaseColor.DARK_GRAY)

        val semester = subjects.firstOrNull()?.semester ?: "2nd Sem."
        val schoolYear = subjects.firstOrNull()?.schoolYear ?: "S.Y. 2025-2026"
        val dateEnrolled = subjects.firstOrNull()?.dateEnrolled ?: "--"

        val header = PdfPTable(floatArrayOf(1.1f, 5f, 1.8f)).apply {
            widthPercentage = 100f
            spacingAfter = 10f
        }
        header.addCell(noBorderCell(loadLogo(context), Element.ALIGN_LEFT))
        header.addCell(noBorderCell(Phrase("COLEGIO DE ALICIA\nAlicia, Bohol\n\nOFFICIAL STUDY LOAD", titleFont), Element.ALIGN_CENTER))
        header.addCell(noBorderCell(Phrase("$semester\n$schoolYear", boldFont), Element.ALIGN_RIGHT))
        document.add(header)

        val info = PdfPTable(floatArrayOf(1.2f, 2.5f, 2.0f, 1.5f)).apply {
            widthPercentage = 100f
            spacingAfter = 12f
        }
        listOf(
            "ID NO.\n${student.rollNumber}",
            "STUDENT\n${student.name.uppercase()}",
            "PROGRAM\n${student.course.uppercase()}",
            "SECTION\n${student.section.uppercase()}"
        ).forEach {
            info.addCell(PdfPCell(Phrase(it, boldFont)).apply {
                backgroundColor = lightGreen
                borderColor = green
                setPadding(6f)
            })
        }
        document.add(info)
        document.add(Chunk.NEWLINE)

        val table = PdfPTable(floatArrayOf(1.1f, 1.45f, 1.8f, 0.8f, 0.85f, 0.75f, 1.8f)).apply {
            widthPercentage = 100f
            spacingBefore = 2f
            spacingAfter = 12f
        }
        listOf("SCHED. NO.", "COURSE NO.", "TIME", "DAYS", "ROOM", "UNITS", "REMARKS").forEach {
            table.addCell(tableCell(it, headerFont, green, Element.ALIGN_CENTER))
        }
        subjects.forEach { subject ->
            table.addCell(tableCell(subject.scheduleNumber.ifBlank { "--" }, normalFont))
            table.addCell(tableCell(subject.courseNumber.ifBlank { subject.code }, normalFont))
            table.addCell(tableCell(subject.time.ifBlank { subject.schedule }, normalFont))
            table.addCell(tableCell(subject.days.ifBlank { "--" }, normalFont, alignment = Element.ALIGN_CENTER))
            table.addCell(tableCell(subject.room, normalFont, alignment = Element.ALIGN_CENTER))
            table.addCell(tableCell(subject.units.toString(), normalFont, alignment = Element.ALIGN_CENTER))
            table.addCell(tableCell(subject.remarks, normalFont))
        }
        document.add(table)

        val footer = PdfPTable(floatArrayOf(1f, 1f)).apply { widthPercentage = 100f }
        footer.addCell(noBorderCell(Phrase("DATE ENROLLED: $dateEnrolled", boldFont), Element.ALIGN_LEFT))
        footer.addCell(noBorderCell(Phrase("TOTAL: ${subjects.sumOf { it.units }}", boldFont), Element.ALIGN_RIGHT))
        document.add(footer)
        document.add(Paragraph("LEGEND: (W) = Withdrawn     ** = Dissolved Subject", smallFont))
        document.add(Chunk.NEWLINE)
        document.add(Paragraph("This official study load is generated from Colegio De Alicia parent portal records.", smallFont))
        document.add(Paragraph("Generated copy is for parent/student viewing and verification purposes.", Font(Font.FontFamily.HELVETICA, 8f, Font.BOLD, green)))
        document.close()
    }

    private fun loadLogo(context: Context): Image {
        val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.school_logo)
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return Image.getInstance(stream.toByteArray()).apply {
            scaleToFit(62f, 62f)
        }
    }

    private fun noBorderCell(content: Any, alignment: Int): PdfPCell {
        val cell = when (content) {
            is Image -> PdfPCell(content)
            is Phrase -> PdfPCell(content)
            else -> PdfPCell(Phrase(content.toString()))
        }
        cell.border = Rectangle.NO_BORDER
        cell.horizontalAlignment = alignment
        cell.verticalAlignment = Element.ALIGN_MIDDLE
        cell.setPadding(6f)
        return cell
    }

    private fun tableCell(
        text: String,
        font: Font,
        background: BaseColor? = null,
        alignment: Int = Element.ALIGN_LEFT
    ): PdfPCell {
        return PdfPCell(Phrase(text, font)).apply {
            horizontalAlignment = alignment
            verticalAlignment = Element.ALIGN_MIDDLE
            borderColor = BaseColor(158, 165, 143)
            minimumHeight = 20f
            setPadding(5f)
            if (background != null) backgroundColor = background
        }
    }
}
