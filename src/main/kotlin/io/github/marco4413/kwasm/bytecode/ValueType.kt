package io.github.marco4413.kwasm.bytecode

enum class ValueType(val value: U8) {
    I32(0x7Fu),
    I64(0x7Eu),
    F32(0x7Du),
    F64(0x7Cu),
    V128(0x7Bu),
    FunctionRef(0x70u),
    ExternalRef(0x6Fu);

    companion object {
        fun fromValue(type: U8) : ValueType {
            return when (type) {
                I32.value -> I32
                I64.value -> I64
                F32.value -> F32
                F64.value -> F64
                V128.value -> V128
                FunctionRef.value -> FunctionRef
                ExternalRef.value -> ExternalRef
                else -> throw IllegalStateException("Invalid Value Type $type")
            }
        }
    }
}
