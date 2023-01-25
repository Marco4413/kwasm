package io.github.marco4413.kwasm.instructions

import io.github.marco4413.kwasm.Trap
import io.github.marco4413.kwasm.bytecode.*
import io.github.marco4413.kwasm.runtime.*

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
        return InstrBlock(if (blockType < 0) null else blockType.toUInt(), body)
    }
}

class InstrBlock(val blockType: TypeIdx?, val body: Expression) : Instruction(BlockDescriptor) {
    override fun execute(config: Configuration, stack: Stack) {
        if (blockType == null) {
            val label = Label(body)
            stack.pushLabel(label)
            config.thread.frame.module.executeLabel(label, config, stack)
        } else {
            val type = config.thread.frame.module.types[blockType.toInt()]
            val values = stack.popTopValues()
            assert(values.size == type.parameters.size)
            val label = Label(body)
            stack.pushLabel(label)
            for (v in values.reversed())
                stack.pushValue(v)
            config.thread.frame.module.executeLabel(label, config, stack)
        }
    }
}

val LoopDescriptor = object : InstructionDescriptor("loop", 0x03u) {
    override fun read(s: WasmInputStream): Instruction {
        val blockType = s.readI33()
        val body = readBlock(s, false).body1
        return Loop(if (blockType < 0) null else blockType.toUInt(), body)
    }
}

class Loop(val blockType: TypeIdx?, val body: Expression) : Instruction(LoopDescriptor) {
    override fun execute(config: Configuration, stack: Stack) {
        if (blockType == null) {
            val label = LoopLabel(body)
            stack.pushLabel(label)
            config.thread.frame.module.executeLabel(label, config, stack)
        } else {
            val type = config.thread.frame.module.types[blockType.toInt()]
            val values = stack.popTopValues()
            assert(values.size == type.parameters.size)
            val label = LoopLabel(body)
            stack.pushLabel(label)
            for (v in values.reversed())
                stack.pushValue(v)
            config.thread.frame.module.executeLabel(label, config, stack)
        }
    }
}

private fun br(l: LabelIdx, config: Configuration, stack: Stack) {
    val label = stack.getNthLabelFromTop(l)
    val values = stack.popTopValues()
    for (i in 0u..l) {
        while (stack.lastType == StackValueType.Value)
            stack.popValue()
        assert(stack.lastType == StackValueType.Label)
        stack.popLabel().jumpToEnd()
    }
    stack.pushLabel(label)
    for (v in values.reversed()) stack.pushValue(v)
    label.branch()
}

val BrDescriptor = object : InstructionDescriptor("br", 0x0Cu) {
    override fun read(s: WasmInputStream): Instruction = Br(s.readU32())
}

class Br(val labelIdx: LabelIdx) : Instruction(BrDescriptor) {
    override fun execute(config: Configuration, stack: Stack) = br(labelIdx, config, stack)
}

val BrIfDescriptor = object : InstructionDescriptor("br_if", 0x0Du) {
    override fun read(s: WasmInputStream): Instruction = BrIf(s.readU32())
}

class BrIf(val labelIdx: LabelIdx) : Instruction(BrIfDescriptor) {
    override fun execute(config: Configuration, stack: Stack) {
        val c = stack.popValue() as ValueI32
        if (c.value != 0) br(labelIdx, config, stack)
    }
}

val BrTableDescriptor = object : InstructionDescriptor("br_table", 0x0Eu) {
    override fun read(s: WasmInputStream): Instruction = BrTable(s.readVector { s.readU32() }, s.readU32())
}

class BrTable(val branches: List<LabelIdx>, val lastBranch: LabelIdx) : Instruction(BrTableDescriptor) {
    override fun execute(config: Configuration, stack: Stack) {
        val lI = stack.popValue() as ValueI32
        val labelIdx = if (lI.value < branches.size) branches[lI.value] else lastBranch
        br(labelIdx, config, stack)
    }
}

val ReturnDescriptor = object : InstructionDescriptor("return", 0x0Fu) {
    override fun read(s: WasmInputStream): Instruction = Return()
}

class Return : Instruction(ReturnDescriptor) {
    override fun execute(config: Configuration, stack: Stack) {
        val values = stack.popTopValues()
        while (stack.lastType != StackValueType.Frame) {
            when (stack.lastType) {
                StackValueType.Label -> stack.popLabel().jumpToEnd()
                StackValueType.Value -> stack.popValue()
                else -> throw IllegalStateException("Unreachable")
            }
        }
        assert(stack.popFrame() == config.thread.frame)
        for (v in values.reversed()) stack.pushValue(v)
    }
}

val CallDescriptor = object : InstructionDescriptor("call", 0x10u) {
    override fun read(s: WasmInputStream): Instruction = Call(s.readU32())
}

class Call(val funcIdx: FunctionIdx) : Instruction(CallDescriptor) {
    override fun execute(config: Configuration, stack: Stack) {
        val addr = config.thread.frame.module.functions[funcIdx.toInt()]
        config.thread.frame.module.invoke(addr, stack)
    }
}
