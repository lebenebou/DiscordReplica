package com.example.audiorecord_test3

import android.Manifest
import android.content.pm.PackageManager
import android.media.*
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private var audioRecord: AudioRecord? = null
    private var audioTrack: AudioTrack? = null
    private var intBufferSize = 0
    private lateinit var shortAudioData: ShortArray
    private var intGain = 1
    private var isActive = false
    private var audioThread: Thread? = null
    private val deferred = CompletableDeferred<Boolean>()

    private val audioSource = MediaRecorder.AudioSource.VOICE_COMMUNICATION //so we can use earphones
    private val sampleRate = 44100
    private val channelConfig = AudioFormat.CHANNEL_IN_STEREO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val startButton = findViewById<Button>(R.id.button)
        val stopButton = findViewById<Button>(R.id.button2)
        startButton.setOnClickListener {
            println(isActive)
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
        GlobalScope.launch {
            startCommunication()
        }
    }
    fun buttonStop() {
        isActive= false
        audioTrack?.stop()
        audioRecord?.stop()
        audioRecord?.release()
        audioTrack?.release()
    }

    @Override
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            0 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //to continue the code when the user check permission
                    deferred.complete(true)

                } else {
                    buttonStop()
                    println("ACCES DENIED, PLEASE ENABLE MICROPHONE SO YOU WILL BE ABLE TO CALL")
                }
            }
        }
    }


    private suspend fun startCommunication() {
        val intRecordSampleRate = AudioTrack.getNativeOutputSampleRate(AudioManager.STREAM_MUSIC)
        //we calculate the optimal size of the buffer (7680 bytes)
        intBufferSize = AudioRecord.getMinBufferSize(
            intRecordSampleRate,
            AudioFormat.CHANNEL_IN_STEREO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        //create an array containing intBufferSize values initialized with 0
        shortAudioData = ShortArray(intBufferSize)


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 0)
            deferred.await() //we suspend the activity till the user answer
        }

        println("ENTERING HERE")
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
        audioThread = threadLoop()
        audioThread!!.start()
    }


    private fun threadLoop(): Thread? {
        return Thread {//Le thread empeche le code de bloquer sur le while et d'avoir l'acces au bouton stop
            while (isActive) {
                //we read the bytes captured by audioRecord and save them in SHORT FORMAT inside shortAudioData (not bytes)
                audioRecord!!.read(shortAudioData, 0, shortAudioData.size)

                println("SHOWING shortAudioData: ${shortAudioData.sliceArray(0..99).contentToString()}")

                //to amplify the sound, does not have sense if intGain =1
                for (i in shortAudioData.indices) {
                    shortAudioData[i] = (shortAudioData[i] * intGain).toShort()
                        .coerceIn(Short.MIN_VALUE, Short.MAX_VALUE)
                }

                println("SHOWING shortAudioData after using gain: ${shortAudioData.sliceArray(0..99).contentToString()}")

                if (audioTrack?.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    audioTrack!!.write(shortAudioData, 0, shortAudioData.size)
                }
            }
        }
    }


}

