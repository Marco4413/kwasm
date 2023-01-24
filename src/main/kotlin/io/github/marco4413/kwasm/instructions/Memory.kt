package io.github.marco4413.kwasm.instructions

import io.github.marco4413.kwasm.Trap
import io.github.marco4413.kwasm.UnknownInstructionException
import io.github.marco4413.kwasm.bytecode.DataIdx
import io.github.marco4413.kwasm.bytecode.U32
import io.github.marco4413.kwasm.bytecode.WasmInputStream
import io.github.marco4413.kwasm.runtime.Configuration
import io.github.marco4413.kwasm.runtime.Stack
import io.github.marco4413.kwasm.runtime.ValueI32

class MemoryArgument(val align: U32, val offset: U32)

val I32Load8UDescriptor = object : InstructionDescriptor("i32.load8_u", 0x2Du) {
    override fun read(s: WasmInputStream): Instruction = I32Load8U(MemoryArgument(s.readU32(), s.readU32()))
}

class I32Load8U(val memoryArg: MemoryArgument) : Instruction(I32Load8UDescriptor) {
    override fun execute(config: Configuration, stack: Stack) {
        val memory = config.store.getMemory(config.thread.frame.module.memories[0])
        val baseAddr = stack.popValue() as ValueI32
        val addr = (baseAddr.value + memoryArg.offset.toInt()).toUInt()
        if ((addr + 1u).toInt() > memory.data.size)
            throw Trap("Memory out of bounds.")
        stack.pushValue(ValueI32(memory[addr].toInt()))
    }
}

val MemoryRelatedDescriptor = object : InstructionDescriptor("memory.init|copy|fill/data.drop", 0xFCu) {
    override fun read(s: WasmInputStream): Instruction {
        return when (s.readU32()) {
            8u -> {
                val memInit = MemoryInit(s.readU32())
                assert(s.readU8() == (0u).toUByte())
                memInit
            }
            9u -> DataDrop(s.readU32())
            else -> throw UnknownInstructionException(opcode)
        }
    }
}

class MemoryInit(val dataIdx: DataIdx) : Instruction(MemoryRelatedDescriptor) {
    override fun execute(config: Configuration, stack: Stack) {
        val memory = config.store.getMemory(config.thread.frame.module.memories[0])
        val data = config.store.getData(config.thread.frame.module.data[dataIdx.toInt()])

        val length = stack.popValue() as ValueI32
        val source = stack.popValue() as ValueI32
        val target = stack.popValue() as ValueI32

        if (source.value + length.value > data.data.size)
            throw Trap("Data out of bounds.")
        if (target.value + length.value > memory.data.size)
            throw Trap("Memory out of bounds.")

        for (i in 0 until length.value)
            memory[target.value + i] = data.data[source.value + i]
    }
}

class DataDrop(val dataIdx: DataIdx) : Instruction(MemoryRelatedDescriptor) {
    override fun execute(config: Configuration, stack: Stack) =
        config.store.dropData(config.thread.frame.module.data[dataIdx.toInt()])
}
