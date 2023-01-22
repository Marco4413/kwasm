package io.github.marco4413.kwasm.bytecode.section

import io.github.marco4413.kwasm.bytecode.U8
import io.github.marco4413.kwasm.bytecode.ValueType
import io.github.marco4413.kwasm.bytecode.WasmInputStream


const val FunctionTypeId: UByte = 96u
class FunctionType(val parameters: List<ValueType>,
                   val results: List<ValueType>) {
    fun signatureEquals(other: FunctionType) : Boolean {
        if (parameters.size != other.parameters.size ||
            results.size != other.results.size) return false

        for (i in parameters.indices) {
            if (parameters[i] != other.parameters[i])
                return false
        }

        for (i in results.indices) {
            if (results[i] != other.results[i])
                return false
        }

        return true
    }
}

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
