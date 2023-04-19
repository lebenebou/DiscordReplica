package com.example.audiorecord_test3

import android.util.Base64
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

class Encoding {
    fun compressList(list: MutableList<Short>): String {
        // Convert the list to a delta-encoded byte array
        val deltaEncoded = deltaEncode(list)
        // Compress the byte array using GZIP
        val compressed = compress(deltaEncoded)
        // Encode the compressed byte array as a base64 string
        return Base64.encodeToString(compressed, Base64.DEFAULT)
        // Truncate the string to a maximum length of 1.2 million characters
    }

    private fun compress(data: ByteArray): ByteArray {
        val baos = ByteArrayOutputStream()
        GZIPOutputStream(baos).use { gzip -> gzip.write(data) }
        return baos.toByteArray()
    }

    private fun deltaEncode(arr: MutableList<Short>): ByteArray {
        val result = IntArray(arr.size)
        var prev: Short = 0
        for (i in arr.indices) {
            result[i] = arr[i].toInt() - prev.toInt()
            prev = arr[i]
        }
        val byteBuffer = ByteBuffer.allocate(result.size * 4)
        byteBuffer.asIntBuffer().put(result)
        return byteBuffer.array()
    }


    fun decompressString(compressedString: String): MutableList<Short> {
        // Decode the base64 string to a compressed byte array
        val compressed = Base64.decode(compressedString, Base64.DEFAULT)
        // Decompress the byte array using GZIP
        val deltaEncoded = decompress(compressed)
        // Convert the delta-encoded byte array to a list of shorts
        return deltaDecode(deltaEncoded)
    }

    private fun decompress(compressed: ByteArray): ByteArray {
        val bais = ByteArrayInputStream(compressed)
        return GZIPInputStream(bais).use { it.readBytes() }
    }

    private fun deltaDecode(data: ByteArray): MutableList<Short> {
        val byteBuffer = ByteBuffer.wrap(data)
        val result = mutableListOf<Short>()
        var prev: Short = 0
        for (i in 0 until byteBuffer.remaining() / 4) {
            val value = byteBuffer.getInt()
            result.add((prev + value).toShort())
            prev = (prev + value).toShort()
        }
        return result
    }



}