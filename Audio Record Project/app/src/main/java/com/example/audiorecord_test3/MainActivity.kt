package com.example.audiorecord_test3

import android.Manifest
import android.content.pm.PackageManager
import android.media.*
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

import java.io.File
import ws.schild.jave.encode.EncodingAttributes

import java.io.DataOutputStream
import java.io.FileOutputStream

import ws.schild.jave.Encoder
import ws.schild.jave.MultimediaObject

import java.io.*


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
    private var theReceivedRecord: MutableList<Short> = mutableListOf()


    private val voiceEncodedFile = File(Environment.getExternalStorageDirectory(),"voiceEncodedRecord.mp3")


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
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
            }


            val voiceNotEncodedFile = File(Environment.getExternalStorageDirectory(),"voiceRecord.txt")
            saveShortListToFile(theRecord, voiceNotEncodedFile)


            convertTxtToMp3(voiceNotEncodedFile,voiceEncodedFile)
        }
    }
    private fun saveShortListToFile(shortList: MutableList<Short>, file: File) {
        val outputStream = DataOutputStream(FileOutputStream(file))
        try {
            for (s in shortList) {
                outputStream.writeShort(s.toInt())
            }
        } finally {
            outputStream.close()
        }
    }


    private fun readShortListFromFile(file: File): MutableList<Short> {
        val shortList = mutableListOf<Short>()
        val inputStream = DataInputStream(FileInputStream(file))
        try {
            while (inputStream.available() > 0) {
                val value = inputStream.readShort()
                shortList.add(value)
            }
        } finally {
            inputStream.close()
        }
        return shortList
    }


    private fun convertTxtToMp3(source: File, target: File) {
        try {
            // Audio Attributes
            val audio = ws.schild.jave.encode.AudioAttributes()
            audio.setCodec("libmp3lame")
            audio.setBitRate(128000)
            audio.setChannels(2)
            audio.setSamplingRate(44100)

            // Encoding Attributes
            val attrs = EncodingAttributes()
            attrs.setOutputFormat("mp3")
            attrs.setAudioAttributes(audio)

            // Encode
            val encoder = Encoder()
            encoder.encode(MultimediaObject(source), target, attrs)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
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

///////////////////////////////////////////////////////////////////////

        val voiceDecodedFile = File(Environment.getExternalStorageDirectory(),"voiceDecodedFile.txt")

        convertMp3ToTxt(voiceEncodedFile,voiceDecodedFile)

        theReceivedRecord=readShortListFromFile(voiceDecodedFile)


        val shortAudioDataForPlaying = ShortArray(theReceivedRecord.size)

        var i=0
        while(i< theReceivedRecord.size){
            shortAudioDataForPlaying[i]= theReceivedRecord[i]
            i++
        }

        if (audioTrack?.playState == AudioTrack.PLAYSTATE_PLAYING) {
            audioTrack!!.write(shortAudioDataForPlaying, 0, shortAudioDataForPlaying.size)
        }
    }




    private fun convertMp3ToTxt(mp3File: File, txtFile: File) {
        try {
            // Audio Attributes
            val audio = ws.schild.jave.encode.AudioAttributes()
            audio.setCodec("pcm_s16le")
            audio.setBitRate(16)
            audio.setChannels(1)
            audio.setSamplingRate(16000)

            // Encoding Attributes
            val attrs = EncodingAttributes()
            attrs.setOutputFormat("s16le")
            attrs.setAudioAttributes(audio)

            // Encode
            val encoder = Encoder()
            encoder.encode(MultimediaObject(mp3File), txtFile, attrs)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }


}