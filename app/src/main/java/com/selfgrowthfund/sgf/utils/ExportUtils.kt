package com.selfgrowthfund.sgf.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.selfgrowthfund.sgf.session.SessionEntry
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.OutputStreamWriter

object ExportUtils {

    private const val AUTHORITY_SUFFIX = ".provider"

    /**
     * Returns a file in either cacheDir (temporary, sharable) or
     * externalFilesDir/Documents (persistent, visible).
     */
    private fun getExportFile(context: Context, fileName: String, permanent: Boolean): File {
        return if (permanent) {
            File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
        } else {
            File(context.cacheDir, fileName)
        }
    }

    /**
     * Generic: Exports data to CSV file.
     */
    fun exportToCsv(
        context: Context,
        headers: List<String>,
        rows: List<List<String>>,
        fileName: String = "Export.csv",
        permanent: Boolean = false
    ): File {
        val file = getExportFile(context, fileName, permanent)
        FileOutputStream(file).use { fos ->
            OutputStreamWriter(fos).use { writer ->
                writer.write(headers.joinToString(","))
                writer.write("\n")
                rows.forEach { row ->
                    writer.write(row.joinToString(","))
                    writer.write("\n")
                }
                writer.flush()
            }
        }
        return file
    }

    /**
     * Exports simple text PDF (extendable for tables later).
     */
    fun exportToPdf(
        context: Context,
        title: String,
        lines: List<String>,
        fileName: String = "Export.pdf",
        permanent: Boolean = false
    ): File {
        val file = getExportFile(context, fileName, permanent)
        val pdfWriter = PdfWriter(file)
        val pdfDocument = PdfDocument(pdfWriter)
        val document = Document(pdfDocument)

        document.add(Paragraph(title))
        lines.forEach { line ->
            document.add(Paragraph(line))
        }

        document.close()
        return file
    }

    /**
     * Gets sharable Uri for file using FileProvider.
     */
    private fun getUriForFile(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            context.packageName + AUTHORITY_SUFFIX,
            file
        )
    }

    /**
     * Shares a file via external apps (Gmail, WhatsApp, Drive, etc.).
     */
    fun shareFile(context: Context, file: File, mimeType: String) {
        val uri = getUriForFile(context, file)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(
            Intent.createChooser(intent, "Share file via")
        )
    }

    // ----------------------
    // Session-specific CSV
    // ----------------------

    /**
     * Exports a list of SessionEntry into CSV and returns the file.
     */
    fun exportSessionHistoryCSV(
        context: Context,
        sessionEntries: List<SessionEntry>,
        permanent: Boolean = false
    ): File {
        val fileName = "session_history.csv"
        val file = File(context.getExternalFilesDir(null), fileName)
        FileWriter(file).use { writer ->
            // Header
            writer.append("Sr,ShareholderId,Name,Current Month Sessions,Lifetime Sessions\n")
            // Rows
            sessionEntries.forEach {
                writer.append("${it.sr},${it.shareholderId},${it.name},${it.currentMonthSessions},${it.lifetimeSessions}\n")
            }
        }
        return file
    }
}

// ----------------------
    // Utility builder
    // ----------------------

    /**
     * Builds CSV as a plain String (useful if you just need text).
     */
    fun buildCsvString(headers: List<String>, rows: List<List<String>>): String {
        val csvBuilder = StringBuilder()
        csvBuilder.append(headers.joinToString(",")).append("\n")
        rows.forEach { row ->
            csvBuilder.append(row.joinToString(",")).append("\n")
        }
        return csvBuilder.toString()
    }

