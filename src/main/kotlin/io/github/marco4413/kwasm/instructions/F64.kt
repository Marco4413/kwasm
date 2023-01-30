package io.github.marco4413.kwasm.instructions

import io.github.marco4413.kwasm.bytecode.F64
import io.github.marco4413.kwasm.bytecode.WasmInputStream
import io.github.marco4413.kwasm.bytecode.WasmOutputStream
import io.github.marco4413.kwasm.runtime.Configuration
import io.github.marco4413.kwasm.runtime.Stack
import io.github.marco4413.kwasm.runtime.ValueF64
import io.github.marco4413.kwasm.runtime.ValueI32

val F64ConstDescriptor = object : InstructionDescriptor("f64.const", 0x44u) {
    override fun read(s: WasmInputStream): Instruction = F64Const(s.readF64())
}

class F64Const(val value: F64) : Instruction(F64ConstDescriptor) {
    override fun execute(config: Configuration, stack: Stack) { stack.pushValue(ValueF64(value)) }
    override fun write(s: WasmOutputStream) {
        super.write(s)
        s.writeF64(value)
    }
}

val F64MulDescriptor = object : InstructionDescriptor("f64.mul", 0xA2u) {
    override fun read(s: WasmInputStream): Instruction = F64Mul()
}

class F64Mul : Instruction(F64MulDescriptor) {
    override fun execute(config: Configuration, stack: Stack) {
        val b = stack.popValue() as ValueF64
        val a = stack.popValue() as ValueF64
        stack.pushValue(ValueF64(a.value * b.value))
    }
}

val F64ConvertI32UDescriptor = object : InstructionDescriptor("f64.convert_i32_u", 0xB8u) {
    override fun read(s: WasmInputStream): Instruction = F64ConvertI32U()
}

class F64ConvertI32U : Instruction(F64ConvertI32UDescriptor) {
    override fun execute(config: Configuration, stack: Stack) {
        val value = stack.popValue() as ValueI32
        stack.pushValue(ValueF64(value.value.toUInt().toDouble()))
    }
}
