package com.mis.parentapp.features.services

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.itextpdf.text.*
import com.itextpdf.text.pdf.*
import com.mis.parentapp.R
import java.io.ByteArrayOutputStream
import java.io.OutputStream

/**
 * Handles the creation and styling of the PDF Receipt
 */
class ReceiptPdfGenerator {

    /**
     * Generates the PDF content into the output stream
     */
    fun createPdfContent(context: Context, outputStream: OutputStream, record: PaymentRecord) {
        val document = Document()
        PdfWriter.getInstance(document, outputStream)
        document.open()

        // 1. Add Logo
        try {
            val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.school_logo)
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val image = Image.getInstance(stream.toByteArray())
            image.scaleToFit(100f, 100f)
            image.alignment = Element.ALIGN_CENTER
            document.add(image)
            document.add(Chunk.NEWLINE)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Title
        val titleFont = Font(Font.FontFamily.HELVETICA, 18f, Font.BOLD)
        titleFont.color = BaseColor(27, 77, 19)
        val title = Paragraph("OFFICIAL SCHOOL RECEIPT", titleFont)
        title.alignment = Element.ALIGN_CENTER
        document.add(title)
        document.add(Chunk.NEWLINE)

        // 3. Top Info
        val normalFont = Font(Font.FontFamily.HELVETICA, 10f, Font.NORMAL)
        val boldFont = Font(Font.FontFamily.HELVETICA, 10f, Font.BOLD)

        document.add(Paragraph("Receipt Number: ${record.invoiceNumber}          Date: ${record.paidDate}", boldFont))
        document.add(Chunk.NEWLINE)
        document.add(Paragraph("Student Name: Nathaniel B. McClure", normalFont))
        document.add(Paragraph("Student ID: ____________________          Course/Level: BSIT-3", normalFont))
        document.add(Chunk.NEWLINE)

        // 4. Items Table
        val table = PdfPTable(3)
        table.widthPercentage = 100f
        table.setWidths(floatArrayOf(50f, 20f, 30f))

        val headerFont = Font(Font.FontFamily.HELVETICA, 10f, Font.BOLD)
        headerFont.color = BaseColor(27, 77, 19)

        // Headers
        table.addCell(createCenterCell("Item Description", headerFont))
        table.addCell(createCenterCell("Quantity", headerFont))
        table.addCell(createCenterCell("Amount", headerFont))

        // Items
        if (record.pdfBreakdown.isNotEmpty()) {
            val items = record.pdfBreakdown.split("\n")
            items.forEach { itemLine ->
                val parts = itemLine.split("|")
                if (parts.size == 3) {
                    table.addCell(Phrase(parts[0], normalFont))
                    table.addCell(createCenterCell(parts[1], normalFont))
                    table.addCell(createRightCell("PHP ${parts[2]}", normalFont))
                }
            }
        }

        // Empty rows for spacing
        repeat(5) {
            table.addCell(Phrase("", normalFont))
            table.addCell(Phrase("", normalFont))
            table.addCell(Phrase("", normalFont))
        }

        // Subtotal
        val subTotalCell = PdfPCell(Phrase("Subtotal", boldFont))
        subTotalCell.colspan = 2
        subTotalCell.horizontalAlignment = Element.ALIGN_RIGHT
        subTotalCell.border = Rectangle.NO_BORDER
        table.addCell(subTotalCell)
        table.addCell(createRightCell("PHP ${"%.2f".format(record.totalAmount)}", boldFont))

        // Total
        val totalCell = PdfPCell(Phrase("Total Amount Paid", boldFont))
        totalCell.colspan = 2
        totalCell.horizontalAlignment = Element.ALIGN_RIGHT
        totalCell.border = Rectangle.NO_BORDER
        table.addCell(totalCell)
        table.addCell(createRightCell("PHP ${"%.2f".format(record.totalAmount)}", boldFont))

        document.add(table)
        document.add(Chunk.NEWLINE)

        // 5. Amount in Words (Calls your utility function automatically)
        val amountInWords = numberToWords(record.totalAmount)
        document.add(Paragraph("Amount in Words: $amountInWords", boldFont))
        document.add(Chunk.NEWLINE)

        document.add(Paragraph("Payment Method: ${record.paymentOption}", normalFont))
        document.add(Chunk.NEWLINE)
        document.add(Chunk.NEWLINE)

        // Signature Line
        document.add(Paragraph("_________________________________", normalFont))
        document.add(Paragraph("Cashier/Authorized Representative", normalFont))

        document.close()
    }

    // Helper functions to make table cells look clean
    private fun createCenterCell(text: String, font: Font): PdfPCell {
        val cell = PdfPCell(Phrase(text, font))
        cell.horizontalAlignment = Element.ALIGN_CENTER
        return cell
    }

    private fun createRightCell(text: String, font: Font): PdfPCell {
        val cell = PdfPCell(Phrase(text, font))
        cell.horizontalAlignment = Element.ALIGN_RIGHT
        return cell
    }
}