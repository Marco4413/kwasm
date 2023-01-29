package io.github.marco4413.kwasm.bytecode.section

import io.github.marco4413.kwasm.bytecode.*

enum class ExportType(val value: U8) {
    Function(0u),
    Table(1u),
    Memory(2u),
    Global(3u);

    companion object {
        fun fromValue(type: U8) : ExportType {
            return when (type) {
                Function.value -> Function
                Table.value -> Table
                Memory.value -> Memory
                Global.value -> Global
                else -> throw IllegalStateException("Invalid Export Type")
            }
        }
    }
}

class ExportDescription(val type: ExportType, val idx: U32)
class Export(val name: Name, val description: ExportDescription)

const val ExportSectionId: U8 = 7u
typealias ExportSection = List<Export>

fun readExportSection(s: WasmInputStream) : ExportSection {
    s.readU32() // SIZE
    return s.readVector {
        Export(s.readName(), ExportDescription(
            ExportType.fromValue(s.readU8()), s.readU32()
        ))
    }
}

fun writeExportSection(s: WasmOutputStream, sec: ExportSection) {
    s.writeU8(ExportSectionId)
    s.writeSize {
        s.writeVector(sec) {
            _, export ->
            s.writeName(export.name)
            s.writeU8(export.description.type.value)
            s.writeU32(export.description.idx)
        }
    }
}
