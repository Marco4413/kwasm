package io.github.marco4413.kwasm.bytecode

import io.github.marco4413.kwasm.bytecode.section.FunctionType
import io.github.marco4413.kwasm.instructions.Instruction
import java.lang.IllegalStateException

const val BlockElse: U8 = 0x05u
const val BlockEnd: U8 = 0x0Bu
const val BlockVoid: U8 = 0x40u

class BlockType(val type: FunctionType?, val typeIdx: TypeIdx) {
    companion object {
        val Void = BlockType(FunctionType(listOf(), listOf()), TypeIdx.MAX_VALUE)
        fun withResults(vararg type: ValueType) =
            BlockType(FunctionType(listOf(), listOf(*type)), TypeIdx.MAX_VALUE)
    }
}

typealias Expression = List<Instruction>
class Block(val body1: Expression, val body2: Expression)

fun readBlockType(s: WasmInputStream) : BlockType {
    s.mark(WasmInputStream.MAX_I32_LEB128_BYTES)
    val blockType = s.readI33()
    if (blockType < 0) {
        s.reset()
        val type = s.readU8()
        if (type == BlockVoid)
            return BlockType.Void
        return BlockType.withResults(ValueType.fromValue(type))
    }
    return BlockType(null, blockType.toUInt())
}

fun writeBlockType(s: WasmOutputStream, blockType: BlockType) {
    if (blockType.type == null) {
        s.writeU32(blockType.typeIdx)
        return
    }
    assert(blockType.type.parameters.isEmpty())
    when (blockType.type.results.size) {
        0 -> s.writeU8(BlockVoid)
        1 -> s.writeU8(blockType.type.results[0].value)
        else -> assert(false)
    }
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

        val instr = Instruction.read(s, opcode)
        if (atBody1) body1.add(instr)
        else body2.add(instr)
    }

    return Block(body1, body2)
}

fun writeBlock(s: WasmOutputStream, block: Block) {
    for (instr in block.body1)
        instr.write(s)
    if (block.body2.isNotEmpty()) {
        s.writeU8(BlockElse)
        for (instr in block.body2)
            instr.write(s)
    }
    s.writeU8(BlockEnd)
}
