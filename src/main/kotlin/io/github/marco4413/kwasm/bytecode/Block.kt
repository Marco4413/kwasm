package io.github.marco4413.kwasm.bytecode

import io.github.marco4413.kwasm.bytecode.section.FunctionType
import io.github.marco4413.kwasm.instructions.Instruction
import java.lang.IllegalStateException

const val BlockElse: U8 = 0x05u
const val BlockEnd: U8 = 0x0Bu
const val BlockVoid: U8 = 0x40u

class BlockType(val type: FunctionType?, val typeIdx: TypeIdx)
typealias Expression = List<Instruction>
class Block(val body1: Expression, val body2: Expression)

fun readBlockType(s: WasmInputStream) : BlockType {
    s.mark(WasmInputStream.MAX_I32_LEB128_BYTES)
    val blockType = s.readI33()
    if (blockType < 0) {
        s.reset()
        val type = s.readU8()
        if (type == BlockVoid)
            return BlockType(FunctionType(listOf(), listOf()), TypeIdx.MAX_VALUE)
        return BlockType(FunctionType(listOf(), listOf(ValueType.fromValue(type))), TypeIdx.MAX_VALUE)
    }
    return BlockType(null, blockType.toUInt())
}

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
