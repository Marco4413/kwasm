package io.github.marco4413.kwasm.bytecode.section

import io.github.marco4413.kwasm.bytecode.Name
import io.github.marco4413.kwasm.bytecode.U32
import io.github.marco4413.kwasm.bytecode.U8
import io.github.marco4413.kwasm.bytecode.WasmInputStream

enum class ExportType(val value: U8) {
    Type(0u),
    Table(1u),
    Memory(2u),
    Global(3u);

    companion object {
        fun fromValue(type: U8) : ExportType {
            return when (type) {
                Type.value -> Type
                Table.value -> Table
                Memory.value -> Memory
                Global.value -> Global
                else -> throw IllegalStateException("Invalid Export Type")
            }
        }
    }
}

data class ExportDescription(val type: ExportType, val idx: U32)
data class Export(val name: Name, val description: ExportDescription)

const val ExportSectionId: U8 = 7u
typealias ExportSection = ArrayList<Export>

fun readExportSection(s: WasmInputStream) : Array<Export> {
    s.readU32() // SIZE
    return s.readVector {
        Export(s.readName(), ExportDescription(
            ExportType.fromValue(s.readU8()), s.readU32()
        ))
    }
}
