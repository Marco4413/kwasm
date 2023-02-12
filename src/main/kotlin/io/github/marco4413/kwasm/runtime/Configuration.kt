package io.github.marco4413.kwasm.runtime

import io.github.marco4413.kwasm.bytecode.Expression
import io.github.marco4413.kwasm.bytecode.I32

class Frame(val arity: I32, val locals: MutableList<Value>, val module: ModuleInstance)
class ComputationThread(val frame: Frame, val instructions: Expression) {
    private var _trap: Trap? = null
    var trap: Trap?
        get() = _trap
        set(value) {
            if (_trap != null) throw IllegalStateException()
            _trap = value
        }
}

class Configuration(val store: Store, val thread: ComputationThread)
