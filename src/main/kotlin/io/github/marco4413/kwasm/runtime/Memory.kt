package io.github.marco4413.kwasm.runtime

import io.github.marco4413.kwasm.bytecode.*
import io.github.marco4413.kwasm.bytecode.section.MemoryType

class MemoryInstance(type: MemoryType) {
    companion object {
        const val PAGE_SIZE: U32 = 65536u // 2^16
    }

    var type = type
        private set
    val pages: U32 get() = type.min
    var data: MutableList<U8> = ArrayList(List((pages * PAGE_SIZE).toInt()) { 0u })
        private set

    fun grow(n: U32) {
        assert(data.size.toUInt() % PAGE_SIZE == 0u)
        val len = pages + n
        if (len > PAGE_SIZE) TODO("B I G Memory")
        val limits1 = MemoryType(len, type.max)
        if (!limits1.isValid()) TODO("Invalid Limits")
        for (i in 0u until (n * PAGE_SIZE)) data.add(0u)
        type = limits1
    }

    fun setF64(index: U32, value: F64) = setI64(index, value.toRawBits())
    fun setI64(index: U32, value: I64) = setU64(index, value.toULong())
    fun setU64(index: U32, value: U64) {
        this[index   ] = (value       ).toUByte()
        this[index+1u] = (value shr  8).toUByte()
        this[index+2u] = (value shr 16).toUByte()
        this[index+3u] = (value shr 24).toUByte()
        this[index+4u] = (value shr 32).toUByte()
        this[index+5u] = (value shr 40).toUByte()
        this[index+6u] = (value shr 48).toUByte()
        this[index+7u] = (value shr 56).toUByte()
    }

    fun setF32(index: U32, value: F32) = setI32(index, value.toRawBits())
    fun setI32(index: U32, value: I32) = setU32(index, value.toUInt())
    fun setU32(index: U32, value: U32) {
        this[index   ] = (value       ).toUByte()
        this[index+1u] = (value shr  8).toUByte()
        this[index+2u] = (value shr 16).toUByte()
        this[index+3u] = (value shr 24).toUByte()
    }

    fun getF64(index: U32) : F64 = F64.fromBits(getI64(index))
    fun getI64(index: U32) : I64 = getU64(index).toLong()
    fun getU64(index: U32) : U64 = (
            (this[index]).toULong() or
            ((this[index+1u]).toULong() shl  8) or
            ((this[index+2u]).toULong() shl 16) or
            ((this[index+3u]).toULong() shl 24) or
            ((this[index+4u]).toULong() shl 32) or
            ((this[index+5u]).toULong() shl 40) or
            ((this[index+6u]).toULong() shl 48) or
            ((this[index+7u]).toULong() shl 56)
    )

    fun getF32(index: U32) : F32 = F32.fromBits(getI32(index))
    fun getI32(index: U32) : I32 = getU32(index).toInt()
    fun getU32(index: U32) : U32 = (
            (this[index]).toUInt() or
            ((this[index+1u]).toUInt() shl  8) or
            ((this[index+2u]).toUInt() shl 16) or
            ((this[index+3u]).toUInt() shl 24)
    )

    operator fun get(index: U32) : U8 = this[index.toInt()]
    operator fun get(index: I32) : U8 {
        if (index !in data.indices) throw IndexOutOfBoundsException()
        return data[index]
    }

    operator fun set(index: U32, value: U8) { this[index.toInt()] = value }
    operator fun set(index: I32, value: U8) {
        if (index !in data.indices) throw IndexOutOfBoundsException()
        data[index] = value
    }
}
