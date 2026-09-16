/**
 * StegaSuite - FileSteganography
 * © Designed by alvandcode
 * Generic binary file LSB steganography - works with any format
 * Skips file headers to preserve file integrity
 */
package com.stegasuite.app

import java.nio.ByteBuffer
import java.util.zip.CRC32

object FileSteganography {

    private val MAGIC = byteArrayOf('S'.code.toByte(), 'G'.code.toByte(), 'F'.code.toByte(), '1'.code.toByte())
    private const val HEADER_SIZE = 12

    fun capacityBytes(data: ByteArray): Long {
        val safeStart = getSafeOffset(data)
        return (data.size.toLong() - safeStart) / 8L
    }

    fun maxPayloadBytes(data: ByteArray): Long = capacityBytes(data) - HEADER_SIZE - 50

    fun hide(carrierData: ByteArray, payload: ByteArray, fileName: String, password: String?): ByteArray {
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
        val safeStart = getSafeOffset(carrierData)
        val capacityBits = (carrierData.size.toLong() - safeStart)
        require(packet.size.toLong() * 8L <= capacityBits) {
            "File too large! Capacity: ${capacityBytes(carrierData)/1024}KB, Needed: ${packet.size/1024}KB"
        }

        val out = carrierData.copyOf()
        var bitIndex = 0
        val totalBits = packet.size * 8

        for (i in safeStart until out.size) {
            if (bitIndex >= totalBits) break
            val byteVal = out[i].toInt() and 255
            val dataBit = (packet[bitIndex / 8].toInt() and 255 shr (7 - (bitIndex % 8))) and 1
            out[i] = ((byteVal and 0xFE) or dataBit).toByte()
            bitIndex++
        }

        return out
    }

    fun extract(carrierData: ByteArray, password: String?): ExtractResult {
        val safeStart = getSafeOffset(carrierData)
        val magicBits = readBits(carrierData, safeStart, 4 * 8)
        val magic = bitsToBytes(magicBits)

        require(magic.contentEquals(MAGIC)) { "No hidden file found in this file" }

        val headerBits = readBits(carrierData, safeStart, HEADER_SIZE * 8)
        val head = bitsToBytes(headerBits)
        val len = ByteBuffer.wrap(head.copyOfRange(4, 12)).long
        require(len >= 0 && len <= 50L * 1024 * 1024) { "Suspicious length" }

        val needed = (HEADER_SIZE.toLong() + len) * 8L
        require(needed <= (carrierData.size.toLong() - safeStart) * 8L) { "Data corrupted" }

        val all = readBits(carrierData, safeStart, needed.toInt())
        val encPayload = bitsToBytes(all.copyOfRange(HEADER_SIZE * 8, all.size))

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

    fun getSafeOffset(data: ByteArray): Long {
        if (data.size < 4) return 0L

        val b0 = data[0].toInt() and 0xFF
        val b1 = data[1].toInt() and 0xFF
        val b2 = data[2].toInt() and 0xFF
        val b3 = data[3].toInt() and 0xFF

        if (b0 == 0xFF && b1 == 0xD8) return findJpegContentStart(data)
        if (b0 == 0x25 && b1 == 0x25) return findPdfContentStart(data)
        if (b0 == 0x50 && b1 == 0x4B && b2 == 0x03 && b3 == 0x04) return 30L
        if (b0 == 0x52 && b1 == 0x69 && b2 == 0x63 && b3 == 0x68) return 12L
        if (b0 == 0x1A && b1 == 0x45 && b2 == 0xDF && b3 == 0xA3) return 4L
        if (b0 == 0x47 && b1 == 0x49 && b2 == 0x46) return 13L
        if (b0 == 0x49 && b1 == 0x49 && b2 == 0x2A && b3 == 0x00) return 8L
        if (b0 == 0x4D && b1 == 0x4D && b2 == 0x00 && b3 == 0x2A) return 8L

        return 4L
    }

    private fun findJpegContentStart(data: ByteArray): Long {
        var i = 2
        while (i < data.size - 1) {
            if (data[i].toInt() and 0xFF == 0xFF) {
                val marker = data[i + 1].toInt() and 0xFF
                if (marker == 0xD9) return (i + 2).toLong()
                if (marker == 0xDA) return (i + 2).toLong()
                if (marker in 0xC0..0xCF && marker != 0xC4 && marker != 0xC8 && marker != 0xCC) {
                    if (i + 3 < data.size) {
                        val len = ((data[i + 2].toInt() and 0xFF) shl 8) or (data[i + 3].toInt() and 0xFF)
                        i += 2 + len
                        continue
                    }
                }
                if (marker == 0xE1 || marker == 0xE0 || marker == 0xFE || (marker >= 0xE2 && marker <= 0xEF)) {
                    if (i + 3 < data.size) {
                        val len = ((data[i + 2].toInt() and 0xFF) shl 8) or (data[i + 3].toInt() and 0xFF)
                        i += 2 + len
                        continue
                    }
                }
                i += 2
            } else {
                i++
            }
        }
        return i.toLong()
    }

    private fun findPdfContentStart(data: ByteArray): Long {
        val headerEnd = minOf(1024L, data.size.toLong())
        var i = 0L
        while (i < headerEnd - 1) {
            if (data[i.toInt()] == '%'.code.toByte() && data[(i + 1).toInt()] == 'E'.code.toByte() &&
                i + 4 < data.size &&
                data[(i + 2).toInt()] == 'O'.code.toByte() && data[(i + 3).toInt()] == 'F'.code.toByte()) {
                return i
            }
            i++
        }
        return headerEnd
    }

    fun readBits(data: ByteArray, startOffset: Long, count: Int): IntArray {
        val result = IntArray(count)
        var idx = 0
        var byteIdx = startOffset.toInt()
        while (byteIdx < data.size && idx < count) {
            val b = data[byteIdx].toInt() and 255
            for (bit in 7 downTo 0) {
                if (idx >= count) break
                result[idx++] = (b shr bit) and 1
            }
            byteIdx++
        }
        return result
    }

    fun bitsToBytes(bits: IntArray): ByteArray {
        require(bits.size % 8 == 0) { "Bit count must be multiple of 8" }
        val out = ByteArray(bits.size / 8)
        for (i in out.indices) {
            var v = 0
            for (j in 0..7) v = (v shl 1) or bits[i * 8 + j]
            out[i] = v.toByte()
        }
        return out
    }
}
