package io.github.marco4413.kwasm.runtime

import io.github.marco4413.kwasm.StackTypeException
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

    val valueCount: U32 get() = vStack.size.toUInt()
    val labelCount: U32 get() = lStack.size.toUInt()
    val frameCount: U32 get() = fStack.size.toUInt()

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

    /** The first element in the returned List is the top-most value on the stack */
    fun popTopValues() : List<Value> {
        val values = ArrayList<Value>()
        for (type in tStack.reversed()) {
            if (type != StackValueType.Value) break
            values.add(vStack[vStack.size - values.size - 1])
        }
        return values
    }

    // fun getTopValues() : List<Value> {
    //     val values = ArrayList<Value>()
    //     for (type in tStack.reversed()) {
    //         if (type != StackValueType.Value) break
    //         values.add(vStack[vStack.size - values.size - 1])
    //     }
    //     return values
    // }

    // fun getTopValueTypes() : List<ValueType> {
    //     val valueTypes = ArrayList<ValueType>()
    //     for (type in tStack.reversed()) {
    //         if (type != StackValueType.Value) break
    //         valueTypes.add(vStack[vStack.size - valueTypes.size - 1].type)
    //     }
    //     return valueTypes
    // }

    // inline fun <reified T : Value> popValueType() : T {
    //     val value = popValue()
    //     if (value is T) return value
    //     throw StackTypeException(StackValueType.Value, null)
    // }

    fun pushLabel(l: Label) {
        lStack.add(l)
        tStack.add(StackValueType.Label)
    }

    fun popLabel() : Label {
        popType(StackValueType.Label)
        return lStack.removeLast()
    }

    fun getNthLabelFromTop(n: U32) : Label {
        return lStack[lStack.size - n.toInt() - 1]
    }

    fun popLastLabel() : Label {
        val index = tStack.lastIndexOf(StackValueType.Label)
        tStack.removeAt(index)
        return lStack.removeLast()
    }

    // fun popLastLabels(n: U32) : List<Label> =
    //     List(n.toInt()) { popLastLabel() }

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
