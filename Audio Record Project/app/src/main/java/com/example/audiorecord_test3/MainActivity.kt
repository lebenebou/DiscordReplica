package com.example.audiorecord_test3

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.media.*
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.io.File
import java.io.FileOutputStream

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
        voipCommunication = VoipCommunication(this)
        voipCommunication?.startCommunication()
    }

    private fun stopCommunication() {
        voipCommunication?.stopCommunication()
        voipCommunication = null
    }
}


class VoipCommunication(private val context: Context) {
    //Change the value here:
    private val BUFFER_SIZE = 4096
    private val SAMPLE_RATE = 8000

    private var audioRecord: AudioRecord? = null
    private var audioTrack: AudioTrack? = null

    //to test the record audio
    val file = File(context.getExternalFilesDir(null), "Sound_test.raw")
    var outputStream: FileOutputStream? = null
    var isRecording = false


    fun startCommunication() {
        outputStream = FileOutputStream(file)
        // Configure the audio recorder
        val bufferSize = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        // this test allow AudioRecord to use the mic
        if (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(Manifest.permission.RECORD_AUDIO), 0)
            return
        }

        audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize)


        // Create a thread to send data
        Thread {
            val buffer = ByteArray(BUFFER_SIZE)
            isRecording = true
            audioRecord?.startRecording()

            while (isRecording) {
                val count = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                if (count > 0) {
                    val outputStream = this.outputStream
                    outputStream?.write(buffer, 0, count)
                    //TODO: send it to the server
                }
            }
            audioRecord?.release()
            audioRecord = null
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
        isRecording = false

        audioRecord?.stop()
        audioRecord?.let {
            it.release()
            audioRecord = null
        }

        audioTrack?.stop()
        audioTrack?.release()
        audioTrack = null

        outputStream?.flush()
        outputStream?.close()
        outputStream = null

    }
}
