package io.github.marco4413.kwasm

import io.github.marco4413.kwasm.instructions.BlockEnd
import io.github.marco4413.kwasm.instructions.Instruction
import java.io.InputStream

typealias DoubleArray<T> = Array<Array<T>>

typealias TypeIdx = U32
typealias FunctionIdx = U32
typealias TableIdx = U32
typealias MemoryIdx = U32
typealias GlobalIdx = U32
typealias ElementIdx = U32
typealias DataIdx = U32
typealias LocalIdx = U32
typealias LabelIdx = U32

enum class ValueType(val value: U8) {
    I32(0x7Fu),
    I64(0x7Eu),
    U32(0x7Du),
    U64(0x7Cu),
    V128(0x7Bu),
    FunctionRef(0x70u),
    ExternalRef(0x6Fu);

    companion object {
        fun fromValue(type: U8) : ValueType {
            return when (type) {
                I32.value -> I32
                I64.value -> I64
                U32.value -> U32
                U64.value -> U64
                V128.value -> V128
                FunctionRef.value -> FunctionRef
                ExternalRef.value -> ExternalRef
                else -> throw IllegalStateException("Invalid Value Type")
            }
        }
    }
}

enum class ExportType(val value: U8) {
    Type(0u),
    Table(1u),
    Memory(2u),
    Global(3u);

    companion object {
        fun fromValue(type: U8) : ExportType {
            return when (type) {
                Type.value -> Type
                Table.value -> Table
                Memory.value -> Memory
                Global.value -> Global
                else -> throw IllegalStateException("Invalid Export Type")
            }
        }
    }
}

data class ExportDescription(val type: ExportType, val idx: U32)
data class Export(val name: Name, val description: ExportDescription)

// typealias Instruction = U8
// const val InstructionEnd: Instruction = 0x0Bu
typealias Expression = ArrayList<Instruction>

data class Function(val locals: DoubleArray<ValueType>, val expression: Expression)

const val FunctionTypeId: UByte = 96u
data class FunctionType(val parameters: Array<ValueType>,
                        val results: Array<ValueType>)

const val CustomSectionId: U8 = 0u
data class CustomSection(val id: U8, val size: U32, val data: Array<U8>)

const val TypeSectionId: U8 = 1u
typealias TypeSection = ArrayList<FunctionType>

const val FunctionSectionId: U8 = 3u
typealias FunctionSection = ArrayList<TypeIdx>

const val ExportSectionId: U8 = 7u
typealias ExportSection = ArrayList<Export>

const val CodeSectionId: U8 = 10u
typealias CodeSection = ArrayList<Function>

private fun readCustomSection(s: WasmInputStream, sId: U8) : CustomSection {
    val data = s.readVector { s.readU8() }
    return CustomSection(sId, data.size.toUInt(), data)
}

private fun readTypeSection(s: WasmInputStream) : Array<FunctionType> {
    s.readU32() // SIZE
    return s.readVector {
        val id = s.readU8()
        if (id != FunctionTypeId)
            TODO("Unsupported Type $id")
        FunctionType(s.readVector { ValueType.fromValue(s.readU8()) },
            s.readVector { ValueType.fromValue(s.readU8()) })
    }
}

private fun readFunctionSection(s: WasmInputStream) : Array<TypeIdx> {
    s.readU32() // SIZE
    return s.readVector { s.readU32() }
}

private fun readExportSection(s: WasmInputStream) : Array<Export> {
    s.readU32() // SIZE
    return s.readVector {
        Export(s.readName(), ExportDescription(
            ExportType.fromValue(s.readU8()), s.readU32()
        ))
    }
}

private fun readExpression(s: WasmInputStream) : Expression {
    val expression = Expression()
    while (true) {
        val instr = Instruction.fromStream(s)
        if (instr.descriptor.opcode == BlockEnd) break
        expression.add(instr)
    }
    return expression
}

private fun readCodeSection(s: WasmInputStream) : Array<Function> {
    s.readU32() // SIZE
    return s.readVector {
        s.readU32() // SIZE
        // TODO: Figure out why this is a 2D Array
        Function(s.readVector {
            s.readVector { ValueType.fromValue(s.readU8()) }
        }, readExpression(s))
    }
}

class Module(val magic: U32, val version: U32,
             val types: TypeSection, val functions: FunctionSection,
             val exports: ExportSection, code: CodeSection) {
    companion object {
        fun fromStream(inStream: InputStream) : Module {
            val s = WasmInputStream(inStream)

            val magic = s.readRawU32()
            val version = s.readRawU32()
            val types = TypeSection()
            val functions = FunctionSection()
            val exports = ExportSection()
            val code = CodeSection()

            while (s.available() > 0) {
                when (val sId = s.readU8()) {
                    CustomSectionId -> readCustomSection(s, sId)
                    TypeSectionId -> types.addAll(readTypeSection(s))
                    FunctionSectionId -> functions.addAll(readFunctionSection(s))
                    ExportSectionId -> exports.addAll(readExportSection(s))
                    CodeSectionId -> code.addAll(readCodeSection(s))
                    else -> TODO("Unimplemented Section $sId")
                }
            }

            return Module(magic, version, types, functions, exports, code)
        }
    }
}
