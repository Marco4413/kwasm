package io.github.marco4413.kwasm.runtime

import io.github.marco4413.kwasm.bytecode.Expression
import io.github.marco4413.kwasm.bytecode.I32
import io.github.marco4413.kwasm.instructions.Instruction

open class Label(val arity: I32, val body: Expression) : Iterator<Instruction> {
    constructor(body: Expression) : this(0, body)

    var index: I32 = 0
        protected set

    override fun hasNext(): Boolean {
        return index in body.indices
    }

    override fun next(): Instruction {
        return body[index++]
    }

    fun jumpToStart() { index = 0 }
    fun jumpToEnd() { index = body.size }
    open fun branch() = jumpToEnd()
}

class LoopLabel(arity: I32, body: Expression) : Label(arity, body) {
    constructor(body: Expression) : this(0, body)
    override fun branch() = jumpToStart()
}
