package com.example.pulseoximeter

import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.ContentValues
import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.PersistableBundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_start.*
import kotlinx.android.synthetic.main.control_layout.*
import kotlinx.android.synthetic.main.control_layout.progressBar
import kotlinx.android.synthetic.main.control_layout.textViewBPM
import kotlinx.android.synthetic.main.control_layout.textViewSpO2
import org.jetbrains.anko.async
import java.io.IOException
import java.io.InputStream
import java.lang.Exception
import java.util.*
import kotlin.math.roundToInt
import android.os.AsyncTask as AsyncTask
import org.jetbrains.anko.toast
import java.lang.Thread.sleep
import java.text.SimpleDateFormat
import kotlin.properties.Delegates


var  timeMs : Long = 0
var allDataList : List<String>? = null
var isRunning: Boolean = true
var mEdned : Boolean = false
var mBPM: Int = 0
var mSpO2 : Int = 0


class ControlActivity: AppCompatActivity() {
////////////////// dane do połączenia ///////////////////////////////////
    companion object{
        var m_myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        var m_bluetoothSocket: BluetoothSocket? = null
        lateinit var m_progress: ProgressDialog
        lateinit var m_bluetoothAdapter: BluetoothAdapter
        var m_isConnected: Boolean = false
        lateinit var m_adress: String
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.control_layout)

        //////////////////////User Settings ///////////////////////
        val myPreferences =
            PreferenceManager.getDefaultSharedPreferences(this@ControlActivity)
        val mAge = myPreferences.getInt("AGE", 25)
        val mTime = myPreferences.getInt("TIME", 15)
        val mIsAlerted = myPreferences.getBoolean("Alert?", false)
        timeMs = (mTime * 1000).toLong()

        ///////////////////////////////////////////////////////////////////
        m_adress = intent.getStringExtra(SelectDevice.EXTRA_ADRESS).toString()
        ConnectToDevice(this).execute()

        ////////////////////////// Obsługa pomiaru ///////////////////////////////
        buttonControlStart.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            measurementTimer()
            receiveData()

