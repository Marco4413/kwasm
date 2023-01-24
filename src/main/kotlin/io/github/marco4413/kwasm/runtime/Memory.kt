package io.github.marco4413.kwasm.runtime

import io.github.marco4413.kwasm.bytecode.I32
import io.github.marco4413.kwasm.bytecode.U32
import io.github.marco4413.kwasm.bytecode.U8
import io.github.marco4413.kwasm.bytecode.section.MemoryType

class MemoryInstance(val type: MemoryType) {
    companion object {
        const val PAGE_SIZE: U32 = 65532u
    }

    var size: U32 private set
    var data: MutableList<U8> private set

    init {
        size = type.limit.min.coerceAtLeast(1u)
        data = ArrayList(List((size * PAGE_SIZE).toInt()) { 0u })
    }

    // TODO: Grow

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
