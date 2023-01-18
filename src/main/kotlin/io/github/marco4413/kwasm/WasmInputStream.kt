package io.github.marco4413.kwasm

import java.io.BufferedInputStream
import java.io.EOFException
import java.io.InputStream
import java.nio.charset.Charset

typealias U8 = UByte
typealias S32 = Int
typealias U32 = UInt
typealias S64 = Long
typealias U64 = ULong
typealias Name = String

class WasmInputStream(stream: InputStream) : InputStream() {
    companion object {
        val NAME_CHARSET: Charset = Charset.forName("UTF-8")
        const val MAX_U32_LEB128_BYTES = 5
        const val MAX_U64_LEB128_BYTES = 10
        const val MAX_S32_LEB128_BYTES = 5
        const val MAX_S64_LEB128_BYTES = 10
    }

    private val iStream = BufferedInputStream(stream)

    fun readRawS32(): S32 = (
            read() or
            (read() shl  8) or
            (read() shl 16) or
            (read() shl 24)
    )

    fun readRawS64(): S64 = (
            readAsLong() or
            (readAsLong() shl  8) or
            (readAsLong() shl 16) or
            (readAsLong() shl 24) or
            (readAsLong() shl 32) or
            (readAsLong() shl 40) or
            (readAsLong() shl 48) or
            (readAsLong() shl 56)
    )

    fun readRawU32(): U32 =
        readRawS32().toUInt()

    fun readRawU64(): U64 =
        readRawS64().toULong()

    private fun readULEB128(maxBytes: Int): ULong {
        var res: ULong = 0u
        var i = 0
        while (true) {
            val nextByte = read().toULong()
            val value = nextByte and 0x7Fu
            res = res or (value shl (7 * i))
            if ((nextByte and 0x80u) == 0uL) break
            else if (++i > maxBytes)
                throw IllegalStateException()
        }
        return res
    }

    fun readU8() : U8 =
        read().toUByte()

    fun readU32(): U32 =
        readULEB128(MAX_U32_LEB128_BYTES).toUInt()

    fun readU64(): U64 =
        readULEB128(MAX_U64_LEB128_BYTES)

    private fun readSLEB128(maxBytes: Int, bits: Int): ULong {
        // TODO: This has not been tested, this is a copy-paste from wikipedia.
        var res: ULong = 0u
        var i = 0
        while (true) {
            val nextByte = read().toULong()
            val value = nextByte and 0x7Fu
            res = res or (value shl (7 * i))
            if ((nextByte and 0x80u) == 0uL) {
                if (7 * i < bits && (nextByte and 0x40u) != 0uL)
                    return res or (0uL.inv() shl (7 * i))
                return res
            } else if (++i > maxBytes)
                throw IllegalStateException()
        }
    }

    fun readS32(): S32 =
        readSLEB128(MAX_S32_LEB128_BYTES, 32).toInt()

    fun readS64(): S64 =
        readSLEB128(MAX_S64_LEB128_BYTES, 64).toLong()

    fun readName(): Name =
        readNBytes(readU32()).toString(NAME_CHARSET)

    inline fun <reified T> readVector(f: (s: WasmInputStream) -> T): Array<T> {
        val length = readU32().toInt()
        // println(length)
        if (length < 0) throw NegativeArraySizeException()
        return Array(length) { f(this) }
    }

    override fun available(): Int =
        iStream.available()

    override fun read(): Int {
        val r = iStream.read()
        if (r < 0) throw EOFException()
        // println(r)
        return r
    }

    private fun readAsLong(): Long =
        read().toLong()

    fun readNBytes(len: UInt): ByteArray {
        val length = len.toInt()
        if (length < 0) throw NegativeArraySizeException()
        return readNBytes(length)
    }

    override fun close() =
        iStream.close()
}
