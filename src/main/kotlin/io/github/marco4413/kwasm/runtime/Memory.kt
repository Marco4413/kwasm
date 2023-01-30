package io.github.marco4413.kwasm.runtime

import io.github.marco4413.kwasm.bytecode.*
import io.github.marco4413.kwasm.bytecode.section.MemoryType

class MemoryInstance(type: MemoryType) {
    companion object {
        const val PAGE_SIZE: Int = 65536 // 2^16
    }

    var type = type
        private set
    val pages: Int get() = type.min.toInt()
    var data: MutableList<Byte> = ArrayList(List((pages * PAGE_SIZE)) { 0 })
        private set

    fun grow(n: Int) {
        assert(data.size % PAGE_SIZE == 0)
        val len = pages + n
        if (len > PAGE_SIZE) TODO("B I G Memory")
        val limits1 = MemoryType(len.toUInt(), type.max)
        if (!limits1.isValid()) TODO("Invalid Limits")
        for (i in 0 until (n * PAGE_SIZE)) data.add(0)
        type = limits1
    }

    fun setF64(index: Int, value: F64) = setI64(index, value.toRawBits())
    fun setI64(index: Int, value: I64) {
        this[index  ] = (value        ).toByte()
        this[index+1] = (value ushr  8).toByte()
        this[index+2] = (value ushr 16).toByte()
        this[index+3] = (value ushr 24).toByte()
        this[index+4] = (value ushr 32).toByte()
        this[index+5] = (value ushr 40).toByte()
        this[index+6] = (value ushr 48).toByte()
        this[index+7] = (value ushr 56).toByte()
    }

    fun setF32(index: Int, value: F32) = setI32(index, value.toRawBits())
    fun setI32(index: Int, value: I32) {
        this[index  ] = (value        ).toByte()
        this[index+1] = (value ushr  8).toByte()
        this[index+2] = (value ushr 16).toByte()
        this[index+3] = (value ushr 24).toByte()
    }

    fun getF64(index: Int) : F64 = F64.fromBits(getI64(index))
    fun getI64(index: Int) : I64 {
        return (
                ( this[index  ]).toLong() and 0xFF or
                ((this[index+1]).toLong() and 0xFF shl  8) or
                ((this[index+2]).toLong() and 0xFF shl 16) or
                ((this[index+3]).toLong() and 0xFF shl 24) or
                ((this[index+4]).toLong() and 0xFF shl 32) or
                ((this[index+5]).toLong() and 0xFF shl 40) or
                ((this[index+6]).toLong() and 0xFF shl 48) or
                ((this[index+7]).toLong() and 0xFF shl 56)
        )
    }

    fun getF32(index: Int) : F32 = F32.fromBits(getI32(index))
    fun getI32(index: Int) : I32 {
        return (
                ( this[index  ]).toInt() and 0xFF or
                ((this[index+1]).toInt() and 0xFF shl  8) or
                ((this[index+2]).toInt() and 0xFF shl 16) or
                ((this[index+3]).toInt() and 0xFF shl 24)
        )
    }

    inline operator fun get(index: Int) : Byte = data[index]
    inline operator fun set(index: Int, value: U8) { this[index] = value.toByte() }
    inline operator fun set(index: Int, value: Byte) {
        if (index !in data.indices) throw IndexOutOfBoundsException()
        data[index] = value
    }
}
