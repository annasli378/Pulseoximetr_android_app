package com.example.pulseoximeter

//import androidx.room.Room
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.ContentValues
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.provider.SyncStateContract
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_start.*
import org.jetbrains.anko.toast
import java.io.IOException
import java.io.InputStream
import java.io.PrintStream
import java.lang.StringBuilder
import java.lang.System.out
import java.text.SimpleDateFormat
import java.util.*
/*
class ConnectThread : Thread() {
    private lateinit var mmSocket: BluetoothSocket
    private lateinit var mmDevice: BluetoothDevice
    private var myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    fun ConnectThread(device: BluetoothDevice) {
        var tmp : BluetoothSocket? = null
        mmDevice = device
        try {
            tmp = device.createRfcommSocketToServiceRecord(myUUID)
        } catch (e: IOException) {
            println(e.message)
        }
        if (tmp != null) {
            mmSocket = tmp
        }
    }

    fun Run(out: PrintStream) {
        mBluetoothAdapter.cancelDiscovery()
        try {
            mmSocket.connect()
        } catch (connectException : IOException) {
            println(connectException.message)
        }

        var mConnectedThread: ConnectedThread = ConnectedThread()
        mConnectedThread.ConnectedThread(mmSocket)
        mConnectedThread.start()
    }

    fun Cancel() {
        try {
            mmSocket.close()
        } catch (e: IOException) {
            println(e.message)
        }
    }
}

class ConnectedThread : Thread() {
    lateinit var mmSocket: BluetoothSocket
    lateinit var mmInStream: InputStream

    fun ConnectedThread(socket: BluetoothSocket) {
        mmSocket = socket
        var tmpIn: InputStream? = null
        try {
            tmpIn = socket.inputStream
        } catch (e: IOException) {
            println(e.message)
        }
        if (tmpIn != null) {
            mmInStream = tmpIn
        }
    }
    fun Run() {
        var buffer = ByteArray(1024)
        var bytes: Int
        var readMessage: StringBuilder = StringBuilder()
        while (true) {
            try {
                bytes = mmInStream.read(buffer)
                var read : String = String(buffer, 0 , bytes)
                readMessage.append(read)
                println(readMessage)
/*
                if (read.contains("\n")) {
                    myHandler.obtainMessage(SyncStateContract.Constants.DATA.toInt(), bytes, -1, readMessage.toString() ).sendToTarget()
                    readMessage.setLength(0)
                }

 */
            } catch (e:IOException) {
                println(e.message)
                break
            }
        }
        /*
        var buffer = ByteArray(8192)

        try {
            while (true) {
                val length = mmInStream.read(buffer)
                if (length <= 0) {
                    break
                }
                out.write(buffer, 0, length)
            }
            out.flush()
            return out
        } catch (e:Exception) {
            println(e.message)
        }
        return null


        var begins: Int = 0
        var bytes: Int = 0
        while (true) {
            try {
                bytes += mmInStream.read(buffer, bytes, buffer.size - bytes)
                for (i in begins..bytes) {
                    if (buffer[i] == "#".toByte()) {
                        //mHandler.obtainMessage(1, begins, i, buffer).sendToTarget()
                        begins = i+1
                        if (i==bytes - 1) {
                            bytes = 0
                            begins = 0
                        }
                    }
                }
            } catch (e: IOException) {
                println(e.message)
                break
            }
        } */
    }

    fun Cancel() {
        try {
            mmSocket.close()
        } catch (e: IOException) {
            println(e.message)
        }
    }
}

 */

class StartActivity : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        ////////Connectingg with device///////////

