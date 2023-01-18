package io.github.marco4413.kwasm.bytecode.section

import io.github.marco4413.kwasm.bytecode.Array2D
import io.github.marco4413.kwasm.bytecode.U8
import io.github.marco4413.kwasm.bytecode.ValueType
import io.github.marco4413.kwasm.bytecode.WasmInputStream
import io.github.marco4413.kwasm.instructions.BlockEnd
import io.github.marco4413.kwasm.instructions.Instruction

typealias Expression = ArrayList<Instruction>

data class Function(val locals: Array2D<ValueType>, val expression: Expression)

const val CodeSectionId: U8 = 10u
typealias CodeSection = ArrayList<Function>

private fun readExpression(s: WasmInputStream) : Expression {
    val expression = Expression()
    while (true) {
        val opcode = s.readU8()
        if (opcode == BlockEnd) break
        expression.add(Instruction.fromStream(s, opcode))
    }
    return expression
}

fun readCodeSection(s: WasmInputStream) : Array<Function> {
    s.readU32() // SIZE
    return s.readVector {
        s.readU32() // SIZE
        // TODO: Figure out why this is a 2D Array
        Function(s.readVector {
            s.readVector { ValueType.fromValue(s.readU8()) }
        }, readExpression(s))
    }
}
