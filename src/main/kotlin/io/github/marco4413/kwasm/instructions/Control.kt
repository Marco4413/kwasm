package io.github.marco4413.kwasm.instructions

import io.github.marco4413.kwasm.bytecode.*
import io.github.marco4413.kwasm.runtime.*

val UnreachableDescriptor = object : InstructionDescriptor("unreachable", 0x00u) {
    override fun read(s: WasmInputStream): Instruction = Unreachable()
}

class Unreachable : Instruction(UnreachableDescriptor) {
    override fun execute(config: Configuration, stack: Stack) {
        trap(config, stack, UnreachableTrap(config))
    }
}

val NopDescriptor = object : InstructionDescriptor("nop", 0x01u) {
    override fun read(s: WasmInputStream): Instruction = Nop()
}

class Nop : Instruction(NopDescriptor) {
    override fun execute(config: Configuration, stack: Stack) { }
}

val BlockDescriptor = object : InstructionDescriptor("block", 0x02u) {
    override fun read(s: WasmInputStream): Instruction =
        Block(readBlockType(s), readExpression(s))
}

class Block(val blockType: BlockType, val body: Expression) : Instruction(BlockDescriptor) {
    override fun execute(config: Configuration, stack: Stack) {
        val type = blockType.type ?: config.thread.frame.module.types[blockType.typeIdx.toInt()]
        val label = Label(type.results.size, body)
        val values = stack.popNValues(type.parameters.size)
        // Enter the Block
        stack.pushLabel(label)
        for (v in values.reversed()) stack.pushValue(v)
        config.thread.frame.module.executeLabel(label, config, stack)
    }

    override fun write(s: WasmOutputStream) {
        super.write(s)
        writeBlockType(s, blockType)
        writeExpression(s, body)
    }
}

val LoopDescriptor = object : InstructionDescriptor("loop", 0x03u) {
    override fun read(s: WasmInputStream): Instruction =
        Loop(readBlockType(s), readExpression(s))
}

class Loop(val blockType: BlockType, val body: Expression) : Instruction(LoopDescriptor) {
    override fun execute(config: Configuration, stack: Stack) {
        val type = blockType.type ?: config.thread.frame.module.types[blockType.typeIdx.toInt()]
        val label = LoopLabel(type.parameters.size, body)
        val values = stack.popNValues(type.parameters.size)
        // Enter the Loop
        stack.pushLabel(label)
        for (v in values.reversed()) stack.pushValue(v)
        config.thread.frame.module.executeLabel(label, config, stack)
    }

    override fun write(s: WasmOutputStream) {
        super.write(s)
        writeBlockType(s, blockType)
        writeExpression(s, body)
    }
}

val IfDescriptor = object : InstructionDescriptor("if", 0x04u) {
    override fun read(s: WasmInputStream): Instruction =
        If(readBlockType(s), readThenElseBlock(s))
}

class If(val blockType: BlockType, val block: ThenElseBlock) : Instruction(IfDescriptor) {
    override fun execute(config: Configuration, stack: Stack) {
        val cond = stack.popValue() as ValueI32
        val body = (if (cond.value != 0) block.then else block.otherwise) ?: return

        val type = blockType.type ?: config.thread.frame.module.types[blockType.typeIdx.toInt()]
        val label = Label(type.results.size, body)
        val values = stack.popNValues(type.parameters.size)
        // Enter the Block
        stack.pushLabel(label)
        for (v in values.reversed()) stack.pushValue(v)
        config.thread.frame.module.executeLabel(label, config, stack)
    }

    override fun write(s: WasmOutputStream) {
        super.write(s)
        writeBlockType(s, blockType)
        writeThenElseBlock(s, block)
    }
}

private fun br(l: LabelIdx, config: Configuration, stack: Stack) {
    val label = stack.getNthLabelFromTop(l)
    val values = stack.popNValues(label.arity)
    for (i in 0u until l) {
        stack.popAndDiscardTopValues()
        assert(stack.lastType == StackValueType.Label)
        stack.popLabel().jumpToEnd()
    }
    for (v in values.reversed()) stack.pushValue(v)
    label.branch()
}

val BrDescriptor = object : InstructionDescriptor("br", 0x0Cu) {
    override fun read(s: WasmInputStream): Instruction = Br(s.readU32())
}

class Br(val labelIdx: LabelIdx) : Instruction(BrDescriptor) {
    override fun execute(config: Configuration, stack: Stack) = br(labelIdx, config, stack)
    override fun write(s: WasmOutputStream) {
        super.write(s)
        s.writeU32(labelIdx)
    }
}

val BrIfDescriptor = object : InstructionDescriptor("br_if", 0x0Du) {
    override fun read(s: WasmInputStream): Instruction = BrIf(s.readU32())
}

class BrIf(val labelIdx: LabelIdx) : Instruction(BrIfDescriptor) {
    override fun execute(config: Configuration, stack: Stack) {
        val c = stack.popValue() as ValueI32
        if (c.value != 0) br(labelIdx, config, stack)
    }

    override fun write(s: WasmOutputStream) {
        super.write(s)
        s.writeU32(labelIdx)
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

    override fun write(s: WasmOutputStream) {
        super.write(s)
        s.writeVector(branches) { _, idx -> s.writeU32(idx) }
        s.writeU32(lastBranch)
    }
}

val ReturnDescriptor = object : InstructionDescriptor("return", 0x0Fu) {
    override fun read(s: WasmInputStream): Instruction = Return()
}

class Return : Instruction(ReturnDescriptor) {
    override fun execute(config: Configuration, stack: Stack) {
        val values = stack.popNValues(config.thread.frame.arity)
        while (stack.lastType != StackValueType.Frame) {
            when (stack.lastType) {
                StackValueType.Label -> stack.popLabel().jumpToEnd()
                StackValueType.Value -> stack.popAndDiscardTopValues()
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
        val trap = config.thread.frame.module.invoke(addr, stack)
        // Propagate Trap to next Frame
        if (trap != null) trap(config, stack, trap)
    }

    override fun write(s: WasmOutputStream) {
        super.write(s)
        s.writeU32(funcIdx)
    }
}
