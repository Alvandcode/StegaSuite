/**
 * StegaSuite - AudioSteganography
 * © Designed by alvandcode
 * WAV audio LSB steganography - hides data in audio sample LSBs
 */
package com.stegasuite.app

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.CRC32

object AudioSteganography {

    private val MAGIC = byteArrayOf('S'.code.toByte(), 'G'.code.toByte(), 'A'.code.toByte(), '1'.code.toByte())
    private const val HEADER_SIZE = 12

    fun isWav(data: ByteArray): Boolean {
        if (data.size < 12) return false
        return data[0] == 'R'.code.toByte() && data[1] == 'I'.code.toByte() &&
               data[2] == 'F'.code.toByte() && data[3] == 'F'.code.toByte() &&
               data[8] == 'W'.code.toByte() && data[9] == 'A'.code.toByte() &&
               data[10] == 'V'.code.toByte() && data[11] == 'E'.code.toByte()
    }

    fun findDataChunkOffset(data: ByteArray): Int {
        if (data.size < 12) return 44
        var offset = 12
        while (offset + 8 <= data.size) {
            val chunkId = String(data, offset, 4, Charsets.US_ASCII)
            val chunkSize = ByteBuffer.wrap(data, offset + 4, 4).order(ByteOrder.LITTLE_ENDIAN).int
            if (chunkSize < 0 || offset + 8 + chunkSize > data.size) return 44
            if (chunkId == "data") return offset + 8
            offset += 8 + chunkSize + (chunkSize and 1)
        }
        return 44
    }

