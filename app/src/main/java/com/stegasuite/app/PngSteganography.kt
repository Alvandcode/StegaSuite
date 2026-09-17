/**
 * StegaSuite - PngSteganography
 * © Designed by alvandcode - https://github.com/Alvandcode
 * Unified steganography engine - supports PNG, BMP, TIFF, WebP, WAV, and generic files
 */
package com.stegasuite.app

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.nio.ByteBuffer
import java.util.zip.CRC32

data class ExtractResult(val bytes: ByteArray, val fileName: String)

enum class CarrierType { PNG, BMP, TIFF, WEBP, WAV, GENERIC, UNKNOWN }

object PngSteganography {
    private val MAGIC_V1 = byteArrayOf('S'.code.toByte(), 'G'.code.toByte(), 'P'.code.toByte(), '1'.code.toByte())
    private val MAGIC_V2 = byteArrayOf('S'.code.toByte(), 'G'.code.toByte(), 'P'.code.toByte(), '2'.code.toByte())
    private const val HEADER_V1 = 12
    private const val HEADER_V2 = 12

    fun detectCarrierType(fileName: String, data: ByteArray): CarrierType {
        val lower = fileName.lowercase()
        return when {
            data.size >= 4 && AudioSteganography.isWav(data) -> CarrierType.WAV
            lower.endsWith(".png") -> CarrierType.PNG
            lower.endsWith(".bmp") -> CarrierType.BMP
            lower.endsWith(".tiff") || lower.endsWith(".tif") -> CarrierType.TIFF
            lower.endsWith(".webp") -> CarrierType.WEBP
            lower.endsWith(".wav") -> CarrierType.WAV
            lower.endsWith(".mp3") || lower.endsWith(".flac") || lower.endsWith(".ogg") ||
            lower.endsWith(".m4a") || lower.endsWith(".aac") -> CarrierType.GENERIC
            lower.endsWith(".mp4") || lower.endsWith(".avi") || lower.endsWith(".mkv") ||
            lower.endsWith(".mov") || lower.endsWith(".webm") -> CarrierType.GENERIC
            lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".gif") ||
            lower.endsWith(".svg") -> CarrierType.GENERIC
            lower.endsWith(".txt") || lower.endsWith(".pdf") || lower.endsWith(".doc") ||
            lower.endsWith(".docx") || lower.endsWith(".xlsx") || lower.endsWith(".pptx") ||
            lower.endsWith(".csv") || lower.endsWith(".json") || lower.endsWith(".xml") ||
            lower.endsWith(".md") -> CarrierType.GENERIC
            lower.endsWith(".zip") || lower.endsWith(".rar") || lower.endsWith(".7z") ||
            lower.endsWith(".tar") || lower.endsWith(".gz") -> CarrierType.GENERIC
            lower.endsWith(".js") || lower.endsWith(".py") || lower.endsWith(".ts") ||
            lower.endsWith(".html") || lower.endsWith(".css") -> CarrierType.GENERIC
            else -> CarrierType.GENERIC
        }
    }

    fun getCarrierDescription(type: CarrierType): String = when (type) {
        CarrierType.PNG -> "PNG Image (lossless, recommended)"
        CarrierType.BMP -> "BMP Image (lossless)"
        CarrierType.TIFF -> "TIFF Image (lossless)"
        CarrierType.WEBP -> "WebP Image (lossless)"
        CarrierType.WAV -> "WAV Audio (lossless)"
        CarrierType.GENERIC -> "Generic File (may lose data in lossy formats)"
        CarrierType.UNKNOWN -> "Unknown Format"
    }

    fun capacityBytesForType(type: CarrierType, data: ByteArray): Long = when (type) {
        CarrierType.PNG, CarrierType.BMP -> {
            val bmp = BitmapFactory.decodeByteArray(data, 0, data.size)
            if (bmp != null) {
                val cap = (bmp.width.toLong() * bmp.height * 3L) / 8L
                bmp.recycle()
                cap
            } else 0L
        }
        CarrierType.WAV -> AudioSteganography.capacityBytes(data)
        else -> FileSteganography.capacityBytes(data)
    }

    fun capacityBytes(bitmap: Bitmap): Long = (bitmap.width.toLong() * bitmap.height * 3L) / 8L
    fun maxPayloadBytes(bitmap: Bitmap): Long = capacityBytes(bitmap) - HEADER_V2 - 50

    fun hide(bitmap: Bitmap, original: ByteArray, fileName: String, password: String?): Bitmap {
        val safeName = if (fileName.isBlank()) "file" else fileName
        val nameBytes = safeName.toByteArray(Charsets.UTF_8)
        require(nameBytes.size <= 1024) { "Filename too long" }

        val inner = ByteBuffer.allocate(4 + nameBytes.size + original.size)
            .putInt(nameBytes.size).put(nameBytes).put(original).array()

        val payload: ByteArray = if (password.isNullOrEmpty()) {
            val crc = CRC32().apply { update(inner) }.value.toInt()
            val crcBytes = ByteBuffer.allocate(4).putInt(crc).array()
            inner + crcBytes
        } else {
            StegaCrypto.encrypt(inner, password)
        }

        val packet = MAGIC_V2 + ByteBuffer.allocate(8).putLong(payload.size.toLong()).array() + payload

        val opaqueBmp = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(opaqueBmp)
        canvas.drawColor(android.graphics.Color.WHITE)
        canvas.drawBitmap(bitmap, 0f, 0f, null)

        val capacityBits = opaqueBmp.width.toLong() * opaqueBmp.height * 3L
        require(packet.size.toLong() * 8L <= capacityBits) {
            "File too large! Capacity: ${capacityBytes(opaqueBmp)/1024}KB, Needed: ${packet.size/1024}KB"
        }

        val out = opaqueBmp.copy(Bitmap.Config.ARGB_8888, true)
        val w = out.width; val h = out.height
        val pixels = IntArray(w * h)
        out.getPixels(pixels, 0, w, 0, 0, w, h)

        var bitIndex = 0
        val totalBits = packet.size * 8
        for (i in pixels.indices) {
            if (bitIndex >= totalBits) break
            val c = pixels[i]
            val a = c and -0x1000000
            val alphaVal = (a shr 24) and 0xFF
            var r = (c shr 16) and 255
            var g = (c shr 8) and 255
            var b = c and 255

            if (alphaVal != 0 && alphaVal != 255) {
                r = (r * 255 + alphaVal / 2) / alphaVal
                g = (g * 255 + alphaVal / 2) / alphaVal
                b = (b * 255 + alphaVal / 2) / alphaVal
            }

            val channels = intArrayOf(r, g, b)
            for (ch in 0..2) {
                if (bitIndex >= totalBits) break
                val byteVal = packet[bitIndex / 8].toInt() and 255
                val bit = (byteVal shr (7 - (bitIndex % 8))) and 1
                channels[ch] = (channels[ch] and 0xFE) or bit
                bitIndex++
            }

            if (alphaVal != 0 && alphaVal != 255) {
                channels[0] = (channels[0] * alphaVal + 127) / 255
                channels[1] = (channels[1] * alphaVal + 127) / 255
                channels[2] = (channels[2] * alphaVal + 127) / 255
            }
            pixels[i] = a or (channels[0] shl 16) or (channels[1] shl 8) or channels[2]
        }
        out.setPixels(pixels, 0, w, 0, 0, w, h)
        opaqueBmp.recycle()
        return out
    }

    fun extract(bitmap: Bitmap, password: String?): ExtractResult {
        val magicBits = readBits(bitmap, 4 * 8)
        val magic = bitsToBytes(magicBits)

        if (magic.contentEquals(MAGIC_V1)) {
            val headerBits = readBits(bitmap, HEADER_V1 * 8)
            val head = bitsToBytes(headerBits)
            val len = ByteBuffer.wrap(head.copyOfRange(4, 12)).long
            require(len >= 0 && len <= Int.MAX_VALUE) { "Invalid length" }
            val needed = (HEADER_V1.toLong() + len) * 8L
            require(needed <= bitmap.width.toLong() * bitmap.height * 3L) { "Data corrupted" }
            val all = readBits(bitmap, needed.toInt())
            val payload = bitsToBytes(all.copyOfRange(HEADER_V1 * 8, all.size))
            val data = if (password.isNullOrEmpty()) payload else StegaCrypto.decrypt(payload, password)
            return ExtractResult(data, "recovered_file")
        }

        require(magic.contentEquals(MAGIC_V2)) { "No hidden file found" }

        val headerBits = readBits(bitmap, HEADER_V2 * 8)
        val head = bitsToBytes(headerBits)
        val len = ByteBuffer.wrap(head.copyOfRange(4, 12)).long
        require(len >= 0 && len <= 50L * 1024 * 1024) { "Suspicious length" }
        val needed = (HEADER_V2.toLong() + len) * 8L
        require(needed <= bitmap.width.toLong() * bitmap.height * 3L) { "Data corrupted" }
        val all = readBits(bitmap, needed.toInt())
        val payload = bitsToBytes(all.copyOfRange(HEADER_V2 * 8, all.size))

        val inner: ByteArray = if (password.isNullOrEmpty()) {
            require(payload.size >= 4) { "Data corrupted" }
            val innerPart = payload.copyOfRange(0, payload.size - 4)
            val storedCrc = ByteBuffer.wrap(payload.copyOfRange(payload.size - 4, payload.size)).int
            val calcCrc = CRC32().apply { update(innerPart) }.value.toInt()
            require(storedCrc == calcCrc) { "File corrupted or wrong password" }
            innerPart
        } else {
            StegaCrypto.decrypt(payload, password)
        }

        require(inner.size >= 4) { "Internal data corrupted" }
        val nameLen = ByteBuffer.wrap(inner.copyOfRange(0, 4)).int
        require(nameLen in 0..1024 && inner.size >= 4 + nameLen) { "Filename corrupted" }
        val fileName = String(inner.copyOfRange(4, 4 + nameLen), Charsets.UTF_8)
        val data = inner.copyOfRange(4 + nameLen, inner.size)
        return ExtractResult(data, fileName)
    }

    fun hideGeneric(carrierData: ByteArray, payload: ByteArray, fileName: String, password: String?, carrierType: CarrierType): ByteArray {
        return when (carrierType) {
            CarrierType.WAV -> AudioSteganography.hide(carrierData, payload, fileName, password)
            else -> FileSteganography.hide(carrierData, payload, fileName, password)
        }
    }

    fun extractGeneric(carrierData: ByteArray, password: String?, carrierType: CarrierType): ExtractResult {
        return when (carrierType) {
            CarrierType.WAV -> AudioSteganography.extract(carrierData, password)
            else -> FileSteganography.extract(carrierData, password)
        }
    }

    fun extractFromBytes(carrierData: ByteArray, password: String?, carrierType: CarrierType): ExtractResult {
        if (carrierType == CarrierType.PNG || carrierType == CarrierType.BMP) {
            val bmp = BitmapFactory.decodeByteArray(carrierData, 0, carrierData.size)
            if (bmp != null) {
                try {
                    return extract(bmp, password)
                } finally {
                    bmp.recycle()
                }
            }
        }
        return extractGeneric(carrierData, password, carrierType)
    }

    private fun readBits(bitmap: Bitmap, count: Int): IntArray {
        val w = bitmap.width; val h = bitmap.height
        val pixels = IntArray(w * h)
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h)
        val result = IntArray(count)
        var idx = 0
        for (pix in pixels) {
            if (idx >= count) break
            val a = pix and -0x1000000
            val alphaVal = (a shr 24) and 0xFF
            var r = (pix shr 16) and 255
            var g = (pix shr 8) and 255
            var b = pix and 255

            if (alphaVal != 0 && alphaVal != 255) {
                r = (r * 255 + alphaVal / 2) / alphaVal
                g = (g * 255 + alphaVal / 2) / alphaVal
                b = (b * 255 + alphaVal / 2) / alphaVal
            }

            val chs = intArrayOf(r, g, b)
            for (c in chs) {
                if (idx >= count) break
                result[idx++] = c and 1
            }
        }
        return result
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
