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
import ws.schild.jave.*
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
    }

    private fun playAudio(){

        println("LIST OF theRecord: $theRecord")
        println("SIZE OF theRecord: ${theRecord.size}")

        sendToServer(theRecord)

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

        }
    }


    private fun sendToServer(record: MutableList<Short>){
        val encoding = Encoding()
        compressedByteArray= encoding.compressList(record)
        println("compressedByteArray.size ${compressedByteArray.length}")
    }

    private fun receivedFromServer(compressedString:String):MutableList<Short>{
        val encoding = Encoding()
        return encoding.decompressString(compressedString)
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


        val theRecordAfterCompression = receivedFromServer(compressedByteArray)


        val shortAudioDataForPlaying = ShortArray(theRecordAfterCompression.size)

        var i=0
        while(i< theRecordAfterCompression.size){
            shortAudioDataForPlaying[i]= theRecordAfterCompression[i]
            i++
        }

        if (audioTrack?.playState == AudioTrack.PLAYSTATE_PLAYING) {
            audioTrack!!.write(shortAudioDataForPlaying, 0, shortAudioDataForPlaying.size)
        }
    }



/**

    private fun encodeShortListToMp3(audioData: MutableList<Short>): JSONObject {
        val audio = ws.schild.jave.encode.AudioAttributes()
        audio.setCodec("libmp3lame")
        audio.setBitRate(128000)
        audio.setChannels(2)
        audio.setSamplingRate(44100)

        val attrs = EncodingAttributes()
        attrs.setOutputFormat("mp3")
        attrs.setAudioAttributes(audio)

        val encoder = Encoder()

        val encodedData: ByteArray
        val byteArrayInputStream = ByteArrayInputStream(AudioUtils.shortArrayToByteArray(audioData.toShortArray()))
        val multimediaObject = MultimediaObject(byteArrayInputStream)

        try {
            val outputStream = ByteArrayOutputStream()
            encoder.encode(multimediaObject, outputStream, attrs)
            encodedData = outputStream.toByteArray()
        } catch (ex: Exception) {
            ex.printStackTrace()
            return JSONObject()
        }

        val json = JSONObject()
        json.put("data", Base64.encodeToString(encodedData, Base64.DEFAULT))
        return json
    }



    object AudioUtils {
        fun shortArrayToByteArray(data: ShortArray): ByteArray {
            val bytes = ByteArray(data.size * 2)
            for (i in data.indices) {
                bytes[i * 2] = (data[i] and 0x00FF).toByte()
                bytes[i * 2 + 1] = (data[i].toInt() shr 8).toByte()
            }
            return bytes
        }
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
    **/

}