package io.github.marco4413.kwasm.instructions

import io.github.marco4413.kwasm.WasmContext
import io.github.marco4413.kwasm.bytecode.S32
import io.github.marco4413.kwasm.bytecode.WasmInputStream

val I32ConstDescriptor = object : InstructionDescriptor("i32.const", 0x41u) {
    override fun read(s: WasmInputStream): Instruction = I32Const(s.readS32())
}

class I32Const(val value: S32) : Instruction(I32ConstDescriptor) {
    override fun execute(ctx: WasmContext) = TODO("Not yet implemented")
}

val I32AddDescriptor = object : InstructionDescriptor("i32.add", 0x6Au) {
    override fun read(s: WasmInputStream): Instruction = I32Add()
}

class I32Add : Instruction(I32AddDescriptor) {
    override fun execute(ctx: WasmContext) = TODO("Not yet implemented")
}
