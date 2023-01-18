package io.github.marco4413.kwasm.instructions

import io.github.marco4413.kwasm.WasmInputStream

val NopDescriptor = object : InstructionDescriptor("Nop", 0x01u) {
    override fun read(s: WasmInputStream): Instruction = Nop()
}

class Nop : Instruction(NopDescriptor)
