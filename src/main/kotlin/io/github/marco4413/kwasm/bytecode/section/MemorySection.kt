package io.github.marco4413.kwasm.bytecode.section

import io.github.marco4413.kwasm.bytecode.U32
import io.github.marco4413.kwasm.bytecode.U8
import io.github.marco4413.kwasm.bytecode.WasmInputStream

class Limit(val min: U32, val max: U32 = U32.MAX_VALUE) {
    private val _range = min..max
    fun isValid() : Boolean = min <= max
    operator fun contains(v: U32) : Boolean = v in _range
}

fun readLimit(s: WasmInputStream) : Limit {
    return when (s.readU8().toUInt()) {
        0u -> Limit(s.readU32())
        1u -> Limit(s.readU32(), s.readU32())
        else -> throw IllegalStateException()
    }
}

typealias MemoryType = Limit
class Memory(val type: MemoryType)

const val MemorySectionId: U8 = 5u
typealias MemorySection = List<Memory>

fun readMemorySection(s: WasmInputStream) : MemorySection {
    s.readU32() // SIZE
    return s.readVector { Memory(readLimit(s)) }
}
