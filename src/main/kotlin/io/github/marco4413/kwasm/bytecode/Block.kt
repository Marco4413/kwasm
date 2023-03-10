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
// class Block(val body1: Expression, val body2: Expression)
class ThenElseBlock(val then: Expression, val otherwise: Expression?)

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

fun readThenElseBlock(s: WasmInputStream) : ThenElseBlock {
    val then = ArrayList<Instruction>()
    var otherwise: ArrayList<Instruction>? = null

    while (true) {
        val opcode = s.readU8()
        if (opcode == BlockElse) {
            if (otherwise != null) throw IllegalStateException("Else already parsed")
            otherwise = ArrayList()
            continue
        } else if (opcode == BlockEnd) break

        val instr = Instruction.read(s, opcode)
        if (otherwise == null) then.add(instr)
        else otherwise.add(instr)
    }

    return ThenElseBlock(then, otherwise)
}

fun writeThenElseBlock(s: WasmOutputStream, block: ThenElseBlock) {
    for (instr in block.then)
        instr.write(s)
    if (block.otherwise != null) {
        s.writeU8(BlockElse)
        for (instr in block.otherwise)
            instr.write(s)
    }
    s.writeU8(BlockEnd)
}

fun readExpression(s: WasmInputStream) : Expression {
    val expr = ArrayList<Instruction>()
    while (true) {
        val opcode = s.readU8()
        if (opcode == BlockElse) {
            throw IllegalStateException("Can't have else in exression")
        } else if (opcode == BlockEnd) break

        val instr = Instruction.read(s, opcode)
        expr.add(instr)
    }
    return expr
}

fun writeExpression(s: WasmOutputStream, expr: Expression) {
    for (instr in expr)
        instr.write(s)
    s.writeU8(BlockEnd)
}
