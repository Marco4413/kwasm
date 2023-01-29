package io.github.marco4413.kwasm

import io.github.marco4413.kwasm.bytecode.*
import io.github.marco4413.kwasm.bytecode.section.FunctionType
import io.github.marco4413.kwasm.bytecode.section.ImportType
import io.github.marco4413.kwasm.runtime.ExternalType
import io.github.marco4413.kwasm.runtime.StackValueType
import io.github.marco4413.kwasm.runtime.Trap
import java.lang.RuntimeException

open class WasmRuntimeException(message: String) : RuntimeException(message)

class TrapException(trap: Trap) : WasmRuntimeException(trap.message)

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

class NotEnoughImportsException(expected: U32, got: U32) :
        WasmRuntimeException("Expected at least $expected imports, got $got")
class UndefinedImportException(name: String, type: ImportType) :
        WasmRuntimeException("Import $name of type $type is not defined")

class InvalidImportTypeException(expected: ImportType, got: ExternalType, importName: Name) :
        WasmRuntimeException("Import $importName is a $got, expected $expected")
class InvalidImportFunctionTypeException(expected: FunctionType, got: FunctionType, importName: Name) :
        WasmRuntimeException("Import $importName has signature $got, expected $expected")
class InvalidImportGlobalTypeException(expected: ValueType, got: ValueType, importName: Name) :
        WasmRuntimeException("Import $importName has type $got, expected $expected")

class InvalidExternalTypeException(expected: ExternalType, got: ExternalType?) :
        WasmRuntimeException("Expected export of type $expected, got $got")
class IllegalArgumentCountException(fAddr: Address, expected: U32, got: U32) :
        WasmRuntimeException("Function 0x${fAddr.toString(16)} requires $expected arguments, got $got")
class InvalidArgumentTypeException(fAddr: Address, argIndex: U32, expected: ValueType, got: ValueType?) :
        WasmRuntimeException("Argument of 0x${fAddr.toString(16)} at $argIndex has the wrong Value Type, expected $expected, got $got")
class InvalidResultTypeException(fAddr: Address, resIndex: U32, expected: ValueType, got: ValueType?) :
        WasmRuntimeException("Result of 0x${fAddr.toString(16)} at $resIndex has the wrong Value Type, expected $expected, got $got")
