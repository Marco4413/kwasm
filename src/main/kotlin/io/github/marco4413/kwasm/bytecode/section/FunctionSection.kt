package io.github.marco4413.kwasm.bytecode.section

import io.github.marco4413.kwasm.bytecode.TypeIdx
import io.github.marco4413.kwasm.bytecode.U8
import io.github.marco4413.kwasm.bytecode.WasmInputStream

const val FunctionSectionId: U8 = 3u
typealias FunctionSection = ArrayList<TypeIdx>

fun readFunctionSection(s: WasmInputStream) : Array<TypeIdx> {
    s.readU32() // SIZE
    return s.readVector { s.readU32() }
}
