package io.github.marco4413.kwasm.instructions

import io.github.marco4413.kwasm.U32
import io.github.marco4413.kwasm.WasmContext
import io.github.marco4413.kwasm.WasmInputStream

val LocalGetDescriptor = object : InstructionDescriptor("local.get", 0x20u) {
    override fun read(s: WasmInputStream): Instruction = LocalGet(s.readU32())
}

class LocalGet(val index: U32) : Instruction(LocalGetDescriptor) {
    override fun execute(ctx: WasmContext) = TODO("Not yet implemented")
}