            toast("Measuring... Please wait")
            val handler = Handler()
            handler.postDelayed(Runnable {
                progressBar.visibility = View.INVISIBLE
                checkAndShow(mAge, mIsAlerted)
            }, timeMs)

        }
    }

    //////////////////////// Sprawdzenie wartości, wyświetlanie dla uzytkownika ////////////////////
    fun checkAndShow(mAge: Int, mIsAlerted:Boolean) {
        while(!mEdned) {
            try {

            } catch ( e:Exception) { break}
        }
        textViewBPM.text = "$mBPM BPM"
        textViewSpO2.text = "$mSpO2 %"
        //obliczenie na podstawie wieku czy pomiar w normie, gdy alert = true to poinformowanie użytkownika o tym
        //tętno maksymalne: 208 - 0.7*wiek
        var HRMax:Double = 208 - 0.7 * mAge
        println("Max HR $HRMax")
        if (mIsAlerted == true)
        {
            if (mBPM > HRMax) {
                toast("Your heart rate is too high - medical attention required")
            }
            if (mBPM < 50) {
                toast("Your heart rate is too low - medical attention required")
            }
            if (mSpO2 < 94) {
                toast("Your saturation is too low - medical attention required")
            }
        }
        saveData()

        disconnectDevice()

    }

    ///////////////////////// zapis do bazy danych wartosci ////////////////////////////////////////
    fun saveData() {
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
        row1.put("bpm", mBPM)
        row1.put("spo2", mSpO2)
        row1.put("date", newDate)

        myDB.insert("measurements", null, row1);
        myDB.close()
    }

    ////////////////////////// TIMER odlicza czas pomiaru //////////////////////////////////////////
    private fun measurementTimer() {
        lateinit var countDownTimer: CountDownTimer

        fun startTimer( time_in_ms : Long) {
            countDownTimer = object  : CountDownTimer(time_in_ms, 1000) {
                override fun onFinish() {
                    countDownTimer.cancel()
                    isRunning = false
                    //progressBar.visibility = View.INVISIBLE
                }
                override fun onTick(millisUntilFinished: Long) {
                }
            }.start()
        }
        try {
            startTimer(timeMs)
        } catch (e:Exception) { println(e.message) }

    }

    //////////////////////wywołanie i tworzenie wątku odbierającego dane ///////////////////////////
    private  fun receiveData() {
        if (m_bluetoothSocket != null) {
            println("--------------pobieranie danych----------")
            var mConnectedThread: ConnectedThread = ConnectedThread()
            mConnectedThread.ConnectedThread(m_bluetoothSocket!!)
            mConnectedThread.start()
        }

    }

    ///////////////////////////// rozłączanie urządzenia  //////////////////////////////////////////
    private fun disconnectDevice() {
        //tu będzie się zakańczac połączenie
        if (m_bluetoothSocket != null) {
            try {
                m_bluetoothSocket!!.close()
                m_bluetoothSocket = null
                m_isConnected = false
            } catch (e: IOException) {e.printStackTrace()}
        }
    }

    /////////////////////////////// łączenie z urzadzeniem /////////////////////////////////////////
    private class ConnectToDevice(c: Context) : AsyncTask<Void, Void, String>() {
        private  var connectSuccess : Boolean = true
        private  val context: Context

        init {
            this.context = c
        }
        override fun onPreExecute() {
            super.onPreExecute()
            m_progress = ProgressDialog.show(context, "Conecting...", "wait")
        }
        override fun doInBackground(vararg params: Void?): String {
            try {
                if (m_bluetoothSocket == null || !m_isConnected) {
                    m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                    val device: BluetoothDevice = m_bluetoothAdapter.getRemoteDevice(m_adress)
                    m_bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(m_myUUID)
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
                    m_bluetoothSocket!!.connect()
                }
            }
            catch (e: IOException) {
                connectSuccess = false
                e.printStackTrace()
            }
            return null.toString()
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (!connectSuccess) {
                Log.i("data", "Couldn't connect" )
            }
            else {
                m_isConnected = true
            }
            m_progress.dismiss()
        }

    }

    /////////////////////////////wątek do odbioru informacji ///////////////////////////////////////
    class ConnectedThread : Thread() {
        lateinit var mmSocket: BluetoothSocket
        lateinit var mmInStream: InputStream

        fun ConnectedThread(socket: BluetoothSocket) {
            println("Tworzy watek...")
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

        override fun run() {
            var buffer = ByteArray(1024)
            var bytes: Int
            var readMessage: StringBuilder = StringBuilder()

            while (isRunning) {

                    try {
                        bytes = mmInStream.read(buffer)
                        var read : String = String(buffer, 0 , bytes)
                        readMessage.append(read)
                    } catch (e:IOException) {
                        println(e.message)
                        break
                    }
            }
            Log.i("", "Measurement ended")

            try {
                allDataList = readMessage.split("\n")

            } catch (e:Exception) { println(e.message)}
            measureMeasurements()
            Cancel()
        }

        fun Cancel() {
            println("Koniec wątku")
            try {
                mmSocket.close()
            } catch (e: IOException) {
                println(e.message)
            }
        }
        //////////////obliczenia wartości ///////////////
        fun measureMeasurements()
        {
            var tmpList= mutableListOf<String>()
            var tmpBPMList = mutableListOf<String>()
            var tmpSpO2List  = mutableListOf<String>()
            var resultBPM:Int = 0
            var resultSpo2:Int = 0

            Log.i("all data: ", "$allDataList")

            if (allDataList!=null && allDataList!!.count() != 0) {
                try {
                    if ( allDataList!!.size == 2) { //jesli tylko 1 pomiar zarejestrowano
                        try {
                            var tt = allDataList!![0].split(";")
                            tmpList.add(tt.toString())
                            var t = tmpList[0].split(",")
                            var tb : String = t.get(0).trim('[')
                            var ts:String  = t.get(1).trim(']')
                            resultBPM += tb.toDouble().roundToInt()
                            resultSpo2 += ts.toDouble().roundToInt()
                            Log.i("wynik: ", "bmp: $resultBPM , spo2: $resultSpo2")
                        } catch ( e:Exception) {
                            println(e.message)}
                    }
                    else { //wiecej wartości - obliczanie
                        for (i in 0..allDataList!!.size-2) {
                            var t = allDataList!![i].split(";")
                            tmpList.add(t.toString())
                        }
                        Log.i("tmp:", "$tmpList, dl: ${tmpBPMList.size}")

                        for (i in 1..tmpList.size - 1) {

                            //if(tmpList!![i].contains("SUCCESS") ) {
                            //    tmpList!!.removeAt(i)
                            //}

                            var t = tmpList[i].split(",")
                            var tb : String = t.get(0).trim('[')
                            var ts:String  = t.get(1).trim(']')

                            try {
                                tmpBPMList.add(tb)
                                tmpSpO2List.add(ts)
                            } catch ( e:Exception) {
                                println(e.message)}

                        }
                        Log.i("po obciaciu:", "$tmpList, dl: ${tmpBPMList.size}")
                        for (i in 0..tmpBPMList.size - 1) {
                            resultBPM += tmpBPMList[i].toDouble().roundToInt()
                            resultSpo2 += tmpSpO2List[i].toDouble().roundToInt()
                        }
                        Log.i("dane", "bpm: $tmpBPMList, spo2: $tmpSpO2List")
                        resultBPM = ( resultBPM.toDouble() / (tmpBPMList.count()) ).roundToInt()
                        resultSpo2 = ( resultSpo2.toDouble() / (tmpSpO2List.count()) ).roundToInt()

                        mBPM = resultBPM
                        mSpO2 = resultSpo2
                        Log.i("ostateczny wynik ", "bmp: $mBPM , spo2: $mSpO2")
                        mEdned = true
                    }
                } catch (e:Exception) {
                    mBPM = 0
                    mSpO2 = 0
                    mEdned = true
                    var M = e.message
                    Log.i("blad!", "$M")}
            }
            else { Log.i("blad!", "Brak pomiarów?") }
        }
    }
}