package io.github.marco4413.kwasm.runtime

import io.github.marco4413.kwasm.bytecode.Address
import io.github.marco4413.kwasm.bytecode.U32
import io.github.marco4413.kwasm.bytecode.ValueType
import java.lang.RuntimeException

open class Trap(message: String) : RuntimeException(message)
open class WasmRuntimeException(message: String) : RuntimeException(message)

class StackTypeException(expected: StackValueType, got: StackValueType?) :
        WasmRuntimeException("Expected Stack Item Type $expected, got $got")

class InvalidExternalType(expected: ExternalType, got: ExternalType?) :
        WasmRuntimeException("Expected export of type $expected, got $got")
class WrongArgumentCount(fAddr: Address, expected: U32, got: U32) :
        WasmRuntimeException("Function 0x${fAddr.toString(16)} requires $expected arguments, got $got")
class InvalidArgumentTypeException(fAddr: Address, argIndex: U32, expected: ValueType, got: ValueType?) :
        WasmRuntimeException("Argument of 0x${fAddr.toString(16)} at $argIndex has the wrong Value Type, expected $expected, got $got")
class InvalidResultTypeException(fAddr: Address, resIndex: U32, expected: ValueType, got: ValueType?) :
        WasmRuntimeException("Result of 0x${fAddr.toString(16)} at $resIndex has the wrong Value Type, expected $expected, got $got")
