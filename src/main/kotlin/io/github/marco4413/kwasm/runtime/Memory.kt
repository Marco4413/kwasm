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
