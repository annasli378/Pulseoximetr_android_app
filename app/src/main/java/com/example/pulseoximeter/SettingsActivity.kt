package com.example.pulseoximeter

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.BaseColumns
import android.text.Editable
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_settings.*
import org.jetbrains.anko.toast



class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        //odebranie wieku i czasu, zmiana -> zapis zmiany ponownie do pliku z ustawieniami
        //
        val myPreferences =
            PreferenceManager.getDefaultSharedPreferences(this@SettingsActivity)
        val myEditor = myPreferences.edit()

        val age = myPreferences.getInt("AGE", 25)
        val time = myPreferences.getInt("TIME", 15)
        val isAlerted = myPreferences.getBoolean("Alert?", false)

        var myAge: Int = age
        var myTime: Int = time
        var alertMe: Boolean = isAlerted

        editTextAge.hint = "" + age
        editTextTime.hint = "" + time

        if (isAlerted == true) {
            checkBoxAlert.isChecked = true
        }

        buttonSave.setOnClickListener() {
            try {
                if (checkBoxAlert.isChecked) {
                    alertMe = true
                    toast("Alert checked")
                }
                else {
                    alertMe = false
                }

                if(editTextAge.text.toString() != "" || editTextTime.text.toString()!= "" ) {
                    myAge = editTextAge.text.toString().toInt()
                    myTime = editTextTime.text.toString().toInt()

                    if (myAge <= 0 || myAge > 120) {
                        myAge = 25
                    }
                    if (myTime < 15 || myTime > 90) {
                        myTime = 30
                    }
                }
                else {
                    toast("No changes made")
                    myAge = myAge
                    myTime = myTime

                }
                myEditor.putInt("AGE", myAge)
                myEditor.putInt("TIME", myTime)
                myEditor.putBoolean("Alert?", alertMe)
                myEditor.commit()

                toast( "Your age: $myAge, Your Time: $myTime")
            }
            catch (e: Exception ){
                print(e.printStackTrace())
                toast("Error")
            }
        }






    }
}