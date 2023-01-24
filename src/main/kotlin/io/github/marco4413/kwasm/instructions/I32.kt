package io.github.marco4413.kwasm.instructions

import io.github.marco4413.kwasm.bytecode.I32
import io.github.marco4413.kwasm.bytecode.WasmInputStream
import io.github.marco4413.kwasm.runtime.Configuration
import io.github.marco4413.kwasm.runtime.Stack
import io.github.marco4413.kwasm.runtime.ValueI32

val I32ConstDescriptor = object : InstructionDescriptor("i32.const", 0x41u) {
    override fun read(s: WasmInputStream): Instruction = I32Const(s.readI32())
}

class I32Const(val value: I32) : Instruction(I32ConstDescriptor) {
    override fun execute(config: Configuration, stack: Stack) {
        stack.pushValue(ValueI32(value))
        // println("Const $value")
    }
}

val I32EqZDescriptor = object : InstructionDescriptor("i32.eqz", 0x45u) {
    override fun read(s: WasmInputStream): Instruction = I32EqZ()
}

class I32EqZ : Instruction(I32EqZDescriptor) {
    override fun execute(config: Configuration, stack: Stack) {
        val c = stack.popValueType<ValueI32>()
        stack.pushValue(ValueI32(if (c.value == 0) 1 else 0))
    }
}

val I32GTUDescriptor = object : InstructionDescriptor("i32.gt_u", 0x4Bu) {
    override fun read(s: WasmInputStream): Instruction = I32GTU()
}

class I32GTU : Instruction(I32GTUDescriptor) {
    override fun execute(config: Configuration, stack: Stack) {
        val b = stack.popValueType<ValueI32>()
        val a = stack.popValueType<ValueI32>()
        stack.pushValue(ValueI32(
            if (a.value.toUInt() > b.value.toUInt()) 1 else 0
        ))
    }
}

val I32AddDescriptor = object : InstructionDescriptor("i32.add", 0x6Au) {
    override fun read(s: WasmInputStream): Instruction = I32Add()
}

class I32Add : Instruction(I32AddDescriptor) {
    override fun execute(config: Configuration, stack: Stack) {
        val b = stack.popValueType<ValueI32>()
        val a = stack.popValueType<ValueI32>()
        stack.pushValue(ValueI32(a.value + b.value))
    }
}

val I32SubDescriptor = object : InstructionDescriptor("i32.sub", 0x6Bu) {
    override fun read(s: WasmInputStream): Instruction = I32Sub()
}

class I32Sub : Instruction(I32SubDescriptor) {
    override fun execute(config: Configuration, stack: Stack) {
        val b = stack.popValueType<ValueI32>()
        val a = stack.popValueType<ValueI32>()
        stack.pushValue(ValueI32(a.value - b.value))
    }
}

val I32AndDescriptor = object : InstructionDescriptor("i32.and", 0x71u) {
    override fun read(s: WasmInputStream): Instruction = I32And()
}

class I32And : Instruction(I32AndDescriptor) {
    override fun execute(config: Configuration, stack: Stack) {
        val b = stack.popValueType<ValueI32>()
        val a = stack.popValueType<ValueI32>()
        stack.pushValue(ValueI32(a.value and b.value))
    }
}

val I32SHLDescriptor = object : InstructionDescriptor("i32.shl", 0x74u) {
    override fun read(s: WasmInputStream): Instruction = I32SHL()
}

class I32SHL : Instruction(I32SHLDescriptor) {
    override fun execute(config: Configuration, stack: Stack) {
        val b = stack.popValueType<ValueI32>()
        val a = stack.popValueType<ValueI32>()
        stack.pushValue(ValueI32(a.value shl b.value))
    }
}

val I32SHRSDescriptor = object : InstructionDescriptor("i32.shr_s", 0x75u) {
    override fun read(s: WasmInputStream): Instruction = I32SHRS()
}

class I32SHRS : Instruction(I32SHRSDescriptor) {
    override fun execute(config: Configuration, stack: Stack) {
        val b = stack.popValueType<ValueI32>()
        val a = stack.popValueType<ValueI32>()
        stack.pushValue(ValueI32(a.value shr b.value))
    }
}
