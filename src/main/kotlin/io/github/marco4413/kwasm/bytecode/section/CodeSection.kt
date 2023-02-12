package io.github.marco4413.kwasm.bytecode.section

import io.github.marco4413.kwasm.bytecode.*

class Locals(val count: U32, val type: ValueType)
class Function(val locals: List<Locals>, val body: Expression)

const val CodeSectionId: U8 = 10u
typealias CodeSection = List<Function>

fun readCodeSection(s: WasmInputStream) : CodeSection {
    s.readU32() // SIZE
    return s.readVector {
        s.readU32() // SIZE
        Function(s.readVector {
            Locals(s.readU32(), ValueType.fromValue(s.readU8()))
        }, readExpression(s))
    }
}

fun writeCodeSection(s: WasmOutputStream, sec: CodeSection) {
    s.writeU8(CodeSectionId)
    s.writeSize {
        s.writeVector(sec) {
            _, func ->
            s.writeSize {
                s.writeVector(func.locals) {
                    _, local ->
                    s.writeU32(local.count)
                    s.writeU8(local.type.value)
                }
                writeExpression(s, func.body)
            }
        }
    }
}
