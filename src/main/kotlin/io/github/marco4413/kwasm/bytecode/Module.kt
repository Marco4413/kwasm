package io.github.marco4413.kwasm.bytecode

import io.github.marco4413.kwasm.bytecode.section.*
import io.github.marco4413.kwasm.bytecode.section.Function
import java.io.InputStream
import java.io.OutputStream
import java.lang.RuntimeException

class InvalidWasmFile :
    RuntimeException("Invalid Wasm file.")
class UnsupportedWasmVersion(got: U32, expected: U32) :
    RuntimeException("Unsupported Wasm version $got, supported $expected.")

const val StartSectionId: U8 = 8u

class Module(val magic: U32,
             val version: U32,
             val customs: CustomSection,
             val types: TypeSection,
             val imports: ImportSection,
             val functions: FunctionSection,
             val memories: MemorySection,
             val exports: ExportSection,
             val start: FunctionIdx?,
             val code: CodeSection,
             val data: DataSection) {
    companion object {
        const val WASM_MAGIC: U32 = 1836278016u
        const val WASM_VERSION: U32 = 1u

        fun read(inStream: InputStream) : Module {
            val s = WasmInputStream(inStream)

            val magic = s.readRawU32()
            if (magic != WASM_MAGIC) throw InvalidWasmFile()
            val version = s.readRawU32()
            if (version != WASM_VERSION) throw UnsupportedWasmVersion(version, WASM_VERSION)

            val customs = ArrayList<Custom>()
            val types = ArrayList<FunctionType>()
            val imports = ArrayList<Import>()
            val functions = ArrayList<TypeIdx>()
            val memories = ArrayList<Memory>()
            val exports = ArrayList<Export>()
            var start: FunctionIdx? = null
            val code = ArrayList<Function>()
            val data = ArrayList<Data>()

            while (s.available() > 0) {
                when (val sId = s.readU8()) {
                    CustomSectionId -> customs.addAll(readCustomSection(s))
                    TypeSectionId -> types.addAll(readTypeSection(s))
                    ImportSectionId -> imports.addAll(readImportSection(s))
                    FunctionSectionId -> functions.addAll(readFunctionSection(s))
                    MemorySectionId -> memories.addAll(readMemorySection(s))
                    ExportSectionId -> exports.addAll(readExportSection(s))
                    StartSectionId -> { s.readU32(); /* SIZE */ start = s.readU32() }
                    CodeSectionId -> code.addAll(readCodeSection(s))
                    DataSectionId -> data.addAll(readDataSection(s))
                    else -> TODO("Unimplemented Section $sId")
                }
            }

            return Module(magic, version, customs, types, imports, functions, memories, exports, start, code, data)
        }
    }

    fun write(oStream: OutputStream) {
        val s = WasmOutputStream(oStream)
        s.writeRawU32(magic)
        s.writeRawU32(version)
        if (types.isNotEmpty()) writeTypeSection(s, types)
        if (imports.isNotEmpty()) writeImportSection(s, imports)
        if (functions.isNotEmpty()) writeFunctionSection(s, functions)
        if (memories.isNotEmpty()) writeMemorySection(s, memories)
        if (exports.isNotEmpty()) writeExportSection(s, exports)
        if (start != null) s.writeSize { s.writeU32(start) }
        if (code.isNotEmpty()) writeCodeSection(s, code)
        if (data.isNotEmpty()) writeDataSection(s, data)
        if (customs.isNotEmpty()) writeCustomSection(s, customs)
        s.flush()
    }
}
