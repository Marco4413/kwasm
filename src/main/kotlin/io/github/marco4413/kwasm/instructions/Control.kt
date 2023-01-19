package io.github.marco4413.kwasm.instructions

import io.github.marco4413.kwasm.WasmContext
import io.github.marco4413.kwasm.bytecode.WasmInputStream


val ReturnDescriptor = object : InstructionDescriptor("return", 0x0Fu) {
    override fun read(s: WasmInputStream): Instruction = Return()
}

class Return : Instruction(ReturnDescriptor) {
    override fun execute(ctx: WasmContext) = TODO("Not yet implemented")
}
