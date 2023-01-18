package io.github.marco4413.kwasm.bytecode

import io.github.marco4413.kwasm.bytecode.section.*
import java.io.InputStream

class Module(val magic: U32, val version: U32,
             val types: TypeSection, val functions: FunctionSection,
             val exports: ExportSection, val code: CodeSection) {
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
