package io.github.marco4413.kwasm.instructions

import io.github.marco4413.kwasm.bytecode.LocalIdx
import io.github.marco4413.kwasm.bytecode.WasmInputStream
import io.github.marco4413.kwasm.bytecode.WasmOutputStream
import io.github.marco4413.kwasm.runtime.Configuration
import io.github.marco4413.kwasm.runtime.Stack

val LocalGetDescriptor = object : InstructionDescriptor("local.get", 0x20u) {
    override fun read(s: WasmInputStream): Instruction = LocalGet(s.readU32())
}

class LocalGet(val index: LocalIdx) : Instruction(LocalGetDescriptor) {
    override fun execute(config: Configuration, stack: Stack) {
        val local = config.thread.frame.locals[index.toInt()]
        stack.pushValue(local)
    }

    override fun write(s: WasmOutputStream) {
        super.write(s)
        s.writeU32(index)
    }
}

val LocalSetDescriptor = object : InstructionDescriptor("local.set", 0x21u) {
    override fun read(s: WasmInputStream): Instruction = LocalSet(s.readU32())
}

class LocalSet(val index: LocalIdx) : Instruction(LocalSetDescriptor) {
    override fun execute(config: Configuration, stack: Stack) {
        val value = stack.popValue()
        config.thread.frame.locals[index.toInt()] = value
    }

    override fun write(s: WasmOutputStream) {
        super.write(s)
        s.writeU32(index)
    }
}

val LocalTeeDescriptor = object : InstructionDescriptor("local.tee", 0x22u) {
    override fun read(s: WasmInputStream): Instruction = LocalTee(s.readU32())
}

class LocalTee(val index: LocalIdx) : Instruction(LocalTeeDescriptor) {
    override fun execute(config: Configuration, stack: Stack) {
        val value = stack.popValue()
        stack.pushValue(value)
        config.thread.frame.locals[index.toInt()] = value
    }

    override fun write(s: WasmOutputStream) {
        super.write(s)
        s.writeU32(index)
    }
}
