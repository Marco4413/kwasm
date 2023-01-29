package io.github.marco4413.kwasm.bytecode.section

import io.github.marco4413.kwasm.bytecode.TypeIdx
import io.github.marco4413.kwasm.bytecode.U8
import io.github.marco4413.kwasm.bytecode.WasmInputStream
import io.github.marco4413.kwasm.bytecode.WasmOutputStream

const val FunctionSectionId: U8 = 3u
typealias FunctionSection = List<TypeIdx>

fun readFunctionSection(s: WasmInputStream) : FunctionSection {
    s.readU32() // SIZE
    return s.readVector { s.readU32() }
}

fun writeFunctionSection(s: WasmOutputStream, sec: FunctionSection) {
    s.writeU8(FunctionSectionId)
    s.writeSize {
        s.writeVector(sec) {
            _, typeIdx -> s.writeU32(typeIdx)
        }
    }
}
