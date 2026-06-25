package com.example.mmp_app.feature.student.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.print.PrintAttributes
import android.print.PrintManager
import android.util.Base64
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.mmp_app.domain.model.ExamSummaryDto
import com.example.mmp_app.domain.model.StudentDashboardDto
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class MarksheetPrinter(private val context: Context) {

    fun print(student: StudentDashboardDto?, exam: ExamSummaryDto) {
        val webView = WebView(context)
        val htmlContent = generateHtml(student, exam)
        
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
                val jobName = "Marksheet_${student?.studentName ?: "Student"}_${exam.examName}"
                val printAdapter = webView.createPrintDocumentAdapter(jobName)
                
                printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
            }
        }
        
        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
    }

    private fun getBase64Logo(): String {
        return try {
            val bitmap = BitmapFactory.decodeResource(context.resources, com.example.mmp_app.core.R.drawable.mmplogo)
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            val byteArray = outputStream.toByteArray()
            Base64.encodeToString(byteArray, Base64.DEFAULT)
        } catch (e: Exception) {
            ""
        }
    }

    private fun generateHtml(student: StudentDashboardDto?, exam: ExamSummaryDto): String {
        val allPassed = exam.subjects.all { it.isPassed }
        val totalObtained = exam.subjects.sumOf { it.score.toDouble() }
        val totalFull = exam.subjects.sumOf { it.total.toDouble() }.let { if (it == 0.0) exam.subjects.size * 25.0 else it }
        val percentage = if (totalFull > 0) (totalObtained / totalFull * 100) else 0.0
        val dateStr = SimpleDateFormat("yyyy-MM-dd 'at' hh:mm a", Locale.getDefault()).format(Date())
        val logoBase64 = getBase64Logo()

        val deptName = student?.program?.replace("Diploma in ", "") ?: "Computer Engineering"
        val progName = if (student?.program?.contains("Diploma") == true) student.program else "Diploma in ${student?.program ?: "Computer Engineering"}"

        val rows = exam.subjects.joinToString("") { mark ->
            """
            <tr>
                <td><b>${mark.subject}</b><br/><small>${mark.code ?: ""}</small></td>
                <td align="center">${mark.total.toInt()}</td>
                <td align="center">${mark.passMarks.toInt()}</td>
                <td align="center" style="color: ${if (mark.isPassed) "#16A34A" else "#DC2626"}; font-weight: bold;">${"%.2f".format(mark.score)}</td>
                <td align="center"><span class="badge ${if (mark.isPassed) "pass" else "fail"}">${if (mark.isPassed) "Pass" else "Fail"}</span></td>
            </tr>
            """
        }

        return """
        <html>
        <head>
            <style>
                body { font-family: sans-serif; padding: 20px; color: #111827; }
                .header { text-align: center; position: relative; margin-bottom: 20px; }
                .status-badge { position: absolute; top: 0; right: 0; padding: 5px 10px; border-radius: 5px; font-weight: bold; font-size: 12px; }
                .pass-bg { background-color: #DCFCE7; color: #16A34A; }
                .fail-bg { background-color: #FEE2E2; color: #DC2626; }
                .title { color: #7C3AED; font-size: 18px; font-weight: bold; margin: 10px 0; }
                .info-grid { display: flex; background: #F9FAFB; padding: 10px; margin-bottom: 20px; border-bottom: 1px solid #eee; }
                .info-item { flex: 1; font-size: 10px; }
                .info-value { font-weight: bold; font-size: 12px; display: block; margin-top: 4px; }
                table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }
                th { border-bottom: 2px solid #E5E7EB; padding: 10px; font-size: 10px; color: #4B5563; text-transform: uppercase; }
                td { border-bottom: 1px solid #F3F4F6; padding: 10px; font-size: 11px; }
                .total-row { font-weight: bold; background: #fdfdfd; }
                .footer-summary { display: flex; justify-content: space-between; padding: 0 10px; margin-top: 20px; }
                .summary-item { text-align: center; }
                .summary-label { font-size: 10px; color: #9CA3AF; text-transform: uppercase; }
                .summary-value { font-size: 16px; font-weight: 800; margin-top: 4px; }
                .signatures { display: flex; justify-content: space-between; margin-top: 60px; padding: 0 20px; }
                .sig-box { text-align: center; width: 40%; }
                .sig-line { border-top: 1.5px solid #111827; margin-top: 30px; padding-top: 8px; font-weight: 800; font-size: 11px; }
                .badge { padding: 2px 8px; border-radius: 10px; font-size: 9px; font-weight: bold; }
                .pass { background: #DCFCE7; color: #16A34A; }
                .fail { background: #FEE2E2; color: #DC2626; }
                .logo { width: 80px; height: 80px; margin-bottom: 10px; }
            </style>
        </head>
        <body>
            <div class="header">
                <div class="status-badge ${if (allPassed) "pass-bg" else "fail-bg"}">${if (allPassed) "PASSED" else "FAILED"}</div>
                ${if (logoBase64.isNotEmpty()) "<img src=\"data:image/jpeg;base64,$logoBase64\" class=\"logo\" />" else ""}
                <h2 style="margin:0; font-size: 20px; letter-spacing: 0.5px;">MANMOHAN MEMORIAL POLYTECHNIC</h2>
                <div style="color: #4B5563; font-weight: 600; margin-top: 4px;">$deptName</div>
                <div style="font-size: 12px; color: #6B7280; margin-top: 2px;">$progName</div>
                <hr style="width: 60%; border: 0; border-top: 1.5px solid #E5E7EB; margin: 15px auto;"/>
                <div class="title">${exam.examName.uppercase()}</div>
                <div style="font-size: 11px; color: #6B7280;">Monthly Assessment - ${exam.startDate ?: "N/A"}</div>
            </div>

            <div class="info-grid">
                <div class="info-item" style="flex: 1.5;">
                    <span class="summary-label">STUDENT NAME</span>
                    <span class="info-value">${student?.studentName?.uppercase() ?: "N/A"}</span>
                </div>
                <div class="info-item">
                    <span class="summary-label">ROLL NO.</span>
                    <span class="info-value">${student?.rollNumber ?: "N/A"}</span>
                </div>
                <div class="info-item">
                    <span class="summary-label">SEMESTER</span>
                    <span class="info-value">${student?.semester ?: "N/A"}</span>
                </div>
                <div class="info-item">
                    <span class="summary-label">SECTION</span>
                    <span class="info-value">A</span>
                </div>
            </div>

            <table>
                <thead>
                    <tr>
                        <th align="left">SUBJECT</th>
                        <th>FULL</th>
                        <th>PASS</th>
                        <th>OBTAINED</th>
                        <th>REMARKS</th>
                    </tr>
                </thead>
                <tbody>
                    $rows
                    <tr class="total-row">
                        <td align="right">TOTAL</td>
                        <td align="center">${totalFull.toInt()}</td>
                        <td align="center">-</td>
                        <td align="center" style="color: #16A34A;">${"%.2f".format(totalObtained)}</td>
                        <td align="center"><span class="badge ${if (allPassed) "pass" else "fail"}">${if (allPassed) "PASS" else "FAIL"}</span></td>
                    </tr>
                </tbody>
            </table>

            <div class="footer-summary">
                <div class="summary-item">
                    <div class="summary-label">TOTAL MARKS</div>
                    <div class="summary-value">${"%.2f".format(totalObtained)} <small style="font-size: 10px; color: #9CA3AF; font-weight: normal;">/ ${totalFull.toInt()}</small></div>
                </div>
                <div class="summary-item">
                    <div class="summary-label">PERCENTAGE</div>
                    <div class="summary-value" style="color: #7C3AED;">${"%.1f".format(percentage)}%</div>
                </div>
                <div class="summary-item">
                    <div class="summary-label">RESULT</div>
                    <div class="summary-value" style="color: ${if (allPassed) "#16A34A" else "#DC2626"};">${if (allPassed) "PASSED" else "FAILED"}</div>
                </div>
            </div>

            <div class="signatures">
                <div class="sig-box">
                    <div class="summary-label">Prepared By:</div>
                    <div class="sig-line">Examination Department</div>
                </div>
                <div class="sig-box">
                    <div class="summary-label">Head of Department</div>
                    <div class="sig-line">$deptName</div>
                </div>
            </div>

            <div style="margin-top: 40px; text-align: center; font-size: 10px; color: #9CA3AF; border-top: 1px dashed #eee; padding-top: 10px;">
                Generated on $dateStr
            </div>
        </body>
        </html>
        """
    }
}
