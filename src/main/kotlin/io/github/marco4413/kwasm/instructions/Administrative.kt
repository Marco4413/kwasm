package io.github.marco4413.kwasm.instructions

import io.github.marco4413.kwasm.runtime.Configuration
import io.github.marco4413.kwasm.runtime.Stack
import io.github.marco4413.kwasm.runtime.StackValueType
import io.github.marco4413.kwasm.runtime.Trap

fun trap(config: Configuration, stack: Stack, trap: Trap) {
    assert(config.thread.trap == null)
    while (stack.lastType != StackValueType.Frame) {
        when (stack.lastType) {
            StackValueType.Value -> stack.popAndDiscardTopValues()
            StackValueType.Label -> stack.popLabel().jumpToEnd()
            else -> assert(false)
        }
    }
    assert(stack.lastFrame == config.thread.frame)
    config.thread.trap = trap
}
