package io.github.marco4413.kwasm.instructions

import io.github.marco4413.kwasm.bytecode.*
import io.github.marco4413.kwasm.runtime.Configuration
import io.github.marco4413.kwasm.runtime.Stack
import io.github.marco4413.kwasm.runtime.Trap
import io.github.marco4413.kwasm.runtime.ValueI32

val UnreachableDescriptor = object : InstructionDescriptor("unreachable", 0x00u) {
    override fun read(s: WasmInputStream): Instruction = Unreachable()
}

class Unreachable : Instruction(UnreachableDescriptor) {
    override fun execute(config: Configuration, stack: Stack) {
        throw Trap("Unreachable")
    }
}

val NopDescriptor = object : InstructionDescriptor("nop", 0x01u) {
    override fun read(s: WasmInputStream): Instruction = Nop()
}

class Nop : Instruction(NopDescriptor) {
    override fun execute(config: Configuration, stack: Stack) { }
}

val BlockDescriptor = object : InstructionDescriptor("block", 0x02u) {
    override fun read(s: WasmInputStream): Instruction {
        val blockType = s.readI33()
        val body = readBlock(s, false).body1
        return InstrBlock(if (blockType < 0) null else ValueType.fromValue(blockType.toUByte()), body)
    }
}

class InstrBlock(val blockType: ValueType?, val body: Expression) : Instruction(BlockDescriptor) {
    override fun execute(config: Configuration, stack: Stack) { stack.pushLabel(body) }
}

val BrIfDescriptor = object : InstructionDescriptor("br_if", 0x0Du) {
    override fun read(s: WasmInputStream): Instruction = BrIf(s.readU32())
}

class BrIf(val labelIdx: LabelIdx) : Instruction(BrIfDescriptor) {
    override fun execute(config: Configuration, stack: Stack) {
        val c = stack.popValueType<ValueI32>()
        if (c.value != 0)
            stack.popLastLabels(labelIdx + 1u)
    }
}

val BrTableDescriptor = object : InstructionDescriptor("br_table", 0x0Eu) {
    override fun read(s: WasmInputStream): Instruction = BrTable(s.readVector { s.readU32() }, s.readU32())
}

class BrTable(val branches: List<LabelIdx>, val lastBranch: LabelIdx) : Instruction(BrTableDescriptor) {
    override fun execute(config: Configuration, stack: Stack) {
        val lI = stack.popValueType<ValueI32>()
        val labelIdx = if (lI.value < branches.size) branches[lI.value] else lastBranch
        stack.popLastLabels(labelIdx + 1u)
    }
}

val ReturnDescriptor = object : InstructionDescriptor("return", 0x0Fu) {
    override fun read(s: WasmInputStream): Instruction = Return()
}

class Return : Instruction(ReturnDescriptor) {
    override fun execute(config: Configuration, stack: Stack) {
        assert(stack.popLastFrame() == config.thread.frame)
    }
}

val CallDescriptor = object : InstructionDescriptor("call", 0x10u) {
    override fun read(s: WasmInputStream): Instruction = Call(s.readU32())
}

class Call(val funcIdx: FunctionIdx) : Instruction(CallDescriptor) {
    override fun execute(config: Configuration, stack: Stack) {
        val addr = config.thread.frame.module.functionAddresses[funcIdx.toInt()]
        config.thread.frame.module.invoke(addr, stack)
    }
}
