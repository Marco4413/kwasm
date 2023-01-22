package io.github.marco4413.kwasm.runtime

import io.github.marco4413.kwasm.bytecode.Address
import io.github.marco4413.kwasm.bytecode.Module
import io.github.marco4413.kwasm.bytecode.U32
import io.github.marco4413.kwasm.bytecode.U64
import io.github.marco4413.kwasm.bytecode.section.Function
import io.github.marco4413.kwasm.bytecode.section.FunctionType
import io.github.marco4413.kwasm.bytecode.section.GlobalType
import io.github.marco4413.kwasm.bytecode.section.Mutability

class FunctionInstance(val type: FunctionType,
                       val module: ModuleInstance,
                       val code: Function,
                       val hostcode: U64)

class GlobalInstance(val type: GlobalType, initialValue: Value) {
    private var _value = initialValue
    var value: Value
        get() = _value
        set(newValue) {
            if (newValue.type != type.type)
                throw IllegalArgumentException("Trying to set Global Value of type ${type.type} to ${newValue.type}")
            _value = newValue
        }

    init { value = initialValue }
}

class Store {
    val functions = ArrayList<FunctionInstance>()
    /* TODO: https://webassembly.github.io/spec/core/exec/runtime.html#store
     * tables
     * memories
     */
    val globals = ArrayList<GlobalInstance>()
    /* elements
     * data
     */

    fun allocateFunction(module: Module, typeIdx: U32, moduleInstance: ModuleInstance) : Address {
        val intTypeIdx = typeIdx.toInt()
        val function = FunctionInstance(module.types[intTypeIdx], moduleInstance, module.code[intTypeIdx], 0u)
        functions.add(function)
        return functions.size.toUInt() - 1u
    }

    fun allocateGlobal(type: GlobalType, value: Value) : Address {
        val global = GlobalInstance(type, value)
        globals.add(global)
        return globals.size.toUInt() - 1u
    }

    fun getGlobal(addr: Address) : GlobalInstance {
        if (addr.toInt() !in globals.indices)
            throw NullPointerException("Invalid Global Address 0x${addr.toString(16)}")
        return globals[addr.toInt()]
    }

    fun setGlobal(addr: Address, value: Value) {
        val global = getGlobal(addr)
        if (global.type.mutability == Mutability.Constant)
            throw IllegalAccessException("Setting value of Constant Global 0x${addr.toString(16)}")
        if (global.type.type != value.type)
            throw IllegalArgumentException("Trying to set Global 0x${addr.toString(16)} of type ${global.type.type} to type ${value.type}")
        global.value = value
    }
}
