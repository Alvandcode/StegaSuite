package com.stegasuite.app

import android.graphics.Bitmap
import java.nio.ByteBuffer
import java.util.zip.CRC32

data class ExtractResult(val bytes: ByteArray, val fileName: String)

object PngSteganography {
    private val MAGIC_V1 = byteArrayOf('S'.code.toByte(), 'G'.code.toByte(), 'P'.code.toByte(), '1'.code.toByte())
    private val MAGIC_V2 = byteArrayOf('S'.code.toByte(), 'G'.code.toByte(), 'P'.code.toByte(), '2'.code.toByte())
    private const val HEADER_V1 = 12 // 4 magic + 8 len
    private const val HEADER_V2 = 12 // 4 magic + 8 len (پسورد + اسم فایل داخل payload است)

    fun capacityBytes(bitmap: Bitmap): Long = (bitmap.width.toLong() * bitmap.height * 3L) / 8L
    fun maxPayloadBytes(bitmap: Bitmap): Long = capacityBytes(bitmap) - HEADER_V2 - 50 // 50 بایت برای رمز و اسم فایل

    fun hide(bitmap: Bitmap, original: ByteArray, fileName: String, password: String?): Bitmap {
        val safeName = if (fileName.isBlank()) "file" else fileName
        val nameBytes = safeName.toByteArray(Charsets.UTF_8)
        require(nameBytes.size <= 1024) { "نام فایل خیلی طولانی است" }

        // داخل payload اسم فایل را هم می‌گذاریم: [4 بایت طول اسم] + [اسم] + [داده اصلی]
        val inner = ByteBuffer.allocate(4 + nameBytes.size + original.size)
            .putInt(nameBytes.size).put(nameBytes).put(original).array()

        val payload: ByteArray = if (password.isNullOrEmpty()) {
            val crc = CRC32().apply { update(inner) }.value.toInt()
            val crcBytes = ByteBuffer.allocate(4).putInt(crc).array()
            inner + crcBytes // بدون رمز -> CRC برای تشخیص خرابی
        } else {
            StegaCrypto.encrypt(inner, password)
        }

        val packet = MAGIC_V2 + ByteBuffer.allocate(8).putLong(payload.size.toLong()).array() + payload
        val capacityBits = bitmap.width.toLong() * bitmap.height * 3L
        require(packet.size.toLong() * 8L <= capacityBits) {
            "فایل خیلی بزرگ است! ظرفیت عکس: ${capacityBytes(bitmap)/1024}KB ، نیاز: ${packet.size/1024}KB - عکس بزرگتر انتخاب کن"
        }

        val out = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val w = out.width; val h = out.height
        val pixels = IntArray(w * h)
        out.getPixels(pixels, 0, w, 0, 0, w, h) // یکبار خواندن - 100 برابر سریعتر از getPixel

        var bitIndex = 0
        val totalBits = packet.size * 8
        for (i in pixels.indices) {
            if (bitIndex >= totalBits) break
            val c = pixels[i]
            val a = c and -0x1000000
            var r = (c shr 16) and 255
            var g = (c shr 8) and 255
            var b = c and 255
            val channels = intArrayOf(r, g, b)
            for (ch in 0..2) {
                if (bitIndex >= totalBits) break
                val byteVal = packet[bitIndex / 8].toInt() and 255
                val bit = (byteVal shr (7 - (bitIndex % 8))) and 1
                channels[ch] = (channels[ch] and 0xFE) or bit
                bitIndex++
            }
            pixels[i] = a or (channels[0] shl 16) or (channels[1] shl 8) or channels[2]
        }
        out.setPixels(pixels, 0, w, 0, 0, w, h)
        return out
    }

    fun extract(bitmap: Bitmap, password: String?): ExtractResult {
        // 1. اول Magic را بخوان
        val magicBits = readBits(bitmap, 4 * 8)
        val magic = bitsToBytes(magicBits)

        // حالت قدیمی SGP1 برای عقب‌گرد
        if (magic.contentEquals(MAGIC_V1)) {
            val headerBits = readBits(bitmap, HEADER_V1 * 8)
            val head = bitsToBytes(headerBits)
            val len = ByteBuffer.wrap(head.copyOfRange(4, 12)).long
            require(len >= 0 && len <= Int.MAX_VALUE) { "طول خراب است" }
            val needed = (HEADER_V1.toLong() + len) * 8L
            require(needed <= bitmap.width.toLong() * bitmap.height * 3L) { "داده خراب است" }
            val all = readBits(bitmap, needed.toInt())
            val payload = bitsToBytes(all.copyOfRange(HEADER_V1 * 8, all.size))
            val data = if (password.isNullOrEmpty()) payload else StegaCrypto.decrypt(payload, password)
            return ExtractResult(data, "recovered_file")
        }

        require(magic.contentEquals(MAGIC_V2)) { "هیچ فایل مخفی StegaSuite در این عکس پیدا نشد" }
        
        val headerBits = readBits(bitmap, HEADER_V2 * 8)
        val head = bitsToBytes(headerBits)
        val len = ByteBuffer.wrap(head.copyOfRange(4, 12)).long
        require(len >= 0 && len <= 20L * 1024 * 1024) { "طول مشکوک است" }
        val needed = (HEADER_V2.toLong() + len) * 8L
        require(needed <= bitmap.width.toLong() * bitmap.height * 3L) { "داده خراب است" }
        val all = readBits(bitmap, needed.toInt())
        val payload = bitsToBytes(all.copyOfRange(HEADER_V2 * 8, all.size))

        val inner: ByteArray = if (password.isNullOrEmpty()) {
            require(payload.size >= 4) { "داده خراب است" }
            val innerPart = payload.copyOfRange(0, payload.size - 4)
            val storedCrc = ByteBuffer.wrap(payload.copyOfRange(payload.size - 4, payload.size)).int
            val calcCrc = CRC32().apply { update(innerPart) }.value.toInt()
            require(storedCrc == calcCrc) { "عکس خراب شده یا با رمز دیگری قفل شده" }
            innerPart
        } else {
            StegaCrypto.decrypt(payload, password)
        }

        require(inner.size >= 4) { "داده داخلی خراب است" }
        val nameLen = ByteBuffer.wrap(inner.copyOfRange(0, 4)).int
        require(nameLen in 0..1024 && inner.size >= 4 + nameLen) { "نام فایل خراب است" }
        val fileName = String(inner.copyOfRange(4, 4 + nameLen), Charsets.UTF_8)
        val data = inner.copyOfRange(4 + nameLen, inner.size)
        return ExtractResult(data, fileName)
    }

    private fun readBits(bitmap: Bitmap, count: Int): IntArray {
        val w = bitmap.width; val h = bitmap.height
        val pixels = IntArray(w * h)
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h)
        val result = IntArray(count)
        var idx = 0
        for (pix in pixels) {
            if (idx >= count) break
            val r = (pix shr 16) and 255
            val g = (pix shr 8) and 255
            val b = pix and 255
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
