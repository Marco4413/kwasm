package io.github.marco4413.kwasm.instructions

import io.github.marco4413.kwasm.bytecode.U8
import io.github.marco4413.kwasm.WasmContext
import io.github.marco4413.kwasm.bytecode.WasmInputStream
import java.io.OutputStream

const val BlockElse: U8 = 0x05u
const val BlockEnd: U8 = 0x0Bu

fun createInstructionMap(vararg descriptor: InstructionDescriptor) : MutableMap<U8, InstructionDescriptor> {
    val map = mutableMapOf<U8, InstructionDescriptor>()
    descriptor.forEach { map[it.opcode] = it }
    return map
}

abstract class InstructionDescriptor(val name: String, val opcode: U8) {
    abstract fun read(s: WasmInputStream) : Instruction
    override fun toString(): String = "$name#$opcode"
}

abstract class Instruction(val descriptor: InstructionDescriptor) {
    companion object {
        private val instr = createInstructionMap(
            NopDescriptor, LocalGetDescriptor, I32AddDescriptor
        )

        fun fromStream(s: WasmInputStream, _opcode: U8? = null) : Instruction {
            // TODO: Move this to Module.kt?
            val opcode = _opcode ?: s.readU8()
            val descriptor = instr[opcode]
                ?: throw NullPointerException("No instruction with opcode 0x${opcode.toString(16)} found.")
            return descriptor.read(s)
        }
    }

    abstract fun execute(ctx: WasmContext)
    open fun write(s: OutputStream) {
        s.write(descriptor.opcode.toInt())
    }
}
