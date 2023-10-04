package com.example.trajectory_road_sensing_a5

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import java.math.BigDecimal
import java.math.RoundingMode
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import kotlin.math.pow
import kotlin.math.sqrt


class Lift_Detection : AppCompatActivity() , SensorEventListener{

    private lateinit var sensorManager: SensorManager
    private lateinit var MagnetometerSensor: Sensor
    private lateinit var AccelerometerSensor: Sensor
    private var prevAccelerationZ: Double = 0.0
    private var temp = false;
    private lateinit var zaccl:TextView

    private var showToast = true
    private var lastToastTime = 0L
    private val toastInterval = 5000 // Interval in milliseconds (5 seconds)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lift_detection)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        MagnetometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        AccelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        zaccl = findViewById(R.id.ztxt)
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this,MagnetometerSensor, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this,AccelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this,MagnetometerSensor)
        sensorManager.unregisterListener(this,AccelerometerSensor)
    }


    override fun onSensorChanged(event: SensorEvent?) {
//        if (event?.sensor?.type == Sensor.TYPE_MAGNETIC_FIELD) {
//            val accelerationX = event.values[0] // Acceleration along the X-axis
//            val accelerationY = event.values[1] // Acceleration along the Y-axis
//            val accelerationZ = event.values[2] // Acceleration along the Z-axis
//
//            val roundedNumber = (Math.round(accelerationZ * 10) / 10.0)
//
//            if (roundedNumber != prevAccelerationZ) {
//                Log.i("wrr_grp", roundedNumber.toString())
//
//
//                if (roundedNumber - prevAccelerationZ >5) {
//                    Toast.makeText(applicationContext, "Lift going Down!", Toast.LENGTH_SHORT).show()
//                } else if (roundedNumber - prevAccelerationZ<-5) {
//                    Toast.makeText(applicationContext, "Lift going UP!", Toast.LENGTH_SHORT).show()
//                }
//
//                val accelerationMagnitude = sqrt(
//                    accelerationX.toDouble().pow(2) + accelerationY.toDouble().pow(2) + accelerationZ.toDouble().pow(2)
//                )
//
//                zaccl.text = "Z-Acceleration: $accelerationMagnitude"
//                prevAccelerationZ = roundedNumber
//            }
//        }

        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val accelerationX = event.values[0] // Acceleration along the X-axis
            val accelerationY = event.values[1] // Acceleration along the Y-axis
            val accelerationZ = event.values[2] // Acceleration along the Z-axis

            val roundedNumber = Math.round(accelerationZ * 10) / 10.0

            if (roundedNumber != prevAccelerationZ) {
                Log.i("wrr_grp", roundedNumber.toString())
                Log.i("values_wrr", "$accelerationX $accelerationY $accelerationZ")

                // Compare the change in Z-axis acceleration with a threshold (e.g., 5)
                if (roundedNumber - prevAccelerationZ > 2) {
                    val currentTime = System.currentTimeMillis()
                    val timeDifference = currentTime - lastToastTime

                    // Show the toast only if the last toast was shown more than 5 seconds ago
                    if (timeDifference >= 3000) {
                        if(temp==true){
                            Toast.makeText(applicationContext, "Lift going Down!", Toast.LENGTH_SHORT).show()
                            lastToastTime = currentTime
                        }
                        temp=true;
                    }
                } else if (roundedNumber - prevAccelerationZ < -2) {
                    val currentTime = System.currentTimeMillis()
                    val timeDifference = currentTime - lastToastTime

                    // Show the toast only if the last toast was shown more than 5 seconds ago
                    if (timeDifference >= 3000) {
                        if(temp==true){
                            Toast.makeText(applicationContext, "Lift going UP!", Toast.LENGTH_SHORT).show()
                            lastToastTime = currentTime
                        }
                        temp=true;

                    }
                }

                val accelerationMagnitude = sqrt(
                    accelerationX.toDouble().pow(2) + accelerationY.toDouble().pow(2) + accelerationZ.toDouble().pow(2)
                )

                zaccl.text = "Z-Acceleration: $accelerationMagnitude"
                prevAccelerationZ = roundedNumber
            }
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }
}


