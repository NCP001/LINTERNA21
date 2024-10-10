package com.example.linterna21

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var cameraManager: CameraManager
    private var cameraId: String? = null
    private var isFlashOn: Boolean = false
    private var handler: Handler = Handler(Looper.getMainLooper())
    private var blinkSpeed: Long = 1000L
    private var blinkRunnable: Runnable? = null

    private val CAMERA_PERMISSION_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val seekBar = findViewById<SeekBar>(R.id.seekBar)
        val flashlightStatus = findViewById<TextView>(R.id.flashlightStatus)

        cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            initializeCamera()
        } else {
            requestCameraPermission()
        }

        // Configura el SeekBar
        seekBar.max = 4 // Cambia el máximo a 4
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val actualSpeed = progress * 25 // Multiplica el progreso por 25
                if (actualSpeed == 0) {
                    stopBlinking()
                    flashlightStatus.text = "Linterna apagada"
                } else {
                    startBlinking(actualSpeed)
                    flashlightStatus.text = "Velocidad de parpadeo: $actualSpeed"
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun initializeCamera() {
        cameraId = cameraManager.cameraIdList[0]
    }

    // Encender/apagar la linterna con cierto parpadeo
    private fun startBlinking(speed: Int) {
        blinkSpeed = (1000L - (speed * 10))

        if (blinkRunnable != null) {
            handler.removeCallbacks(blinkRunnable!!)
        }

        blinkRunnable = object : Runnable {
            override fun run() {
                toggleFlashlight()
                handler.postDelayed(this, blinkSpeed)
            }
        }
        handler.post(blinkRunnable!!)
    }

    private fun stopBlinking() {
        handler.removeCallbacks(blinkRunnable!!)
        turnOffFlashlight()
    }

    private fun toggleFlashlight() {
        if (isFlashOn) {
            turnOffFlashlight()
        } else {
            turnOnFlashlight()
        }
    }

    private fun turnOnFlashlight() {
        cameraId?.let {
            cameraManager.setTorchMode(it, true)
            isFlashOn = true
        }
    }

    private fun turnOffFlashlight() {
        cameraId?.let {
            cameraManager.setTorchMode(it, false)
            isFlashOn = false
        }
    }

    private fun requestCameraPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            Toast.makeText(this, "Se requiere acceso a la cámara para controlar la linterna.", Toast.LENGTH_LONG).show()
        }
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeCamera()
            } else {
                Toast.makeText(this, "Permiso de cámara denegado. No se puede controlar la linterna.", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopBlinking()
    }
}
