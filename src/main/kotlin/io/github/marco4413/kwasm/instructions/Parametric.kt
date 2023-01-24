package io.github.marco4413.kwasm.instructions

import io.github.marco4413.kwasm.bytecode.WasmInputStream
import io.github.marco4413.kwasm.runtime.Configuration
import io.github.marco4413.kwasm.runtime.Stack
import io.github.marco4413.kwasm.runtime.ValueI32

val SelectDescriptor = object : InstructionDescriptor("select", 0x1Bu) {
    override fun read(s: WasmInputStream): Instruction = Select()
}

class Select : Instruction(SelectDescriptor) {
    override fun execute(config: Configuration, stack: Stack) {
        val c = stack.popValue() as ValueI32
        val val2 = stack.popValue() as ValueI32
        val val1 = stack.popValue() as ValueI32
        stack.pushValue(if (c.value != 0) val1 else val2)
    }
}
