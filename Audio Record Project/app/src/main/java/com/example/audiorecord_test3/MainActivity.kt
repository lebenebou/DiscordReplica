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
    private var intGain = 5
    private var isActive = false
    private var audioThread: Thread? = null
    private var isRecording = false
    private var isPlaying = false

    //TODO(): adapt the values to get the best performance:
    private val audioSource = MediaRecorder.AudioSource.MIC
    private val sampleRate = 44100
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT


    //TODO(): quand on refuse de donnee l'acces au microphone, on ne doit pas continuer comme si de rien n'etait
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val startButton = findViewById<Button>(R.id.button)
        val stopButton = findViewById<Button>(R.id.button2)
        startButton.setOnClickListener {
            if(!isActive){
                isActive = true
                buttonStart()
            }else{
                println("You can't click on start, since you are already calling")
            }
        }
        stopButton.setOnClickListener {
            if(isActive){
                isActive = false
                buttonStop()
            }else{
                println("You can't click on stop, since you are not calling")
            }
        }
    }

    fun buttonStart() {
        println("Button start has been clicked!")
        if (!isRecording) {
            isRecording = true
            if (!isPlaying) {
                isPlaying = true
                threadLoop()
            }
        }
    }
    fun buttonStop() {
        println("Button stop has been clicked!")
        isRecording = false
        isPlaying = false
        Thread.sleep(10) // Ajouter une pause de 10 ms
        audioTrack?.stop()
        audioRecord?.stop()
        audioRecord?.release()
        audioTrack?.release()
    }

    private fun threadLoop() {
        val intRecordSampleRate = AudioTrack.getNativeOutputSampleRate(AudioManager.STREAM_MUSIC)
        //we calculate the optimal size of the buffer (7680 bytes)
        intBufferSize = AudioRecord.getMinBufferSize(
            intRecordSampleRate,
            AudioFormat.CHANNEL_IN_STEREO,
            AudioFormat.ENCODING_PCM_16BIT)

        //create an array containing intBufferSize values initialized with 0
        shortAudioData = ShortArray(intBufferSize)


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO), 0
            )
        }

        //TODO(): adjust the code so it won't continue before getting the permission, provisoire pour ne pas avoir d'erreur,
        // on ne peut pas commencer le call au 1er start, il faut start stop puis restart pr le lancer correctement:
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            audioRecord = AudioRecord(
                audioSource,
                sampleRate,
                channelConfig,
                audioFormat,
                intBufferSize
            )

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()

            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(audioAttributes)
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(intRecordSampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                        .build()
                )
                .setBufferSizeInBytes(intBufferSize)
                .build()

            //Set the playback rate to the sampleRate
            audioTrack!!.playbackRate = intRecordSampleRate


            audioRecord!!.startRecording()
            audioTrack!!.play()

            isActive = true
            audioThread = Thread {//Le thread empeche le code de bloquer sur le while et d'avoir l'acces au bouton stop
                while (isPlaying) {
                    //we read the bytes captured by audioRecord and save them in SHORT FORMAT inside shortAudioData (not bytes)
                    audioRecord!!.read(shortAudioData, 0, shortAudioData.size)

                    println("SHOWING shortAudioData: ${shortAudioData.sliceArray(0..99).contentToString()}")

                    for (i in shortAudioData.indices) {
                        shortAudioData[i] = (shortAudioData[i] * intGain).toShort().coerceIn(Short.MIN_VALUE, Short.MAX_VALUE)
                    }

                    if (audioTrack?.playState == AudioTrack.PLAYSTATE_PLAYING) {
                        audioTrack!!.write(shortAudioData, 0, shortAudioData.size)
                    }
                }
            }
            audioThread!!.start()
        }
    }
}

