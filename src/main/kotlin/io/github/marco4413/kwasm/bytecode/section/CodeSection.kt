package io.github.marco4413.kwasm.bytecode.section

import io.github.marco4413.kwasm.bytecode.U32
import io.github.marco4413.kwasm.bytecode.U8
import io.github.marco4413.kwasm.bytecode.ValueType
import io.github.marco4413.kwasm.bytecode.WasmInputStream
import io.github.marco4413.kwasm.instructions.BlockEnd
import io.github.marco4413.kwasm.instructions.Instruction

typealias Expression = List<Instruction>
data class Locals(val count: U32, val type: ValueType)
data class Function(val locals: List<Locals>, val expression: Expression)

const val CodeSectionId: U8 = 10u
typealias CodeSection = List<Function>

fun readExpression(s: WasmInputStream) : Expression {
    val expression = ArrayList<Instruction>()
    while (true) {
        val opcode = s.readU8()
        if (opcode == BlockEnd) break
        expression.add(Instruction.fromStream(s, opcode))
    }
    return expression
}

fun readCodeSection(s: WasmInputStream) : CodeSection {
    s.readU32() // SIZE
    return s.readVector {
        s.readU32() // SIZE
        Function(s.readVector {
            Locals(s.readU32(), ValueType.fromValue(s.readU8()))
        }, readExpression(s))
    }
}
