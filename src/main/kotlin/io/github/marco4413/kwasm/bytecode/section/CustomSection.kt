package io.github.marco4413.kwasm.bytecode.section

import io.github.marco4413.kwasm.bytecode.U32
import io.github.marco4413.kwasm.bytecode.U8
import io.github.marco4413.kwasm.bytecode.WasmInputStream

const val CustomSectionId: U8 = 0u
data class CustomSection(val id: U8, val size: U32, val data: Array<U8>)

fun readCustomSection(s: WasmInputStream, sId: U8) : CustomSection {
    val data = s.readVector { s.readU8() }
    return CustomSection(sId, data.size.toUInt(), data)
}


