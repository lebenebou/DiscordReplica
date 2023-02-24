package com.example.audiorecord_test3

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.*
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {

    private var voipCommunication: VoipCommunication? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val startButton = findViewById<Button>(R.id.start_button)
        val stopButton = findViewById<Button>(R.id.stop_button)

        startButton.setOnClickListener { startCommunication() }
        stopButton.setOnClickListener { stopCommunication() }
    }

    private fun startCommunication() {
        voipCommunication = VoipCommunication()
        voipCommunication?.startCommunication()
    }

    private fun stopCommunication() {
        voipCommunication?.stopCommunication()
        voipCommunication = null
    }
}


class VoipCommunication() {
    //Change the value here:
    private val BUFFER_SIZE = 4096
    private val SAMPLE_RATE = 8000

    private val context: Context = TODO()

    private var audioRecord: AudioRecord? = null
    private var audioTrack: AudioTrack? = null



    fun startCommunication() {
        // Configure the audio recorder
        val bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)

        // this test allow AudioRecord to use the mic
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }

        audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize)

        // Create a thread to send data
        Thread {
            val buffer = ByteArray(BUFFER_SIZE)
            audioRecord?.startRecording()

            while (true) {
                val count = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                if (count > 0) {
                    //TODO: send it to the server
                }
            }
        }.start()

        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build())
            .setAudioFormat(AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(SAMPLE_RATE)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build())
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        audioTrack?.play()

        // Create a thread to recive the data and to play
        Thread {
            val buffer = ByteArray(BUFFER_SIZE)
            while (true) {
                //TODO: recive and play the voice using write method
            }
        }.start()
    }


    fun stopCommunication() {
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null

        audioTrack?.stop()
        audioTrack?.release()
        audioTrack = null
    }
}
