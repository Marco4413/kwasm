package io.github.marco4413.kwasm.instructions

import io.github.marco4413.kwasm.bytecode.GlobalIdx
import io.github.marco4413.kwasm.bytecode.WasmInputStream
import io.github.marco4413.kwasm.runtime.Configuration
import io.github.marco4413.kwasm.runtime.Stack

val GlobalSetDescriptor = object : InstructionDescriptor("global.set", 0x24u) {
    override fun read(s: WasmInputStream): Instruction = GlobalSet(s.readU32())
}

class GlobalSet(val index: GlobalIdx) : Instruction(GlobalSetDescriptor) {
    override fun execute(config: Configuration, stack: Stack) {
        config.store.setGlobal(config.thread.frame.module.globalAddresses[index.toInt()], stack.popValue())
    }
}
