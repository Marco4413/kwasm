package io.github.marco4413.kwasm.runtime

import io.github.marco4413.kwasm.bytecode.Expression
import io.github.marco4413.kwasm.bytecode.U32
import io.github.marco4413.kwasm.instructions.Instruction

open class Label(val body: Expression) : Iterator<Instruction> {
    var index: U32 = 0u
        protected set

    override fun hasNext(): Boolean {
        return index.toInt() in body.indices
    }

    override fun next(): Instruction {
        return body[index++.toInt()]
    }

    // WHY DID IT TAKE SO LONG TO ACTUALLY FIGURE THIS OUT
    fun jumpToStart() { index = 0u }
    fun jumpToEnd() { index = body.size.toUInt() }
    // I SPENT LIKE 2 DAYS DEBUGGING A LOOP TO FIND THIS OUT
    open fun branch() = jumpToEnd()
}

class LoopLabel(body: Expression) : Label(body) {
    override fun branch() = jumpToStart()
}
