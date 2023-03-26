package com.example.audiorecord_test3

import android.Manifest
import android.content.pm.PackageManager
import android.media.*
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {
    private var audioRecord: AudioRecord? = null
    private var audioTrack: AudioTrack? = null
    private var intBufferSize = 0
    private lateinit var shortAudioData: ShortArray
    private var intGain = 1
    private var isActive = false

    //TODO(): quand on refuse de donnee l'acces au microphone, on ne doit pas continuer comme si de rien n'etait
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val startButton = findViewById<Button>(R.id.button)
        val stopButton = findViewById<Button>(R.id.button2)

        startButton.setOnClickListener {
            if(!isActive){
                buttonStart()
                isActive = true

            }else{
                println("You can't click on start, since you are already calling")
            }
        }

        stopButton.setOnClickListener {
            if(isActive){
                buttonStop()
                isActive = false
            }else{
                println("You can't click on stop, since you are not already calling")
            }
        }
    }

    fun buttonStart() {
        println("START RECORDING")
        threadLoop()
    }
    fun buttonStop() {
        println("STOP RECORDING")
        audioTrack?.stop()
        audioRecord?.stop()
        audioRecord?.release()
        audioTrack?.release()
    }


    private fun threadLoop() {
        println("START THREADLOOP")

        val intRecordSampleRate = AudioTrack.getNativeOutputSampleRate(AudioManager.STREAM_MUSIC)
        intBufferSize = AudioRecord.getMinBufferSize(
            intRecordSampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
        )
        shortAudioData = ShortArray(intBufferSize)


        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO), 0)
        }


        //TODO(): adjust the code so it won't continue before getting the permission, provisoire pour ne pas avoir d'erreur
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED){
            println("Start the record")

            val audioSource = MediaRecorder.AudioSource.MIC
            val sampleRate = 44100
            val channelConfig = AudioFormat.CHANNEL_IN_MONO
            val audioFormat = AudioFormat.ENCODING_PCM_16BIT

            audioRecord = AudioRecord(
                audioSource,
                sampleRate,
                channelConfig,
                audioFormat,
                intBufferSize
            )

            audioTrack = AudioTrack(
                AudioManager.STREAM_MUSIC,
                intRecordSampleRate,
                AudioFormat.CHANNEL_IN_STEREO,
                AudioFormat.ENCODING_PCM_16BIT,
                intBufferSize,
                AudioTrack.MODE_STREAM
            )


            audioTrack!!.playbackRate = intRecordSampleRate
            audioRecord!!.startRecording()

            audioTrack!!.play()
            while (true) {
                if (!isActive) break
                audioRecord!!.read(shortAudioData, 0, shortAudioData.size)
                for (i in shortAudioData.indices) {
                    shortAudioData[i] = (shortAudioData[i] * intGain).toShort().coerceIn(Short.MIN_VALUE, Short.MAX_VALUE)
                }
                audioTrack!!.write(shortAudioData, 0, shortAudioData.size)
            }
        }
    }
}

