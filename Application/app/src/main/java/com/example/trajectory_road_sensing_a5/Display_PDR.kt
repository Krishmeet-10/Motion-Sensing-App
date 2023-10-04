package com.example.trajectory_road_sensing_a5

import android.content.Context
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlin.math.log
import kotlin.math.pow
import kotlin.math.sqrt

class Display_PDR : AppCompatActivity() ,SensorEventListener{
    private lateinit var nos:TextView
    private lateinit var direction:TextView
    private lateinit var currsts:TextView

    private lateinit var sensorManager: SensorManager
    private lateinit var MagnetometerSensor: Sensor
    private lateinit var AccelerometerSensor: Sensor
    private lateinit var grvsensor: Sensor

    private lateinit var lineChart:LineChart
    private lateinit var lineData: LineData
    private lateinit var set: LineDataSet
    private var lastPlotted = 0

    private var Staircase = false
    private var s1 = false
    private var s2 = false



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_display_pdr)

        val height_inp = intent.getIntExtra("height",0)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        MagnetometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        AccelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        grvsensor = sensorManager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR)

        lineChart = findViewById(R.id.lineChart)

        nos = findViewById(R.id.nosteps)
        direction = findViewById(R.id.direc)
        currsts = findViewById(R.id.cst)

        set = LineDataSet(null, "Steps")
        set.color = Color.BLUE
        set.lineWidth = 3f

        lineData = LineData(set)
        lineChart.data = lineData
        lineChart.description.isEnabled = false
        lineChart.setTouchEnabled(true)
        lineChart.setPinchZoom(true)
        lineChart.isDragEnabled = true
        lineChart.setScaleEnabled(true)
        lineChart.setDrawGridBackground(false)
        val legend = lineChart.legend
        legend.isEnabled = true
        legend.form = Legend.LegendForm.LINE

    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this,MagnetometerSensor)
        sensorManager.unregisterListener(this,AccelerometerSensor)
        sensorManager.unregisterListener(this,grvsensor)
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this,MagnetometerSensor, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this,AccelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, grvsensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    private var temp_val_prev = floatArrayOf(0f, 0f, 0f)
    private var no_of_steps = 0
    private var curr = 0
    private var prev = 0
    private var time_strt: Long = 0
    private var high_time: Long = 0
    private var high_prev: Long = 0

    private var showToast = true
    private var lastToastTime = 0L
    private val toastInterval = 5000
    private var prevAccelerationZ: Double = 0.0
    private var temp = false;

    private var azimuth = 0f

    override fun onSensorChanged(event: SensorEvent?) {

        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER){

            val accelerationX = event.values[0] // Acceleration along the X-axis
            val accelerationY = event.values[1] // Acceleration along the Y-axis
            val accelerationZ = event.values[2] // Acceleration along the Z-axis

            val data = calculateMagnitude(accelerationX, accelerationY, accelerationZ)

            updateStepCount(data)

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
                            Toast.makeText(applicationContext, "You are going in a Lift", Toast.LENGTH_SHORT).show()
                            currsts.text="Currently in a Lift"
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
                            Toast.makeText(applicationContext, "You are going in a Lift", Toast.LENGTH_SHORT).show()
                            s2=true
                            currsts.text="Currently in a Lift"
                            lastToastTime = currentTime
                        }
                        else{
                            s2=false
                        }
                        temp=true;

                    }
                }

                val accelerationMagnitude = sqrt(
                    accelerationX.toDouble().pow(2) + accelerationY.toDouble().pow(2) + accelerationZ.toDouble().pow(2)
                )

                prevAccelerationZ = roundedNumber
            }
        }

        if (event?.sensor?.type == Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR){
            val rotationVector = event.values
            val rotationMatrix = FloatArray(9)
            SensorManager.getRotationMatrixFromVector(rotationMatrix, rotationVector)
            val orientation = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientation)

            azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
            Log.d("azimuth_angle", "$azimuth")

            if (lastPlotted != no_of_steps) {

                val angle: Int = when {
                    azimuth >= -22.5 && azimuth < 23 -> 9
                    azimuth >= 23 && azimuth < 68 -> 18
                    azimuth >= 68 && azimuth < 113 -> 27
                    azimuth >= 113 && azimuth < 158 -> 0
                    azimuth >= 158 || azimuth < -158 -> 27
                    azimuth >= -158 && azimuth < -113 -> 27
                    azimuth >= -113 && azimuth < -68 -> 18
                    azimuth >= -68 && azimuth < -22.5 -> 9
                    else -> 0
                }

                Log.d("Final", "$angle $no_of_steps")

                set.addEntry(Entry(angle.toFloat(), no_of_steps.toFloat()))
                lineData.notifyDataChanged()
                lineChart.notifyDataSetChanged()
            }


            lastPlotted = no_of_steps
        }
    }
    private fun calculateMagnitude(x: Float, y: Float, z: Float): Double {
        return Math.sqrt(x.toDouble().pow(2) + y.toDouble().pow(2) + z.toDouble().pow(2))
    }

    private fun updateStepCount(data: Double) {
        if (data > 10.5f) {
            curr = 1
            if (prev != curr) {
                high_time = System.currentTimeMillis()
                if ((high_time - high_prev) <= 250f) {
                    high_prev = System.currentTimeMillis()
                    return
                }
                high_prev = high_time
                Log.d("STATES:", "$high_prev $high_time")
                no_of_steps++
                s1=true;
            }
            prev = curr
        } else if (data < 10.5f) {
            curr = 0
            prev = curr
            s1=false
        }
        currsts.text="Walking"
        nos.text = "No of Steps : $no_of_steps"
    }

//    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
//        if(s1 && s2){
//            Toast.makeText(this,"You are going in Stairs!!!!",Toast.LENGTH_SHORT).show()
//        }
//        return super.onCreateView(name, context, attrs)
//    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }
}