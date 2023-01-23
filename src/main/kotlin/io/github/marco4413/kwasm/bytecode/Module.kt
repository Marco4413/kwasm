package io.github.marco4413.kwasm.bytecode

import io.github.marco4413.kwasm.bytecode.section.*
import io.github.marco4413.kwasm.bytecode.section.Function
import java.io.InputStream
import java.lang.RuntimeException

class InvalidWasmFile :
    RuntimeException("Invalid Wasm file.")
class UnsupportedWasmVersion(got: U32, expected: U32) :
    RuntimeException("Unsupported Wasm version $got, supported $expected.")

const val StartSectionId: U8 = 8u

class Module(val magic: U32, val version: U32,
             val types: TypeSection, val imports: ImportSection,
             val functions: FunctionSection, val memories: MemorySection,
             val exports: ExportSection, val start: FunctionIdx?, val code: CodeSection) {
    companion object {
        const val WASM_MAGIC: U32 = 1836278016u
        const val WASM_VERSION: U32 = 1u

        fun fromStream(inStream: InputStream) : Module {
            val s = WasmInputStream(inStream)

            val magic = s.readRawU32()
            if (magic != WASM_MAGIC) throw InvalidWasmFile()
            val version = s.readRawU32()
            if (version != WASM_VERSION) throw UnsupportedWasmVersion(version, WASM_VERSION)

            val types = ArrayList<FunctionType>()
            val imports = ArrayList<Import>()
            val functions = ArrayList<TypeIdx>()
            val memories = ArrayList<Memory>()
            val exports = ArrayList<Export>()
            var start: FunctionIdx? = null
            val code = ArrayList<Function>()

            while (s.available() > 0) {
                when (val sId = s.readU8()) {
                    CustomSectionId -> readCustomSection(s, sId)
                    TypeSectionId -> types.addAll(readTypeSection(s))
                    ImportSectionId -> imports.addAll(readImportSection(s))
                    FunctionSectionId -> functions.addAll(readFunctionSection(s))
                    MemorySectionId -> memories.addAll(readMemorySection(s))
                    ExportSectionId -> exports.addAll(readExportSection(s))
                    StartSectionId -> { s.readU32(); /* SIZE */ start = s.readU32() }
                    CodeSectionId -> code.addAll(readCodeSection(s))
                    else -> TODO("Unimplemented Section $sId")
                }
            }

            return Module(magic, version, types, imports, functions, memories, exports, start, code)
        }
    }
}
