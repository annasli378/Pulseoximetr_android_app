package com.example.pulseoximeter

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.provider.BaseColumns
import androidx.appcompat.app.AppCompatActivity
import android.text.InputType
import android.widget.EditText
//import androidx.room.ColumnInfo
//import androidx.room.Entity
//import androidx.room.PrimaryKey
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.toast
import java.io.BufferedOutputStream
import java.io.IOException
import java.io.InputStream
import java.lang.Exception
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /////////////listenery do przycisków:   //////////////////////////
        try {
            var intentStart = Intent(this, SelectDevice::class.java)
            buttonStart.setOnClickListener {
                startActivity(intentStart)
            }
        } catch (e:Exception) {
            println(e.message)
        }

        var intentSett = Intent(this, SettingsActivity::class.java)
        buttonSettings.setOnClickListener {
            startActivity(intentSett)
        }
        var intentStat = Intent(this, StatisticActivity::class.java)
        buttonStatistic.setOnClickListener {
            startActivity(intentStat)
        }

    }
}
