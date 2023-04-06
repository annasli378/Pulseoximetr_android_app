package com.example.pulseoximeter

import android.content.Context
import android.database.Cursor
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import kotlinx.android.synthetic.main.activity_statistic.*
import java.lang.Exception
import java.security.KeyStore
import kotlin.math.roundToInt

class StatisticActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistic)

        ////////////////////// wczytanie pliku z pomiarami/////////////////////
        var allBpmList = mutableListOf<Int>()
        var allSpo2List = mutableListOf<Int>()
        var allDataList = mutableListOf<String>()


        try {
            val myDB = openOrCreateDatabase("my.db", Context.MODE_PRIVATE, null)
            myDB.execSQL(
                "CREATE TABLE IF NOT EXISTS measurements (bpm INT, spo2 INT, date VARCHAR(20))"
            )

            val myCursor: Cursor = myDB.rawQuery("select bpm, spo2, date from measurements", null)
            while (myCursor.moveToNext()) {
                val bpm = myCursor.getInt(0)
                val spo2 = myCursor.getInt(1)
                val date = myCursor.getString(2)
                allBpmList.add(bpm)
                allSpo2List.add(spo2)
                allDataList.add(date)
            }
            myCursor.close()
            myDB.close()

        } catch (e: Exception) {
            println(e.message)
        }

/////////// znalezienie min, max, dat i obliczenie avg/////////////////
        var maxBPM: Int? = 0
        var minBPM:Int? = 0
        var avgBPM:Int? = 0
        var maxBPMDate: String? = null
        var minBPMDate: String? = null

        if (allBpmList.count() > 0) {
            maxBPM = allBpmList.max()
            minBPM = allBpmList.min()
            avgBPM = ( (allBpmList.sum().toDouble()) / (allBpmList.count()) ).roundToInt()
            maxBPMDate = allDataList[allBpmList.indexOf(maxBPM)]
            minBPMDate = allDataList[allBpmList.indexOf(minBPM)]
            println(" $maxBPM - $maxBPMDate, $minBPM - $minBPMDate, $avgBPM" )
        }
        ////////////////wyświetlanie wartości w widoku///////
        textViewMaxValue.text = "" + maxBPM
        textViewMinValue.text = "" + minBPM
        textViewAvgValue.text = "" + avgBPM
        textViewMaxDate.text = "" + maxBPMDate?.substring(0,10)
        textViewMinDate.text = "" + minBPMDate?.substring(0,10)

        ////////////wykres ///////////////////////////
        var mChart = findViewById<View>(R.id.chart)

        var values : ArrayList<Entry> = ArrayList<Entry>()
        var e1 : Entry
        if (allBpmList.count() > 30) {
            for (i in (allBpmList.count() - 20)..(allBpmList.count() -1)) {
                e1 = Entry(i.toFloat(), allBpmList[i].toFloat())
                values.add(e1)
            }
        } else if (allBpmList.count() > 0) {
            for (i in 0..(allBpmList.count() -1)) {
                e1 = Entry(i.toFloat(), allBpmList[i].toFloat())
                values.add(e1)
            }
        }
        else {
            e1 = Entry(0.toFloat(), 0.toFloat())
            values.add(e1)
        }

        var set1: LineDataSet = LineDataSet(values, "Measurements")
        set1.setDrawIcons(false)
        set1.enableDashedLine(10f, 5f, 0f)
        set1.setColor(Color.LTGRAY)
        set1.setCircleColor(Color.LTGRAY)
        set1.lineWidth = 1f
        set1.circleHoleRadius = 3f
        set1.setDrawCircleHole(false)
        set1.valueTextSize = 9f
        set1.setDrawFilled(true)
        set1.formLineDashEffect = DashPathEffect(floatArrayOf(10f, 5f), 0f)
        set1.formSize = 15f
        set1.fillColor = Color.RED

        var dataSets : ArrayList<ILineDataSet> = ArrayList()
        dataSets.add(set1)

        var data : LineData = LineData(dataSets)
        chart.data = data
        chart.setDrawGridBackground(false)
        chart.description = null

        chart.notifyDataSetChanged()

    }
}