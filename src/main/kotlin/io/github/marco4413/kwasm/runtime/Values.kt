package io.github.marco4413.kwasm.runtime

import io.github.marco4413.kwasm.bytecode.*

open class Value(val type: ValueType)
class ValueI32(val value: I32) : Value(ValueType.I32) {
    override fun toString(): String {
        return value.toString()
    }
}
class ValueI64(val value: I64) : Value(ValueType.I64)
class ValueU32(val value: U32) : Value(ValueType.U32)
class ValueU64(val value: U64) : Value(ValueType.U64)

fun getDefaultForValueType(type: ValueType) : Value {
    return when (type) {
        ValueType.I32 -> ValueI32(0)
        ValueType.I64 -> ValueI64(0)
        ValueType.U32 -> ValueU32(0u)
        ValueType.U64 -> ValueU64(0u)
        ValueType.V128 -> TODO()
        ValueType.FunctionRef -> TODO()
        ValueType.ExternalRef -> TODO()
    }
}

enum class ExternalType {
    FunctionAddress,
    TableAddress,
    MemoryAddress,
    GlobalAddress
}

open class ExternalValue(val type: ExternalType, val address: Address)