/*

        var mConnectThread : ConnectThread = ConnectThread()
        if (mDevice != null) {
            mConnectThread.ConnectThread(mDevice!!)
            mConnectThread.start()

        }

 */


        //////////////////////User Settings /////////////////////
        val myPreferences =
            PreferenceManager.getDefaultSharedPreferences(this@StartActivity)
        val mAge = myPreferences.getInt("AGE", 25)
        val mTime = myPreferences.getInt("TIME", 15)
        val mIsAlerted = myPreferences.getBoolean("Alert?", false)
        ///////////////////////////////////////////////////////////////////

        var timeMs : Long = 5 * 1000 //to dla ułatwienia
        //var timeMs : Long = (mTime * 1000).toLong() //to ostatecznie bd -już działa !!!!

        //tworzenie wyniku:
            val rndsBPM = (45..150).random()
            val rndsSpO2 = (90..100).random()

        toast("Measuring... Please wait")

        val handler = Handler()
        handler.postDelayed(Runnable {

            textViewBPM.text = "$rndsBPM BPM"
            textViewSpO2.text = "$rndsSpO2 %"
            progressBar.visibility = View.INVISIBLE
            toast("Measurment ended")

            //obliczenie na podstawie wieku czy pomiar w normie, gdy alert = true to poinformowanie użytkownika o tym
            //tętno maksymalne: 208 - 0.7*wiek
            var HRMax:Double = 208 - 0.7 * mAge
            println("Max HR $HRMax")

            if (mIsAlerted == true)
            {
                if (rndsBPM > HRMax) {
                    toast("Your heart rate is too high - medical attention required")
                }
                if (rndsBPM < 50) {
                    toast("Your heart rate is too low - medical attention required")
                }
                if (rndsSpO2 < 94) {
                    toast("Your saturation is too low - medical attention required")
                }
            }


        }, timeMs)
        
        // Dane kt bd zapisywane: bpm, spo2, datapomiaru

        //odczytanie daty dziesiejszej:
        fun Date.toString(format: String, locale : Locale = Locale.getDefault()) : String {
            val formatter = SimpleDateFormat(format, locale)
            return formatter.format(this)
        }

        fun getCurrentDateTime(): Date {
            return  Calendar.getInstance().time
        }

        val date = getCurrentDateTime()
        var newDate = date.toString("yyyy/MM/dd HH:mm:ss")
        println(newDate)

        val myDB = openOrCreateDatabase("my.db", Context.MODE_PRIVATE, null)
        myDB.execSQL(
            "CREATE TABLE IF NOT EXISTS measurements (bpm INT, spo2 INT, date VARCHAR(20))"
        )

        val row1 = ContentValues()
        row1.put("bpm", rndsBPM)
        row1.put("spo2", rndsSpO2)
        row1.put("date", newDate)

        myDB.insert("measurements", null, row1);
        myDB.close()
/*
        val myCursor: Cursor = myDB.rawQuery("select bpm, spo2, date from measurements", null)

        while (myCursor.moveToNext()) {
            val bpm = myCursor.getInt(0)
            val spo2 = myCursor.getInt(1)
            val date = myCursor.getString(2)
            println("Odczytano $bpm, $spo2, $date")
        }

        myCursor.close()
        myDB.close()
*/

/*
        var newBPM : String = "" + rndsBPM
        var newSpo2: String = "" + rndsSpO2
        var dataDB : String = "" + newDate

        //////zapis danych do tabeli////////

        val dbHelper = DataBaseHelper(applicationContext)
        val db = dbHelper.writableDatabase

        val value = ContentValues()
        value.put("BPM", newBPM )
        value.put("SPO2", newSpo2 )
        value.put("DATE", dataDB )
        db.insertOrThrow(TableInfo.TABLE_NAME, null, value)

        toast("Dane zapisano $newBPM, $newSpo2, $dataDB")
*/
/*
        //otwarcie bazy z pomiarami, dodanie pomiaru + data, zamknięcie pliku
        try {


            val myDB = openOrCreateDatabase("my.db", Context.MODE_PRIVATE, null)

            myDB.execSQL(
                "CREATE TABLE IF NOT EXISTS user ( BPM INT, SpO2 INT, MeasurementDate String)"
            )

            val row = ContentValues()

            row.put("BPM", 80)
            row.put("SpO2", 99)
            row.put("MeasurementDate", dataDB)

            myDB.insert("user", null, row)

            myDB.close()
        } catch (e:Exception) { print(e.printStackTrace())}
*/

    }
}

