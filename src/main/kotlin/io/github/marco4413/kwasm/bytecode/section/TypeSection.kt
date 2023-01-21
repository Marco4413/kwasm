package io.github.marco4413.kwasm.bytecode.section

import io.github.marco4413.kwasm.bytecode.U8
import io.github.marco4413.kwasm.bytecode.ValueType
import io.github.marco4413.kwasm.bytecode.WasmInputStream


const val FunctionTypeId: UByte = 96u
data class FunctionType(val parameters: List<ValueType>,
                        val results: List<ValueType>)

const val TypeSectionId: U8 = 1u
typealias TypeSection = List<FunctionType>

fun readTypeSection(s: WasmInputStream) : TypeSection {
    s.readU32() // SIZE
    return s.readVector {
        val id = s.readU8()
        if (id != FunctionTypeId)
            TODO("Unsupported Type $id")
        FunctionType(s.readVector { ValueType.fromValue(s.readU8()) },
            s.readVector { ValueType.fromValue(s.readU8()) })
    }
}
