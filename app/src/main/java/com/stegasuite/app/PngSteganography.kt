herepackage com.stegasuite.app

import android.graphics.Bitmap
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer

object PngSteganography {
    private val MAGIC = byteArrayOf('S'.code.toByte(), 'G'.code.toByte(), 'P'.code.toByte(), '1'.code.toByte())
    private const val HEADER_BYTES = 12

    fun capacityBytes(bitmap: Bitmap): Long = (bitmap.width.toLong() * bitmap.height * 3L) / 8L

    fun hide(bitmap: Bitmap, original: ByteArray, password: String?): Bitmap {
        val payload = if (password.isNullOrEmpty()) original else StegaCrypto.encrypt(original, password)
        val packet = MAGIC + ByteBuffer.allocate(8).putLong(payload.size.toLong()).array() + payload
        val capacityBits = bitmap.width.toLong() * bitmap.height * 3L
        require(packet.size.toLong() * 8L <= capacityBits) {
            "Payload is too large for this image. Required: ${packet.size} bytes."
        }

        val out = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        var bitIndex = 0
        for (y in 0 until out.height) {
            for (x in 0 until out.width) {
                val c = out.getPixel(x, y)
                val channels = intArrayOf((c shr 16) and 255, (c shr 8) and 255, c and 255)
                for (ch in 0..2) {
                    if (bitIndex >= packet.size * 8) return out
                    val byte = packet[bitIndex / 8].toInt() and 255
                    val bit = (byte shr (7 - (bitIndex % 8))) and 1
                    channels[ch] = (channels[ch] and 0xFE) or bit
                    bitIndex++
                }
                out.setPixel(x, y, (c and 0xFF000000.toInt()) or
                    (channels[0] shl 16) or (channels[1] shl 8) or channels[2])
            }
        }
        return out
    }

    fun extract(bitmap: Bitmap, password: String?): ByteArray {
        val bits = ArrayList<Int>(HEADER_BYTES * 8)
        var needed = HEADER_BYTES * 8
        val first = readBits(bitmap, needed)
        val head = bitsToBytes(first)
        require(head.copyOfRange(0, 4).contentEquals(MAGIC)) { "No StegaSuite payload found." }
        val len = ByteBuffer.wrap(head.copyOfRange(4, 12)).long
        require(len >= 0 && len <= Int.MAX_VALUE) { "Invalid payload length." }
        needed = (HEADER_BYTES.toLong() + len) * 8L
        require(needed <= bitmap.width.toLong() * bitmap.height * 3L) { "Corrupted payload." }
        val all = readBits(bitmap, needed.toInt())
        val payload = bitsToBytes(all.copyOfRange(HEADER_BYTES * 8, all.size))
        return if (password.isNullOrEmpty()) payload else StegaCrypto.decrypt(payload, password)
    }

    private fun readBits(bitmap: Bitmap, count: Int): IntArray {
        val result = IntArray(count)
        var index = 0
        loop@ for (y in 0 until bitmap.height) {
            for (x in 0 until bitmap.width) {
                val c = bitmap.getPixel(x, y)
                val channels = intArrayOf((c shr 16) and 255, (c shr 8) and 255, c and 255)
                for (ch in 0..2) {
                    if (index >= count) break@loop
                    result[index++] = channels[ch] and 1
                }
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
