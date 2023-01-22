package io.github.marco4413.kwasm.bytecode

import io.github.marco4413.kwasm.instructions.BlockElse
import io.github.marco4413.kwasm.instructions.BlockEnd
import io.github.marco4413.kwasm.instructions.Instruction
import java.lang.IllegalStateException

typealias Expression = List<Instruction>
data class Block(val body1: Expression, val body2: Expression)

/** Doesn't parse Block Types */
fun readBlock(s: WasmInputStream, allowElse: Boolean = true) : Block {
    var atBody1 = true
    val body1 = ArrayList<Instruction>()
    val body2 = ArrayList<Instruction>()

    while (true) {
        val opcode = s.readU8()
        if (opcode == BlockElse) {
            if (!(allowElse && atBody1)) throw IllegalStateException("Invalid Block")
            atBody1 = false
        } else if (opcode == BlockEnd) break

        val instr = Instruction.fromStream(s, opcode)
        if (atBody1) body1.add(instr)
        else body2.add(instr)
    }

    return Block(body1, body2)
}