    fun getAudioInfo(data: ByteArray): String {
        if (!isWav(data)) return "Not a WAV file"
        val buf = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)
        val channels = buf.getShort(22).toInt() and 0xFFFF
        val sampleRate = buf.getInt(24)
        val bitsPerSample = buf.getShort(34).toInt() and 0xFFFF
        val dataOffset = findDataChunkOffset(data)
        val sampleDataSize = data.size - dataOffset
        val durationSec = if (channels > 0 && sampleRate > 0 && bitsPerSample > 0) {
            sampleDataSize.toLong() * 8 / (channels * sampleRate * bitsPerSample)
        } else 0L
        return "${sampleRate}Hz, ${bitsPerSample}bit, ${channels}ch, ${durationSec}s"
    }

    fun capacityBytes(data: ByteArray): Long {
        if (!isWav(data)) return 0L
        val buf = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)
        val channels = buf.getShort(22).toInt() and 0xFFFF
        val bitsPerSample = buf.getShort(34).toInt() and 0xFFFF
        if (channels <= 0 || bitsPerSample <= 0) return 0L
        val dataOffset = findDataChunkOffset(data)
        val sampleDataSize = data.size - dataOffset
        val totalSamples = sampleDataSize / (channels * (bitsPerSample / 8))
        return totalSamples * channels / 8L
    }

    fun maxPayloadBytes(data: ByteArray): Long = capacityBytes(data) - HEADER_SIZE - 50

    fun hide(wavData: ByteArray, payload: ByteArray, fileName: String, password: String?): ByteArray {
        require(isWav(wavData)) { "Not a valid WAV file" }

        val safeName = if (fileName.isBlank()) "file" else fileName
        val nameBytes = safeName.toByteArray(Charsets.UTF_8)
        require(nameBytes.size <= 1024) { "Filename too long" }

        val inner = ByteBuffer.allocate(4 + nameBytes.size + payload.size)
            .putInt(nameBytes.size).put(nameBytes).put(payload).array()

        val encryptedPayload: ByteArray = if (password.isNullOrEmpty()) {
            val crc = CRC32().apply { update(inner) }.value.toInt()
            val crcBytes = ByteBuffer.allocate(4).putInt(crc).array()
            inner + crcBytes
        } else {
            StegaCrypto.encrypt(inner, password)
        }

        val packet = MAGIC + ByteBuffer.allocate(8).putLong(encryptedPayload.size.toLong()).array() + encryptedPayload

        val buf = ByteBuffer.wrap(wavData).order(ByteOrder.LITTLE_ENDIAN)
        val channels = buf.getShort(22).toInt() and 0xFFFF
        val bitsPerSample = buf.getShort(34).toInt() and 0xFFFF
        require(channels > 0 && bitsPerSample > 0) { "Invalid WAV parameters" }

        val bytesPerSample = bitsPerSample / 8
        val dataOffset = findDataChunkOffset(wavData)
        val totalSampleBytes = wavData.size - dataOffset
        val totalSamples = totalSampleBytes / (channels * bytesPerSample)
        val capacityBits = totalSamples * channels.toLong()
        require(packet.size.toLong() * 8L <= capacityBits) {
            "File too large! Audio capacity: ${capacityBytes(wavData)/1024}KB, Needed: ${packet.size/1024}KB"
        }

        val out = wavData.copyOf()
        var bitIndex = 0
        val totalBits = packet.size * 8

        for (i in dataOffset until out.size step bytesPerSample) {
            if (bitIndex >= totalBits) break
            for (ch in 0 until channels) {
                if (bitIndex >= totalBits) break
                val sampleOffset = i + ch * bytesPerSample
                if (sampleOffset + bytesPerSample > out.size) break

                var sample = 0
                for (b in 0 until bytesPerSample) {
                    sample = sample or ((out[sampleOffset + b].toInt() and 0xFF) shl (b * 8))
                }

                val dataBit = (packet[bitIndex / 8].toInt() and 255 shr (7 - (bitIndex % 8))) and 1
                sample = (sample and 0x1.toInt().inv()) or dataBit

                for (b in 0 until bytesPerSample) {
                    out[sampleOffset + b] = ((sample shr (b * 8)) and 0xFF).toByte()
                }
                bitIndex++
            }
        }

        return out
    }

    fun extract(wavData: ByteArray, password: String?): ExtractResult {
        require(isWav(wavData)) { "Not a valid WAV file" }

        val buf = ByteBuffer.wrap(wavData).order(ByteOrder.LITTLE_ENDIAN)
        val channels = buf.getShort(22).toInt() and 0xFFFF
        val bitsPerSample = buf.getShort(34).toInt() and 0xFFFF
        val bytesPerSample = bitsPerSample / 8
        val dataOffset = findDataChunkOffset(wavData)

        val sampleBits = mutableListOf<Int>()
        for (i in dataOffset until wavData.size step bytesPerSample) {
            for (ch in 0 until channels) {
                val sampleOffset = i + ch * bytesPerSample
                if (sampleOffset + bytesPerSample > wavData.size) break
                var sample = 0
                for (b in 0 until bytesPerSample) {
                    sample = sample or ((wavData[sampleOffset + b].toInt() and 0xFF) shl (b * 8))
                }
                sampleBits.add(sample and 1)
            }
        }

        val magicBits = sampleBits.subList(0, 4 * 8).toIntArray()
        val magic = bitsToBytes(magicBits)
        require(magic.contentEquals(MAGIC)) { "No hidden file found in this audio" }

        val headerBits = sampleBits.subList(0, HEADER_SIZE * 8).toIntArray()
        val head = bitsToBytes(headerBits)
        val len = ByteBuffer.wrap(head.copyOfRange(4, 12)).long
        require(len >= 0 && len <= 50L * 1024 * 1024) { "Suspicious length" }

        val needed = (HEADER_SIZE.toLong() + len) * 8L
        require(needed <= sampleBits.size) { "Data corrupted" }

        val allBits = sampleBits.subList(0, needed.toInt()).toIntArray()
        val encPayload = bitsToBytes(allBits.copyOfRange(HEADER_SIZE * 8, allBits.size))

        val inner: ByteArray = if (password.isNullOrEmpty()) {
            require(encPayload.size >= 4) { "Data corrupted" }
            val innerPart = encPayload.copyOfRange(0, encPayload.size - 4)
            val storedCrc = ByteBuffer.wrap(encPayload.copyOfRange(encPayload.size - 4, encPayload.size)).int
            val calcCrc = CRC32().apply { update(innerPart) }.value.toInt()
            require(storedCrc == calcCrc) { "File corrupted or wrong password" }
            innerPart
        } else {
            StegaCrypto.decrypt(encPayload, password)
        }

        require(inner.size >= 4) { "Internal data corrupted" }
        val nameLen = ByteBuffer.wrap(inner.copyOfRange(0, 4)).int
        require(nameLen in 0..1024 && inner.size >= 4 + nameLen) { "Filename corrupted" }
        val fileName = String(inner.copyOfRange(4, 4 + nameLen), Charsets.UTF_8)
        val data = inner.copyOfRange(4 + nameLen, inner.size)
        return ExtractResult(data, fileName)
    }

    private fun bitsToBytes(bits: IntArray): ByteArray {
        require(bits.size % 8 == 0)
        val out = ByteArray(bits.size / 8)
        for (i in out.indices) {
            var v = 0
            for (j in 0..7) v = (v shl 1) or bits[i * 8 + j]
            out[i] = v.toByte()
        }
        return out
    }
}
