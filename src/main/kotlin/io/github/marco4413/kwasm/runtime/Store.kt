package io.github.marco4413.kwasm.runtime

import io.github.marco4413.kwasm.bytecode.Address
import io.github.marco4413.kwasm.bytecode.Module
import io.github.marco4413.kwasm.bytecode.U32
import io.github.marco4413.kwasm.bytecode.U64
import io.github.marco4413.kwasm.bytecode.section.Function
import io.github.marco4413.kwasm.bytecode.section.FunctionType

class FunctionInstance(val type: FunctionType,
                       val module: ModuleInstance,
                       val code: Function,
                       val hostcode: U64)

class Store {
    val functions = ArrayList<FunctionInstance>()
    /* TODO: https://webassembly.github.io/spec/core/exec/runtime.html#store
    tables
    memories
    globals
    elements
    data
    */

    fun allocateFunction(module: Module, typeIdx: U32, moduleInstance: ModuleInstance) : Address {
        val intTypeIdx = typeIdx.toInt()
        val function = FunctionInstance(module.types[intTypeIdx], moduleInstance, module.code[intTypeIdx], 0u)
        functions.add(function)
        return functions.size.toUInt() - 1u
    }
}
