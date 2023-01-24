package io.github.marco4413.kwasm.runtime

import io.github.marco4413.kwasm.bytecode.Address
import io.github.marco4413.kwasm.bytecode.U32
import io.github.marco4413.kwasm.bytecode.U8
import io.github.marco4413.kwasm.bytecode.section.*
import io.github.marco4413.kwasm.bytecode.section.Function

// enum class FunctionInstanceType { Module, Host }
// class FunctionInstance(val instanceType: FunctionInstanceType, val type: FunctionType)

typealias HostCode = (config: Configuration, stack: Stack) -> Unit

open class FunctionInstance(val type: FunctionType)
class ModuleFunctionInstance(type: FunctionType, val module: ModuleInstance, val code: Function) : FunctionInstance(type)
class HostFunctionInstance(type: FunctionType, val code: HostCode) : FunctionInstance(type)

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

class DataInstance(val data: List<U8>)

class Store {
    val functions = ArrayList<FunctionInstance>()
    /* TODO: https://webassembly.github.io/spec/core/exec/runtime.html#store
     * tables
     */
    val memories = ArrayList<MemoryInstance>()
    val globals = ArrayList<GlobalInstance>()
    /* elements */
    val data = ArrayList<DataInstance>()

    fun allocateFunction(typeIdx: U32, module: ModuleInstance, code: Function) : Address {
        val function = ModuleFunctionInstance(module.types[typeIdx.toInt()], module, code)
        functions.add(function)
        return functions.size.toUInt() - 1u
    }

    fun allocateHostFunction(type: FunctionType, hostCode: HostCode) : Address {
        val function = HostFunctionInstance(type, hostCode)
        functions.add(function)
        return functions.size.toUInt() - 1u
    }

    fun getFunction(addr: Address) : FunctionInstance {
        if (addr.toInt() !in functions.indices)
            throw NullPointerException("Invalid Function Address 0x${addr.toString(16)}")
        return functions[addr.toInt()]
    }

    fun allocateMemory(type: MemoryType) : Address {
        val memory = MemoryInstance(type)
        memories.add(memory)
        return memories.size.toUInt() - 1u
    }

    fun getMemory(addr: Address) : MemoryInstance {
        if (addr.toInt() !in memories.indices)
            throw NullPointerException("Invalid Memory Address 0x${addr.toString(16)}")
        return memories[addr.toInt()]
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

    fun allocateData(bytes: List<U8>) : Address {
        val dataInst = DataInstance(bytes)
        data.add(dataInst)
        return data.size.toUInt() - 1u
    }

    fun getData(addr: Address) : DataInstance {
        if (addr.toInt() !in data.indices)
            throw NullPointerException("Invalid Data Address 0x${addr.toString(16)}")
        return data[addr.toInt()]
    }

    fun dropData(addr: Address) {
        if (addr.toInt() !in data.indices)
            throw NullPointerException("Invalid Data Address 0x${addr.toString(16)}")
        data[addr.toInt()] = DataInstance(listOf())
    }
}
