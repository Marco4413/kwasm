package io.github.marco4413.kwasm.bytecode.section

import io.github.marco4413.kwasm.bytecode.*

enum class ImportType(val value: U8) {
    Function(0u),
    Table(1u),
    Memory(2u),
    Global(3u);

    companion object {
        fun fromValue(type: U8) : ImportType {
            return when (type) {
                Function.value -> Function
                Table.value -> Table
                Memory.value -> Memory
                Global.value -> Global
                else -> throw IllegalStateException("Invalid Import Type")
            }
        }
    }
}

enum class Mutability(val value: U8) {
    Constant(0u),
    Variable(1u);

    companion object {
        fun fromValue(type: U8): Mutability {
            return when (type) {
                Constant.value -> Constant
                Variable.value -> Variable
                else -> throw IllegalStateException("Invalid Mutability Type")
            }
        }
    }
}

open class ImportDescription(val type: ImportType)
class ImportDescriptionFunction(val typeIdx: TypeIdx) : ImportDescription(ImportType.Function)

class GlobalType(val type: ValueType, val mutability: Mutability)
class ImportDescriptionGlobal(val value: GlobalType) : ImportDescription(ImportType.Global)

class Import(val module: Name, val name: Name, val description: ImportDescription)

const val ImportSectionId: U8 = 2u
typealias ImportSection = List<Import>

fun readImportSection(s: WasmInputStream) : ImportSection {
    s.readU32() // SIZE
    return s.readVector {
        Import(s.readName(), s.readName(), when (ImportType.fromValue(s.readU8())) {
            ImportType.Function -> ImportDescriptionFunction(s.readU32())
            ImportType.Table -> TODO()
            ImportType.Memory -> TODO()
            ImportType.Global -> ImportDescriptionGlobal(
                GlobalType(ValueType.fromValue(s.readU8()), Mutability.fromValue(s.readU8())))
        })
    }
}
