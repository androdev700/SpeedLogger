package com.andro.speedlogger

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Environment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import org.apache.poi.hssf.usermodel.HSSFCellStyle
import org.apache.poi.hssf.usermodel.HSSFSheet
import org.apache.poi.hssf.util.HSSFColor
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class MainActivity : AppCompatActivity(), IBaseGpsListener {

    private var state = false
    private var workBook: HSSFWorkbook? = null
    private var count = 1
    private var startTimeStamp: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        start_stop_btn.setOnClickListener {
            val locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            state = !state
            if (state) {
                start_stop_btn.text = "Stop"
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    Toast.makeText(this, "Permissions not granted", Toast.LENGTH_SHORT).show()
                } else {
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        0,
                        0f,
                        this
                    )
                    this.updateSpeed(null)
                    createWorkbookSheet()
                }
            } else {
                start_stop_btn.text = "Start"
                locationManager.removeUpdates(this)
                workBook?.let {
                    if (workBook != null) {
                        stopWritingFile("${Date().time}.xls", it)
                    }
                }
            }
        }
    }

    private fun createWorkbookSheet() {
        //New Workbook
        val wb = HSSFWorkbook()

        var c: Cell?

        //Cell style for header row
        val cs = wb.createCellStyle()
        cs.fillForegroundColor = HSSFColor.LIME.index
        cs.fillPattern = HSSFCellStyle.SOLID_FOREGROUND

        //New Sheet
        val sheet1: HSSFSheet? = wb.createSheet("Log Record")

        // Generate column headings
        val row = sheet1!!.createRow(0)

        c = row.createCell(0)
        c!!.setCellValue("Time (s)")
        c!!.cellStyle = cs

        c = row.createCell(1)
        c!!.setCellValue("Speed (km/hr)")
        c!!.cellStyle = cs

        sheet1.setColumnWidth(0, 15 * 500)
        sheet1.setColumnWidth(1, 15 * 500)

        Log.w("FileUtils", "Workbook Created")
        workBook = wb
    }

    private fun startWritingFile(time: Long, speed: String, rowNumber: Int) {
        val sheet1 = workBook?.getSheet("Log Record")
        var c: Cell?

        startTimeStamp?.let {
            val timeInterval = (time - it) / 1000L
            val row = sheet1?.createRow(rowNumber)
            c = row?.createCell(0)
            c?.setCellValue(timeInterval.toString())

            Log.w("FileUtils", "Written time ${c?.stringCellValue}")

            c = row?.createCell(1)
            c?.setCellValue(speed)

            Log.w("FileUtils", "Written speed ${c?.stringCellValue}")

            count++
        }
    }

    private fun stopWritingFile(fileName: String, wb: HSSFWorkbook): Boolean {
        var success = false
        // Create a path where we will place our List of objects on external storage
        val file = File(this.getExternalFilesDir(null), fileName)

        var os: FileOutputStream? = null

        try {
            os = FileOutputStream(file)
            wb.write(os)
            Log.d("FileUtils", "Writing file$file")
            success = true
        } catch (e: IOException) {
            Log.e("FileUtils", "Error writing $file", e)
            Toast.makeText(this, "Failed to write", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.e("FileUtils", "Failed to save file", e)
            Toast.makeText(this, "File failed to save", Toast.LENGTH_LONG).show()
        } finally {
            try {
                os?.close()
            } catch (ex: Exception) {
                Log.e("FileUtils", "Failed to write file", ex)
                Toast.makeText(this, "File failed to write", Toast.LENGTH_LONG).show()
            }
        }
        workBook = null
        count = 1
        startTimeStamp = null
        Toast.makeText(this, "File written to ${file.path}", Toast.LENGTH_LONG).show()
        return success
    }

    override fun finish() {
        super.finish()
        System.exit(0)
    }

    @SuppressLint("SetTextI18n")
    private fun updateSpeed(location: CLocation?) {
        val nCurrentSpeed: Float
        if (location != null) {
            if (startTimeStamp == null) {
                startTimeStamp = location.time
            }
            nCurrentSpeed = location.speed
            val fmt = Formatter(StringBuilder())
            fmt.format(Locale.US, "%5.1f", nCurrentSpeed)
            var strCurrentSpeed = fmt.toString()
            strCurrentSpeed = strCurrentSpeed.replace(' ', '0')
            val strUnits = "km/hr"
            txtCurrentSpeed.text = "$strCurrentSpeed $strUnits"
            gauge.speedTo(strCurrentSpeed.toFloat(), 500)
            startWritingFile(location.time, strCurrentSpeed, count)
        } else {
            txtCurrentSpeed.text = "Not receiving updates.."
        }
    }

    override fun onLocationChanged(location: Location) {
        val myLocation = CLocation(location)
        this.updateSpeed(myLocation)
    }

    override fun onProviderDisabled(provider: String) {

    }

    override fun onProviderEnabled(provider: String) {

    }

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {

    }

    override fun onGpsStatusChanged(event: Int) {

    }
}
