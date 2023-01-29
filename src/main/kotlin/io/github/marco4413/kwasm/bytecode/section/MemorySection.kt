package io.github.marco4413.kwasm.bytecode.section

import io.github.marco4413.kwasm.bytecode.U32
import io.github.marco4413.kwasm.bytecode.U8
import io.github.marco4413.kwasm.bytecode.WasmInputStream
import io.github.marco4413.kwasm.bytecode.WasmOutputStream

class Limit(val min: U32, val max: U32? = null) {
    private val _range = min..(max ?: U32.MAX_VALUE)
    fun isValid() : Boolean = max != null && min <= max
    operator fun contains(v: U32) : Boolean = v in _range
}

fun readLimit(s: WasmInputStream) : Limit {
    return when (s.readU8().toUInt()) {
        0u -> Limit(s.readU32())
        1u -> Limit(s.readU32(), s.readU32())
        else -> throw IllegalStateException()
    }
}

fun writeLimit(s: WasmOutputStream, lim: Limit) {
    if (lim.max == null) {
        s.writeU8(0u)
        s.writeU32(lim.min)
        return
    }

    s.writeU8(1u)
    s.writeU32(lim.min)
    s.writeU32(lim.max)
}

typealias MemoryType = Limit
class Memory(val type: MemoryType)

const val MemorySectionId: U8 = 5u
typealias MemorySection = List<Memory>

fun readMemorySection(s: WasmInputStream) : MemorySection {
    s.readU32() // SIZE
    return s.readVector { Memory(readLimit(s)) }
}

fun writeMemorySection(s: WasmOutputStream, sec: MemorySection) {
    s.writeU8(MemorySectionId)
    s.writeSize {
        s.writeVector(sec) {
            _, memory -> writeLimit(s, memory.type)
        }
    }
}
