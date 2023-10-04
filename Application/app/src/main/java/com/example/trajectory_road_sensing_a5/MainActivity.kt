package com.example.trajectory_road_sensing_a5

import android.content.Context
import android.content.Context.*
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService

class MainActivity : AppCompatActivity() {
    private lateinit var btn:Button
    private lateinit var inp_h:EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        inp_h = findViewById(R.id.height_inp);

        btn = findViewById(R.id.strt);

        //val isEmpty: Boolean



        btn.setOnClickListener {
            var h = inp_h.text.toString()
            var height:Int=h.toInt()
            intent = Intent(this, Display_PDR::class.java)
            intent.putExtra("height",height)
            startActivity(intent)

//            isEmpty: Boolean = inp_h.text.toString().isEmpty()
//            if (isEmpty) {
//                // EditText is empty
//            } else {
//                // EditText is not empty
//            }


        }

    }
}