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
    private var isActive = false
    private var audioThread: Thread? = null
    private val deferred = CompletableDeferred<Boolean>()

    private val audioSource = MediaRecorder.AudioSource.VOICE_COMMUNICATION //so we can use earphones
    private val sampleRate = 44100
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    private val intRecordSampleRate = AudioTrack.getNativeOutputSampleRate(AudioManager.STREAM_MUSIC)

    private val theRecord: MutableList<Short> = mutableListOf()
    private var theCompressedRecord: List<Short> = mutableListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val startButton = findViewById<Button>(R.id.button)
        val stopButton = findViewById<Button>(R.id.button2)
        val playButton = findViewById<Button>(R.id.button3)
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
        playButton.setOnClickListener{
            playAudio()
        }
    }


    fun buttonStart() {
        GlobalScope.launch {
            setUpRecording()
        }
    }
    fun buttonStop() {
        isActive= false
        audioTrack?.stop()
        audioRecord?.stop()
        audioRecord?.release()
        audioTrack?.release()
    }

    private fun playAudio(){
        println("LIST OF theRecord: $theRecord")
        println("SIZE OF theRecord: ${theRecord.size}")
        playRecording()
        theRecord.clear()// for testing purpose
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
                    println("ACCESS DENIED, PLEASE ENABLE MICROPHONE SO YOU WILL BE ABLE TO CALL")
                }
            }
        }
    }


    private suspend fun setUpRecording() {
        //we calculate the optimal size of the buffer (7680 bytes)
        intBufferSize = AudioRecord.getMinBufferSize(
            intRecordSampleRate,
            channelConfig,
            audioFormat
        )

        //create an array containing intBufferSize values initialized with 0
        shortAudioData = ShortArray(intBufferSize)


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 0)
            deferred.await() //we suspend the activity till the user answer
        }

        audioRecord = AudioRecord(
            audioSource,
            sampleRate,
            channelConfig,
            audioFormat,
            intBufferSize
        )

        audioRecord!!.startRecording()

        isActive = true
        audioThread = recording()

        audioThread!!.start()
    }

    private fun recording(): Thread {
        return Thread {//The thread prevents the code from blocking on the while loop and thus allows accessing the stop button
            while (isActive) {
                //we read the bytes captured by audioRecord and save them in SHORT FORMAT inside shortAudioData (not bytes)
                audioRecord!!.read(shortAudioData, 0, shortAudioData.size)


                //we add all shortAudioData inside a buffer
                for (element in shortAudioData) {
                    theRecord.add(element)
                }
            }
            println("BEFORE COMPRESSING:")
            sendToServerNotCompressed(theRecord)
            println("AFTER COMPRESSING:")
            theCompressedRecord = compress(theRecord)
            sendToServer(theCompressedRecord)
        }
    }

    fun compress(data: MutableList<Short>): List<Pair<Short, Int>> {
        val compressedData = mutableListOf<Pair<Short, Int>>()
        var prev = data[0]
        var count = 0
        for (i in 1 until data.size) {
            if (data[i] - prev > 1 || data[i] - prev < -1 || count == Short.MAX_VALUE) {
                compressedData.add(Pair(prev, count))
                prev = data[i]
                count = 0
            } else {
                count++
            }
        }
        compressedData.add(Pair(prev, count))
        return compressedData
    }



    private fun sendToServerNotCompressed(record: MutableList<Short>){
        println("SENDING the following record to the server: $record")
        println("TOTAL SIZE of the record sent to the server: ${record.size}")
    }

    private fun sendToServer(record: List<Short>){
        println("SENDING the following COMPRESSED record to the server: $record")
        println("TOTAL SIZE of the COMPRESSED record  sent to the server: ${record.size}")
    }


    private fun playRecording(){
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()

        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(audioAttributes)
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(audioFormat)
                    .setSampleRate(intRecordSampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(intBufferSize)
            .build()

        //Set the playback rate to the sampleRate
        audioTrack!!.playbackRate = intRecordSampleRate

        audioTrack!!.play()

        val shortAudioDataForPlaying = ShortArray(theRecord.size)

        var i=0
        while(i< theRecord.size){
            shortAudioDataForPlaying[i]= theRecord[i]
            i++
        }

        if (audioTrack?.playState == AudioTrack.PLAYSTATE_PLAYING) {
            audioTrack!!.write(shortAudioDataForPlaying, 0, shortAudioDataForPlaying.size)
        }
    }

}