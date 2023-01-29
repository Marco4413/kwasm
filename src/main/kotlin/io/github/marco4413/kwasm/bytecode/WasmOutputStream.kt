package io.github.marco4413.kwasm.bytecode

import java.io.BufferedOutputStream
import java.io.OutputStream
import java.lang.Exception

private typealias OutWriter = (byte: Int) -> Unit

typealias WasmVectorWriter<T> = (s: WasmOutputStream, item: T) -> Unit
typealias WasmWriter = (s: WasmOutputStream) -> Unit

class WasmOutputStream(stream: OutputStream) : OutputStream() {
    private val oStream = BufferedOutputStream(stream)
    private val _defaultWriter: OutWriter = { oStream.write(it) }
    private var writer = _defaultWriter

    fun writeRawI32(value: I32) {
        write(value)
        write(value shr  8)
        write(value shr 16)
        write(value shr 24)
    }

    fun writeRawI64(value: I64) {
        write((value       ).toInt())
        write((value shr  8).toInt())
        write((value shr 16).toInt())
        write((value shr 24).toInt())
        write((value shr 32).toInt())
        write((value shr 40).toInt())
        write((value shr 48).toInt())
        write((value shr 56).toInt())
    }

    fun writeRawU32(value: U32) = writeRawI32(value.toInt())
    fun writeRawU64(value: U64) = writeRawI64(value.toLong())

    fun writeU8(value: U8) = write(value.toInt())
    fun writeU32(value: U32) = writeULEB128(value.toULong())
    fun writeU64(value: U64) = writeULEB128(value)

    fun writeI32(value: I32) = writeSLEB128(value.toLong())
    fun writeI64(value: I64) = writeSLEB128(value)

    fun writeF32(value: F32) = writeRawI32(value.toRawBits())
    fun writeF64(value: F64) = writeRawI64(value.toRawBits())

    fun writeName(name: Name) {
        writeU32(name.length.toUInt())
        write(name.toByteArray(WasmInputStream.NAME_CHARSET))
    }

    inline fun <reified T> writeVector(vec: List<T>, f: WasmVectorWriter<T>) {
        writeU32(vec.size.toUInt())
        for (item in vec) f(this, item)
    }

    fun writeSize(f: WasmWriter) {
        val buffer = ArrayList<Int>()
        val scopedWriter: OutWriter = { buffer.add(it) }
        val oldWriter = writer

        writer = scopedWriter
        try {
            f(this)
        } catch (ignored: Exception) { }
        writer = oldWriter

        writeU32(buffer.size.toUInt())
        for (b in buffer) write(b)
    }

    private fun writeULEB128(value: U64) {
        var currValue = value
        do {
            val byte = currValue and 0x7Fu
            currValue = currValue shr 7
            write((if (currValue == 0uL) byte else byte or 0x80u).toInt())
        } while (currValue != 0uL)
    }

    private fun writeSLEB128(value: I64) {
        var currValue = value
        while (true) {
            val byte = currValue and 0x7F
            currValue = currValue shr 7
            if ((currValue == 0L && byte and 0x40L == 0L) ||
                (currValue == -1L && byte and 0x40L != 0L)) {
                write(byte.toInt())
                break
            }
            write((byte or 0x80L).toInt())
        }
    }

    override fun write(b: Int) {
        writer(b)
    }

    override fun flush() =
        oStream.flush()

    override fun close() =
        oStream.close()
}


