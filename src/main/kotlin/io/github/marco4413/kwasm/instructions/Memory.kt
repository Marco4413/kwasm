package io.github.marco4413.kwasm.instructions

import io.github.marco4413.kwasm.UnknownInstructionException
import io.github.marco4413.kwasm.bytecode.DataIdx
import io.github.marco4413.kwasm.bytecode.U32
import io.github.marco4413.kwasm.bytecode.WasmInputStream
import io.github.marco4413.kwasm.bytecode.WasmOutputStream
import io.github.marco4413.kwasm.runtime.*

class MemoryArgument(val align: U32, val offset: U32)

val I32Load8UDescriptor = object : InstructionDescriptor("i32.load8_u", 0x2Du) {
    override fun read(s: WasmInputStream): Instruction = I32Load8U(MemoryArgument(s.readU32(), s.readU32()))
}

class I32Load8U(val memoryArg: MemoryArgument) : Instruction(I32Load8UDescriptor) {
    override fun execute(config: Configuration, stack: Stack) {
        val memory = config.store.getMemory(config.thread.frame.module.memories[0])
        val baseAddr = stack.popValue() as ValueI32
        val addr = baseAddr.value + memoryArg.offset.toInt()
        if (addr + 1 > memory.data.size) {
            trap(config, stack, MemoryIndexOutOfBoundsTrap(config))
            return
        }
        stack.pushValue(ValueI32(memory[addr].toInt() and 0xFF))
    }

    override fun write(s: WasmOutputStream) {
        super.write(s)
        s.writeU32(memoryArg.align)
        s.writeU32(memoryArg.offset)
    }
}

val I32StoreDescriptor = object : InstructionDescriptor("i32.store", 0x36u) {
    override fun read(s: WasmInputStream): Instruction = I32Store(MemoryArgument(s.readU32(), s.readU32()))
}

class I32Store(val memoryArg: MemoryArgument) : Instruction(I32StoreDescriptor) {
    override fun execute(config: Configuration, stack: Stack) {
        val memory = config.store.getMemory(config.thread.frame.module.memories[0])
        val value = stack.popValue() as ValueI32

        val index = stack.popValue() as ValueI32
        val address = index.value + memoryArg.offset.toInt()

        if (address + 4 > memory.data.size) {
            trap(config, stack, MemoryIndexOutOfBoundsTrap(config))
            return
        }

        memory.setI32(address, value.value)
    }

    override fun write(s: WasmOutputStream) {
        super.write(s)
        s.writeU32(memoryArg.align)
        s.writeU32(memoryArg.offset)
    }
}

val F64StoreDescriptor = object : InstructionDescriptor("f64.store", 0x39u) {
    override fun read(s: WasmInputStream): Instruction = F64Store(MemoryArgument(s.readU32(), s.readU32()))
}

class F64Store(val memoryArg: MemoryArgument) : Instruction(F64StoreDescriptor) {
    override fun execute(config: Configuration, stack: Stack) {
        val memory = config.store.getMemory(config.thread.frame.module.memories[0])
        val value = stack.popValue() as ValueF64

        val index = stack.popValue() as ValueI32
        val address = index.value + memoryArg.offset.toInt()

        if (address + 8 > memory.data.size) {
            trap(config, stack, MemoryIndexOutOfBoundsTrap(config))
            return
        }

        memory.setF64(address, value.value)
    }

    override fun write(s: WasmOutputStream) {
        super.write(s)
        s.writeU32(memoryArg.align)
        s.writeU32(memoryArg.offset)
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

        if (source.value + length.value > data.data.size) {
            trap(config, stack, DataIndexOutOfBoundsTrap(config))
            return
        }

        if (target.value + length.value > memory.data.size) {
            trap(config, stack, MemoryIndexOutOfBoundsTrap(config))
            return
        }

        for (i in 0 until length.value)
            memory[target.value + i] = data.data[source.value + i]
    }

    override fun write(s: WasmOutputStream) {
        super.write(s)
        s.writeU32(8u)
        s.writeU32(dataIdx)
        s.writeU8(0u)
    }
}

class DataDrop(val dataIdx: DataIdx) : Instruction(MemoryRelatedDescriptor) {
    override fun execute(config: Configuration, stack: Stack) =
        config.store.dropData(config.thread.frame.module.data[dataIdx.toInt()])

    override fun write(s: WasmOutputStream) {
        super.write(s)
        s.writeU8(9u)
        s.writeU32(dataIdx)
    }
}
