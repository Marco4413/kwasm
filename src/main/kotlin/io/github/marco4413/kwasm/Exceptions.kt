package io.github.marco4413.kwasm

import io.github.marco4413.kwasm.bytecode.Address
import io.github.marco4413.kwasm.bytecode.U32
import io.github.marco4413.kwasm.bytecode.U8
import io.github.marco4413.kwasm.bytecode.ValueType
import io.github.marco4413.kwasm.bytecode.section.FunctionType
import io.github.marco4413.kwasm.bytecode.section.ImportType
import io.github.marco4413.kwasm.runtime.ExternalType
import io.github.marco4413.kwasm.runtime.StackValueType
import java.lang.RuntimeException

open class Trap(message: String) : RuntimeException(message) // TODO: Find a neater way to handle Traps
open class WasmRuntimeException(message: String) : RuntimeException(message)

class StackTypeException(expected: StackValueType, got: StackValueType?) :
        WasmRuntimeException("Expected Stack Item Type $expected, got $got")

class UnknownInstructionException(opcode: U8) :
        WasmRuntimeException("Unknown instruction with opcode 0x${opcode.toString(16)}")

open class InvalidAddressException(address: Address, type: String) :
        WasmRuntimeException("Invalid $type Address 0x${address.toString(16)}")
open class InvalidStoreAddressException(address: Address, type: String) :
        InvalidAddressException(address, "Store $type")
class InvalidStoreFunctionAddressException(address: Address) :
        InvalidStoreAddressException(address, "Function")
class InvalidStoreMemoryAddressException(address: Address) :
        InvalidStoreAddressException(address, "Memory")
class InvalidStoreGlobalAddressException(address: Address) :
        InvalidStoreAddressException(address, "Global")
class InvalidStoreDataAddressException(address: Address) :
        InvalidStoreAddressException(address, "Data")

class ConstantGlobalAssignmentException :
        WasmRuntimeException("Assignment to Constant Global")
class InvalidGlobalTypeException(expected: ValueType, got: ValueType?) :
        WasmRuntimeException("Assigning an $got value to an $expected Global")

class WrongImportCount(expected: U32, got: U32) :
        WasmRuntimeException("Expected $expected imports, got $got")
class InvalidImportType(expected: ImportType, got: ExternalType, importIndex: U32) :
        WasmRuntimeException("Import at $importIndex is a $got, expected $expected")
class InvalidImportFunctionType(expected: FunctionType, got: FunctionType, importIndex: U32) :
        WasmRuntimeException("Import at $importIndex has signature $got, expected $expected")
class InvalidImportGlobalType(expected: ValueType, got: ValueType, importIndex: U32) :
        WasmRuntimeException("Import at $importIndex has type $got, expected $expected")

class InvalidExternalType(expected: ExternalType, got: ExternalType?) :
        WasmRuntimeException("Expected export of type $expected, got $got")
class WrongArgumentCount(fAddr: Address, expected: U32, got: U32) :
        WasmRuntimeException("Function 0x${fAddr.toString(16)} requires $expected arguments, got $got")
class InvalidArgumentTypeException(fAddr: Address, argIndex: U32, expected: ValueType, got: ValueType?) :
        WasmRuntimeException("Argument of 0x${fAddr.toString(16)} at $argIndex has the wrong Value Type, expected $expected, got $got")
class InvalidResultTypeException(fAddr: Address, resIndex: U32, expected: ValueType, got: ValueType?) :
        WasmRuntimeException("Result of 0x${fAddr.toString(16)} at $resIndex has the wrong Value Type, expected $expected, got $got")
