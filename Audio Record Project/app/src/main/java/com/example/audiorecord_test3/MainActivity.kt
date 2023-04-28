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

    private val audioSource = MediaRecorder.AudioSource.VOICE_COMMUNICATION
    private val sampleRate = 44100
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    private val intRecordSampleRate = AudioTrack.getNativeOutputSampleRate(AudioManager.STREAM_VOICE_CALL)

    private val theRecord: MutableList<Short> = mutableListOf()
    private var compressedByteArray: String = ""


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


        println("theRecord before encoding $theRecord")
        println("theRecord before encoding: ${theRecord.size}")
        sendToServer(theRecord)
        println("theRecord after encoding $theRecord")
        println("theRecord after encoding: ${theRecord.size}")
        //we clear the buffer after sending it
        theRecord.clear()
    }


    private fun playAudio(){
        playRecording()
    }

    @Override
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
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
        audioThread = startTheRecording()

        audioThread!!.start()
    }

    private fun startTheRecording(): Thread {
        return Thread {//The thread prevents the code from blocking on the while loop and thus allows accessing the stop button
            while (isActive) {
                //we read the bytes captured by audioRecord and save them in SHORT FORMAT inside shortAudioData (not bytes)
                audioRecord!!.read(shortAudioData, 0, shortAudioData.size)


                //we add all shortAudioData inside a buffer
                for (element in shortAudioData) {
                    theRecord.add(element)
                }
            }

        }
    }


    private fun sendToServer(record: MutableList<Short>){
        val mongoClient = MongoClient()
        //element that we will send to the server
        compressedByteArray= mongoClient.compressList(record)
        println("this is the length of the String that we send to the server ${compressedByteArray.length}")
    }

    private fun receivedFromServer(compressedString:String):MutableList<Short>{
        val mongoClient = MongoClient()
        return mongoClient.decompressString(compressedString)
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


        val theVoiceToPlay = receivedFromServer(compressedByteArray)

        println("TheVoiceToPlay which is theRecord that the user receive $theVoiceToPlay")
        println("TheVoiceToPlay which is theRecord that the user receive, size: ${theVoiceToPlay.size}")


        val shortAudioDataForPlaying = ShortArray(theVoiceToPlay.size)

        var i=0
        while(i< theVoiceToPlay.size){
            shortAudioDataForPlaying[i]= theVoiceToPlay[i]
            i++
        }

        if (audioTrack?.playState == AudioTrack.PLAYSTATE_PLAYING) {
            audioTrack!!.write(shortAudioDataForPlaying, 0, shortAudioDataForPlaying.size)
        }
    }

}