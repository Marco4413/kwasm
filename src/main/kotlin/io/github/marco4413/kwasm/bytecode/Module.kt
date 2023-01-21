package io.github.marco4413.kwasm.bytecode

import io.github.marco4413.kwasm.bytecode.section.*
import io.github.marco4413.kwasm.bytecode.section.Function
import java.io.InputStream
import java.lang.RuntimeException

class InvalidWasmFile :
    RuntimeException("Invalid Wasm file.")
class UnsupportedWasmVersion(got: U32, expected: U32) :
    RuntimeException("Unsupported Wasm version $got, supported $expected.")

data class Module(val magic: U32, val version: U32,
             val types: TypeSection, val functions: FunctionSection,
             val exports: ExportSection, val code: CodeSection) {
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
            val functions = ArrayList<TypeIdx>()
            val exports = ArrayList<Export>()
            val code = ArrayList<Function>()

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
