package io.github.marco4413.kwasm.bytecode.section

import io.github.marco4413.kwasm.bytecode.*

data class Locals(val count: U32, val type: ValueType)
data class Function(val locals: List<Locals>, val body: Expression)

const val CodeSectionId: U8 = 10u
typealias CodeSection = List<Function>

fun readExpression(s: WasmInputStream) : Expression =
    readBlock(s, false).body1

fun readCodeSection(s: WasmInputStream) : CodeSection {
    s.readU32() // SIZE
    return s.readVector {
        s.readU32() // SIZE
        Function(s.readVector {
            Locals(s.readU32(), ValueType.fromValue(s.readU8()))
        }, readExpression(s))
    }
}
