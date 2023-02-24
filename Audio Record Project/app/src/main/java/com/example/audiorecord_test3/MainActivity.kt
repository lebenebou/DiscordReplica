package com.example.audiorecord_test3
import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import androidx.core.app.ActivityCompat
import java.io.File
import java.io.FileOutputStream


class MainActivity : AppCompatActivity() {

    private lateinit var startButton: Button
    private lateinit var stopButton: Button

    private var isRecording: Boolean = false

    private lateinit var recorder: AudioRecord
    private var bufferSize = 0 // Déclaration de bufferSize
    private val sampleRate: Int = 44100

    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startButton = findViewById(R.id.start_button)
        stopButton = findViewById(R.id.stop_button)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }

        recorder = AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channelConfig, audioFormat,
            AudioRecord.getMinBufferSize(sampleRate, channelConfig,audioFormat))

        startButton.setOnClickListener {
            startRecording()
        }
        stopButton.setOnClickListener {
            stopRecording()
        }
    }

    private fun startRecording() {
        isRecording = true
        recorder.startRecording()

        val data = ByteArray(bufferSize)

        //to put the bytes in a file
        val file = File(filesDir, "test.pcm")
        val fos = FileOutputStream(file)

        Thread {
            while (isRecording) {
                recorder.read(data, 0, data.size)
                fos.write(data)
            }
            fos.close()
        }.start()
    }


    private fun stopRecording() {
        isRecording = false
        recorder.stop()
        recorder.release()
    }

}
