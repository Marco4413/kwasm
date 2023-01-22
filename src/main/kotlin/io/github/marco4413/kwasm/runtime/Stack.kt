package io.github.marco4413.kwasm.runtime

import io.github.marco4413.kwasm.bytecode.U32

enum class StackValueType {
    Value, Label, Frame
}

class Stack {
    private val tStack = ArrayList<StackValueType>()
    private val vStack = ArrayList<Value>()
    private val lStack = ArrayList<Label>()
    private val fStack = ArrayList<Frame>()

    val lastType  get() = tStack.lastOrNull()
    val lastFrame get() = fStack.lastOrNull()
    val lastLabel get() = lStack.lastOrNull()

    val labelCount: U32 get() = lStack.size.toUInt()

    private fun popType(type: StackValueType) {
        if (lastType != type) throw StackTypeException(type, lastType)
        tStack.removeLast()
    }

    fun pushValue(v: Value) {
        vStack.add(v)
        tStack.add(StackValueType.Value)
    }

    fun popValue() : Value {
        popType(StackValueType.Value)
        return vStack.removeLast()
    }

    inline fun <reified T : Value> popValueType() : T {
        val value = popValue()
        if (value is T) return value
        throw StackTypeException(StackValueType.Value, null)
    }

    fun pushLabel(l: Label) {
        lStack.add(l)
        tStack.add(StackValueType.Label)
    }

    fun popLabel() : Label {
        popType(StackValueType.Label)
        return lStack.removeLast()
    }

    fun popLastLabel() : Label {
        val index = tStack.lastIndexOf(StackValueType.Label)
        tStack.removeAt(index)
        return lStack.removeLast()
    }

    fun popLastLabels(n: U32) : List<Label> =
        List(n.toInt()) { popLastLabel() }

    fun pushFrame(f: Frame) {
        fStack.add(f)
        tStack.add(StackValueType.Frame)
    }

    fun popFrame() : Frame {
        popType(StackValueType.Frame)
        return fStack.removeLast()
    }

    fun popLastFrame() : Frame {
        val index = tStack.lastIndexOf(StackValueType.Frame)
        tStack.removeAt(index)
        return fStack.removeLast()
    }
}
